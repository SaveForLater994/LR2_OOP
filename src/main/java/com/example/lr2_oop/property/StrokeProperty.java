package com.example.lr2_oop.property;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class StrokeProperty {

    private double width;        // толщина линии
    private Color color;         // цвет линии
    private double[] dashArray;  // пунктирная линия (массив длин штрихов и промежутков)

    // ----- Конструкторы -----

    public StrokeProperty() {
        this.width = 2.0;
        this.color = Color.BLACK;
        this.dashArray = new double[0];
    }

    public StrokeProperty(double width, Color color) {
        this.width = width;
        this.color = color;
        this.dashArray = new double[0];
    }

    public StrokeProperty(double width, Color color, double... dashArray) {
        this.width = width;
        this.color = color;
        this.dashArray = dashArray.clone();
    }

    // ----- Применение к JavaFX Shape -----

    /**
     * Применяет свойства обводки к JavaFX фигуре
     */
    public void applyTo(Shape shape) {
        shape.setStroke(color);
        shape.setStrokeWidth(width);

        if (dashArray.length > 0) {
            shape.getStrokeDashArray().setAll(dashArray);
        } else {
            shape.getStrokeDashArray().clear();
        }
    }

    // ----- Копирование -----

    public StrokeProperty copy() {
        StrokeProperty copy = new StrokeProperty(this.width, this.color);
        copy.dashArray = this.dashArray.clone();
        return copy;
    }

    // ----- Геттеры и сеттеры -----

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = Math.max(0, width);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double[] getDashArray() {
        return dashArray.clone();
    }

    public void setDashArray(double... dashArray) {
        this.dashArray = dashArray.clone();
    }

    /**
     * Проверяет, является ли линия пунктирной
     */
    public boolean isDashed() {
        return dashArray.length > 0;
    }

    @Override
    public String toString() {
        return String.format("Stroke[width=%.1f, color=%s, dashed=%b]",
                width, color, isDashed());
    }
}