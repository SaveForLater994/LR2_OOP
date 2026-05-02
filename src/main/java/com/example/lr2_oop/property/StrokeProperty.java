package com.example.lr2_oop.property;

import javafx.scene.paint.Color;

import java.util.List;

//обводка
public class StrokeProperty {
    private double width;
    private Color color;
    private List<Double> dashArray;//для пунктира
    public Color getColor(){
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}
