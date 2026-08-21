package com.zyrotech.service;

import com.zyrotech.dto.OrderDTO;
import com.zyrotech.dto.OrderItemDTO;
import com.zyrotech.entity.*;
import com.zyrotech.util.AppUtil;
import com.zyrotech.util.HibernateUtil;
import com.zyrotech.validation.Validator;
import com.google.gson.JsonObject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;


public class OrderService {
    public Order createPendingOrder(User user, Session hibernateSession) {
        Status pendingStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                .setParameter("value", String.valueOf(Status.Type.PENDING))
                .getSingleResult();
        Order order = new Order();
        order.setUser(user);
        order.setStatus(pendingStatus);

        hibernateSession.persist(order);

        List<Cart> cartList = hibernateSession.createQuery("FROM Cart c WHERE c.user=:user", Cart.class)
                .setParameter("user", user)
                .getResultList();
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setQty(cart.getQty());
            orderItem.setRating(AppUtil.DEFAULT_RATING_VALUE);
            orderItem.setStock(cart.getStock());
            hibernateSession.persist(orderItem);
        }
        // REMOVE THIS LINE - DON'T COMMIT HERE
        // hibernateSession.beginTransaction().commit();
        return order;
    }

    public void completeOrder(String orderId) {
        int oId = Integer.parseInt(orderId.replaceAll(Validator.NON_DIGIT_PATTERN, ""));

        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = hibernateSession.beginTransaction();
            try {
                Order order = hibernateSession.find(Order.class, oId);
                if (order == null) {
                    throw new RuntimeException("Order not found for Order ID: " + oId);
                }
                // update stock quantity
                List<OrderItem> orderItems = order.getOrderItems();
                if (orderItems != null && !orderItems.isEmpty()) {
                    for (OrderItem orderItem : orderItems) {
                        Stock stock = orderItem.getStock();
                        int updatedQty = stock.getQty() - orderItem.getQty();
                        if (updatedQty < 0) {
                            throw new RuntimeException("Insufficient stock for product: " + stock.getProduct().getTitle());
                        }
                        stock.setQty(updatedQty);
                        hibernateSession.merge(stock);
                    }
                }

                // update order status
                Status completedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.COMPLETED))
                        .getSingleResult();
                order.setStatus(completedStatus);
                hibernateSession.merge(order);

                // remove cart items
                List<Cart> cartList = hibernateSession.createQuery("FROM Cart c WHERE c.user=:user", Cart.class)
                        .setParameter("user", order.getUser())
                        .getResultList();
                for (Cart cart : cartList) {
                    hibernateSession.remove(cart);
                }
                transaction.commit();
            } catch (HibernateException e) {
                transaction.rollback();
                throw new RuntimeException("Failed to complete order: " + e.getMessage(), e);
            }
        }
    }

    public void failedOrder(String orderId) {
        int oId = Integer.parseInt(orderId.replaceAll(Validator.NON_DIGIT_PATTERN, ""));
        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = hibernateSession.beginTransaction();
            try {
                Order order = hibernateSession.find(Order.class, oId);
                if (order == null) {
                    throw new RuntimeException("Order not found for Order Id: " + oId);
                }
                Status rejectedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.REJECTED)).getSingleResult();
                order.setStatus(rejectedStatus);
                hibernateSession.merge(order);
                transaction.commit();
            } catch (HibernateException e) {
                transaction.rollback();
                throw new RuntimeException("Failed to reject order: " + oId);
            }
        }
    }

    public String verifyOrderDetails(String orderId){
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";
        int oId = Integer.parseInt(orderId.replaceAll(Validator.NON_DIGIT_PATTERN,""));
        Session hibernateSession = null;
        try {
            hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Order order = hibernateSession.find(Order.class, oId);
            if(order==null){
                message="Incorrect order details. Please check credentials!";
            }else{
                if(order.getStatus().getValue().equals(String.valueOf(Status.Type.COMPLETED))){
                    status=true;
                }
            }
        } catch (Exception e) {
            message = "Error verifying order: " + e.getMessage();
        } finally {
            if (hibernateSession != null) {
                hibernateSession.close();
            }
        }
        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String loadAllOrders() {
        JsonObject responseObject = new JsonObject();
        List<OrderDTO> orderDTOList = new ArrayList<>();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Order> orders = hibernateSession.createQuery("FROM Order o ORDER BY o.createdAt DESC", Order.class)
                    .getResultList();

            for (Order order : orders) {
                OrderDTO dto = new OrderDTO();
                dto.setOrderId(order.getId());
                dto.setStatus(order.getStatus().getValue());
                dto.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");

                User user = order.getUser();
                dto.setCustomerName(user != null ? (user.getFname() + " " + user.getLname()) : "Guest");
                dto.setCustomerEmail(user != null ? user.getEmail() : "");

                double total = 0;
                int itemCount = 0;
                for (OrderItem item : order.getOrderItems()) {
                    total += item.getStock().getPrice() * item.getQty();
                    itemCount += item.getQty();
                }
                dto.setTotalAmount(total);
                dto.setItemCount(itemCount);

                orderDTOList.add(dto);
            }
        } finally {
            hibernateSession.close();
        }

        responseObject.add("orders", AppUtil.GSON.toJsonTree(orderDTOList));
        return AppUtil.GSON.toJson(responseObject);
    }

    public String getOrderDetails(int orderId) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            Order order = hibernateSession.find(Order.class, orderId);
            if (order == null) {
                message = "Order not found";
            } else {
                OrderDTO dto = new OrderDTO();
                dto.setOrderId(order.getId());
                dto.setStatus(order.getStatus().getValue());
                dto.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");

                User user = order.getUser();
                dto.setCustomerName(user != null ? (user.getFname() + " " + user.getLname()) : "Guest");
                dto.setCustomerEmail(user != null ? user.getEmail() : "");

                List<OrderItemDTO> itemDTOList = new ArrayList<>();
                double total = 0;
                int itemCount = 0;

                for (OrderItem item : order.getOrderItems()) {
                    Stock stock = item.getStock();
                    Product product = stock.getProduct();

                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setTitle(product.getTitle());
                    itemDTO.setQty(item.getQty());
                    itemDTO.setPrice(stock.getPrice());
                    itemDTO.setColorValue(product.getColor().getValue());
                    itemDTO.setStorageValue(product.getStorage().getValue());
                    itemDTO.setImage(product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : "");

                    itemDTOList.add(itemDTO);
                    total += stock.getPrice() * item.getQty();
                    itemCount += item.getQty();
                }

                dto.setItems(itemDTOList);
                dto.setTotalAmount(total);
                dto.setItemCount(itemCount);

                responseObject.add("order", AppUtil.GSON.toJsonTree(dto));
                status = true;
            }
        } finally {
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateOrderStatus(int orderId, String statusValue) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            Order order = hibernateSession.find(Order.class, orderId);
            if (order == null) {
                message = "Order not found";
            } else {
                Status newStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", statusValue)
                        .getSingleResult();

                Transaction transaction = hibernateSession.beginTransaction();
                try {
                    order.setStatus(newStatus);
                    hibernateSession.merge(order);
                    transaction.commit();
                    status = true;
                    message = "Order status updated successfully";
                } catch (HibernateException e) {
                    transaction.rollback();
                    message = "Order status update failed";
                }
            }
        } finally {
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

}