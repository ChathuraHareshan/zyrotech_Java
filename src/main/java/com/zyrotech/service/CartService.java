package com.zyrotech.service;

import com.zyrotech.dto.CartDTO;
import com.zyrotech.entity.Cart;
import com.zyrotech.entity.Stock;
import com.zyrotech.entity.User;
import com.zyrotech.util.AppUtil;
import com.zyrotech.util.HibernateUtil;
import com.zyrotech.validation.Validator;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class CartService {

    public String deleteCartItem(String cartId, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (cartId == null || cartId.isBlank()) {
            message = "Invalid ID format!";
        } else if (!cartId.matches(Validator.IS_INTEGER)) {
            message = "Invalid ID format!";
        } else {
            int cId = Integer.parseInt(cartId);
            HttpSession httpSession = request.getSession();
            User sessionUser = (User) httpSession.getAttribute("user");

            if (sessionUser == null) {
                List<Cart> sessionCart = getSessionAttribute(httpSession);
                if (sessionCart != null && !sessionCart.isEmpty()) {
                    boolean removed = sessionCart.removeIf(cart -> cart.getId() == cId);
                    if (removed) {
                        httpSession.setAttribute("sessionCart", sessionCart);
                        status = true;
                        message = "Cart item deleted";
                    } else {
                        message = "Cart item not found!";
                    }
                } else {
                    message = "Cart is empty!";
                }
            } else {
                Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = null;
                try {
                    transaction = hibernateSession.beginTransaction();
                    Cart existingCart = hibernateSession.createQuery(
                                    "FROM Cart c WHERE c.id=:cartId AND c.user.id=:userId", Cart.class)
                            .setParameter("cartId", cId)
                            .setParameter("userId", sessionUser.getId())
                            .getSingleResultOrNull();

                    if (existingCart == null) {
                        message = "Cart item not found!";
                    } else {
                        hibernateSession.remove(existingCart);
                        transaction.commit();
                        status = true;
                        message = "Cart item deleted";
                    }
                } catch (HibernateException e) {
                    if (transaction != null) transaction.rollback();
                    message = "Error deleting cart item!";
                } finally {
                    hibernateSession.close();
                }
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateCartItem(String cartId, String qty, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (cartId == null || cartId.isBlank() || !cartId.matches(Validator.IS_INTEGER)) {
            message = "Invalid cart ID!";
        } else if (qty == null || qty.isBlank() || !qty.matches(Validator.IS_INTEGER)) {
            message = "Invalid quantity value!";
        } else {
            int cId = Integer.parseInt(cartId);
            int newQty = Integer.parseInt(qty);

            if (newQty < 1) {
                message = "Quantity must be at least 1!";
            } else {
                HttpSession httpSession = request.getSession();
                User sessionUser = (User) httpSession.getAttribute("user");

                if (sessionUser == null) {
                    List<Cart> sessionCart = getSessionAttribute(httpSession);
                    if (sessionCart != null && !sessionCart.isEmpty()) {
                        boolean found = false;
                        for (Cart cart : sessionCart) {
                            if (cart.getId() == cId) {
                                Stock stock = cart.getStock();
                                if (newQty <= stock.getQty()) {
                                    cart.setQty(newQty);
                                    httpSession.setAttribute("sessionCart", sessionCart);
                                    status = true;
                                    message = "Cart updated successfully!";
                                } else {
                                    message = "Quantity exceeds available stock!";
                                }
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            message = "Cart item not found!";
                        }
                    } else {
                        message = "Cart is empty!";
                    }
                } else {
                    Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
                    Transaction transaction = null;
                    try {
                        transaction = hibernateSession.beginTransaction();
                        Cart existingCart = hibernateSession.createQuery(
                                        "FROM Cart c WHERE c.id=:cartId AND c.user.id=:userId", Cart.class)
                                .setParameter("cartId", cId)
                                .setParameter("userId", sessionUser.getId())
                                .getSingleResultOrNull();

                        if (existingCart == null) {
                            message = "Cart item not found!";
                        } else {
                            Stock stock = existingCart.getStock();
                            if (newQty <= stock.getQty()) {
                                existingCart.setQty(newQty);
                                hibernateSession.merge(existingCart);
                                transaction.commit();
                                status = true;
                                message = "Cart updated successfully!";
                            } else {
                                message = "Quantity exceeds available stock!";
                            }
                        }
                    } catch (HibernateException e) {
                        if (transaction != null) transaction.rollback();
                        message = "Error updating cart!";
                    } finally {
                        hibernateSession.close();
                    }
                }
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String getAllUserCarts(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";
        HttpSession httpSession = request.getSession();
        User sessionUser = (User) httpSession.getAttribute("user");

        if (sessionUser == null) {
            // Guest user - get cart from session
            List<Cart> sessionCart = getSessionAttribute(httpSession);
            if (sessionCart == null || sessionCart.isEmpty()) {
                message = "Your cart is empty!";
            } else {
                List<CartDTO> cartDTOList = generateCartDTOs(sessionCart);
                responseObject.add("cartItems", AppUtil.GSON.toJsonTree(cartDTOList));
                status = true;
                message = "Cart items loaded successfully";
            }
        } else {
            // Logged in user - get cart from database
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                List<Cart> cartList = hibernateSession.createQuery(
                                "FROM Cart c WHERE c.user.id=:id", Cart.class)
                        .setParameter("id", sessionUser.getId())
                        .getResultList();

                if (cartList.isEmpty()) {
                    message = "Your cart is empty";
                } else {
                    List<CartDTO> cartDTOList = generateCartDTOs(cartList);
                    responseObject.add("cartItems", AppUtil.GSON.toJsonTree(cartDTOList));
                    status = true;
                    message = "Cart items loaded successfully";
                }
            } catch (Exception e) {
                message = "Error loading cart: " + e.getMessage();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public List<CartDTO> generateCartDTOs(List<Cart> cartList) {
        List<CartDTO> cartDTOList = new ArrayList<>();

        for (Cart cart : cartList) {
            CartDTO cartDTO = new CartDTO();
            cartDTO.setCartId(cart.getId());
            cartDTO.setStockId(cart.getStock().getId());
            cartDTO.setProductTitle(cart.getStock().getProduct().getTitle());
            cartDTO.setImages(cart.getStock().getProduct().getImages());
            cartDTO.setQty(cart.getQty());
            cartDTO.setPrice(cart.getStock().getPrice());
            if (cart.getStock().getProduct().getCategory() != null) {
                cartDTO.setCategoryName(cart.getStock().getProduct().getCategory().getName());
            }
            cartDTOList.add(cartDTO);
        }

        return cartDTOList;
    }

    public void mergeUserCarts(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        User sessionUser = (User) httpSession.getAttribute("user");

        if (sessionUser != null) {
            List<Cart> sessionCart = getSessionAttribute(httpSession);
            if (sessionCart != null && !sessionCart.isEmpty()) {
                Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = null;
                try {
                    transaction = hibernateSession.beginTransaction();
                    User dbUser = hibernateSession.find(User.class, sessionUser.getId());

                    for (Cart cart : sessionCart) {
                        Stock stock = hibernateSession.find(Stock.class, cart.getStock().getId());
                        Cart existingCart = hibernateSession.createQuery(
                                        "FROM Cart c WHERE c.user=:user AND c.stock=:stock", Cart.class)
                                .setParameter("user", dbUser)
                                .setParameter("stock", stock)
                                .getSingleResultOrNull();

                        if (existingCart == null) {
                            existingCart = new Cart();
                            existingCart.setQty(cart.getQty());
                            existingCart.setUser(dbUser);
                            existingCart.setStock(stock);
                            hibernateSession.persist(existingCart);
                        } else {
                            int newQty = existingCart.getQty() + cart.getQty();
                            if (newQty <= stock.getQty()) {
                                existingCart.setQty(newQty);
                                hibernateSession.merge(existingCart);
                            }
                        }
                    }
                    transaction.commit();
                } catch (HibernateException e) {
                    if (transaction != null) transaction.rollback();
                } finally {
                    hibernateSession.close();
                }
            }
            httpSession.setAttribute("sessionCart", null);
        }
    }

    public String addToCart(String sId, String qty, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (sId == null || sId.isBlank() || !sId.matches(Validator.IS_INTEGER)) {
            message = "Invalid product ID!";
        } else if (qty == null || qty.isBlank() || !qty.matches(Validator.IS_INTEGER)) {
            message = "Invalid quantity value!";
        } else {
            int stockId = Integer.parseInt(sId);
            int requestQty = Integer.parseInt(qty);
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();

            try {
                Stock stock = hibernateSession.find(Stock.class, stockId);
                if (stock == null) {
                    message = "Product not found!";
                } else {
                    HttpSession httpSession = request.getSession();
                    User user = (User) httpSession.getAttribute("user");
                    List<Cart> sessionCart = getSessionAttribute(httpSession);

                    if (user == null) {
                        // Guest carts are cached as raw entities inside HttpSession
                        // memory and read again long after this Hibernate Session
                        // closes. Force-load the lazy Product/Category/Images
                        // associations now, while the session is still open, so
                        // generateCartDTOs() doesn't hit a LazyInitializationException
                        // later when rendering the cart page.
                        initializeStockForSessionCart(stock);

                        if (sessionCart == null) {
                            return guestUserFirstTime(stock, requestQty, httpSession);
                        } else {
                            return guestUserSecondTime(stock, requestQty, httpSession);
                        }
                    } else {
                        return loggedUserCart(stock, requestQty, httpSession, hibernateSession);
                    }
                }
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }


    private void initializeStockForSessionCart(Stock stock) {
        if (stock.getProduct() != null) {
            Hibernate.initialize(stock.getProduct());
            if (stock.getProduct().getImages() != null) {
                Hibernate.initialize(stock.getProduct().getImages());
            }
            if (stock.getProduct().getCategory() != null) {
                Hibernate.initialize(stock.getProduct().getCategory());
            }
        }
    }

    private String loggedUserCart(Stock stock, int requestQty, HttpSession httpSession, Session hibernateSession) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";
        Transaction transaction = null;

        try {
            User sessionUser = (User) httpSession.getAttribute("user");
            if (sessionUser != null) {
                User dbUser = hibernateSession.find(User.class, sessionUser.getId());
                Cart existingCart = hibernateSession.createQuery(
                                "FROM Cart c WHERE c.user=:user AND c.stock=:stock", Cart.class)
                        .setParameter("user", dbUser)
                        .setParameter("stock", stock)
                        .getSingleResultOrNull();

                transaction = hibernateSession.beginTransaction();

                if (existingCart == null) {
                    existingCart = new Cart();
                    existingCart.setUser(dbUser);
                    existingCart.setStock(stock);
                    existingCart.setQty(requestQty);
                    hibernateSession.persist(existingCart);
                    status = true;
                    message = "Product added to cart";
                } else {
                    int newQty = existingCart.getQty() + requestQty;
                    if (newQty > stock.getQty()) {
                        message = "Product quantity exceeded!";
                    } else {
                        existingCart.setQty(newQty);
                        hibernateSession.merge(existingCart);
                        status = true;
                        message = "User cart updated";
                    }
                }

                if (status) {
                    transaction.commit();
                } else {
                    transaction.rollback();
                }
            }
        } catch (HibernateException e) {
            if (transaction != null) transaction.rollback();
            message = "Error adding to cart!";
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    private String guestUserSecondTime(Stock stock, int requestQty, HttpSession httpSession) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        List<Cart> sessionCart = getSessionAttribute(httpSession);
        boolean found = false;
        Cart cart = null;

        for (Cart c : sessionCart) {
            if (c.getStock().getId() == stock.getId()) {
                found = true;
                cart = c;
                break;
            }
        }

        if (found) {
            int newQty = cart.getQty() + requestQty;
            if (newQty > stock.getQty()) {
                message = "Product quantity exceeded!";
            } else {
                cart.setQty(newQty);
                status = true;
                message = "User cart updated!";
            }
        } else {
            cart = new Cart();
            cart.setId(sessionCart.size() + 1);
            cart.setStock(stock);
            cart.setQty(requestQty);
            cart.setUser(null);
            sessionCart.add(cart);
            httpSession.setAttribute("sessionCart", sessionCart);
            status = true;
            message = "Product added to the cart";
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    private String guestUserFirstTime(Stock stock, int requestQty, HttpSession httpSession) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (requestQty > stock.getQty()) {
            message = "Product quantity exceeded!";
        } else {
            List<Cart> cartList = new ArrayList<>();
            Cart cart = new Cart();
            cart.setId(1);
            cart.setStock(stock);
            cart.setQty(requestQty);
            cart.setUser(null);
            cartList.add(cart);
            httpSession.setAttribute("sessionCart", cartList);
            status = true;
            message = "Product added to the cart";
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(HttpSession httpSession) {
        return (T) httpSession.getAttribute("sessionCart");
    }
}