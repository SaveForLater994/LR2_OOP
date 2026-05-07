package com.example.lr2_oop.property;

public class TransformProperty {

    // Позиция центра фигуры (абсолютные координаты)
    private double x;
    private double y;

    // Базовые размеры фигуры (без учёта масштаба)
    private double width;
    private double height;

    // Угол поворота (в градусах)
    private double rotate;

    // Масштаб
    private double scaleX;
    private double scaleY;

    // ----- Конструкторы -----

    public TransformProperty() {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.rotate = 0;
        this.scaleX = 1.0;
        this.scaleY = 1.0;
    }

    public TransformProperty(double x, double y) {
        this();
        this.x = x;
        this.y = y;
    }

    // ----- Работа с позицией -----

    /**
     * Устанавливает абсолютную позицию центра фигуры
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Перемещает фигуру на заданное смещение
     */
    public void translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // ----- Работа с размером -----

    /**
     * Устанавливает базовый размер фигуры
     */
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Устанавливает ширину
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * Устанавливает высоту
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Возвращает итоговую ширину с учётом масштаба
     */
    public double getWidth() {
        return width * scaleX;
    }

    /**
     * Возвращает итоговую высоту с учётом масштаба
     */
    public double getHeight() {
        return height * scaleY;
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

    // ----- Работа с поворотом -----

    /**
     * Устанавливает угол поворота (в градусах)
     */
    public void setRotate(double angle) {
        this.rotate = angle % 360;
        if (this.rotate < 0) this.rotate += 360;
    }

    /**
     * Поворачивает фигуру на заданный угол (относительно текущего)
     */
    public void rotate(double delta) {
        this.rotate = (this.rotate + delta) % 360;
        if (this.rotate < 0) this.rotate += 360;
    }

    public double getRotate() {
        return rotate;
    }

    // ----- Работа с масштабом -----

    /**
     * Устанавливает масштаб по осям X и Y
     */
    public void setScale(double scaleX, double scaleY) {
        this.scaleX = Math.max(0.01, scaleX);
        this.scaleY = Math.max(0.01, scaleY);
    }

    /**
     * Устанавливает равномерный масштаб
     */
    public void setScale(double scale) {
        setScale(scale, scale);
    }

    /**
     * Масштабирует фигуру относительно центра
     */
    public void scale(double factorX, double factorY) {
        this.scaleX *= Math.max(0.01, factorX);
        this.scaleY *= Math.max(0.01, factorY);
    }

    /**
     * Масштабирует фигуру равномерно
     */
    public void scale(double factor) {
        scale(factor, factor);
    }

    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    // ----- Копирование -----

    public TransformProperty copy() {
        TransformProperty copy = new TransformProperty();
        copy.x = this.x;
        copy.y = this.y;
        copy.width = this.width;
        copy.height = this.height;
        copy.rotate = this.rotate;
        copy.scaleX = this.scaleX;
        copy.scaleY = this.scaleY;
        return copy;
    }

    // ----- Вспомогательные методы -----

    /**
     * Сбрасывает все трансформации (позиция, поворот, масштаб)
     */
    public void reset() {
        this.x = 0;
        this.y = 0;
        this.rotate = 0;
        this.scaleX = 1.0;
        this.scaleY = 1.0;
    }

    /**
     * Сбрасывает позицию в (0,0)
     */
    public void resetPosition() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Сбрасывает поворот
     */
    public void resetRotate() {
        this.rotate = 0;
    }

    /**
     * Сбрасывает масштаб
     */
    public void resetScale() {
        this.scaleX = 1.0;
        this.scaleY = 1.0;
    }

    @Override
    public String toString() {
        return String.format("Transform[pos=(%.1f,%.1f), size=%.1fx%.1f, rotate=%.1f°, scale=%.2fx%.2f]",
                x, y, getWidth(), getHeight(), rotate, scaleX, scaleY);
    }
}