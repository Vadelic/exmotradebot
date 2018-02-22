package com.vadelic.exmo.contract;

/**
 * Created by Komyshenets on 15.02.2018.
 */
public interface TransactionContract {
    int NEW = 0;
    int WORK = 1;
    int DONE = 2;
    int CRASH = 3;

    void closeContract();

    String getProfitResult();

    int getStatus();

    int getIndex();

    void startContract();

    boolean isAlive();

    void setStatus(int status);

    void force();
}
