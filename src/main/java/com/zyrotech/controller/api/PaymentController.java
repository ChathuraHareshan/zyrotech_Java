package com.zyrotech.controller.api;

import com.zyrotech.service.OrderService;
import com.zyrotech.util.Env;
import com.zyrotech.util.PayHereUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/payments")
public class PaymentController {
    @Path("/return")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response paymentSuccess(@QueryParam("orderId") String orderId) {
        return Response.seeOther(URI.create(Env.get("app.url") + "/invoice.html?orderId=" + orderId)).build();
    }

    @Path("/cancel")
    @GET
    public Response paymentCancel() {
        System.out.println("Payment canceled");
        return Response.ok().build();
    }

    @Path("/notify")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response paymentNotify(MultivaluedMap<String, String> form) {
        String orderId = form.getFirst("order_id");
        String statusCode = form.getFirst("status_code");

        if (!PayHereUtil.validateNotify(form)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("INVALID SIGNATURE").build();
        }

        OrderService orderService = new OrderService();
        if (Integer.parseInt(statusCode) == PayHereUtil.PAYMENT_SUCCESS) {
            // success situation
            orderService.completeOrder(orderId);
        } else {
            // failed situation
            orderService.failedOrder(orderId);
        }
        return Response.ok().build();
    }
}
