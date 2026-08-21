package com.zyrotech.controller.api;

import com.zyrotech.annotation.IsUser;
import com.zyrotech.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/carts")
public class CartController {

    CartService cartService = new CartService();

    @Path("/add-to-cart")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response addToCart(@QueryParam("sId") String sId,
                              @QueryParam("qty") String qty,
                              @Context HttpServletRequest request) {
        String responseJson = cartService.addToCart(sId, qty, request);
        return Response.ok().entity(responseJson).build();
    }

    @IsUser
    @Path("/all-carts")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadAllCarts(@Context HttpServletRequest request) {
        String responseJson = cartService.getAllUserCarts(request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/update-cart")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCartItem(@QueryParam("cartId") String cartId,
                                   @QueryParam("qty") String qty,
                                   @Context HttpServletRequest request) {
        String responseJson = cartService.updateCartItem(cartId, qty, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/delete-cart")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCartItem(@QueryParam("cartId") String cartId,
                                   @Context HttpServletRequest request) {
        String responseJson = cartService.deleteCartItem(cartId, request);
        return Response.ok().entity(responseJson).build();
    }
}