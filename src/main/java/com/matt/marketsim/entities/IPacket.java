package com.matt.marketsim.entities;

import com.matt.marketsim.MessageType;

public interface IPacket {
    void arrived();
    MessageType getType();
    Object getPayload();
    void scheduleArrival();
}
