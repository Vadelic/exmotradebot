package com.vadelic.exmo.contract;

import com.vadelic.exmo.controller.MarketController;
import com.vadelic.exmo.model.CompleteOrder;
import com.vadelic.exmo.model.Order;
import com.vadelic.exmo.waiter.OrderWaiter;
import com.vadelic.exmo.waiter.ReOrderWaiter;
import com.vadelic.exmo.waiter.StartOrderWaiter;

import java.util.concurrent.*;

/**
 * Created by Komyshenets on 11.01.2018.
 */
public class HomoRazerContract extends AbstractContract {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final double profitPercent;

    private CompleteOrder startOrderResult = null;
    private CompleteOrder reOrderResult = null;
    private volatile boolean force = false;

    public HomoRazerContract(MarketController controller, String typeContract, double profitInPercent) {
        super(controller, typeContract);
        this.profitPercent = profitInPercent;
        setName(String.format("[%s] [%s] CONTRACT", getPair(), typeContract));
    }


    @Override
    public void run() {
        LOG.debug("Contract started");
        status = WORK;
        try {
            OrderWaiter orderWaiter = createStartOrder();
            startOrderResult = waitOrder(orderWaiter);
            if (startOrderResult != null) {

                double completeIn = startOrderResult.getCompleteIn();
                if (completeIn != 0) {
                    OrderWaiter reOrder = createReOrder(completeIn);
                    reOrderResult = waitOrder(reOrder);
                }
            }
            status = DONE;
        } catch (Exception e) {
            status = CRASH;
        }
    }


    private CompleteOrder waitOrder(OrderWaiter orderWaiter) throws InterruptedException, java.util.concurrent.ExecutionException {
        LOG.debug("start Waiter");

        Future<CompleteOrder> future = executor.submit(orderWaiter);
        while (status == WORK) {

            try {
                if (force){
                    orderWaiter.force();
                    force=false;
                }
                return future.get(3, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
            }
        }
        orderWaiter.closeOrder();
        return null;
    }

    @Override
    public String getProfitResult() {
        if (startOrderResult != null && reOrderResult != null) {
            String outCurrency = startOrderResult.outCurrency;
            double v = reOrderResult.getCompleteIn() * 0.998 - startOrderResult.getCompleteOut();
            double v1 = v / startOrderResult.getCompleteOut() * 100;
            return String.format("%s %f (%f%%)", outCurrency, v, v1);
        }

        return String.format("[%s] %s", typeContract, startOrderResult);
    }

    @Override
    public void force() {
        this.force = true;
    }

    private OrderWaiter createStartOrder() {

        double validDeposit = getDeposit();
        Order order = new Order(typeContract, validDeposit);
        return new StartOrderWaiter(pairController, order, (profitPercent / 100));

    }

    private OrderWaiter createReOrder(double completeIn) {
        Order reTradeOrder;

        double quantity = completeIn /** (1 - 0.02)*/;
        if (SELL.equals(typeContract)) {
            reTradeOrder = new Order(BUY, quantity);
        } else {
            reTradeOrder = new Order(SELL, quantity);
        }

        return new ReOrderWaiter(pairController, reTradeOrder);
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(super.toString());
        sb.append("profit:").append(profitPercent);
        sb.append(", start:").append(startOrderResult);
        sb.append(", reOrder:").append(reOrderResult);
        sb.append('}');
        return sb.toString();
    }
}
