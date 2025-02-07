package am.markov.orderbook.consumer;

import am.markov.orderbook.model.OrderBook;
import am.markov.orderbook.pojo.Action;
import am.markov.orderbook.pojo.Order;

import java.util.*;

/**
 *
 */
public class OrderConsumerImpl implements OrderConsumer {

    private Hashtable<String, OrderBook> orderBooks;

    public void startProcessing() {
        orderBooks = new Hashtable<>();
    }

    public void stopProcessing() {
        System.out.println("Processing stopped\n");
        orderBooks.forEach((symbol, orderBook) -> {
            orderBook.print();
        });
    }

    public void handleOrder(Action action, Order order) {
        switch (action) {
            case ADD:
                OrderBook orderBook = orderBooks.getOrDefault(order.getSymbol(), null);
                if (orderBook == null) {
                    orderBook = new OrderBook(order.getSymbol());
                    orderBooks.put(order.getSymbol(), orderBook);
                }
                orderBook.addAction(order);
                break;
            case EDIT:
                for (Map.Entry<String, OrderBook> entry : orderBooks.entrySet()) {
                    if (entry.getValue().edit(order))
                        break;
                }
                break;
            case REMOVE:
                for (Map.Entry<String, OrderBook> entry : orderBooks.entrySet()) {
                    if (entry.getValue().remove(order))
                        break;
                }
                break;
        }
    }
}
