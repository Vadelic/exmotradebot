package com.vadelic.exmo;

import com.vadelic.exmo.controller.MarketController;
import com.vadelic.exmo.model.CompleteOrder;
import com.vadelic.exmo.model.Order;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Komyshenets on 11.01.2018.
 */
public class TransactionContract implements Callable<Map<String, CompleteOrder>> {
    public static final String SELL = "sell";
    public static final String BUY = "buy";

    private final MarketController controller;
    private final String typeContract;
    private final double profitPercent;
    private final double depositPercent;
    private final Logger LOG = Logger.getLogger(getClass());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Future<CompleteOrder> future = null;

    public TransactionContract(MarketController controller, String typeContract, double profitInPercent, double depositPercent) {
        this.controller = controller;
        this.typeContract = typeContract;
        this.profitPercent = profitInPercent;
        this.depositPercent = depositPercent;
    }


    @Override
    public Map<String, CompleteOrder> call() {
        Map<String, CompleteOrder> response = new HashMap<String, CompleteOrder>() {{
            put("start", null);
            put("re", null);
        }};
        try {
            CompleteOrder startOrderResult = execStartOrder();

            if (startOrderResult != null) {
                response.put("start", startOrderResult);
                if (startOrderResult.getCompleteIn() != 0) {
                    CompleteOrder reOrderResult = execReOrder(startOrderResult.getCompleteIn());
                    response.put("re", reOrderResult);
                }
            }
        } catch (Exception e) {
            if (future != null)
                future.cancel(true);
        }
        return response;
    }

    private CompleteOrder execReOrder(double completeIn) throws InterruptedException, java.util.concurrent.ExecutionException {
        Order reTradeOrder = createReTradeOrder(completeIn);
        ReOrderWaiter reTradeOrderWaiter = new ReOrderWaiter(controller, reTradeOrder);
        reTradeOrderWaiter.setName(String.format("[%s] RE ODER [%s]", controller.getPair(), reTradeOrder.type));
        future = executor.submit(reTradeOrderWaiter);

        return future.get();
    }

    private Order createReTradeOrder(double completeIn) {
        double quantity = completeIn * (1 - 0.02);
        if (SELL.equals(typeContract)) {
            return new Order(BUY, quantity);
        } else {
            return new Order(SELL, quantity);
        }
    }

    private CompleteOrder execStartOrder() throws ExecutionException, InterruptedException {
        double validDeposit = controller.getDeposit(typeContract) * (depositPercent / 100);
        Order order = new Order(typeContract, validDeposit);
        StartOrderWaiter orderWaiter = new StartOrderWaiter(controller, order, (profitPercent / 100));
        orderWaiter.setName(String.format("[%s] START ODER [%s]", controller.getPair(), typeContract));
        future = executor.submit(orderWaiter);

        return future.get();
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TransactionContract{");
        sb.append("pair=").append(controller.getPair());
        sb.append(", typeContract='").append(typeContract).append('\'');
        sb.append(", profit=").append(profitPercent).append("%");
        sb.append('}');
        return sb.toString();
    }
}
