package com.vadelic.exmo;

import com.vadelic.exmo.controller.ControllerFactory;
import com.vadelic.exmo.controller.ExmoPairFactory;
import com.vadelic.exmo.controller.MarketController;
import com.vadelic.exmo.market.Exmo;
import com.vadelic.exmo.market.ExmoRestApiException;
import com.vadelic.exmo.contract.AbstractContract;
import com.vadelic.exmo.contract.HomoRazerContract;
import com.vadelic.exmo.contract.TransactionContract;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Komyshenets on 13.01.2018.
 */
public class BotExmoManager extends Thread {
    private static final Logger LOG = Logger.getLogger(BotExmoManager.class);
    private List<TransactionContract> contractUnits = new CopyOnWriteArrayList<>();
    private final ControllerFactory exmoPairFactory;
    private volatile boolean workFlag;

    public BotExmoManager(Exmo exmo) {
        super("MANAGER");
        this.exmoPairFactory = new ExmoPairFactory(exmo);
    }

    public void stopManager() {
        workFlag = false;
    }

    public void addContract(String pair, double profitInPercent, @Nullable String type) throws ExmoRestApiException {
        MarketController pairController = exmoPairFactory.getPairController(pair);
        if (type == null || AbstractContract.SELL.equals(type))
            contractUnits.add(new HomoRazerContract(pairController, AbstractContract.SELL, profitInPercent));

        if (type == null || AbstractContract.BUY.equals(type))
            contractUnits.add(new HomoRazerContract(pairController, AbstractContract.BUY, profitInPercent));

        controlContractStatus();
    }

    public boolean stopContract(int index) {
        for (TransactionContract contractUnit : contractUnits) {
            if (contractUnit.getIndex() == index) {
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
            stopAllContracts();
        }
        LOG.fatal("MANAGER is down");
    }

    private void stopAllContracts() {
        synchronized (this) {
            for (TransactionContract contractUnit : contractUnits) {
                contractUnit.closeContract();
            }
        }
    }

    private void controlContractStatus() {
        for (TransactionContract contractUnit : contractUnits) {
            if (!contractUnit.isAlive()) {
                switch (contractUnit.getStatus()) {
                    case TransactionContract.NEW: {
                        contractUnit.startContract();
                        break;
                    }
//                    case ContractCarrier.WORK: {
//                        if (contractUnit.isDone()) {
//                            printResult(contractUnit);
//                        }
//                        break;
//                    }
                    case TransactionContract.DONE: {
                        System.out.println(contractUnit.getProfitResult());
//                        contractUnit.startContract();
                        break;
                    }
                }
            }
        }
    }

    private void runContract(TransactionContract contract) {
        ((Thread) contract).start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOG.error(e, e);
        }

    }

    private void printResult(TransactionContract carrier) {
        try {
            LOG.info("\n!!!! CONTRACT IS DONE !!!!");
            System.out.println(carrier.getProfitResult());
            System.out.println(carrier);
            LOG.info("!!!! CONTRACT IS DONE !!!!\n");
        } catch (Exception e) {
            LOG.warn(e, e);
        }
    }


}
