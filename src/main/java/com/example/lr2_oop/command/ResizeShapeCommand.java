package com.example.lr2_oop.command;

import com.example.lr2_oop.model.Shape;

/**
 * Команда изменения размера фигуры
 */
public class ResizeShapeCommand implements Command {

    private final Shape shape;
    private final double oldWidth;
    private final double oldHeight;
    private final double newWidth;
    private final double newHeight;

    public ResizeShapeCommand(Shape shape, double newWidth, double newHeight) {
        this.shape = shape;
        this.oldWidth = shape.getWidth();
        this.oldHeight = shape.getHeight();
        this.newWidth = newWidth;
        this.newHeight = newHeight;
    }

    @Override
    public void execute() {
        shape.setWidth(newWidth);
        shape.setHeight(newHeight);
    }

    @Override
    public void undo() {
        shape.setWidth(oldWidth);
        shape.setHeight(oldHeight);
    }

    @Override
    public String getDescription() {
        return String.format("Изменение размера: %.1fx%.1f -> %.1fx%.1f",
                oldWidth, oldHeight, newWidth, newHeight);
    }
}