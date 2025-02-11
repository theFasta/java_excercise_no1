package am.markov.orderbook.model;

import am.markov.orderbook.pojo.Order;

import java.util.*;

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
            // TODO [AF]: Maybe i misunderstood the mechanics, which i implied of not just "overriding" the value
            //  contained @ the price index, but recalculate. If it's just an override, it is even easier. For now, i'll stick to older recalculate method

            addQuantityToStructure(buyOpsQuantitiesIndexedByPrice, order);
        } else {
            addQuantityToStructure(sellOpsQuantitiesIndexedByPrice, order);
        }
        ordersIndex.put(order.getOrderId(), order);
    }

    private void addQuantityToStructure(TreeMap<Integer, Integer> indexedStructure, Order order) {
        int newQuantity = indexedStructure.getOrDefault(order.getPrice(), 0) + order.getQuantity();
        indexedStructure.put(order.getPrice(), newQuantity);
    }

    public Boolean edit(Order order) {
        Order orderToChange = ordersIndex.get(order.getOrderId());
        if (orderToChange != null) {
            if (orderToChange.isBuy()) {
                // Buy order
                // changing qty in aggregates
                computeNewQuantity(buyOpsQuantitiesIndexedByPrice, orderToChange, order);
            } else {
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
    // TODO [AF]: I actually have not very clear if the value just must be updated with new one or must be recalculated (as I did)
    private void computeNewQuantity(TreeMap<Integer, Integer> opsQuantitiesIndexedByPrice, Order oldIndexedOrder, Order newOrder) {

        // Subtract old quantity, if present, from the previous indexed order
        opsQuantitiesIndexedByPrice.computeIfPresent(oldIndexedOrder.getPrice(), (k, oldQuantity) -> oldQuantity - oldIndexedOrder.getQuantity());

        // Remove if quantity == 0
        if (opsQuantitiesIndexedByPrice.get(oldIndexedOrder.getPrice()) <= 0) {
            opsQuantitiesIndexedByPrice.remove(oldIndexedOrder.getPrice());
        }

        // Changing old order with new price and new qty, and going to compute the new quantities
        oldIndexedOrder.setPrice(newOrder.getPrice());
        oldIndexedOrder.setQuantity(newOrder.getQuantity());

        // If contained we need to sum the new value to the old one, otherwise we'll just set a new <key,value> tuple
        int newQuantity = opsQuantitiesIndexedByPrice.getOrDefault(oldIndexedOrder.getPrice(), 0) + oldIndexedOrder.getQuantity();
        opsQuantitiesIndexedByPrice.put(newOrder.getPrice(), newQuantity);

        if (opsQuantitiesIndexedByPrice.get(oldIndexedOrder.getPrice()) <= 0) {
            opsQuantitiesIndexedByPrice.remove(oldIndexedOrder.getPrice());
        }
    }

    public boolean remove(Order order) {
        Order orderToChange = ordersIndex.get(order.getOrderId());
        if (orderToChange != null) {
            // TODO [LV]: Same as for the add method: use a function for buy / sell as they are the same
            // TODO [AF]: INDEED, I would have been done, as specified earlier, for EDIT operations, since it unclutters code
            if (orderToChange.isBuy()) {
                doRemoveFromStructure(buyOpsQuantitiesIndexedByPrice, orderToChange);
            } else {
                doRemoveFromStructure(sellOpsQuantitiesIndexedByPrice, orderToChange);
            }
            ordersIndex.remove(order.getOrderId());
            return true;
        } else
            return false;
    }

    private void doRemoveFromStructure(TreeMap<Integer, Integer> buyOpsQuantitiesIndexedByPrice, Order orderToChange) {
        buyOpsQuantitiesIndexedByPrice.compute(orderToChange.getPrice(), (k, oldQuantity) -> oldQuantity - orderToChange.getQuantity());
        if (buyOpsQuantitiesIndexedByPrice.get(orderToChange.getPrice()) <= 0) {
            buyOpsQuantitiesIndexedByPrice.remove(orderToChange.getPrice());
        }
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