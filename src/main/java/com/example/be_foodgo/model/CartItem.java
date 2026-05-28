package com.example.be_foodgo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private String id;
    private String userId;
    private String storeId;
    private String foodId;
    private String name;
    private Double price;
    private Integer quantity;
    private String size;
    private Double sizePrice;
    private List<ToppingItem> toppings;
    private String note;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingItem {

        private String name;
        private Double price;

        public boolean equalsByName(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ToppingItem that = (ToppingItem) o;
            return that.name != null && that.name.equals(this.name);
        }
    }

    public boolean coCungTopping(List<ToppingItem> other) {
        boolean thisEmpty = this.toppings == null || this.toppings.isEmpty();
        boolean otherEmpty = other == null || other.isEmpty();
        if (thisEmpty && otherEmpty) return true;
        if (thisEmpty || otherEmpty) return false;
        if (this.toppings.size() != other.size()) return false;
        List<String> thisNames = this.toppings.stream().map(ToppingItem::getName).sorted().toList();
        List<String> otherNames = other.stream().map(ToppingItem::getName).sorted().toList();
        return thisNames.equals(otherNames);
    }
}
