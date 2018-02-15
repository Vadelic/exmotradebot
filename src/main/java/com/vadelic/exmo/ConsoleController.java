package com.vadelic.exmo;

import com.vadelic.exmo.contract.TransactionContract;
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
        BotExmoManager botExmoManager = new BotExmoManager(exmo);
        ConsoleController consoleController = new ConsoleController(botExmoManager);
        consoleController.createContract("BTC_EUR 4 sell");
        consoleController.createContract("ETH_USDT 4 sell");
        consoleController.createContract("BCH_RUB 4 sell");
        consoleController.createContract("LTC_EUR 6 buy");
        consoleController.listenConsole();

//        consoleController.createContract("BTC_EUR 8 sell 100");
//        consoleController.createContract("ETH_USDT 7 sell 100");
//        consoleController.createContract("BCH_RUB 12 sell 100");
//        consoleController.createContract("LTC_EUR 7 buy 100");
    }

    private void listenConsole() {
        botExmoManager.start();
        boolean quit = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!quit) {
            try {
                String zp = reader.readLine();
                String command = zp.split(" ")[0];
                switch (command) {
                    case "quit":
                        quit = true;
                        break;
                    case "stop":
                        botExmoManager.stopManager();
                        break;
                    case "stat":
                        List<TransactionContract> allContracts = botExmoManager.getAllContracts();
                        for (TransactionContract allContract : allContracts) {
                            System.out.println(allContract);
                        }
                        break;
                    case "set":
                        createContract(zp.replace(command,"").trim());
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

            botExmoManager.addContract(pair, profit, type);
            System.out.println("ok");
        } catch (Exception e) {
            System.out.println("can't create");
        }
    }

}
