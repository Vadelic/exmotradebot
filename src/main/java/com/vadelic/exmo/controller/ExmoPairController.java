package com.vadelic.exmo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vadelic.exmo.market.Exmo;
import com.vadelic.exmo.market.ExmoRestApiException;
import com.vadelic.exmo.model.CompleteOrder;
import com.vadelic.exmo.model.Order;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Created by Komyshenets on 12.01.2018.
 */
public class ExmoPairController implements MarketController {
    private final Map<String, Double> pairSettings;
    private Exmo market;
    private String currencyA;
    private String currencyB;

    private final Logger LOG = Logger.getLogger(getClass());

    public ExmoPairController(Exmo market, String pair) throws ExmoRestApiException {
        this.market = market;
        this.pairSettings = market.pairSettings().get(pair);

        int i = pair.indexOf("_");
        currencyA = pair.substring(0, i);
        currencyB = pair.substring(i + 1);
    }


    private String getCurrency(String typeContract) {
        if ("sell".equals(typeContract)) return currencyA;
        else return currencyB;
    }

    private boolean isValidQuantity(Double quantity) {
        return pairSettings.get("min_quantity") <= quantity && pairSettings.get("max_quantity") >= quantity;
    }

    @Override
    public double getDeposit(String typeContract) {
        String currency = getCurrency(typeContract);

        try {
            Map<String, Object> map = market.userInfo();
            Object balances = map.get("balances");
            if (balances instanceof Map) {
                return Double.valueOf(String.valueOf(((Map) balances).get(currency)));
            }

        } catch (ExmoRestApiException e) {
            LOG.warn(e + "\n" + getPair() + "\n" + currency);
        }
        return 0;
    }

    @Override
    public double getPairPrice(String typeContract) {
        try {
            Map<String, Map> book = market.stockOrderBook(1, getPair());
            Map currencyBook = book.get(getPair());
            if ("sell".equals(typeContract))
                return Double.valueOf(String.valueOf(currencyBook.get("ask_top")));

            if ("buy".equals(typeContract))
                return Double.valueOf(String.valueOf(currencyBook.get("bid_top")));
        } catch (ExmoRestApiException e) {
            LOG.warn(e);
        }

        return 0;
    }

    @Override
    public boolean registerOrder(Order order) {
        try {

            double quantity = order.quantity;
            if ("buy".equals(order.type)) {
                quantity /= order.price;
            }
            order.orderId = market.createOrder(getPair(), quantity, order.price, order.type);
            LOG.debug(String.format("Order was create %s", order));
            return true;
        } catch (ExmoRestApiException e) {

            LOG.debug(String.format("\n%s Order %s was't create %s", e, getPair(), order));
            return false;
        }

    }

    @Override
    public CompleteOrder getOrderResult(Order order) {
        try {
            Map<String, Object> fromValue = market.orderTrades(order.orderId);
            CompleteOrder completeOrder = new ObjectMapper().convertValue(fromValue, CompleteOrder.class);
            completeOrder.order = order;
            return completeOrder;

        } catch (ExmoRestApiException e) {
            if (e.getCode() != 50304)
                LOG.warn(e + " " + getPair() + " " + order);
        }
        return new CompleteOrder();
    }

    @Override
    public boolean closeOrder(Order order) {
        try {
            if (market.cancelOrder(order.orderId)) {
                LOG.debug(String.format("\nOrder was close %s", order));
                return true;
            }
        } catch (ExmoRestApiException e) {
            LOG.debug(e + " " + getPair() + " " + order);
        }
        return false;

    }

    @Override
    public boolean orderExist(Order order) {

        try {
            return isOrderExist(order);
        } catch (ExmoRestApiException e) {
            LOG.warn(e + " " + getPair() + " " + order);
            try {
                return isOrderExist(order);
            } catch (ExmoRestApiException e1) {
                return false;
            }
        }
    }


    private boolean isOrderExist(Order order) throws ExmoRestApiException {
        if (order.orderId == 0) return false;
        List<Map<String, Object>> maps = market.openOrders().get(getPair());
        if (maps != null)
            for (Map<String, Object> map : maps) {
                if (String.valueOf(order.orderId).equals(map.get("order_id"))) return true;
            }
        return false;
    }

    @Override
    @NotNull
    public String getPair() {
        return currencyA + "_" + currencyB;
    }

}
