package com.example.lr2_oop.command;

import com.example.lr2_oop.model.DrawingCanvas;
import com.example.lr2_oop.model.Layer;
import com.example.lr2_oop.model.Shape;

/**
 * Команда перемещения фигуры между слоями
 */
public class ChangeLayerCommand implements Command {

    private final DrawingCanvas canvas;
    private final Shape shape;
    private final Layer targetLayer;
    private Layer originalLayer;

    public ChangeLayerCommand(DrawingCanvas canvas, Shape shape, Layer targetLayer) {
        this.canvas = canvas;
        this.shape = shape;
        this.targetLayer = targetLayer;
    }

    @Override
    public void execute() {
        originalLayer = shape.getLayer();
        canvas.moveShapeToLayer(shape, targetLayer);
    }

    @Override
    public void undo() {
        if (originalLayer != null) {
            canvas.moveShapeToLayer(shape, originalLayer);
        }
    }

    @Override
    public String getDescription() {
        return String.format("Перемещение фигуры из '%s' в '%s'",
                originalLayer != null ? originalLayer.getName() : "null",
                targetLayer.getName());
    }
}