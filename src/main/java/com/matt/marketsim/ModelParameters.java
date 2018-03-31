package com.matt.marketsim;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ModelParameters {

    private Map<String, Parameter> parameterMap;

    public ModelParameters() {
        parameterMap = new TreeMap<>();
    }

    public ModelParameters(ModelParameters parameters) {
        this();
        for (Map.Entry<String, Parameter> entry: parameters.parameterMap.entrySet()) {
            parameterMap.put(entry.getKey(), new Parameter(entry.getValue()));
        }
    }

    public void addParameter(Class<?> type, String name, Object value) {
        parameterMap.put(name, new Parameter(type, name, value));
    }

    public Object getParameter(String name) {
        return parameterMap.get(name).getValue();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Collection<Parameter> params = parameterMap.values();
        String newline = System.getProperty("line.separator");
        for (Parameter p: params) {
            builder.append(p.toString()).append(newline);
        }
        return builder.toString();
    }
}
