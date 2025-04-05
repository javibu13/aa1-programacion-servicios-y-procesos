package com.sanvalero.imagefilters.filter;

import javafx.scene.image.Image;

public class FilterStep {
    private Image originalImage;
    private Image resultImage;

    public FilterStep(Image originalImage, Image resultImage) {
        this.originalImage = originalImage;
        this.resultImage = resultImage;
    }

    public Image getOriginalImage() {
        return originalImage;
    }

    public Image getResultImage() {
        return resultImage;
    }
}
