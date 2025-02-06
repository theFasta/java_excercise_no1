package am.markov.orderbook.consumer;

import am.markov.orderbook.pojo.Action;
import am.markov.orderbook.pojo.Order;

/**
 *
 */
public interface OrderConsumer {

    void startProcessing();

    void stopProcessing();

    void handleOrder(Action action, Order order);

}
