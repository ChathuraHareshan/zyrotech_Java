package com.zyrotech.service;

import com.zyrotech.dto.*;
import com.zyrotech.entity.*;
import com.zyrotech.util.AppUtil;
import com.zyrotech.util.Env;
import com.zyrotech.util.HibernateUtil;
import com.zyrotech.util.PayHereUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CheckoutService {
    private final OrderService orderService = new OrderService();

    public String processCheckout(CheckoutRequestDTO requestDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";
        Session hibernateSession = null;
        Transaction transaction = null;

        try {
            hibernateSession = HibernateUtil.getSessionFactory().openSession();
            HttpSession httpSession = request.getSession();
            User sessionUser = (User) httpSession.getAttribute("user");

            if (sessionUser == null) {
                message = "Session expired. Please login again!";
            } else {
                User dbUser = hibernateSession.find(User.class, sessionUser.getId());

                List<Cart> cartList = hibernateSession.createQuery(
                                "FROM Cart c WHERE c.user.id=:userId", Cart.class)
                        .setParameter("userId", dbUser.getId())
                        .getResultList();

                if (cartList.isEmpty()) {
                    message = "Your cart is empty!";
                } else {
                    if (requestDTO.isCurrentAddress()) {
                        Address address = hibernateSession.createQuery(
                                        "FROM Address a WHERE a.user=:user AND a.isPrimary=:primary", Address.class)
                                .setParameter("user", dbUser)
                                .setParameter("primary", true)
                                .getSingleResultOrNull();

                        if (address == null) {
                            message = "Primary address not found. Please add an address!";
                        } else {
                            transaction = hibernateSession.beginTransaction();
                            Order pendingOrder = orderService.createPendingOrder(dbUser, hibernateSession);
                            // Commit the transaction here
                            transaction.commit();

                            PayHereDTO paymentDetails = createPaymentDetails(hibernateSession, pendingOrder);
                            responseObject.add("paymentDetails", AppUtil.GSON.toJsonTree(paymentDetails));
                            status = true;
                            message = "Checkout initiated successfully!";
                        }
                    } else {
                        System.out.println("City value: " + requestDTO.getCity());

                        if (requestDTO.getFirstName() == null || requestDTO.getFirstName().isBlank()) {
                            message = "First Name is required!";
                        } else if (requestDTO.getLastName() == null || requestDTO.getLastName().isBlank()) {
                            message = "Last Name is required!";
                        } else if (requestDTO.getCity() <= 0) {
                            message = "Please select a city!";
                        } else if (requestDTO.getLineOne() == null || requestDTO.getLineOne().isBlank()) {
                            message = "Address line one is required!";
                        } else if (requestDTO.getPostalCode() == null || requestDTO.getPostalCode().isBlank()) {
                            message = "Postal code is required!";
                        } else if (requestDTO.getMobile() == null || requestDTO.getMobile().isBlank()) {
                            message = "Mobile number is required!";
                        } else {
                            City city = hibernateSession.find(City.class, requestDTO.getCity());
                            if (city == null) {
                                message = "City not found. Select correct city!";
                            } else {
                                transaction = hibernateSession.beginTransaction();

                                if (requestDTO.isSaveAddress()) {
                                    Address existingPrimary = hibernateSession.createQuery(
                                                    "FROM Address a WHERE a.user=:user AND a.isPrimary=:primary", Address.class)
                                            .setParameter("user", dbUser)
                                            .setParameter("primary", true)
                                            .getSingleResultOrNull();

                                    if (existingPrimary != null) {
                                        existingPrimary.setPrimary(false);
                                        hibernateSession.merge(existingPrimary);
                                    }

                                    Address address = new Address();
                                    address.setPrimary(true);
                                    address.setLineOne(requestDTO.getLineOne());
                                    address.setLineTwo(requestDTO.getLineTwo());
                                    address.setPostalCode(requestDTO.getPostalCode());
                                    address.setMobile(requestDTO.getMobile());
                                    address.setCity(city);
                                    address.setUser(dbUser);
                                    hibernateSession.persist(address);
                                }

                                Order pendingOrder = orderService.createPendingOrder(dbUser, hibernateSession);
                                transaction.commit();

                                PayHereDTO paymentDetails = createPaymentDetails(hibernateSession, pendingOrder);
                                responseObject.add("paymentDetails", AppUtil.GSON.toJsonTree(paymentDetails));
                                status = true;
                                message = "Checkout initiated successfully!";
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            message = "Error processing checkout: " + e.getMessage();
            e.printStackTrace();
        } finally {
            if (hibernateSession != null && hibernateSession.isOpen()) {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    private PayHereDTO createPaymentDetails(Session hibernateSession, Order order) {
        String orderId = "000" + order.getId();
        String returnURL = Env.get("app.public.url") + "/api/payments/return";
        String cancelURL = Env.get("app.public.url") + "/api/payments/cancel";
        String notifyURL = Env.get("app.public.url") + "/api/payments/notify";

        User user = hibernateSession.find(User.class, order.getUser().getId());

        Address address = hibernateSession.createQuery(
                        "FROM Address a WHERE a.user=:user AND a.isPrimary=:primary", Address.class)
                .setParameter("user", user)
                .setParameter("primary", true)
                .getSingleResult();

        StringBuilder userAddress = new StringBuilder(address.getLineOne());
        if (address.getLineTwo() != null && !address.getLineTwo().isBlank()) {
            userAddress.append(",").append(address.getLineTwo());
        }

        List<OrderItem> orderItems = hibernateSession.createQuery(
                        "FROM OrderItem oi WHERE oi.order=:order", OrderItem.class)
                .setParameter("order", order)
                .getResultList();

        StringBuilder items = new StringBuilder();
        double amount = 0;
        for (OrderItem item : orderItems) {
            if (items.length() > 0) items.append(",");
            items.append(item.getStock().getProduct().getTitle());
            amount += item.getStock().getPrice() * item.getQty();
        }

        String hashValue = PayHereUtil.generateHash(orderId, amount);
        PayHereDTO payHereDTO = new PayHereDTO();
        payHereDTO.setSandbox(true);
        payHereDTO.setMerchant_id(PayHereUtil.getMerchantId());
        payHereDTO.setReturn_url(returnURL);
        payHereDTO.setCancel_url(cancelURL);
        payHereDTO.setNotify_url(notifyURL);
        payHereDTO.setOrder_id(orderId);
        payHereDTO.setItems(items.toString());
        payHereDTO.setAmount(String.valueOf(amount));
        payHereDTO.setCurrency(PayHereUtil.APP_CURRENCY);
        payHereDTO.setHash(hashValue);
        payHereDTO.setFirst_name(user.getFname());
        payHereDTO.setLast_name(user.getLname());
        payHereDTO.setEmail(user.getEmail());
        payHereDTO.setPhone(address.getMobile());
        payHereDTO.setAddress(userAddress.toString());
        payHereDTO.setCity(address.getCity().getName());
        payHereDTO.setCountry(PayHereUtil.APP_COUNTRY);

        return payHereDTO;
    }

    public String getCheckoutData(HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";
        Session hibernateSession = null;

        try {
            HttpSession httpSession = request.getSession();
            User sessionUser = (User) httpSession.getAttribute("user");

            if (sessionUser == null) {
                message = "Please login first!";
            } else {
                hibernateSession = HibernateUtil.getSessionFactory().openSession();

                Address primaryAddress = hibernateSession.createQuery(
                                "FROM Address a WHERE a.user.id=:userId AND a.isPrimary=:primary", Address.class)
                        .setParameter("userId", sessionUser.getId())
                        .setParameter("primary", true)
                        .getSingleResultOrNull();

                List<Cart> cartList = hibernateSession.createQuery(
                                "FROM Cart c WHERE c.user.id=:userId", Cart.class)
                        .setParameter("userId", sessionUser.getId())
                        .getResultList();

                if (cartList.isEmpty()) {
                    message = "Your cart is empty. Please add items first!";
                } else {
                    if (primaryAddress != null) {
                        AddressDTO addressDTO = getAddressDTO(primaryAddress);
                        responseObject.add("userPrimaryAddress", AppUtil.GSON.toJsonTree(addressDTO));
                    }

                    List<CartDTO> cartDTOList = new CartService().generateCartDTOs(cartList);
                    responseObject.add("cartList", AppUtil.GSON.toJsonTree(cartDTOList));
                    status = true;
                    message = "Checkout data loaded successfully!";
                }
            }
        } catch (Exception e) {
            message = "Error loading checkout data: " + e.getMessage();
            e.printStackTrace();
        } finally {
            if (hibernateSession != null && hibernateSession.isOpen()) {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    private AddressDTO getAddressDTO(Address address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(address.getId());
        addressDTO.setUserId(address.getUser().getId());
        addressDTO.setFirstName(address.getUser().getFname());
        addressDTO.setLastName(address.getUser().getLname());
        addressDTO.setLineOne(address.getLineOne());
        addressDTO.setLineTwo(address.getLineTwo());
        addressDTO.setPostalCode(address.getPostalCode());
        addressDTO.setMobile(address.getMobile());
        addressDTO.setPrimary(address.isPrimary());

        CityDTO cityDTO = new CityDTO();
        cityDTO.setId(address.getCity().getId());
        cityDTO.setName(address.getCity().getName());
        addressDTO.setCityDTO(cityDTO);

        return addressDTO;
    }
}