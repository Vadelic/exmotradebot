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
    private String nameThread;

    public ReOrderWaiter(MarketController controller, Order startOrder) {
        this.controller = controller;
        this.order = startOrder;
    }


    public void setName(String name) {
        this.nameThread = name;
    }

    private double calcBestPrice() {
        try {
            if ("sell".equals(order.type))
                return controller.getPairPrice("buy");
            else return controller.getPairPrice("sell");
        } catch (Exception e) {
            LOG.info("Wrong calc price");
            return 0;
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
            if (!orderExist) orderExist = controller.registerOrder(order);

            if (orderExist) {
                LOG.info("ReOrder was started...." + order);
                do {
                    orderExist = controller.orderExist(order);
                    result = controller.getOrderResult(order);

                    if (result.isComplete()) {
                        if (orderExist) {

                            if (counter < 20)
                                counter++;
                            else
                                break;
                        }
                    }

                    Thread.sleep(1000 * 60 * 3);
                } while (orderExist);
            } else {
                LOG.fatal(String.format("Can't ReOpen order %s", order));
            }


        } catch (Exception e) {
//            LOG.fatal(e, e);
//            if (controller.orderExist(order))
            controller.closeOrder(order);
        }

        LOG.info(String.format("\nCOMPLETE: %s", result));
        return result;
    }
}
