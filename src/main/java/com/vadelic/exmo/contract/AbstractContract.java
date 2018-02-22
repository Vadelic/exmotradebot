package com.vadelic.exmo.contract;

import com.vadelic.exmo.controller.MarketController;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Komyshenets on 15.02.2018.
 */
public abstract class AbstractContract extends Thread implements TransactionContract {
    public static final String SELL = "sell";
    public static final String BUY = "buy";

    private static int counter = 0;
    final Logger LOG = Logger.getLogger(getClass());
    final MarketController pairController;
    final String typeContract;
    private final int index;
    volatile int status;

    AbstractContract(MarketController controller, String typeContract) {
        this.pairController = controller;
        this.typeContract = typeContract;
        this.index = counter++;
        this.status = NEW;
    }

    @NotNull
    String getPair() {
        return pairController.getPair();
    }

    double getDeposit() {
        return pairController.getDeposit(typeContract);
    }

    @Override
    public void closeContract() {
        status = CRASH;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void startContract() {
        start();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("index:").append(index);
        sb.append(", status:").append(status);
        sb.append(", pair:\'").append(pairController.getPair()).append('\'');
        sb.append(", type:\'").append(typeContract).append('\'');
        sb.append(", ");
        return sb.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
            return super.clone();
    }
}
