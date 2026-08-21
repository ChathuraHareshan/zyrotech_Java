package com.zyrotech.service;

import com.zyrotech.dto.ProductDTO;
import com.zyrotech.entity.*;
import com.zyrotech.util.AppUtil;
import com.zyrotech.util.HibernateUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ProductService {


    public String updateProduct(Product product) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = hibernateSession.beginTransaction();
        try {
            hibernateSession.merge(product);
            transaction.commit();
            status = true;
            message = "Product images uploading successful";
        } catch (HibernateException e) {
            transaction.rollback();
            message = "Product image uploading failed!";
        }
        hibernateSession.close();
        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }


    public Product getProductById(int id) {
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Product product = hibernateSession.find(Product.class, id);
        hibernateSession.close();
        return product;
    }

    public String editProduct(int productId, ProductDTO dto) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            message = "Product title can not be empty!";
        } else if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            message = "Product description can not be empty!";
        } else if (dto.getCategoryId() <= 0) {
            message = "Please select a category";
        } else if (dto.getColorId() <= 0) {
            message = "Please select a color";
        } else if (dto.getStorageId() <= 0) {
            message = "Please select storage";
        } else if (dto.getPrice() <= 0) {
            message = "Price must be greater than 0";
        } else if (dto.getQty() < 0) {
            message = "Quantity cannot be negative";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                Product product = hibernateSession.find(Product.class, productId);
                if (product == null) {
                    message = "Product not found";
                } else {
                    Category category = hibernateSession.find(Category.class, dto.getCategoryId());
                    Storage storage = hibernateSession.find(Storage.class, dto.getStorageId());
                    Color color = hibernateSession.find(Color.class, dto.getColorId());

                    if (category == null || storage == null || color == null) {
                        message = "Invalid category, storage or color selected";
                    } else {
                        Stock stock = hibernateSession.createQuery("FROM Stock s WHERE s.product.id = :pid", Stock.class)
                                .setParameter("pid", productId)
                                .uniqueResult();

                        Transaction transaction = hibernateSession.beginTransaction();
                        try {
                            product.setTitle(dto.getTitle());
                            product.setDescription(dto.getDescription());
                            product.setCategory(category);
                            product.setStorage(storage);
                            product.setColor(color);
                            hibernateSession.merge(product);

                            if (stock != null) {
                                stock.setPrice(dto.getPrice());
                                stock.setQty(dto.getQty());
                                if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
                                    Status newStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                                            .setParameter("value", dto.getStatus())
                                            .getSingleResult();
                                    stock.setStatus(newStatus);
                                }
                                hibernateSession.merge(stock);
                            }

                            transaction.commit();
                            status = true;
                            message = "Product updated successfully";
                        } catch (HibernateException e) {
                            transaction.rollback();
                            message = "Product update failed";
                        }
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

    public String updateProductStatus(int productId, String statusValue) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            Stock stock = hibernateSession.createQuery("FROM Stock s WHERE s.product.id = :pid", Stock.class)
                    .setParameter("pid", productId)
                    .uniqueResult();

            if (stock == null) {
                message = "Product not found";
            } else {
                Status newStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", statusValue)
                        .getSingleResult();

                Transaction transaction = hibernateSession.beginTransaction();
                try {
                    stock.setStatus(newStatus);
                    hibernateSession.merge(stock);
                    transaction.commit();
                    status = true;
                    message = "Product status updated successfully";
                } catch (HibernateException e) {
                    transaction.rollback();
                    message = "Status update failed";
                }
            }
        } finally {
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String deleteProduct(int productId) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            Product product = hibernateSession.find(Product.class, productId);
            if (product == null) {
                message = "Product not found";
            } else {
                Transaction transaction = hibernateSession.beginTransaction();
                try {
                    Stock stock = hibernateSession.createQuery("FROM Stock s WHERE s.product.id = :pid", Stock.class)
                            .setParameter("pid", productId)
                            .uniqueResult();
                    if (stock != null) {
                        hibernateSession.remove(stock);
                    }
                    hibernateSession.remove(product);
                    transaction.commit();
                    status = true;
                    message = "Product deleted successfully";
                } catch (HibernateException e) {
                    transaction.rollback();
                    message = "Product deletion failed. It may be linked to existing orders.";
                }
            }
        } finally {
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addNewProduct(ProductDTO productDTO, @Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (productDTO.getCategoryId() <= 0) {
            message = "Invalid brand type. Please select a correct Category Id.!";
        } else if (productDTO.getTitle() == null) {
            message = "Product title is required!";
        } else if (productDTO.getTitle().isBlank()) {
            message = "Product title can not be empty!";
        } else if (productDTO.getDescription() == null) {
            message = "Product description is required!";
        } else if (productDTO.getDescription().isBlank()) {
            message = "Product description can not be empty!";
        } else if (productDTO.getStorageId() <= 0) {
            message = "Invalid storage type. Please select a correct storage!";
        } else if (productDTO.getColorId() <= 0) {
            message = "Invalid color type. Please select a correct color!";
        } else if (productDTO.getPrice() <= 0) {
            message = "Product price can not be less than or equal to 0";
        } else if (productDTO.getQty() <= 0) {
            message = "Product quantity can not be less than or equal to 0";
        } else {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null) {
                message = "Session expired! Please logged in";
            } else {
                Session hibernateSession = HibernateUtil.getSessionFactory().openSession();

                Category category = hibernateSession.find(Category.class, productDTO.getCategoryId());
                if (category == null) {
                    message = "Category not found. Please contact administration";
                } else {
                    Storage storage = hibernateSession.find(Storage.class, productDTO.getStorageId());
                    if (storage == null) {
                        message = "Storage not found. Please contact administration";
                    } else {
                        Color color = hibernateSession.find(Color.class, productDTO.getColorId());
                        if (color == null) {
                            message = "Color not found. Please contact administration";
                        } else {
                            Product product = new Product();
                            product.setTitle(productDTO.getTitle());
                            product.setDescription(productDTO.getDescription());
                            product.setCategory(category);
                            product.setStorage(storage);
                            product.setColor(color);

                            Stock stock = new Stock();
                            stock.setProduct(product);
                            stock.setPrice(productDTO.getPrice());
                            stock.setQty(productDTO.getQty());


                            Status pendingStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                                    .setParameter("value", String.valueOf(Status.Type.PENDING))
                                    .getSingleResult();
                            stock.setStatus(pendingStatus);

                            Transaction transaction = hibernateSession.beginTransaction();
                            try {
                                hibernateSession.persist(product);
                                hibernateSession.persist(stock);
                                transaction.commit();
                                status = true;
                                responseObject.addProperty("productId", product.getId());
                            } catch (HibernateException e) {
                                transaction.rollback();
                            }

                        }

                    }


                }
                hibernateSession.close();
            }
        }
        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);

    }
}
