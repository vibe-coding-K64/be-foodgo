package com.example.be_foodgo.model;

import com.example.be_foodgo.dto.CartRequest.SelectedOption;
import com.example.be_foodgo.dto.CartRequest.SelectedOptionGroup;
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
    private String note;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    @Deprecated
    private String size;

    @Deprecated
    private Double sizePrice;

    @Deprecated
    private List<ToppingItem> toppings;

    private List<SelectedOptionGroup> selectedOptions;

    @Deprecated
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
        if (other == null) {
            return selectedOptions == null || selectedOptions.isEmpty();
        }
        return coCungSelectedOptions(convertToSelectedOptions(other));
    }

    public boolean coCungSelectedOptions(List<SelectedOptionGroup> other) {
        log.debug("[coCungSelectedOptions] this.selectedOptions={}, other={}", this.selectedOptions, other);

        boolean thisEmpty = selectedOptions == null || selectedOptions.isEmpty();
        boolean otherEmpty = other == null || other.isEmpty();

        if (thisEmpty && otherEmpty) {
            log.debug("[coCungSelectedOptions] Ca hai deu rong -> TRUE (trung)");
            return true;
        }
        if (thisEmpty || otherEmpty) {
            log.debug("[coCungSelectedOptions] Mot ben rong, mot ben co -> FALSE (khong trung)");
            return false;
        }

        if (this.selectedOptions.size() != other.size()) {
            log.debug("[coCungSelectedOptions] So luong nhom khac nhau: {} vs {} -> FALSE",
                    this.selectedOptions.size(), other.size());
            return false;
        }

        for (SelectedOptionGroup thisGroup : this.selectedOptions) {
            SelectedOptionGroup otherGroup = other.stream()
                    .filter(g -> Objects.equals(g.getName(), thisGroup.getName()))
                    .findFirst()
                    .orElse(null);
            if (otherGroup == null) {
                log.debug("[coCungSelectedOptions] Nhom '{}' khong ton tai ben other -> FALSE",
                        thisGroup.getName());
                return false;
            }
            if (!cungOptionTrongGroup(thisGroup, otherGroup)) {
                return false;
            }
        }

        log.debug("[coCungSelectedOptions] Tat ca deu trung -> TRUE");
        return true;
    }

    private boolean cungOptionTrongGroup(SelectedOptionGroup a, SelectedOptionGroup b) {
        List<String> thisNames = a.getOptions() == null
                ? List.of()
                : a.getOptions().stream().map(SelectedOption::getName).sorted().toList();
        List<String> otherNames = b.getOptions() == null
                ? List.of()
                : b.getOptions().stream().map(SelectedOption::getName).sorted().toList();

        if (thisNames.size() != otherNames.size()) {
            log.debug("[coCungSelectedOptions] Group '{}' - so luong option khac: {} vs {} -> FALSE",
                    a.getName(), thisNames.size(), otherNames.size());
            return false;
        }
        if (!thisNames.equals(otherNames)) {
            log.debug("[coCungSelectedOptions] Group '{}' - ten option khac nhau -> FALSE", a.getName());
            return false;
        }
        return true;
    }

    private List<SelectedOptionGroup> convertToSelectedOptions(List<ToppingItem> toppings) {
        if (toppings == null || toppings.isEmpty()) {
            return null;
        }
        List<SelectedOption> options = toppings.stream()
                .map(t -> SelectedOption.builder().name(t.getName()).build())
                .toList();
        return List.of(
                SelectedOptionGroup.builder()
                        .name("Topping")
                        .options(options)
                        .build()
        );
    }
}
