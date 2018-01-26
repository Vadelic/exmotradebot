package com.vadelic.exmo;

import com.vadelic.exmo.controller.ControllerFactory;
import com.vadelic.exmo.model.CompleteOrder;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Komyshenets on 18.01.2018.
 */
public class ContractCarrier {
    public final static int NEW = 0;
    public final static int WORK = 1;
    public final static int DONE = 2;
    public final static int CRASH = 3;

    private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(15);
    private Future<Map<String, CompleteOrder>> future = null;
    private String pair;
    private String type;
    private double profit;
    private int status;
    private double depositPercent;
    private CompleteOrder startOrderResult = null;
    private CompleteOrder reOrderResult = null;

    public ContractCarrier(String pair, String type, double profitInPercent, double depositPercent) {
        this.pair = pair;
        this.type = type;
        this.profit = profitInPercent;
        this.status = NEW;
        this.depositPercent = depositPercent;
    }

    public int getStatus() {
        return status;
    }

    public boolean runContract(ControllerFactory factory) {
        try {
            TransactionContract contract = new TransactionContract(factory.getController(pair), type, profit, depositPercent);
            future = EXECUTOR.submit(contract);
            status = WORK;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            status = CRASH;
            return false;
        }
    }

    public boolean isDone() {
        if (future != null && future.isDone()) {
            get();
            return true;
        } else {
// TODO: 18.01.2018 empty future
            return false;
        }
    }

    private void get() {
        try {
            Map<String, CompleteOrder> map = future.get();
            startOrderResult = map.get("start");
            reOrderResult = map.get("re");
            if (startOrderResult != null)
                status = DONE;
            else status = CRASH;
        } catch (Exception e) {
            status = CRASH;
        }
    }

    public void stop() {
        status = CRASH;
        future.cancel(true);
    }


    @Override
    public String toString() {
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

    public String getProfit() {
        if (startOrderResult != null && reOrderResult != null) {
            String outCurrency = startOrderResult.outCurrency;
            double v = startOrderResult.getCompleteOut() - reOrderResult.getCompleteIn() * 0.998;
            double v1 = ((reOrderResult.getCompleteIn() * 0.998) / startOrderResult.getCompleteOut() - 1) * 100;
            return String.format("%s%f (%f%%)", outCurrency, v, v1);
        }
        return "0 (0%)";
    }
}
