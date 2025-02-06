package am.markov.orderbook.controller;

import am.markov.orderbook.consumer.OrderConsumer;
import am.markov.orderbook.consumer.OrderConsumerImpl;
import am.markov.orderbook.util.XmlOrderParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 *
 */
public class OrderController {

    private XmlOrderParser xmlOrderParser;
    private OrderConsumer orderConsumer;

    /**
     * default constructor
     */
    public OrderController() {
        orderConsumer = new OrderConsumerImpl();
        xmlOrderParser = new XmlOrderParser();
        xmlOrderParser.setOrderConsumer(orderConsumer);
    }

    public void start(String[] args) {
        String orderFile = args[0];

        orderConsumer.startProcessing();
        try {
            xmlOrderParser.parseOrderFile(orderFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        } finally {
            orderConsumer.stopProcessing();
        }
    }

    public static void main(String[] args) {
        OrderController orderController = new OrderController();
        orderController.start(new String[]{"./resources/Orders1.xml"});
    }

}
