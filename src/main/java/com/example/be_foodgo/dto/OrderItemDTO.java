package com.example.be_foodgo.dto;

public class OrderItemDTO {
    private String name;
    private String options;
    private int quantity;
    private double price;

    public OrderItemDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
