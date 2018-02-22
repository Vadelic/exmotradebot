package com.vadelic.exmo.waiter;

import com.vadelic.exmo.model.CompleteOrder;

import java.util.concurrent.Callable;

/**
 * Created by Komyshenets on 15.02.2018.
 */
public interface OrderWaiter extends Callable<CompleteOrder> {
    void closeOrder();

    void force();
}
