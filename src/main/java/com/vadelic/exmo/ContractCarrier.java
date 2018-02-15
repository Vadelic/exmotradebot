package com.vadelic.exmo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vadelic.exmo.controller.ControllerFactory;
import com.vadelic.exmo.model.CompleteOrder;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by Komyshenets on 18.01.2018.
 */
public class ContractCarrier {
    public final static int NEW = 0;
    public final static int WORK = 1;
    public final static int DONE = 2;
    public final static int CRASH = 3;
    private static int counter = 0;

    @JsonProperty("index")
    public final int index;

    @JsonProperty("status")
    private int status;

    @JsonIgnoreProperties
    private Future<Map<String, CompleteOrder>> future = null;
    @JsonIgnoreProperties
    private TransactionContract contract = null;
    @JsonProperty("pair")
    private String pair;
    @JsonProperty("type")
    private String type;
    @JsonProperty("profit")
    private double profit;
    @JsonProperty("depositPercent")
    private double depositPercent;
    @JsonProperty("startOrderResult")
    private CompleteOrder startOrderResult = null;
    @JsonProperty("reOrderResult")
    private CompleteOrder reOrderResult = null;


    public ContractCarrier(String pair, String type, double profitInPercent, double depositPercent) {
        this.pair = pair;
        this.type = type;
        this.profit = profitInPercent;
        this.status = NEW;
        this.depositPercent = depositPercent;
        this.index = counter++;
    }

    public int getStatus() {
        return status;
    }

    public boolean runContract(ExecutorService executor, ControllerFactory factory) {
        try {
            contract = new TransactionContract(factory.getPairController(pair), type, profit, depositPercent);
            future = executor.submit(contract);
            status = WORK;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            status = CRASH;
            return false;
        }
    }

    public void stopContract() {
        status = CRASH;
        contract.cancel();
        future.cancel(true);
    }

    public boolean isDone() {
        if (future != null && future.isDone()) {
            try {
                Map<String, CompleteOrder> map = future.get();
                startOrderResult = map.get("start");
                reOrderResult = map.get("re");

                if (startOrderResult != null) {
                    status = DONE;
                    return true;
                } else
                    status = CRASH;
            } catch (Exception e) {
                status = CRASH;

            }
        }
        return false;

    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            final StringBuffer sb = new StringBuffer("ContractCarrier{");
            sb.append("pair='").append(pair).append('\'');
            sb.append(", type='").append(type).append('\'');
            sb.append(", profit=").append(profit);
            sb.append(", status=").append(status);
            sb.append(", depositPercent=").append(depositPercent);
            sb.append(", startOrderResult=").append(startOrderResult);
            sb.append(", reOrderResult=").append(reOrderResult);
            sb.append('}');
            return sb.toString();
        }
    }

    public String getProfitResult() {
        if (startOrderResult != null && reOrderResult != null) {
            String outCurrency = startOrderResult.outCurrency;
            double v = reOrderResult.getCompleteIn() * 0.998 - startOrderResult.getCompleteOut();
            double v1 = v / startOrderResult.getCompleteOut() * 100;
            return String.format("%s %f (%f%%)", outCurrency, v, v1);
        }
        return "0 (0%)";
    }
}
