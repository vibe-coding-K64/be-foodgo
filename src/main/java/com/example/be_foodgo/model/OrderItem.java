package com.example.be_foodgo.model;

public class OrderItem {
    private String foodId;
    private String imageUrl;
    private String name;
    private String size;
    private Object options;
    private int quantity;
    private double price;

    public OrderItem() {}

    public OrderItem(String foodId, String imageUrl, String name, String size, Object options, int quantity, double price) {
        this.foodId = foodId;
        this.imageUrl = imageUrl;
        this.name = name;
        this.size = size;
        this.options = options;
        this.quantity = quantity;
        this.price = price;
    }

    public String getFoodId() { return foodId; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
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
