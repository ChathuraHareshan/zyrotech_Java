package com.zyrotech.controller.api;

import com.google.gson.JsonObject;
import com.zyrotech.dto.ProductDTO;
import com.zyrotech.entity.Product;
import com.zyrotech.service.FileUploadService;
import com.zyrotech.service.ProductService;
import com.zyrotech.util.AppUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Path("/products")
public class ProductController {


    @Path("/{productId}/upload-images")
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadProductImages(
            @PathParam("productId") int productId,
            @FormDataParam("images[]") List<FormDataBodyPart> bodyParts,
            @Context ServletContext context) {

        List<FileUploadService.FileItem> fileItems = new ArrayList<>();
        FileUploadService fileUploadService = new FileUploadService(context);
        ProductService productService = new ProductService();
        Product product = productService.getProductById(productId);

        if (bodyParts != null) {
            for (FormDataBodyPart bodyPart : bodyParts) {
                ContentDisposition contentDisposition = bodyPart.getContentDisposition();
                String fileName = contentDisposition.getFileName();

                if (fileName != null && !fileName.trim().isEmpty() && !fileName.equals("null")) {
                    InputStream inputStream = bodyPart.getEntityAs(InputStream.class);
                    FileUploadService.FileItem fileItem = fileUploadService.uploadFile("product/" + productId, inputStream, contentDisposition);
                    fileItems.add(fileItem);
                    product.getImages().add(fileItem.getUrl());
                }
            }
        }

        String responseJson = productService.updateProduct(product);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/save-product")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveProduct(@FormDataParam("product") String productJson, @Context HttpServletRequest request) {
        ProductDTO productDTO = AppUtil.GSON.fromJson(productJson, ProductDTO.class);
        String responseJson = new ProductService().addNewProduct(productDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{productId}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editProduct(@PathParam("productId") int productId, String jsonData) {
        ProductDTO productDTO = AppUtil.GSON.fromJson(jsonData, ProductDTO.class);
        String responseJson = new ProductService().editProduct(productId, productDTO);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{productId}/status")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateStatus(@PathParam("productId") int productId, String jsonData) {
        JsonObject body = AppUtil.GSON.fromJson(jsonData, JsonObject.class);
        String statusValue = body.get("status").getAsString();
        String responseJson = new ProductService().updateProductStatus(productId, statusValue);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{productId}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProduct(@PathParam("productId") int productId) {
        String responseJson = new ProductService().deleteProduct(productId);
        return Response.ok().entity(responseJson).build();
    }

}