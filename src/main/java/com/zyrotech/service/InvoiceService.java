package com.zyrotech.service;


import com.google.gson.JsonObject;
import com.zyrotech.dto.InvoiceDTO;
import com.zyrotech.dto.InvoiceItemDTO;
import com.zyrotech.entity.*;
import com.zyrotech.util.AppUtil;
import com.zyrotech.util.HibernateUtil;
import com.zyrotech.validation.Validator;
import org.hibernate.Session;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InvoiceService {
    private static final String INVOICE_PAID_STATUS = "PAID";

    public String getInvoiceData(String orderId) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        int oId = Integer.parseInt(orderId.replaceAll(Validator.NON_DIGIT_PATTERN, ""));
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Order order = hibernateSession.find(Order.class, oId);
        if (order == null) {
            message = "Incorrect order details. Please check credentials!";
        } else {
            if (order.getStatus().getValue().equals(String.valueOf(Status.Type.COMPLETED))) {
                InvoiceDTO invoiceDTO = new InvoiceDTO();
                invoiceDTO.setInvoiceNo("000" + order.getId());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                invoiceDTO.setInvoiceDate(formatter.format(order.getCreatedAt()));

                User user = order.getUser();
                invoiceDTO.setBuyerName(user.getFname() + " " + user.getLname());
                Address address = hibernateSession.createQuery("FROM Address a WHERE a.user=:user AND a.isPrimary=true", Address.class)
                        .setParameter("user", user)
                        .getSingleResult();
                invoiceDTO.setAddress(address.getLineOne() +
                        (address.getLineTwo() != null && !address.getLineTwo().isBlank() ? ", " + address.getLineTwo() : ""));
                invoiceDTO.setCityName(address.getCity().getName());
                invoiceDTO.setEmail(user.getEmail());

                List<InvoiceItemDTO> itemDTOS = new ArrayList<>();

                for (OrderItem orderItem : order.getOrderItems()) {
                    InvoiceItemDTO itemDTO = new InvoiceItemDTO();
                    itemDTO.setItemName(orderItem.getStock().getProduct().getTitle());
                    itemDTO.setItemQty(orderItem.getQty());
                    itemDTO.setItemPrice(orderItem.getStock().getPrice());
                    itemDTOS.add(itemDTO);

                }
                invoiceDTO.setInvoiceItemDTOList(itemDTOS);
                invoiceDTO.setInvoiceStatus(InvoiceService.INVOICE_PAID_STATUS);

                status = true;
                responseObject.add("invoiceData", AppUtil.GSON.toJsonTree(invoiceDTO));
            }
        }
        hibernateSession.close();

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }
}
