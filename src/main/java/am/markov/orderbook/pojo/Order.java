package am.markov.orderbook.pojo;

import java.util.Objects;

/**
 *
 */
public class Order {
    private final long orderId;
    private final String symbol;
    private final boolean isBuy;
    private int price;
    private int quantity;

    public Order(long orderId, String symbol, boolean isBuy, int price,
                 int quantity) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.isBuy = isBuy;
        this.price = price;
        this.quantity = quantity;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "{orderId:" + orderId + " symbol:" + symbol + " price:" + price + " quantity:" + quantity + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId && Objects.equals(symbol, order.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, symbol);
    }

    public void update(Order order) {
        this.price = order.price;
        this.quantity = order.quantity;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

