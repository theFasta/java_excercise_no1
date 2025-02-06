package am.markov.orderbook.consumer;

import am.markov.orderbook.pojo.Action;
import am.markov.orderbook.pojo.Order;

import java.util.*;

/**
 *
 */
public class OrderConsumerImpl implements OrderConsumer {

    private Hashtable<String, OrderBook> orderBooks = new Hashtable<>();

    public void startProcessing() {

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
                orderBook.addAction(action, order);
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

class OrderBook {
    private final String symbol;
    public TreeSet<Order> buyOps = new TreeSet<>(Comparator.comparingInt(Order::getPrice));
    public TreeSet<Order> sellOps = new TreeSet<>(Comparator.comparingInt(Order::getPrice));
    public Hashtable<Long, Order> orders = new Hashtable<>();

    OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void addAction(Action action, Order order) {
        if (order.isBuy())
            buyOps.add(order);
        else
            sellOps.add(order);
        orders.put(order.getOrderId(), order);
    }

    public Boolean edit(Order order) {
        Order orderToBeChanged = orders.getOrDefault(order.getOrderId(), null);
        if (orderToBeChanged != null) {
            orderToBeChanged.update(order);
            return true;
        } else
            return false;
    }

    public boolean remove(Order order) {
        if (orders.containsKey(order.getOrderId())) {
            orders.remove(order.getOrderId());
            sellOps.remove(order);
            buyOps.remove(order);
            return true;
        } else
            return false;
    }

    public void print() {
        System.out.println("OrderBook: " + symbol);
        System.out.println("BuyOps:");
        buyOps.forEach(System.out::println);
        System.out.println("SellOps:");
        sellOps.forEach(System.out::println);
        System.out.println("----------------------------------\n");
    }
}
