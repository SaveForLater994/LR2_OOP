package com.example.lr2_oop.property;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class FillProperty {

    private Color color;           // цвет заливки
    private boolean transparent;   // прозрачная заливка

    // ----- Конструкторы -----

    public FillProperty() {
        this.color = Color.WHITE;
        this.transparent = false;
    }

    public FillProperty(Color color) {
        this.color = color;
        this.transparent = false;
    }

    public FillProperty(boolean transparent) {
        this.color = Color.WHITE;
        this.transparent = transparent;
    }

    public FillProperty(Color color, boolean transparent) {
        this.color = color;
        this.transparent = transparent;
    }

    // ----- Применение к JavaFX Shape -----

    /**
     * Применяет свойства заливки к JavaFX фигуре
     */
    public void applyTo(Shape shape) {
        if (transparent) {
            shape.setFill(null);
        } else {
            shape.setFill(color);
        }
    }

    // ----- Копирование -----

    public FillProperty copy() {
        return new FillProperty(this.color, this.transparent);
    }

    // ----- Геттеры и сеттеры -----

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.transparent = false;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    @Override
    public String toString() {
        if (transparent) {
            return "Fill[transparent]";
        }
        return String.format("Fill[color=%s]", color);
    }
}