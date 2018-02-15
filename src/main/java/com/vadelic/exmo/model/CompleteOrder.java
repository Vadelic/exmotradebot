package com.vadelic.exmo.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Created by Komyshenets on 09.01.2018.
 */
public class CompleteOrder {

    @JsonProperty("type")
    public String type;

    @JsonProperty("in_currency")
    @JsonAlias({"inCurrency"})
    private String inCurrency;

    @JsonProperty("in_amount")
    @JsonAlias({"inAmount"})
    private double inAmount;

    @JsonProperty("out_currency")
    @JsonAlias({"outCurrency"})
    public String outCurrency;

    @JsonProperty("out_amount")
    @JsonAlias({"outAmount"})
    private double outAmount;

    @JsonProperty("trades")
    private List<Trade> trades;

    public Order order;

    public double getCompleteOut() {
        double result = 0;
        if (trades != null) {
            for (Trade trade : trades) {
                if ("sell".equals(type)) {
                    result += trade.quantity;
                } else result += trade.amount;
            }
        }
        return outAmount;
    }

    public double getCompleteIn() {
        double result = 0;
        if (trades != null) {
            for (Trade trade : trades) {
                if ("buy".equals(type)) {
                    result += trade.quantity;
                } else result += trade.amount;
            }

        }
        return inAmount;
    }


    public boolean isComplete() {
        return trades != null;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            final StringBuffer sb = new StringBuffer("CompleteOrder{");
            sb.append("type='").append(type).append('\'');
            sb.append(", inCurrency='").append(inCurrency).append('\'');
            sb.append(", inAmount=").append(inAmount);
            sb.append(", outCurrency='").append(outCurrency).append('\'');
            sb.append(", outAmount=").append(outAmount);
            sb.append(", trades=").append(trades);
            sb.append(", order=").append(order);
            sb.append('}');
            return sb.toString();
        }
    }
}
