package com.example.lr2_oop.command;

import com.example.lr2_oop.model.DrawingCanvas;
import com.example.lr2_oop.model.Layer;
import com.example.lr2_oop.model.Shape;

/**
 * Команда удаления фигуры с холста
 */
public class RemoveShapeCommand implements Command {

    private final DrawingCanvas canvas;
    private final Shape shape;
    private Layer originalLayer;

    public RemoveShapeCommand(DrawingCanvas canvas, Shape shape) {
        this.canvas = canvas;
        this.shape = shape;
    }

    @Override
    public void execute() {
        originalLayer = shape.getLayer();
        canvas.removeShape(shape);
    }

    @Override
    public void undo() {
        if (originalLayer != null) {
            canvas.addShape(shape, originalLayer);
        } else {
            canvas.addShape(shape);
        }
    }

    @Override
    public String getDescription() {
        return String.format("Удаление %s", shape.getClass().getSimpleName());
    }
}