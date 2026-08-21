package com.zyrotech.controller.api;

import com.zyrotech.service.SingleProductService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/single-products")
public class SingleProductController {

    private final SingleProductService singleProductService = new SingleProductService();

    @Path("/product")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadSingleProduct(@QueryParam("Id") String productId) {
        String responseJson = singleProductService.loadSingleProduct(productId);
        return Response.ok().entity(responseJson).build();
    }
}