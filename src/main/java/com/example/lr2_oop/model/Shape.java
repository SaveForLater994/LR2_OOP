package com.example.lr2_oop.model;

import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import com.example.lr2_oop.property.TransformProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;

public abstract class Shape {

    protected Layer onLayer;
    protected FillProperty fill;
    protected StrokeProperty stroke;
    protected TransformProperty transform;
    protected String id;
    protected boolean isSelected;

    public Shape() {
        this.id = java.util.UUID.randomUUID().toString();
        this.isSelected = false;
        this.fill = new FillProperty();
        this.stroke = new StrokeProperty();
        this.transform = new TransformProperty();
    }

    // ----- Абстрактные методы -----

    public abstract boolean contains(double x, double y);
    public abstract Rectangle2D getBounds();
    public abstract void translate(double dx, double dy);
    public abstract void scale(double factor);
    public abstract Shape copy();

    /**
     * Возвращает JavaFX узел для отрисовки (может быть Shape или Group)
     */
    public abstract Node getNode();

    protected abstract void updateNode();

    // ----- Остальные методы без изменений -----

    public void setPosition(double x, double y) {
        transform.setPosition(x, y);
        updateNode();
    }

    public javafx.geometry.Point2D getPosition() {
        return new javafx.geometry.Point2D(transform.getX(), transform.getY());
    }

    public double getCenterX() {
        return transform.getX();
    }

    public double getCenterY() {
        return transform.getY();
    }

    public void setRotate(double angle) {
        transform.setRotate(angle);
        updateNode();
    }

    public void rotate(double delta) {
        transform.rotate(delta);
        updateNode();
    }

    public double getRotate() {
        return transform.getRotate();
    }

    public void setWidth(double width) {
        transform.setWidth(width);
        updateNode();
    }

    public void setHeight(double height) {
        transform.setHeight(height);
        updateNode();
    }

    public double getWidth() {
        return transform.getWidth();
    }

    public double getHeight() {
        return transform.getHeight();
    }

    public Layer getLayer() {
        return onLayer;
    }

    public void setLayer(Layer layer) {
        this.onLayer = layer;
    }

    public FillProperty getFill() {
        return fill;
    }

    public void setFill(FillProperty fill) {
        this.fill = fill;
        updateNode();
    }

    public StrokeProperty getStroke() {
        return stroke;
    }

    public void setStroke(StrokeProperty stroke) {
        this.stroke = stroke;
        updateNode();
    }

    public TransformProperty getTransform() {
        return transform;
    }

    public void setTransform(TransformProperty transform) {
        this.transform = transform;
        updateNode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        updateNode();
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, pos=(%.1f,%.1f)]",
                getClass().getSimpleName(),
                id.substring(0, 8),
                getCenterX(), getCenterY());
    }
}