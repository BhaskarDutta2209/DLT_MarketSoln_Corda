package com.template.webserver.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderModel {

    private final String productKey;
    private final String buyerAccountName;
    private final String shopAccountName;
    private final String deliveryAddress;

    public OrderModel(@JsonProperty("productKey") String productKey,
                      @JsonProperty("buyerAccountName") String buyerAccountName,
                      @JsonProperty("shopAccountName") String shopAccountName,
                      @JsonProperty("deliveryAddress") String deliveryAddress) {
        this.productKey = productKey;
        this.buyerAccountName = buyerAccountName;
        this.shopAccountName = shopAccountName;
        this.deliveryAddress = deliveryAddress;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getBuyerAccountName() {
        return buyerAccountName;
    }

    public String getShopAccountName() {
        return shopAccountName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }
}
