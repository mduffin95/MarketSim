package com.matt.marketsim.dtos;

import com.matt.marketsim.ModelParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultDto {
    public ModelParameters params;
    //Map of group names, mapping to data names and values
    public List<String[]> entries = new ArrayList<>();
}
