package com.matt.marketsim.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultDto {
    public double delta;
    //Map of group names, mapping to data names and values
    public List<TradeStatisticDto> tradeStatisticDtos = new ArrayList<>();
}
