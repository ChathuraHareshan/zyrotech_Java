package com.zyrotech.service;

import com.zyrotech.dto.ProductDTO;
import com.zyrotech.entity.*;
import com.zyrotech.util.AppUtil;
import com.zyrotech.util.HibernateUtil;
import com.google.gson.JsonObject;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class ContentService {


    public String loadAllColorStorage() {

        JsonObject resJsonObject = new JsonObject();

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        List<Color> colorList = hibernateSession.createQuery("from Color c", Color.class).getResultList();
        List<Storage> storageList = hibernateSession.createQuery("from Storage s", Storage.class).getResultList();
        List<Category> categoryList = hibernateSession.createQuery("from Category ca", Category.class).getResultList();

        resJsonObject.add("colors", AppUtil.GSON.toJsonTree(colorList));
        resJsonObject.add("storage", AppUtil.GSON.toJsonTree(storageList));
        resJsonObject.add("category", AppUtil.GSON.toJsonTree(categoryList));

        hibernateSession.close();

        return AppUtil.GSON.toJson(resJsonObject);


    }


    public String loadProducts(){

        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        List<ProductDTO> productDTOList = new ArrayList<>();

        List<Stock> stockList = hibernateSession.createQuery("FROM Stock s ORDER BY s.createdAt DESC", Stock.class)
                .getResultList();

        for(Stock stock :stockList){
            Product product = stock.getProduct();
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(product.getId());
            productDTO.setTitle(product.getTitle());
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
            productDTO.setDescription(product.getDescription());
            productDTOList.add(productDTO);
        }
        hibernateSession.close();
        responseObject.add("newArrivals", AppUtil.GSON.toJsonTree(productDTOList));
        return AppUtil.GSON.toJson(responseObject);

    }

}
