package com.zyrotech.controller.api;

import com.zyrotech.service.InvoiceService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService = new InvoiceService();

    @Path("/user-invoice")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadInvoiceData(@QueryParam("orderId") String orderId) {
        String responseJson = invoiceService.getInvoiceData(orderId);
        return Response.ok().entity(responseJson).build();
    }
}
