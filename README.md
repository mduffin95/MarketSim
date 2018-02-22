#MarketSim

##Implementation Notes

###Week 1 - 8th - 14th Feb
Got the basic simulator working. Could add fixed latency to all packets. Implemented ZI-C and ZI-U traders. 
###Week 2 - 15th - 21st Feb
Agents were previously instantly notified when they traded. Now it is sent as a packet as with all other information. 
Added ability for trading agents to handle price updates.
ZIP is doing something but not working properly yet.
###Week 3 - 22nd - 28th
There is a mistake in equation 5 of the ZIP section of the paper "Behavioural Investigations of Financial Trading 
Agents using Exchange Portal (ExPo)" by Stotter, Cartlidge and Cliff. The subtract 1 should be outside the division.
There is also a mistake in equation 6. 
