package com.vadelic.exmo.waiter;

import com.vadelic.exmo.controller.MarketController;
import com.vadelic.exmo.model.CompleteOrder;
import com.vadelic.exmo.model.Order;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Komyshenets on 11.01.2018.
 */
public class ReOrderWaiter implements OrderWaiter {
    private final MarketController controller;
    private Order order;
    private final Logger LOG = Logger.getLogger(getClass());
    private volatile boolean workFlag;

    public ReOrderWaiter(MarketController controller, Order startOrder) {
        this.controller = controller;
        this.order = startOrder;
        this.workFlag = true;
    }


    @Nullable
    @Override
    public CompleteOrder call() {

        Thread.currentThread().setName(String.format("[%s] [%s] RE ODER", controller.getPair(), order.type));
        if (isOpenOrder()) {
            LOG.info("ReOrder was started...." + order);

            while (controller.orderExist(order) && workFlag) {

                try {
                    Thread.sleep(1000 * 60 * 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            LOG.fatal(String.format("Can't ReOpen order %s", order));
        }


        if (controller.orderExist(order))
            controller.closeOrder(order);

        CompleteOrder result = controller.getOrderResult(order);
        LOG.info(String.format("COMPLETE: %s", result));
        return result;
    }

    private boolean isOpenOrder() {
        order.price = controller.getPairPrice(order.type);
        boolean orderExist = controller.registerOrder(order);
        if (!orderExist) orderExist = controller.registerOrder(order);
        return orderExist;
    }

    @Override
    public void closeOrder() {
        workFlag = false;
    }
}
