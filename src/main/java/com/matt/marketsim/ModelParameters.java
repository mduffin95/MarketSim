package com.matt.marketsim;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.cli.*;

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

    public void updateParams(String[] args) {
        Options options = new Options();

        for (Map.Entry<String, Parameter> entry: parameterMap.entrySet()) {
            Class<?> cls = entry.getValue().getType();
            Option opt = Option.builder()
                    .longOpt(entry.getKey())
                    .required(false)
                    .hasArg(true)
                    .build();

            options.addOption(opt);
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        for (Map.Entry<String, Parameter> entry: parameterMap.entrySet()) {
            String name = entry.getKey();
            if (cmd.hasOption(name)) {
                Class<?> type = entry.getValue().getType();
                if (type == Boolean.class) {
                    entry.setValue(new Parameter(type, name, Boolean.valueOf(cmd.getOptionValue(name))));
                } if (type == Integer.class) {
                    entry.setValue(new Parameter(type, name, Integer.valueOf(cmd.getOptionValue(name))));
                } if (type == Double.class) {
                    entry.setValue(new Parameter(type, name, Double.valueOf(cmd.getOptionValue(name))));
                }
            }
        }

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
