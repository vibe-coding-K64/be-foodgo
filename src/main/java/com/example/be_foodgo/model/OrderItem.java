package com.example.be_foodgo.model;

public class OrderItem {
    private String name;
    private Object options;
    private int quantity;
    private double price;

    public OrderItem() {}

    public OrderItem(String name, Object options, int quantity, double price) {
        this.name = name;
        this.options = options;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOptions() {
        if (options instanceof java.util.List) {
            try {
                return String.join(", ", (java.util.List<String>) options);
            } catch (Exception e) {
                return options.toString();
            }
        } else if (options != null) {
            return options.toString();
        }
        return "";
    }
    public void setOptions(Object options) { this.options = options; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
