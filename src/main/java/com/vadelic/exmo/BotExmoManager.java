package com.vadelic.exmo;

import com.vadelic.exmo.contract.AbstractContract;
import com.vadelic.exmo.contract.HomoRazerContract;
import com.vadelic.exmo.contract.TransactionContract;
import com.vadelic.exmo.controller.ExmoPairController;
import com.vadelic.exmo.controller.MarketController;
import com.vadelic.exmo.market.Exmo;
import com.vadelic.exmo.market.ExmoRestApiException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Komyshenets on 13.01.2018.
 */
public class BotExmoManager extends Thread {
    private static final Logger LOG = Logger.getLogger(BotExmoManager.class);
    private List<TransactionContract> contractUnits = new CopyOnWriteArrayList<>();
    private volatile boolean workFlag;
    private final Exmo exmo;

    public BotExmoManager(Exmo exmo) throws ExmoRestApiException {
        super("MANAGER");
        this.exmo = exmo;
        closeAllOrders();
    }

    public void closeAllOrders() throws ExmoRestApiException {
        Map<String, List<Map<String, Object>>> stringListMap = this.exmo.openOrders();
        for (List<Map<String, Object>> maps : stringListMap.values()) {
            for (Map<String, Object> map : maps) {
                Object order_id = map.get("order_id");
                this.exmo.cancelOrder((Integer.parseInt(order_id.toString())));
            }

        }
    }

    public void stopManager() {
        workFlag = false;
    }

    public void addContract(String pair, double profitInPercent, @Nullable String type) throws ExmoRestApiException {
        MarketController pairController = new ExmoPairController(exmo, pair);
        if (type == null || AbstractContract.SELL.equals(type))
            contractUnits.add(new HomoRazerContract(pairController, AbstractContract.SELL, profitInPercent));

        if (type == null || AbstractContract.BUY.equals(type))
            contractUnits.add(new HomoRazerContract(pairController, AbstractContract.BUY, profitInPercent));

        controlContractStatus();
    }

    public void addContract(String pair, double profitInPercent) throws ExmoRestApiException {
        addContract(pair, profitInPercent, null);
    }

    public boolean stopContract(int index) {
        for (TransactionContract contractUnit : contractUnits) {
            if (index < 0 || contractUnit.getIndex() == index) {
                contractUnit.closeContract();
                return true;
            }
        }
        return false;
    }

    public List<TransactionContract> getAllContracts() {
        return Collections.unmodifiableList(contractUnits);
    }

    @Override
    public void run() {
        workFlag = true;
        try {
            while (workFlag) {
                controlContractStatus();
                Thread.sleep(1000 * 3);
            }
        } catch (Exception e) {
            LOG.fatal(e, e);
        } finally {
            stopContract(-1);
        }
        LOG.fatal("MANAGER is down");
    }


    private void controlContractStatus() {
        for (TransactionContract contractUnit : contractUnits) {
            if (!contractUnit.isAlive()) {
                switch (contractUnit.getStatus()) {
                    case TransactionContract.NEW: {
                        contractUnit.startContract();
                        break;
                    }
                    case TransactionContract.DONE: {
                        printResult(contractUnit);
                        contractUnit.setStatus(-1);

                        break;
                    }
                }
            }
        }
    }


    private void printResult(TransactionContract contract) {
        try {
            LOG.info("\n!!!! CONTRACT IS DONE !!!!");
            System.out.println(contract.getProfitResult());
            System.out.println(contract);
            LOG.info("!!!! CONTRACT IS DONE !!!!\n");
        } catch (Exception e) {
            LOG.warn(e, e);
        }
    }

}
