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
public class OrderWaiter implements Callable<CompleteOrder> {
    private final MarketController controller;
    private Order order;
    private final Logger LOG = Logger.getLogger(getClass());
    private final double profit;
    private String nameThread;

    public OrderWaiter(MarketController controller, Order startOrder, double profit) {
        this.controller = controller;
        this.order = startOrder;
        this.profit = profit;

    }

    public OrderWaiter(MarketController controller, Order reOrder) {
        this(controller, reOrder, 0);
    }

    public void setName(String name) {
        this.nameThread = name;
    }

    private double calcBestPrice() {
        try {
            double price = controller.getPairPrice(order.type);
            double profit;

            if ("sell".equals(order.type)) {
                profit = this.profit;
            } else {
                profit = -1 * this.profit;
            }

            double v = price /** Math.pow(1 - 0.02, 2)*/ * (1 + profit);
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
                else {
                    controller.registerOrder(order); //timeout
                    LOG.info(String.format("Set new price(after timeout) : %f", newPrice));
                }
            }
        }
    }

    @Nullable
    @Override
    public CompleteOrder call() {
        Thread.currentThread().setName(nameThread);

        CompleteOrder result = null;
        try {
            order.price = calcBestPrice();
            int counter = 0;
            boolean orderExist = controller.registerOrder(order);
            if (orderExist)
                do {
                    orderExist = controller.orderExist(order);
                    result = controller.getOrderResult(order);

                    if (result.isComplete() && !orderExist) {
                        controller.closeOrder(order);
                    }

                    if (result.isComplete() && orderExist) {
                        if (counter < 20) {
                            counter++;
                            Thread.sleep(1000 * 60 * 5);
                            continue;
                        }
                        controller.closeOrder(order);
                    }
                    if (!result.isComplete() && orderExist && profit != 0) {
                        reOpenOrder();
                    }

                    Thread.sleep(profit == 0 ? 1000 * 60 * 3 : 1000 * 60);
                } while (orderExist);
            else {
                LOG.fatal(String.format("Can't open order %s", order));
            }


        } catch (Exception e) {
            controller.closeOrder(order);
        }

        LOG.info(String.format("\nCOMPLETE: %s", result));
        return result;
    }
}
