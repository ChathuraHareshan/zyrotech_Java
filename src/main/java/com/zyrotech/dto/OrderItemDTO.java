package com.zyrotech.dto;

public class OrderItemDTO {
    private String title;
    private int qty;
    private double price;
    private String colorValue;
    private String storageValue;
    private String image;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getColorValue() {
        return colorValue;
    }

    public void setColorValue(String colorValue) {
        this.colorValue = colorValue;
    }

    public String getStorageValue() {
        return storageValue;
    }

    public void setStorageValue(String storageValue) {
        this.storageValue = storageValue;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}