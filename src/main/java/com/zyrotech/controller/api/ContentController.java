package com.zyrotech.controller.api;

import com.zyrotech.service.CityService;
import com.zyrotech.service.ContentService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/content")
public class ContentController {

    @Path("/cities")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadCities() {
        String loadAllCities = new CityService().loadAllCities();
        return Response.ok().entity(loadAllCities).build();
    }

    @Path("/colorStorage")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadColorStorage() {
        String responseJson = new ContentService().loadAllColorStorage();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/product")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadProductTab() {
        String responseJson = new ContentService().loadProducts();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/data/cities")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadCitiesData() {
        String loadAllCities = new CityService().loadAllCities();
        return Response.ok().entity(loadAllCities).build();
    }
}