package com.example.lr2_oop.command;

import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.property.TransformProperty;

/**
 * Команда для сохранения состояния всех трансформаций
 * Полезно для сложных операций (перемещение + поворот + масштаб)
 */
public class TransformCommand implements Command {

    private final Shape shape;
    private final TransformProperty oldTransform;
    private final TransformProperty newTransform;

    public TransformCommand(Shape shape, TransformProperty newTransform) {
        this.shape = shape;
        this.oldTransform = shape.getTransform().copy();
        this.newTransform = newTransform.copy();
    }

    @Override
    public void execute() {
        shape.setTransform(newTransform);
    }

    @Override
    public void undo() {
        shape.setTransform(oldTransform);
    }

    @Override
    public String getDescription() {
        return String.format("Трансформация фигуры");
    }
}