package com.example.lr2_oop.command;

import com.example.lr2_oop.model.DrawingCanvas;
import com.example.lr2_oop.model.Layer;
import com.example.lr2_oop.model.Shape;

/**
 * Команда добавления фигуры на холст
 */
public class AddShapeCommand implements Command {

    private final DrawingCanvas canvas;
    private final Shape shape;
    private final Layer targetLayer;
    private Shape addedShape;

    public AddShapeCommand(DrawingCanvas canvas, Shape shape) {
        this.canvas = canvas;
        this.shape = shape;
        this.targetLayer = canvas.getActiveLayer();
    }

    public AddShapeCommand(DrawingCanvas canvas, Shape shape, Layer targetLayer) {
        this.canvas = canvas;
        this.shape = shape;
        this.targetLayer = targetLayer;
    }

    @Override
    public void execute() {
        addedShape = shape;
        canvas.addShape(addedShape, targetLayer);
    }

    @Override
    public void undo() {
        canvas.removeShape(addedShape);
    }

    @Override
    public String getDescription() {
        return String.format("Добавление %s", shape.getClass().getSimpleName());
    }
}