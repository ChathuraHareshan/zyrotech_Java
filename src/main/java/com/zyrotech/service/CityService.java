package com.zyrotech.service;

import com.zyrotech.entity.City;
import com.zyrotech.util.AppUtil;
import com.zyrotech.util.HibernateUtil;
import com.google.gson.JsonObject;
import org.hibernate.Session;

import java.util.List;

public class CityService {

    public String loadAllCities() {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = null;

        try {
            hibernateSession = HibernateUtil.getSessionFactory().openSession();
            List<City> cityList = hibernateSession.createQuery("FROM City c ORDER BY c.name", City.class).getResultList();

            responseObject.add("cities", AppUtil.GSON.toJsonTree(cityList));
            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Cities loaded successfully");
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load cities: " + e.getMessage());
        } finally {
            if (hibernateSession != null) {
                hibernateSession.close();
            }
        }

        return AppUtil.GSON.toJson(responseObject);
    }
}