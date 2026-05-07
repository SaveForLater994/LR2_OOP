package com.example.lr2_oop.model.shapes;

import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Line;

public class LineShape extends Shape {

    // Локальные координаты (относительно центра)
    private double startX;
    private double startY;
    private double endX;
    private double endY;

    private transient Line javafxLine;

    // ----- Конструкторы -----

    /**
     * Конструктор линии по двум точкам (абсолютные координаты)
     * @param x1 X координата начала
     * @param y1 Y координата начала
     * @param x2 X координата конца
     * @param y2 Y координата конца
     */
    public LineShape(double x1, double y1, double x2, double y2) {
        super();

        // Вычисляем центр отрезка
        double centerX = (x1 + x2) / 2;
        double centerY = (y1 + y2) / 2;

        // Сохраняем локальные координаты (относительно центра)
        this.startX = x1 - centerX;
        this.startY = y1 - centerY;
        this.endX = x2 - centerX;
        this.endY = y2 - centerY;

        this.transform.setPosition(centerX, centerY);

        // Вычисляем размеры для bounds
        double width = Math.abs(x2 - x1);
        double height = Math.abs(y2 - y1);
        this.transform.setSize(width, height);

        updateNode();
    }

    /**
     * Конструктор линии по двум точкам с заданными стилями
     */
    public LineShape(double x1, double y1, double x2, double y2,
                     StrokeProperty stroke, FillProperty fill) {
        this(x1, y1, x2, y2);
        this.stroke = stroke;
        this.fill = fill;
        updateNode();
    }

    // Приватный конструктор для copy()
    private LineShape(LineShape original) {
        super();
        this.startX = original.startX;
        this.startY = original.startY;
        this.endX = original.endX;
        this.endY = original.endY;
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
        if (javafxLine == null) {
            javafxLine = new Line();
        }

        double centerX = transform.getX();
        double centerY = transform.getY();

        javafxLine.setStartX(centerX + startX);
        javafxLine.setStartY(centerY + startY);
        javafxLine.setEndX(centerX + endX);
        javafxLine.setEndY(centerY + endY);

        stroke.applyTo(javafxLine);

        // Линия не использует заливку, но для единообразия оставляем
        fill.applyTo(javafxLine);

        if (isSelected) {
            javafxLine.setStrokeWidth(stroke.getWidth() + 3);
            javafxLine.getStrokeDashArray().addAll(5.0, 5.0);
        } else {
            stroke.applyTo(javafxLine);
            javafxLine.getStrokeDashArray().clear();
        }
    }

    @Override
    public Node getNode() {
        updateNode();
        return javafxLine;
    }

    // ----- Реализация абстрактных методов -----

    @Override
    public boolean contains(double x, double y) {
        double centerX = transform.getX();
        double centerY = transform.getY();

        double ax = centerX + startX;
        double ay = centerY + startY;
        double bx = centerX + endX;
        double by = centerY + endY;

        double distance = pointToSegmentDistance(x, y, ax, ay, bx, by);
        double hitRadius = Math.max(stroke.getWidth() / 2, 5.0);

        return distance <= hitRadius;
    }

    @Override
    public Rectangle2D getBounds() {
        double centerX = transform.getX();
        double centerY = transform.getY();

        double ax = centerX + startX;
        double ay = centerY + startY;
        double bx = centerX + endX;
        double by = centerY + endY;

        double minX = Math.min(ax, bx);
        double minY = Math.min(ay, by);
        double maxX = Math.max(ax, bx);
        double maxY = Math.max(ay, by);

        double padding = stroke.getWidth() / 2;

        return new Rectangle2D(
                minX - padding,
                minY - padding,
                (maxX - minX) + padding * 2,
                (maxY - minY) + padding * 2
        );
    }

    @Override
    public void translate(double dx, double dy) {
        transform.translate(dx, dy);
        updateNode();
    }

    @Override
    public void scale(double factor) {
        // Масштабируем локальные координаты относительно центра (0,0)
        startX *= factor;
        startY *= factor;
        endX *= factor;
        endY *= factor;

        // Обновляем размеры в transform
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        transform.setSize(width, height);

        updateNode();
    }

    @Override
    public Shape copy() {
        return new LineShape(this);
    }

    // ----- Вспомогательные методы -----

    /**
     * Вычисляет расстояние от точки P до отрезка AB
     */
    private double pointToSegmentDistance(double px, double py,
                                          double ax, double ay,
                                          double bx, double by) {
        double abX = bx - ax;
        double abY = by - ay;
        double apX = px - ax;
        double apY = py - ay;

        double dot = apX * abX + apY * abY;
        double abLenSq = abX * abX + abY * abY;

        if (abLenSq == 0) {
            // Отрезок вырожден в точку
            return Math.hypot(apX, apY);
        }

        double t = dot / abLenSq;

        if (t < 0) {
            // Ближайшая точка - A
            return Math.hypot(apX, apY);
        } else if (t > 1) {
            // Ближайшая точка - B
            double bpX = px - bx;
            double bpY = py - by;
            return Math.hypot(bpX, bpY);
        } else {
            // Ближайшая точка на отрезке
            double closestX = ax + t * abX;
            double closestY = ay + t * abY;
            return Math.hypot(px - closestX, py - closestY);
        }
    }

    // ----- Геттеры для инструментов рисования -----

    /**
     * Возвращает абсолютную X координату начала линии
     */
    public double getAbsoluteStartX() {
        return transform.getX() + startX;
    }

    /**
     * Возвращает абсолютную Y координату начала линии
     */
    public double getAbsoluteStartY() {
        return transform.getY() + startY;
    }

    /**
     * Возвращает абсолютную X координату конца линии
     */
    public double getAbsoluteEndX() {
        return transform.getX() + endX;
    }

    /**
     * Возвращает абсолютную Y координату конца линии
     */
    public double getAbsoluteEndY() {
        return transform.getY() + endY;
    }

    /**
     * Возвращает локальную X координату начала (относительно центра)
     */
    public double getStartX() {
        return startX;
    }

    /**
     * Возвращает локальную Y координату начала (относительно центра)
     */
    public double getStartY() {
        return startY;
    }

    /**
     * Возвращает локальную X координату конца (относительно центра)
     */
    public double getEndX() {
        return endX;
    }

    /**
     * Возвращает локальную Y координату конца (относительно центра)
     */
    public double getEndY() {
        return endY;
    }

    /**
     * Устанавливает начальную точку в абсолютных координатах
     */
    public void setStartPoint(double x, double y) {
        double centerX = transform.getX();
        double centerY = transform.getY();
        this.startX = x - centerX;
        this.startY = y - centerY;
        updateBoundsFromPoints();
        updateNode();
    }

    /**
     * Устанавливает конечную точку в абсолютных координатах
     */
    public void setEndPoint(double x, double y) {
        double centerX = transform.getX();
        double centerY = transform.getY();
        this.endX = x - centerX;
        this.endY = y - centerY;
        updateBoundsFromPoints();
        updateNode();
    }

    /**
     * Обновляет размеры в transform на основе текущих локальных точек
     */
    private void updateBoundsFromPoints() {
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        transform.setSize(width, height);
    }

    /**
     * Возвращает длину линии
     */
    public double getLength() {
        double dx = endX - startX;
        double dy = endY - startY;
        return Math.hypot(dx, dy);
    }

    @Override
    public String toString() {
        return String.format("LineShape[id=%s, from=(%.1f,%.1f), to=(%.1f,%.1f)]",
                id.substring(0, 8),
                getAbsoluteStartX(), getAbsoluteStartY(),
                getAbsoluteEndX(), getAbsoluteEndY());
    }
}