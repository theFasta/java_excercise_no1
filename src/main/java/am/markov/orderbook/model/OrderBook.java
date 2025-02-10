package am.markov.orderbook.model;

import am.markov.orderbook.pojo.Order;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.TreeMap;

public class OrderBook {


    private final String symbol;

    /**
     * Hash containing ordered quantities, indexed by price
     */
    public TreeMap<Integer, Integer> buyOpsQuantitiesIndexedByPrice = new TreeMap<>(Comparator.reverseOrder());
    public TreeMap<Integer, Integer> sellOpsQuantitiesIndexedByPrice = new TreeMap<>(Comparator.reverseOrder());

    /**
     * General Orders index, to avoid search() overheads. When an operation EDIT operation gets invoked we are going
     * to retrieve the interested Order with direct-access (by orderId)
     */
    public Hashtable<Long, Order> ordersIndex = new Hashtable<>();
    /**
     * Indexed hashtables containing Buy and Sell orders, used to understand if an EDIT Actions is requested for a Buy or
     * for a Sell Order
     */
    // TODO [LV]: You don't need them at all. You can get from the orderIndex, as they use the same key with direct access.
    //  You can read the buy / sell from the original order object.
    //  TODO [AF]: I thought about it, then i Realized that maybe with the isBuy() property I could completely refactor in order to avoid using / managing them
    public Hashtable<Long, Order> buyOrdersIndex = new Hashtable<>();
    public Hashtable<Long, Order> sellOrdersIndex = new Hashtable<>();


    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void addAction(Order order) {
        // TODO [LV]: Add a private function to avoid repeating operations for buy / sell.
        //  TODO [AF] Yes, in refactoring, I would have encapsulated the two operations in a single method, like I did with "edit" operation incapsulated with the computeNewQuantity() method below
        if (order.isBuy()) {
            // TODO [LV]: The method put in a Java map is adding the element if it doesn't exist or override the value if exists with the same key.
            // TIPS: What about using put method with getOrDefault on actual quantity, and add the order quantity?
            // TODO [AF]: Maybe i misunderstood the mechanics, which i implied of not just "overriding" the value contained @ the price index, but recalculate. If it's just an override, it is even easier
            if (buyOpsQuantitiesIndexedByPrice.containsKey(order.getPrice())) {
                buyOpsQuantitiesIndexedByPrice.compute(order.getPrice(), (k, oldQuantity) -> oldQuantity + order.getQuantity());
            } else {
                buyOpsQuantitiesIndexedByPrice.put(order.getPrice(), order.getQuantity());
            }
            buyOrdersIndex.put(order.getOrderId(), order);
        } else {
            if (sellOpsQuantitiesIndexedByPrice.containsKey(order.getPrice())) {
                sellOpsQuantitiesIndexedByPrice.compute(order.getPrice(), (k, oldQuantity) -> oldQuantity + order.getQuantity());
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
            if (buyOrdersIndex.containsKey(order.getOrderId())) {
                // Buy order
                // changing qty in aggregates
                computeNewQuantity(buyOpsQuantitiesIndexedByPrice, orderToChange, order);
            } else if (sellOrdersIndex.containsKey(order.getOrderId())) {
                // Sell order
                // changing qty in aggregates
                computeNewQuantity(sellOpsQuantitiesIndexedByPrice, orderToChange, order);

            }
            return true;
        } else
            return false;
    }

    // TODO [LV]: What about using remove and add instead of re-writing it?
    // TODO [AF]: It's another way of handling it, it is actually a cleaner method of doing it without any hassle of double checking for existance. Will do in refactoring
    private void computeNewQuantity(TreeMap<Integer, Integer> opsQuantitiesIndexedByPrice, Order oldIndexedOrder, Order newOrder) {

        opsQuantitiesIndexedByPrice.computeIfPresent(oldIndexedOrder.getPrice(), (k, oldQuantity) -> oldQuantity - oldIndexedOrder.getQuantity());
        if (opsQuantitiesIndexedByPrice.get(oldIndexedOrder.getPrice()) <= 0) {
            opsQuantitiesIndexedByPrice.remove(oldIndexedOrder.getPrice());
        }

        // Changing old order with new price and new qty, and going to compute the new quantities
        oldIndexedOrder.setPrice(newOrder.getPrice());
        oldIndexedOrder.setQuantity(newOrder.getQuantity());

        // If contained we need to sum the new value to the old one, otherwise we'll just set a new <key,value> tuple
        if (opsQuantitiesIndexedByPrice.containsKey(newOrder.getPrice())) {
            opsQuantitiesIndexedByPrice.compute(oldIndexedOrder.getPrice(), (k, oldQuantity) -> oldQuantity + oldIndexedOrder.getQuantity());
        } else {
            opsQuantitiesIndexedByPrice.put(oldIndexedOrder.getPrice(), oldIndexedOrder.getQuantity());
        }
        if (opsQuantitiesIndexedByPrice.get(oldIndexedOrder.getPrice()) <= 0) {
            opsQuantitiesIndexedByPrice.remove(oldIndexedOrder.getPrice());
        }
    }

    public boolean remove(Order order) {
        boolean isBuy = buyOrdersIndex.containsKey(order.getOrderId());
        Order orderToChange = ordersIndex.get(order.getOrderId());
        if (orderToChange != null) {
            // TODO [LV]: Same as for the add method: use a function for buy / sell as they are the same
            // TODO [AF]: INDEED, I would have been done, as specified earlier, for EDIT operations, since it unclutters code
            if (isBuy) {
                buyOpsQuantitiesIndexedByPrice.compute(orderToChange.getPrice(), (k, oldQuantity) -> oldQuantity - orderToChange.getQuantity());
                buyOrdersIndex.remove(order.getOrderId());
                if (buyOpsQuantitiesIndexedByPrice.get(orderToChange.getPrice()) <= 0) {
                    buyOpsQuantitiesIndexedByPrice.remove(orderToChange.getPrice());
                }
            } else {
                sellOpsQuantitiesIndexedByPrice.compute(orderToChange.getPrice(), (k, oldQuantity) -> oldQuantity - orderToChange.getQuantity());
                sellOrdersIndex.remove(order.getOrderId());
                if (sellOpsQuantitiesIndexedByPrice.get(orderToChange.getPrice()) <= 0) {
                    sellOpsQuantitiesIndexedByPrice.remove(orderToChange.getPrice());
                }
            }
            ordersIndex.remove(order.getOrderId());
            return true;
        } else
            return false;
    }

    public void print() {
        System.out.println("\n----------------------------------\n");
        System.out.println("OrderBook: " + symbol);
        System.out.println("BuyOps:");
        buyOpsQuantitiesIndexedByPrice.forEach((price, quantity) -> {
            System.out.println("Price: " + price + " Quantity: " + quantity);
        });
        System.out.println("\nSellOps:");
        sellOpsQuantitiesIndexedByPrice.forEach((price, quantity) -> {
            System.out.println("Price: " + price + " Quantity: " + quantity);
        });
        System.out.println("\n----------------------------------\n");
    }
}