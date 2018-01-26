package com.vadelic.exmo;

import com.vadelic.exmo.controller.ControllerFactory;
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
    private List<ContractCarrier> contractUnits = new CopyOnWriteArrayList<>();

    private final ControllerFactory factory;

    public BotExmoManager(ControllerFactory controllerFactory) {
        super("MANAGER");
        this.factory = controllerFactory;
    }


    public void addContract(String pair, double profitInPercent, @Nullable String type, double depositPercent) {
        synchronized (this) {
            if (type == null || "sell".equals(type))
                contractUnits.add(new ContractCarrier(pair, TransactionContract.SELL, profitInPercent, depositPercent));

            if (type == null || "buy".equals(type))
                contractUnits.add(new ContractCarrier(pair, TransactionContract.BUY, profitInPercent, depositPercent));

            try {
                controlContractStatus();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<ContractCarrier> getAllContracts() {
        synchronized (this) {
            return Collections.unmodifiableList(contractUnits);
        }
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                controlContractStatus();
                Thread.sleep(1000 * 3);
            }
        } catch (Exception e) {
            LOG.fatal("MANAGER is down");
        } finally {
            closeContracts();
        }

    }

    public void closeContracts() {
        synchronized (this) {
            for (ContractCarrier contractUnit : contractUnits) {
                contractUnit.stop();
            }
        }
    }


    private void controlContractStatus() throws InterruptedException {
        synchronized (this) {
            for (ContractCarrier contractUnit : contractUnits) {
                switch (contractUnit.getStatus()) {
                    case ContractCarrier.NEW: {
                        Thread.sleep(3000);
                        contractUnit.runContract(factory);
                        break;
                    }
                    case ContractCarrier.DONE: {
                        contractUnit.runContract(factory);
                        break;
                    }
                    case ContractCarrier.WORK: {
                        if (contractUnit.isDone()) {
                            printResult(contractUnit);

                        }
                        break;
                    }
                }
            }
        }
    }

    private boolean printResult(ContractCarrier carrier) {
        try {
            System.out.println(carrier.getProfit());
            System.out.println(carrier);
            LOG.info("\n!!!! CONTRACT IS DONE !!!!\n");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
