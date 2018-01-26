package com.vadelic.exmo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Komyshenets on 09.01.2018.
 */
public class CompleteOrder {

    @JsonProperty("type")
    public String type;

    @JsonProperty("in_currency")
    private String inCurrency;

    @JsonProperty("in_amount")
    private double inAmount;

    @JsonProperty("out_currency")
    public String outCurrency;

    @JsonProperty("out_amount")
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
        System.out.println(result + " " + outAmount);
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
        System.out.println(result + " " + inAmount);
        return inAmount;
    }


    public boolean isComplete() {
        return trades != null;
    }

    @Override
    public String toString() {
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
