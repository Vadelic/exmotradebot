package com.vadelic.exmo.controller;

import com.vadelic.exmo.model.CompleteOrder;
import com.vadelic.exmo.model.Order;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Komyshenets on 17.01.2018.
 */
public interface MarketController {

    double getDeposit(String typeContract);

    double getPairPrice(String typeContract);

    boolean registerOrder(Order order);

    CompleteOrder getOrderResult(Order order);

    boolean closeOrder(Order order);

    boolean orderExist(Order order);

    @NotNull
    String getPair();
}
