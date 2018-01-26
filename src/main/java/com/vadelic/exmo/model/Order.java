package com.vadelic.exmo.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Komyshenets on 09.01.2018.
 */
public class Order {
    @JsonProperty("type")
    @JsonAlias({"order_type"})
    public String type;

    @JsonProperty("price")
    public double price;

    @JsonProperty("quantity")
    public double quantity;

    @JsonProperty("order_id")
    public int orderId;

    public Order() {
    }

    public Order(String typeContract, double quantity) {
        this.type = typeContract;
        this.price = 0;
        this.quantity = quantity;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Order{");
        sb.append("type='").append(type).append('\'');
        sb.append(", price=").append(price);
        sb.append(", quantity=").append(quantity);
        sb.append(", order_id=").append(orderId);
        sb.append('}');
        return sb.toString();
    }

}
