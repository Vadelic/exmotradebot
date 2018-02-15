package com.vadelic.exmo;

import com.vadelic.exmo.controller.ControllerFactory;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Komyshenets on 13.01.2018.
 */
public class BotExmoManager extends Thread {
    private static final Logger LOG = Logger.getLogger(BotExmoManager.class);
    private List<ContractCarrier> contractUnits = new CopyOnWriteArrayList<>();
    private final static ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private final ControllerFactory controllerMarketFactory;
    private volatile boolean workFlag;

    public BotExmoManager(ControllerFactory controllerFactory) {
        super("MANAGER");
        this.controllerMarketFactory = controllerFactory;
    }

    public void stopManager() {
        workFlag = false;
    }

    public void addContract(String pair, double profitInPercent, @Nullable String type, double depositPercent) {
        synchronized (this) {
            if (type == null || TransactionContract.SELL.equals(type))
                contractUnits.add(new ContractCarrier(pair, TransactionContract.SELL, profitInPercent, depositPercent));

            if (type == null || TransactionContract.BUY.equals(type))
                contractUnits.add(new ContractCarrier(pair, TransactionContract.BUY, profitInPercent, depositPercent));

            controlContractStatus();
        }
    }

    public boolean stopContract(int index) {
        synchronized (this) {
            for (ContractCarrier contractUnit : contractUnits) {
                if (contractUnit.index == index) {
                    contractUnit.stopContract();
                    return true;
                }
            }
            return false;
        }
    }

    public List<ContractCarrier> getAllContracts() {
        synchronized (this) {
            return Collections.unmodifiableList(contractUnits);
        }
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
            for (ContractCarrier contractUnit : contractUnits) {
                contractUnit.stopContract();
            }
        }
    }

    private void controlContractStatus() {
        synchronized (this) {
            for (ContractCarrier contractUnit : contractUnits) {
                switch (contractUnit.getStatus()) {
                    case ContractCarrier.NEW: {
                        runContract(contractUnit);
                        break;
                    }
                    case ContractCarrier.WORK: {
                        if (contractUnit.isDone()) {
                            printResult(contractUnit);
                        }
                        break;
                    }
                    case ContractCarrier.DONE: {
                        contractUnit.runContract(EXECUTOR, controllerMarketFactory);
                        break;
                    }
                }
            }
        }
    }

    private void runContract(ContractCarrier contractUnit) {
        contractUnit.runContract(EXECUTOR, controllerMarketFactory);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOG.error(e, e);
        }

    }

    private void printResult(ContractCarrier carrier) {
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
