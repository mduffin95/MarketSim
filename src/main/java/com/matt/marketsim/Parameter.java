package com.matt.marketsim;

public class Parameter {

    private Class<?> type;
    private String name;
    private Object value;

    public Parameter(Class<?> type, String name, Object value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public Parameter(Parameter p) {
        this(p.type, p.name, p.value);
    }

    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%-20s %-10s %-20s", name, value.toString(), type.toString());
    }
}
