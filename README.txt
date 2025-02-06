Un paio di indicazioni sull'esercizio (oltre a quelle già presenti nel README):
Richiede un po' di analisi sul business, capire cosa significa avere una depth di prezzi;
Bid corrisponde a Buy e Ask a Sell, in questo caso;
Non considerare il matching dei prices buys/sell in merito all'esercizio, ma trattali come entità indipendenti. Altrimenti, dovresti gestire i trade.
Un livello corrisponde a Qty | Price.


The task is to build order books from a stream of orders and print out the ‘By Level’ order book(s) at the end.

You should:
1.     Initialise your structures in startProcessing()
2.     Build the ‘By Level’ Order Books by processing the order events in handleEvent(Action action, Order order).
3.     Write out the ‘By Level’ Order Books in stopProcessing().

A couple of points:
1.     Separate OrderBooks are maintained for each instrument.
2.     You must maintain the By Level Order Books in real time and not wait until the stopProcessing() event to build them.
3.     It is not required that you ‘match’ bid / ask orders. The requirement is to aggregate the orders for each level.
