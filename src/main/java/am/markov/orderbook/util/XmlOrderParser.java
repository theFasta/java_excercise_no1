package am.markov.orderbook.util;

import am.markov.orderbook.consumer.OrderConsumer;
import am.markov.orderbook.pojo.Action;
import am.markov.orderbook.pojo.Order;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

/**
 *
 */
public class XmlOrderParser extends DefaultHandler {

    private OrderConsumer orderConsumer;

    public void setOrderConsumer(OrderConsumer orderConsumer) {
        this.orderConsumer = orderConsumer;
    }

    public void parseOrderFile(String orderFilename) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory.newInstance().newSAXParser().parse(orderFilename, this);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Order order = null;
        Action action = null;
        switch (qName) {
            case "add":
                action = Action.ADD;
                order = new Order(
                        Long.valueOf(attributes.getValue("orderId")),
                        attributes.getValue("symbol"),
                        attributes.getValue("type").equals("buy"),
                        Integer.valueOf(attributes.getValue("price")),
                        Integer.valueOf(attributes.getValue("quantity"))
                );
                break;
            case "edit":
                action = Action.EDIT;
                order = new Order(
                        Long.valueOf(attributes.getValue("orderId")),
                        null,
                        true,
                        Integer.valueOf(attributes.getValue("price")),
                        Integer.valueOf(attributes.getValue("quantity"))
                );
                break;
            case "remove":
                action = Action.REMOVE;
                order = new Order(
                        Long.valueOf(attributes.getValue("orderId")),
                        null,
                        true,
                        -1,
                        -1
                );
                break;
            default:
                if (!qName.equals("commands")) {
                    throw new IllegalArgumentException("Non supported action: " + qName);
                }
        }

        if (null != order) {
            orderConsumer.handleOrder(action, order);
        }
    }
}
