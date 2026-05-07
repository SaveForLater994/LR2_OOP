package com.example.lr2_oop.command;

import com.example.lr2_oop.model.Shape;

/**
 * Команда поворота фигуры
 */
public class RotateShapeCommand implements Command {

    private final Shape shape;
    private double deltaAngle;
    private double oldAngle;

    public RotateShapeCommand(Shape shape, double deltaAngle) {
        this.shape = shape;
        this.deltaAngle = deltaAngle;
    }

    @Override
    public void execute() {
        oldAngle = shape.getRotate();
        shape.rotate(deltaAngle);
    }

    @Override
    public void undo() {
        shape.setRotate(oldAngle);
    }

    @Override
    public boolean canMergeWith(Command other) {
        return other instanceof RotateShapeCommand &&
                ((RotateShapeCommand) other).shape == this.shape;
    }

    @Override
    public void mergeWith(Command other) {
        if (other instanceof RotateShapeCommand) {
            RotateShapeCommand otherRotate = (RotateShapeCommand) other;
            this.deltaAngle += otherRotate.deltaAngle;
        }
    }

    @Override
    public String getDescription() {
        return String.format("Поворот на %.1f°", deltaAngle);
    }
}