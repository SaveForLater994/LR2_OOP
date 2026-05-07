package com.example.lr2_oop.model.shapes;

import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Ellipse;

public class EllipseShape extends Shape {

    // Локальные параметры (относительно центра)
    private double radiusX;  // радиус по оси X
    private double radiusY;  // радиус по оси Y

    private transient Ellipse javafxEllipse;

    // ----- Конструкторы -----

    /**
     * Конструктор эллипса по центру и радиусам
     */
    public EllipseShape(double centerX, double centerY, double radiusX, double radiusY) {
        super();
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.transform.setPosition(centerX, centerY);
        this.transform.setSize(radiusX * 2, radiusY * 2);
        updateNode();
    }

    /**
     * Конструктор эллипса по ограничивающему прямоугольнику
     */
//    public EllipseShape(double minX, double minY, double maxX, double maxY) {
//        this((minX + maxX) / 2, (minY + maxY) / 2, (maxX - minX) / 2, (maxY - minY) / 2);
//    }

    // Приватный конструктор для copy()
    private EllipseShape(EllipseShape original) {
        super();
        this.radiusX = original.radiusX;
        this.radiusY = original.radiusY;
        this.stroke = original.stroke.copy();
        this.fill = original.fill.copy();
        this.transform = original.transform.copy();
        this.id = java.util.UUID.randomUUID().toString();
        this.isSelected = false;
        updateNode();
    }

    // ----- Обновление JavaFX узла -----

    @Override
    protected void updateNode() {
        if (javafxEllipse == null) {
            javafxEllipse = new Ellipse();
        }

        javafxEllipse.setCenterX(transform.getX());
        javafxEllipse.setCenterY(transform.getY());
        javafxEllipse.setRadiusX(radiusX * transform.getScaleX());
        javafxEllipse.setRadiusY(radiusY * transform.getScaleY());
        javafxEllipse.setRotate(transform.getRotate());

        stroke.applyTo(javafxEllipse);
        fill.applyTo(javafxEllipse);

        if (isSelected) {
            javafxEllipse.setStrokeWidth(stroke.getWidth() + 3);
            javafxEllipse.getStrokeDashArray().addAll(5.0, 5.0);
        } else {
            stroke.applyTo(javafxEllipse);
            javafxEllipse.getStrokeDashArray().clear();
        }
    }

    @Override
    public Node getNode() {
        updateNode();
        return javafxEllipse;
    }

    // ----- Реализация абстрактных методов -----

    @Override
    public boolean contains(double x, double y) {
        double cx = transform.getX();
        double cy = transform.getY();
        double rx = radiusX * transform.getScaleX();
        double ry = radiusY * transform.getScaleY();
        double rotate = Math.toRadians(transform.getRotate());

        double dx = x - cx;
        double dy = y - cy;

        if (rotate != 0) {
            double cos = Math.cos(-rotate);
            double sin = Math.sin(-rotate);
            double newX = dx * cos - dy * sin;
            double newY = dx * sin + dy * cos;
            dx = newX;
            dy = newY;
        }

        double rx2 = rx * rx;
        double ry2 = ry * ry;
        double strokeOffset = stroke.getWidth() / 2;
        double tolerance = 1.0 + (strokeOffset / Math.min(rx, ry));

        double value = (dx * dx) / rx2 + (dy * dy) / ry2;

        return value <= tolerance;
    }

    @Override
    public Rectangle2D getBounds() {
        double cx = transform.getX();
        double cy = transform.getY();
        double rx = radiusX * transform.getScaleX();
        double ry = radiusY * transform.getScaleY();
        double rotate = transform.getRotate();
        double padding = stroke.getWidth() / 2;

        if (rotate == 0) {
            return new Rectangle2D(
                    cx - rx - padding,
                    cy - ry - padding,
                    rx * 2 + padding * 2,
                    ry * 2 + padding * 2
            );
        }

        double rad = Math.toRadians(rotate);
        double cos = Math.abs(Math.cos(rad));
        double sin = Math.abs(Math.sin(rad));

        double width = 2 * (rx * cos + ry * sin);
        double height = 2 * (rx * sin + ry * cos);

        return new Rectangle2D(
                cx - width / 2 - padding,
                cy - height / 2 - padding,
                width + padding * 2,
                height + padding * 2
        );
    }

    @Override
    public void translate(double dx, double dy) {
        transform.translate(dx, dy);
        updateNode();
    }

    @Override
    public void scale(double factor) {
        radiusX *= factor;
        radiusY *= factor;
        transform.setSize(radiusX * 2, radiusY * 2);
        updateNode();
    }

    @Override
    public Shape copy() {
        return new EllipseShape(this);
    }

    // ----- Дополнительные методы -----

    public double getFinalRadiusX() {
        return radiusX * transform.getScaleX();
    }

    public double getFinalRadiusY() {
        return radiusY * transform.getScaleY();
    }

    public double getBaseRadiusX() {
        return radiusX;
    }

    public double getBaseRadiusY() {
        return radiusY;
    }

    public void setRadii(double radiusX, double radiusY) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        transform.setSize(radiusX * 2, radiusY * 2);
        updateNode();
    }

    public boolean isCircle() {
        return Math.abs(radiusX - radiusY) < 0.001;
    }

    @Override
    public String toString() {
        return String.format("EllipseShape[id=%s, center=(%.1f,%.1f), rx=%.1f, ry=%.1f]",
                id.substring(0, 8), getCenterX(), getCenterY(),
                getFinalRadiusX(), getFinalRadiusY());
    }
}