package test.java;

import am.markov.orderbook.model.OrderBook;
import am.markov.orderbook.pojo.Order;
import com.sun.istack.internal.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by Andrea Fastame
 *
 * @email a.fastame@gmail.com
 * @since 11/02/2025 - 15:13
 */

class OrderBookUT {
    private Order getOrder(Long orderId, @Nullable String symbol, @Nullable boolean isBuy, int price, int quantity) {
        return new Order(orderId, symbol, isBuy, price, quantity);
    }

    private Order getOrderEdit(Long orderId, int price, int quantity) {
        return new Order(orderId, null, true, price, quantity);
    }

    private Order getOrderRemove(Long orderId) {
        return new Order(orderId, null, true, -1, -1);
    }

    /**
     * Tests ADD operations for a single order
     */
    @Test
    public void addSingleOrderTest() {
        OrderBook orderBook = new OrderBook("GOOGL");
        orderBook.addAction(
                getOrder(
                        1L,
                        "GOOGL",
                        true,
                        10,
                        100
                ));
        int contained = orderBook.ordersIndex.size();
        assertEquals(1, contained);
    }

    @Test
    public void addMultipleOrdersWithSameSymbolTest() {
        OrderBook orderBook = new OrderBook("GOOGL");
        orderBook.addAction(
                getOrder(
                        1L,
                        "GOOGL",
                        true,
                        10,
                        100
                ));
        orderBook.addAction(
                getOrder(
                        2L,
                        "GOOGL",
                        true,
                        7,
                        200
                ));
        orderBook.addAction(
                getOrder(
                        3L,
                        "GOOGL",
                        true,
                        8,
                        75
                ));

        int contained = orderBook.ordersIndex.size();
        assertEquals(3, contained);
    }

    /**
     * Adding same order multiple times, expecting it to be overwritten
     */
    @Test
    public void addMultipleOrdersWithSameIdTest() {
        OrderBook orderBook = new OrderBook("GOOGL");
        orderBook.addAction(
                getOrder(
                        1L,
                        "GOOGL",
                        true,
                        10,
                        100
                ));
        orderBook.addAction(
                getOrder(
                        1L,
                        "GOOGL",
                        true,
                        7,
                        200
                ));
        orderBook.addAction(
                getOrder(
                        1L,
                        "GOOGL",
                        true,
                        8,
                        75
                ));

        int contained = orderBook.ordersIndex.size();
        assertEquals(1, contained);
    }

    /**
     * Add order and edit it's quantity.
     */
    @Test
    public void addAndEditOrder() {
        OrderBook orderBook = new OrderBook("GOOGL");
        orderBook.addAction(
                getOrder(
                        1L,
                        "GOOGL",
                        true,
                        10,
                        100
                ));
        orderBook.edit(getOrderEdit(1L, 10, 120));

        int quantity = orderBook.buyOpsQuantitiesIndexedByPrice.get(10);
        assertEquals(120, quantity);
    }

    /**
     * Add two different orders, then add and EDIT operation, changing the first order with a new price matching the second
     * order added. So we are expecting the total sum of the old 2° order and the new edit.
     */
    @Test
    public void addOrdersAndChangeOnesPrice() {
        OrderBook orderBook = new OrderBook("GOOGL");
        orderBook.addAction(
                getOrder(
                        1L,
                        "GOOGL",
                        true,
                        5,
                        200
                ));
        orderBook.addAction(
                getOrder(
                        4L,
                        "GOOGL",
                        true,
                        7,
                        150
                ));
        orderBook.edit(getOrderEdit(1L, 7, 200));

        int totalOrders = orderBook.buyOpsQuantitiesIndexedByPrice.size();
        assertEquals(1, totalOrders);

        int quantity = orderBook.buyOpsQuantitiesIndexedByPrice.get(7);
        assertEquals(350, quantity);

    }

    @Test
    public void addAndRemoveOrder() {
        OrderBook orderBook = new OrderBook("GOOGL");
        orderBook.addAction(
                getOrder(
                        1L,
                        "GOOGL",
                        true,
                        10,
                        100
                ));
        orderBook.remove(getOrderRemove(1L));

        Order order = orderBook.ordersIndex.getOrDefault(1L, null);
        assertNull(order);
    }


}
