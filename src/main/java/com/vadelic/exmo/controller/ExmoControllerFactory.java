package com.vadelic.exmo.controller;

import com.vadelic.exmo.market.Exmo;
import com.vadelic.exmo.market.ExmoRestApiException;

/**
 * Created by Komyshenets on 18.01.2018.
 */
public class ExmoControllerFactory implements ControllerFactory {
    private final Exmo exmo;

    public ExmoControllerFactory(Exmo exmo) {
        this.exmo = exmo;
    }

    @Override
    public MarketController getController(String pair) throws ExmoRestApiException {
        return new ExmoMarketController(exmo, pair);
    }
}
