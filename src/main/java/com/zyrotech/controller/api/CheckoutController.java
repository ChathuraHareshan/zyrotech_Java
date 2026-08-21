package com.zyrotech.controller.api;

import com.zyrotech.dto.CheckoutRequestDTO;
import com.zyrotech.service.CheckoutService;
import com.zyrotech.util.AppUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/checkouts")
public class CheckoutController {
    private final CheckoutService checkoutService = new CheckoutService();

    @Path("/user-checkout")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response userCheckout(String requestData, @Context HttpServletRequest request) {
        CheckoutRequestDTO checkoutRequestDTO = AppUtil.GSON.fromJson(requestData, CheckoutRequestDTO.class);
        String responseJson = checkoutService.processCheckout(checkoutRequestDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/user-checkout-data")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadUserCheckoutData(@Context HttpServletRequest request) {
        String responseJson = checkoutService.getCheckoutData(request);
        return Response.ok().entity(responseJson).build();
    }
}