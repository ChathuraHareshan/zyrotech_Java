package com.zyrotech.service;

import com.zyrotech.dto.ProductDTO;
import com.zyrotech.entity.Product;
import com.zyrotech.entity.Stock;
import com.zyrotech.util.AppUtil;
import com.zyrotech.util.HibernateUtil;
import com.google.gson.JsonObject;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class SingleProductService {

    public String loadSingleProduct(String id) {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = null;
        boolean status = false;
        String message = "";
        List<ProductDTO> productDTOList = new ArrayList<>();

        try {
            hibernateSession = HibernateUtil.getSessionFactory().openSession();

            if (id == null || id.trim().isEmpty()) {
                message = "Product ID is required!";
                responseObject.addProperty("status", status);
                responseObject.addProperty("message", message);
                return AppUtil.GSON.toJson(responseObject);
            }

            try {
                int productId = Integer.parseInt(id);

                Stock stock = hibernateSession.createQuery("FROM Stock s WHERE s.product.id = :pId", Stock.class)
                        .setParameter("pId", productId)
                        .getSingleResult();

                if (stock != null) {
                    Product product = stock.getProduct();
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setProductId(product.getId());
                    productDTO.setTitle(product.getTitle());
                    productDTO.setDescription(product.getDescription());
                    productDTO.setColorId(product.getColor().getId());
                    productDTO.setColorValue(product.getColor().getValue());
                    productDTO.setImages(product.getImages());
                    productDTO.setStockId(stock.getId());
                    productDTO.setQty(stock.getQty());
                    productDTO.setPrice(stock.getPrice());
                    productDTO.setStorageId(product.getStorage().getId());
                    productDTO.setStorageValue(product.getStorage().getValue());
                    productDTO.setCategoryId(product.getCategory().getId());
                    productDTO.setCategoryName(product.getCategory().getName());
                    productDTOList.add(productDTO);
                    status = true;
                    message = "Product loaded successfully!";
                } else {
                    message = "Product not found!";
                }
            } catch (NumberFormatException e) {
                message = "Invalid product ID format!";
            } catch (Exception e) {
                message = "Product not found!";
            }

        } catch (Exception e) {
            message = "Error loading product: " + e.getMessage();
        } finally {
            if (hibernateSession != null) {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        responseObject.add("newArrivals", AppUtil.GSON.toJsonTree(productDTOList));
        return AppUtil.GSON.toJson(responseObject);
    }
}