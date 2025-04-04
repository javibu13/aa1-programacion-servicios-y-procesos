package com.sanvalero.imagefilters.filter;

import java.awt.Color;

public abstract class Filter {
    private String name; // Name of the filter

    public Filter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract Color apply(Color color); // Abstract method to apply the filter

    @Override
    public String toString() {
        return name;
    }
}
