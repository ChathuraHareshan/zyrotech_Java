package com.zyrotech.controller.api;

import com.google.gson.JsonObject;
import com.zyrotech.service.OrderService;
import com.zyrotech.util.AppUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orders")
public class OrderController {
    private final OrderService orderService = new OrderService();
    @Path("/verify-order")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyOrder(@QueryParam("orderId")String orderId){
        String responseJson = orderService.verifyOrderDetails(orderId);
        return Response.ok().entity(responseJson).build();
    }


    @Path("/list")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadAllOrders() {
        String responseJson = orderService.loadAllOrders();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{orderId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrderDetails(@PathParam("orderId") int orderId) {
        String responseJson = orderService.getOrderDetails(orderId);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{orderId}/status")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateOrderStatus(@PathParam("orderId") int orderId, String jsonData) {
        JsonObject body = AppUtil.GSON.fromJson(jsonData, JsonObject.class);
        String statusValue = body.get("status").getAsString();
        String responseJson = orderService.updateOrderStatus(orderId, statusValue);
        return Response.ok().entity(responseJson).build();
    }
}
