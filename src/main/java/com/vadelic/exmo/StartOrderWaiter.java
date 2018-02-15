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
public class StartOrderWaiter implements Callable<CompleteOrder> {
    private final MarketController controller;
    private Order order;
    private final Logger LOG = Logger.getLogger(getClass());
    private final double profit;

    public StartOrderWaiter(MarketController controller, Order startOrder, double profit) {
        this.controller = controller;
        this.order = startOrder;
        this.profit = profit;
    }

    private double calcBestPrice() {
        try {
            double price = controller.getPairPrice(order.type);
            double v;
            if ("sell".equals(order.type)) {
                v = price * ((1 + this.profit) / (1 - 0.002 * 0.998));
            } else {
                v = price * (1 - 0.002 * 0.998) / (1 + this.profit);
            }
            return Math.ceil(v * 10000) / 10000;
        } catch (Exception e) {
            LOG.info("Wrong calc price");
            return 0;
        }
    }


    private void reOpenOrder() {
        double newPrice = calcBestPrice();
        if (order.price != newPrice && newPrice != 0) {
            if (controller.closeOrder(order)) {
                order.price = newPrice;
                if (controller.registerOrder(order))
                    LOG.info(String.format("Set new price: %f", newPrice));
                else controller.registerOrder(order); //timeout
            }
        }
    }

    @Nullable
    @Override
    public CompleteOrder call() {
        Thread.currentThread().setName(String.format("[%s] [%s] START ODER", controller.getPair(), order.type));

        CompleteOrder result = null;
        try {
            order.price = calcBestPrice();

            if (controller.registerOrder(order)) {
                LOG.info("Order was started...." + order);

                while (controller.orderExist(order)) {
                    result = controller.getOrderResult(order);

                    if (result.isComplete()) {
                        controller.closeOrder(order);
                        break;
                    } else {
                        reOpenOrder();
                    }

                    Thread.sleep(1000 * 60);
                }
                result = controller.getOrderResult(order);

            } else {
                LOG.fatal(String.format("Can't open order %s", order));
            }


        } catch (Exception e) {
            controller.closeOrder(order);
        }

        LOG.info(String.format("\nCOMPLETE: %s", result));
        return result;
    }
}
