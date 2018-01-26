package com.vadelic.exmo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by Komyshenets on 09.01.2018.
 */
public class Trade {

    @JsonProperty("trade_id")
    public Long trade_id;

    @JsonProperty("date")
    public Date created;

    @JsonProperty("type")
    public String type;

    @JsonProperty("pair")
    public String pair;

    @JsonProperty("order_id")
    public Long orderId;

    @JsonProperty("quantity")
    public double quantity;

    @JsonProperty("price")
    public double price;

    @JsonProperty("amount")
    public double amount;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Trade{");
        sb.append("trade_id=").append(trade_id);
        sb.append(", created=").append(created);
        sb.append(", type='").append(type).append('\'');
        sb.append(", pair=").append(pair);
        sb.append(", order_id=").append(orderId);
        sb.append(", quantity=").append(quantity);
        sb.append(", price=").append(price);
        sb.append(", amount=").append(amount);
        sb.append('}');
        return sb.toString();
    }

}
