package com.sanvalero.imagefilters.filter;

import java.awt.Color;

public class InvertColorsFilter extends Filter {
    public InvertColorsFilter() {
        super("Invert Colors");
    }

    @Override
    public Color apply(Color color) {
        // Implement the logic to apply the invert colors filter to the image
        return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
    }

}
