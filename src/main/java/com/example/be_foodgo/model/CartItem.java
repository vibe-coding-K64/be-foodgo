package com.example.be_foodgo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private static final Logger log = LoggerFactory.getLogger(CartItem.class);

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
        log.debug("[coCungTopping] this.toppings={}, other={}",
                this.toppings, other);

        boolean thisEmpty = this.toppings == null || this.toppings.isEmpty();
        boolean otherEmpty = other == null || other.isEmpty();

        log.debug("[coCungTopping] thisEmpty={}, otherEmpty={}", thisEmpty, otherEmpty);

        if (thisEmpty && otherEmpty) {
            log.debug("[coCungTopping] Ca hai deu rong -> TRUE (trung)");
            return true;
        }
        if (thisEmpty || otherEmpty) {
            log.debug("[coCungTopping] Mot ben rong, mot ben co -> FALSE (khong trung)");
            return false;
        }
        if (this.toppings.size() != other.size()) {
            log.debug("[coCungTopping] So luong topping khac nhau: {} vs {} -> FALSE (khong trung)",
                    this.toppings.size(), other.size());
            return false;
        }

        List<String> thisNames = this.toppings.stream().map(ToppingItem::getName).sorted().toList();
        List<String> otherNames = other.stream().map(ToppingItem::getName).sorted().toList();
        log.debug("[coCungTopping] thisNames={}, otherNames={}", thisNames, otherNames);

        if (!thisNames.equals(otherNames)) {
            log.debug("[coCungTopping] Ten topping khac nhau -> FALSE (khong trung)");
            return false;
        }

        for (ToppingItem thisTopping : this.toppings) {
            ToppingItem otherTopping = other.stream()
                    .filter(t -> Objects.equals(t.getName(), thisTopping.getName()))
                    .findFirst()
                    .orElse(null);
            if (otherTopping == null) {
                log.debug("[coCungTopping] Topping '{}' khong tim thay ben other -> FALSE (khong trung)",
                        thisTopping.getName());
                return false;
            }
            if (!Objects.equals(thisTopping.getPrice(), otherTopping.getPrice())) {
                log.debug("[coCungTopping] Topping '{}' gia khac nhau: {} vs {} -> FALSE (khong trung)",
                        thisTopping.getName(), thisTopping.getPrice(), otherTopping.getPrice());
                return false;
            }
        }
        log.debug("[coCungTopping] Tat ca deu trung -> TRUE (trung)");
        return true;
    }
}
