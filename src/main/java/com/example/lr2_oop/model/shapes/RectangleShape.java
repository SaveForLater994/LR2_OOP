package com.example.lr2_oop.model.shapes;

import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

public class RectangleShape extends Shape {

    // Локальные параметры (относительно центра)
    private double width;   // ширина прямоугольника
    private double height;  // высота прямоугольника

    private transient Rectangle javafxRectangle;

    // ----- Конструкторы -----

    /**
     * Конструктор прямоугольника по двум противоположным углам
     * @param x1 X координата первого угла
     * @param y1 Y координата первого угла
     * @param x2 X координата второго угла
     * @param y2 Y координата второго угла
     */
    public RectangleShape(double x1, double y1, double x2, double y2) {
        super();

        // Вычисляем центр
        double centerX = (x1 + x2) / 2;
        double centerY = (y1 + y2) / 2;

        // Вычисляем размеры
        this.width = Math.abs(x2 - x1);
        this.height = Math.abs(y2 - y1);

        this.transform.setPosition(centerX, centerY);
        this.transform.setSize(width, height);

        updateNode();
    }

    /**
     * Конструктор прямоугольника по двум углам с заданными стилями
     */
    public RectangleShape(double x1, double y1, double x2, double y2,
                          StrokeProperty stroke, FillProperty fill) {
        this(x1, y1, x2, y2);
        this.stroke = stroke;
        this.fill = fill;
        updateNode();
    }

    // Приватный конструктор для copy()
    private RectangleShape(RectangleShape original) {
        super();
        this.width = original.width;
        this.height = original.height;
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
        if (javafxRectangle == null) {
            javafxRectangle = new Rectangle();
        }

        double centerX = transform.getX();
        double centerY = transform.getY();
        double finalWidth = getFinalWidth();
        double finalHeight = getFinalHeight();

        // Устанавливаем позицию (левый верхний угол)
        javafxRectangle.setX(centerX - finalWidth / 2);
        javafxRectangle.setY(centerY - finalHeight / 2);
        javafxRectangle.setWidth(finalWidth);
        javafxRectangle.setHeight(finalHeight);

        // Устанавливаем поворот
        javafxRectangle.setRotate(transform.getRotate());

        // Применяем стили
        stroke.applyTo(javafxRectangle);
        fill.applyTo(javafxRectangle);

        // Эффект выделения
        if (isSelected) {
            javafxRectangle.setStrokeWidth(stroke.getWidth() + 3);
            javafxRectangle.getStrokeDashArray().addAll(5.0, 5.0);
        } else {
            stroke.applyTo(javafxRectangle);
            javafxRectangle.getStrokeDashArray().clear();
        }
    }

    @Override
    public Node getNode() {
        updateNode();
        return javafxRectangle;
    }

    // ----- Реализация абстрактных методов -----

    @Override
    public boolean contains(double x, double y) {
        double centerX = transform.getX();
        double centerY = transform.getY();
        double finalWidth = getFinalWidth();
        double finalHeight = getFinalHeight();
        double rotate = Math.toRadians(transform.getRotate());

        // Смещение относительно центра
        double dx = x - centerX;
        double dy = y - centerY;

        // Если прямоугольник повёрнут, поворачиваем точку обратно
        if (rotate != 0) {
            double cos = Math.cos(-rotate);
            double sin = Math.sin(-rotate);
            double newX = dx * cos - dy * sin;
            double newY = dx * sin + dy * cos;
            dx = newX;
            dy = newY;
        }

        // Половина ширины и высоты
        double halfWidth = finalWidth / 2;
        double halfHeight = finalHeight / 2;

        // Проверка попадания в прямоугольник (с учётом толщины линии)
        double tolerance = stroke.getWidth() / 2;

        return (dx >= -halfWidth - tolerance && dx <= halfWidth + tolerance &&
                dy >= -halfHeight - tolerance && dy <= halfHeight + tolerance);
    }

    @Override
    public Rectangle2D getBounds() {
        double centerX = transform.getX();
        double centerY = transform.getY();
        double finalWidth = getFinalWidth();
        double finalHeight = getFinalHeight();
        double rotate = transform.getRotate();
        double padding = stroke.getWidth() / 2;

        // Если прямоугольник не повёрнут, bounds вычисляется просто
        if (rotate == 0) {
            return new Rectangle2D(
                    centerX - finalWidth / 2 - padding,
                    centerY - finalHeight / 2 - padding,
                    finalWidth + padding * 2,
                    finalHeight + padding * 2
            );
        }

        // Если повёрнут, вычисляем охватывающий прямоугольник
        double rad = Math.toRadians(rotate);
        double cos = Math.abs(Math.cos(rad));
        double sin = Math.abs(Math.sin(rad));

        double halfWidth = finalWidth / 2;
        double halfHeight = finalHeight / 2;

        double width = 2 * (halfWidth * cos + halfHeight * sin);
        double height = 2 * (halfWidth * sin + halfHeight * cos);

        return new Rectangle2D(
                centerX - width / 2 - padding,
                centerY - height / 2 - padding,
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
        // Масштабируем ширину и высоту
        width *= factor;
        height *= factor;

        // Обновляем размер в transform
        transform.setSize(width, height);

        updateNode();
    }

    @Override
    public Shape copy() {
        return new RectangleShape(this);
    }

    // ----- Дополнительные методы -----

    /**
     * Возвращает итоговую ширину с учётом масштаба
     */
    public double getFinalWidth() {
        return width * transform.getScaleX();
    }

    /**
     * Возвращает итоговую высоту с учётом масштаба
     */
    public double getFinalHeight() {
        return height * transform.getScaleY();
    }

    /**
     * Возвращает базовую ширину (без масштаба)
     */
    public double getBaseWidth() {
        return width;
    }

    /**
     * Возвращает базовую высоту (без масштаба)
     */
    public double getBaseHeight() {
        return height;
    }

    /**
     * Устанавливает размеры прямоугольника
     */
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
        transform.setSize(width, height);
        updateNode();
    }

    /**
     * Возвращает X координату левого верхнего угла (абсолютную)
     */
    public double getMinX() {
        return transform.getX() - getFinalWidth() / 2;
    }

    /**
     * Возвращает Y координату левого верхнего угла (абсолютную)
     */
    public double getMinY() {
        return transform.getY() - getFinalHeight() / 2;
    }

    /**
     * Возвращает X координату правого нижнего угла (абсолютную)
     */
    public double getMaxX() {
        return transform.getX() + getFinalWidth() / 2;
    }

    /**
     * Возвращает Y координату правого нижнего угла (абсолютную)
     */
    public double getMaxY() {
        return transform.getY() + getFinalHeight() / 2;
    }

    /**
     * Проверяет, является ли прямоугольник квадратом
     */
    public boolean isSquare() {
        return Math.abs(width - height) < 0.001;
    }

    @Override
    public String toString() {
        return String.format("RectangleShape[id=%s, center=(%.1f,%.1f), width=%.1f, height=%.1f]",
                id.substring(0, 8), getCenterX(), getCenterY(),
                getFinalWidth(), getFinalHeight());
    }
}