package com.vadelic.exmo.controller;

import com.vadelic.exmo.market.Exmo;
import com.vadelic.exmo.market.ExmoRestApiException;

/**
 * Created by Komyshenets on 18.01.2018.
 */
public class ExmoPairFactory implements ControllerFactory {
    private final Exmo exmo;

    public ExmoPairFactory(Exmo exmo) {
        this.exmo = exmo;
    }

    @Override
    public MarketController getPairController(String pair) throws ExmoRestApiException {
        return new ExmoPairController(exmo, pair);
    }
}
