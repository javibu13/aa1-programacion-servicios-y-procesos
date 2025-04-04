package com.sanvalero.imagefilters.filter;

import java.awt.Color;

public class BrightnessFilter extends Filter {
    private int brightnessLevel; // Brightness level (-255 to 255)

    public BrightnessFilter() {
        super("Brightness");
        this.brightnessLevel = 50; // Default brightness level
    }
    
    public BrightnessFilter(int brightnessLevel) {
        super("Brightness");
        this.brightnessLevel = brightnessLevel;
    }

    public int getBrightnessLevel() {
        return brightnessLevel;
    }

    public void setBrightnessLevel(int brightnessLevel) {
        this.brightnessLevel = brightnessLevel;
    }

    @Override
    public Color apply(Color color) {
        // Implement the logic to apply the brightness filter to the image
        int red = Math.min(Math.max(color.getRed() + brightnessLevel, 0), 255);
        int green = Math.min(Math.max(color.getGreen() + brightnessLevel, 0), 255);
        int blue = Math.min(Math.max(color.getBlue() + brightnessLevel, 0), 255);
        return new Color(red, green, blue);
    }

}
