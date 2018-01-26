package com.vadelic.exmo.controller;

import com.vadelic.exmo.market.ExmoRestApiException;

/**
 * Created by Komyshenets on 18.01.2018.
 */
public interface ControllerFactory {
    MarketController getController(String pair) throws ExmoRestApiException;
}
