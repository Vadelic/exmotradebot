package com.vadelic.exmo;

import com.vadelic.exmo.controller.MarketController;
import com.vadelic.exmo.model.CompleteOrder;
import com.vadelic.exmo.model.Order;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

/**
 * Created by Komyshenets on 11.01.2018.
 */
public class ReOrderWaiter implements Callable<CompleteOrder> {
    private final MarketController controller;
    private Order order;
    private final Logger LOG = Logger.getLogger(getClass());

    public ReOrderWaiter(MarketController controller, Order startOrder) {
        this.controller = controller;
        this.order = startOrder;
    }


    private double calcBestPrice() {
        try {
            return controller.getPairPrice(order.type);
        } catch (Exception e) {
            LOG.info("Wrong calc price");
            return 0;
        }
    }


    @Nullable
    @Override
    public CompleteOrder call() {
        Thread.currentThread().setName(String.format("[%s] [%s] RE ODER", controller.getPair(), order.type));

        CompleteOrder result;
        try {
            order.price = calcBestPrice();
            boolean orderExist = controller.registerOrder(order);
            if (!orderExist) orderExist = controller.registerOrder(order);

            if (orderExist) {
                LOG.info("ReOrder was started...." + order);
                do {
                    orderExist = controller.orderExist(order);
                    Thread.sleep(1000 * 60 * 1);
                } while (orderExist);
            } else {
                LOG.fatal(String.format("Can't ReOpen order %s", order));
            }


        } catch (Exception e) {
            controller.closeOrder(order);
        }
        result = controller.getOrderResult(order);
        LOG.info(String.format("\nCOMPLETE: %s", result));
        return result;
    }
}
