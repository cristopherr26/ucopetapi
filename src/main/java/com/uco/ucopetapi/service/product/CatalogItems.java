package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.CatalogItemDomain;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

public final class CatalogItems {

    private CatalogItems() {
    }

    public static void applyCommonFields(CatalogItemDomain item, String name, String description,
                                         String imageUrl, Integer price, TaxCategory taxCategory,
                                         Boolean sellable) {
        if (name != null) {
            item.setName(name);
        }
        if (description != null) {
            item.setDescription(description);
        }
        if (imageUrl != null) {
            item.setImageUrl(imageUrl);
        }
        if (price != null) {
            item.setPrice(price);
        }
        if (taxCategory != null) {
            item.setTaxCategory(taxCategory);
        }
        if (sellable != null) {
            item.setSellable(sellable);
        }
    }

    public static void applyPriceConsistency(CatalogItemDomain item, String itemLabel) {
        if (Boolean.TRUE.equals(item.getSellable())) {
            CatalogValidationUtils.requirePriceWhenSellable(true, item.getPrice(), itemLabel);
        } else {
            item.setPrice(null);
        }
    }
}
