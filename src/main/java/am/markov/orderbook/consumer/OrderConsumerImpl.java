package am.markov.orderbook.consumer;

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

class OrderBook {


    private final String symbol;

    /**
     * Hash contentente le quantità indicizzate per prezzo
     */
    public TreeMap<Integer, Integer> buyOpsQuantitiesIndexedByPrice = new TreeMap<>(Comparator.reverseOrder());
    public TreeMap<Integer, Integer> sellOpsQuantitiesIndexedByPrice = new TreeMap<>(Comparator.reverseOrder());

    /**
     * Indice generale degli ordini, per evitare ricerche, quindi overhead. Quando viene compiuta un'operazione di
     * EDIT si va a cercare qui dentro in modo da accedere direttamente all'oggetto, qualora esista, indicizzata
     * per orderId
     */
    public Hashtable<Long, Order> ordersIndex = new Hashtable<>();
    /**
     * Indici che contengono gli ordini di vendita e acquisto separatamente, per permettere l'aggregazione dei prezzi,
     * indicizzati per prezzo
     */
    public Hashtable<Long, Order> buyOrdersIndex = new Hashtable<>();
    public Hashtable<Long, Order> sellOrdersIndex = new Hashtable<>();


    OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void addAction(Order order) {
        if (order.isBuy()) {
            if (buyOpsQuantitiesIndexedByPrice.containsKey(order.getPrice())) {
                buyOpsQuantitiesIndexedByPrice.compute(order.getPrice(), (k, oldQuantity) -> oldQuantity + order.getQuantity());
            } else {
                buyOpsQuantitiesIndexedByPrice.put(order.getPrice(), order.getQuantity());
            }
            buyOrdersIndex.put(order.getOrderId(), order);
        } else {
            if (sellOpsQuantitiesIndexedByPrice.containsKey(order.getPrice())) {
                sellOpsQuantitiesIndexedByPrice.compute(order.getPrice(), (k, oldQuantity) -> oldQuantity - order.getQuantity());
            } else {
                sellOpsQuantitiesIndexedByPrice.put(order.getPrice(), order.getQuantity());
            }
            sellOrdersIndex.put(order.getOrderId(), order);
        }
        ordersIndex.put(order.getOrderId(), order);
    }

    public Boolean edit(Order order) {
        Order orderToChange = ordersIndex.get(order.getOrderId());
        if (orderToChange != null) {
            int oldPrice = orderToChange.getPrice();
            // v 100 n 40 diff 60
            // v 40 n 100 diff -60
            int quantityDifference = orderToChange.getQuantity() - order.getQuantity();

            if (buyOrdersIndex.containsKey(order.getOrderId())) {
                // E' un ordine di acquisto
                // cambio la quantità negli aggregati
                computeNewQuantity(buyOpsQuantitiesIndexedByPrice, orderToChange, quantityDifference, order, oldPrice);
            } else if (sellOrdersIndex.containsKey(order.getOrderId())) {
                // E' un ordine di vendita
                // cambio la quantità negli aggregati
                computeNewQuantity(sellOpsQuantitiesIndexedByPrice, orderToChange, quantityDifference, order, oldPrice);

            }
            return true;
        } else
            return false;
    }

    private void computeNewQuantity(TreeMap<Integer, Integer> opsQuantitiesIndexedByPrice, Order oldIndexedOrder, int quantityDifference, Order newOrder, int oldPrice) {

        opsQuantitiesIndexedByPrice.computeIfPresent(oldIndexedOrder.getPrice(), (k, oldQuantity) -> oldQuantity - oldIndexedOrder.getQuantity());

        // cambio il vecchio ordine con nuovo prezzo e nuvova quantità e risommo negli aggregati
        oldIndexedOrder.setPrice(newOrder.getPrice());
        oldIndexedOrder.setQuantity(newOrder.getQuantity());

        if (opsQuantitiesIndexedByPrice.containsKey(newOrder.getPrice())) {
            opsQuantitiesIndexedByPrice.compute(oldIndexedOrder.getPrice(), (k, oldQuantity) -> oldQuantity + oldIndexedOrder.getQuantity());
        } else {
            opsQuantitiesIndexedByPrice.put(oldIndexedOrder.getPrice(), oldIndexedOrder.getQuantity());
        }
    }

    public boolean remove(Order order) {
        boolean isBuy = buyOrdersIndex.containsKey(order.getOrderId());
        Order orderToChange = ordersIndex.get(order.getOrderId());
        if (orderToChange != null) {
            if (isBuy) {
                buyOpsQuantitiesIndexedByPrice.compute(orderToChange.getPrice(), (k, oldQuantity) -> oldQuantity - orderToChange.getQuantity());
                buyOrdersIndex.remove(order.getOrderId());
            } else {
                sellOpsQuantitiesIndexedByPrice.compute(orderToChange.getPrice(), (k, oldQuantity) -> oldQuantity - orderToChange.getQuantity());
                sellOrdersIndex.remove(order.getOrderId());
            }
            ordersIndex.remove(order.getOrderId());
            return true;
        } else
            return false;
    }

    public void print() {
        System.out.println("OrderBook: " + symbol);
        System.out.println("BuyOps:");
        buyOpsQuantitiesIndexedByPrice.forEach((price, quantity) -> {
            if (quantity > 0)
                System.out.println("Price: " + price + " Quantity: " + quantity);
        });
        System.out.println("SellOps:");
        sellOpsQuantitiesIndexedByPrice.forEach((price, quantity) -> {
            if (quantity > 0)
                System.out.println("Price: " + price + " Quantity: " + quantity);
        });
        System.out.println("----------------------------------\n");
    }
}
