package com.sanvalero.imagefilters.filter;

import java.awt.Color;

public class GrayscaleFilter extends Filter {
    public GrayscaleFilter() {
        super("Grayscale");
    }

    @Override
    public Color apply(Color color) {
        // Implement the logic to apply the grayscale filter to the image
        int gray = (int) (0.3 * color.getRed() + 0.59 * color.getGreen() + 0.11 * color.getBlue());
        return new Color(gray, gray, gray);
    }

}
