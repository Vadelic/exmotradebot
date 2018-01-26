package com.vadelic.exmo;

import com.vadelic.exmo.controller.ControllerFactory;
import com.vadelic.exmo.controller.ExmoControllerFactory;
import com.vadelic.exmo.market.Exmo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Komyshenets on 17.01.2018.
 */
public class ConsoleController {
    private BotExmoManager botExmoManager;

    private ConsoleController(BotExmoManager botExmoManager) {
        this.botExmoManager = botExmoManager;
    }

    public static void main(String[] args) {
        Exmo exmo = new Exmo(args[0], args[1]);
        ControllerFactory controllerFactory = new ExmoControllerFactory(exmo);
        BotExmoManager botExmoManager = new BotExmoManager(controllerFactory);
        ConsoleController consoleController = new ConsoleController(botExmoManager);
        consoleController.createContract("BCH_RUB 5 sell 100");
        consoleController.createContract("ETH_USDT 5 sell 100");
        consoleController.createContract("BTC_EUR 5 sell 100");
        consoleController.listenConsole();

//        consoleController.botExmoManager.addContract("BCH_RUB", 5, "sell", 100);
//        consoleController.botExmoManager.addContract("ETH_USDT", 5, "sell", 100);
//        consoleController.botExmoManager.addContract("BTC_EUR", 5, "sell", 100);
    }

    private void listenConsole() {
        botExmoManager.start();
        boolean quit = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!quit) {
            try {
                String zp = reader.readLine();
                switch (zp) {
                    case "quit":
                        quit = true;
                        break;
                    case "stop":
                        botExmoManager.closeContracts();
                        break;
                    case "stat":
                        List<ContractCarrier> allContracts = botExmoManager.getAllContracts();
                        for (ContractCarrier allContract : allContracts) {
                            System.out.println(allContract);
                        }
                        break;
                    case "set":
                        System.out.println("ENTER pair , profit, type, deposit");
                        createContract(reader.readLine());
                        break;
                    default:
                        System.out.println("wrong!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        botExmoManager.interrupt();
        System.out.println("EXIT");
    }

    private void createContract(String s) {
        try {
            String[] split = s.split(" ");
            String pair = split[0];
            double profit = Double.parseDouble(split[1]);
            String type = split[2];
            double deposit = Double.parseDouble(split[3]);
            botExmoManager.addContract(pair, profit, type, deposit);
            System.out.println("ok");
        } catch (Exception e) {
            System.out.println("can't create");
        }
    }

}
