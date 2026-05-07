package com.example.lr2_oop.command;

import com.example.lr2_oop.model.Shape;
import javafx.geometry.Point2D;

/**
 * Команда перемещения фигуры
 */
public class MoveShapeCommand implements Command {

    private final Shape shape;
    private double deltaX;
    private double deltaY;
    private Point2D oldPosition;

    public MoveShapeCommand(Shape shape, double deltaX, double deltaY) {
        this.shape = shape;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    @Override
    public void execute() {
        oldPosition = shape.getPosition();
        shape.translate(deltaX, deltaY);
    }

    @Override
    public void undo() {
        shape.setPosition(oldPosition.getX(), oldPosition.getY());
    }

    @Override
    public boolean canMergeWith(Command other) {
        return other instanceof MoveShapeCommand &&
                ((MoveShapeCommand) other).shape == this.shape;
    }

    @Override
    public void mergeWith(Command other) {
        if (other instanceof MoveShapeCommand) {
            MoveShapeCommand otherMove = (MoveShapeCommand) other;
            this.deltaX += otherMove.deltaX;
            this.deltaY += otherMove.deltaY;
        }
    }

    @Override
    public String getDescription() {
        return String.format("Перемещение на (%.1f, %.1f)", deltaX, deltaY);
    }
}