# MarketSim

## Implementation Notes

### Week 1 - 8th - 14th Feb
Got the basic simulator working. Could add fixed latency to all packets. Implemented ZI-C and ZI-U traders. 
### Week 2 - 15th - 21st Feb
Agents were previously instantly notified when they traded. Now it is sent as a packet as with all other information. 
Added ability for trading com.matt.marketsim.entities.agents to handle price updates.
com.matt.marketsim.entities.agents.ZIP is doing something but not working properly yet.
### Week 3 - 22nd - 28th
There is a mistake in equation 5 of the com.matt.marketsim.entities.agents.ZIP section of the paper "Behavioural Investigations of Financial Trading 
Agents using com.matt.marketsim.entities.Exchange Portal (ExPo)" by Stotter, Cartlidge and Cliff. The subtract 1 should be outside the division.
There is also a mistake in equation 6. 

Adding unit testing so that we can be sure the simulator is behaving properly. 

Adding an interface that allows trading com.matt.marketsim.entities.agents to be generated. This way we can create a number of different pre-defined 
experiments with different compositions of trading com.matt.marketsim.entities.agents.

Looking at using GraphML to represent the graph.

### Week 4 - March 1st - March 6th
Decided to assume that each trading agent is only trading one unit. This simplifies the design. 

Currently not seeing any difference in allocative efficiency between agents with long and short delays. 

### Week 5 - March 7th - March 14th
Found out that the issue was that there was a slight delay set on the connection between the exchange and the SIP. This 
meant arbitrageur could still profit even when internal SIP delta was zero.

Can't work out how the Wellman paper decides which primary market a trader gets. It says an 'equal proportion' but does that mean split exactly in half
or are they assigned with 0.5 probability?

### Week 6 - March 15th - March 21st
Added a better method for producing graphs after repeated simulation runs. Using a python script and matplotlib to produce the graphs.

In the paper the way that the NBBO is calculated sounds slightly odd. It says a ProcessQuote activity is entered at a time
t + delta, however this sounds like it could lead to later quotes being included in the processing.

Initially was using the same seed for all latencies, so that the same stream of orders arrived for each latency setting. 
However I switched to using to using a fixed seed only at the beginning of the entire experiment. That way there would be 
a fluctuation between each latency.

### Week 7 - March 22nd - March 28th
Found a small quirk in the way that MarketUpdates (quotes) are handled by trading agents. Take a scenario where a quote is sent simultaneously
from an exchange to its observers (trading agents) and also the SIP. Imagine an order in this quote makes it into the NBBO.
Now this order executes on the exchange and a new quote is sent out. However, the NBBO has already been sent to the trading agents.
Now the stale NBBO will replace the correct quote from the primary exchange. This is something that you wouldn't do in real
life though because you would trust your data from your primary exchange more than the NBBO.

Need to plan this out properly. Need a QuoteData object that represents nothing on the exchange, but still allows us to 
reference the exchange itself. 