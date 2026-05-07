package com.example.lr2_oop.command;

import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Команда одновременного изменения обводки и заливки фигуры (или нескольких фигур)
 */
public class ChangeStyleCommand implements Command {

    private final List<Shape> shapes;
    private final List<StrokeProperty> oldStrokes;
    private final List<FillProperty> oldFills;
    private final StrokeProperty newStroke;
    private final FillProperty newFill;
    private final boolean changeStroke;
    private final boolean changeFill;

    // Для одиночной фигуры
    public ChangeStyleCommand(Shape shape, StrokeProperty newStroke, FillProperty newFill) {
        this.shapes = new ArrayList<>();
        this.shapes.add(shape);
        this.oldStrokes = new ArrayList<>();
        this.oldFills = new ArrayList<>();
        this.newStroke = newStroke != null ? newStroke.copy() : null;
        this.newFill = newFill != null ? newFill.copy() : null;
        this.changeStroke = newStroke != null;
        this.changeFill = newFill != null;

        oldStrokes.add(shape.getStroke().copy());
        oldFills.add(shape.getFill().copy());
    }

    // Для нескольких фигур (мультиселект)
    public ChangeStyleCommand(List<Shape> shapes, StrokeProperty newStroke, FillProperty newFill) {
        this.shapes = new ArrayList<>(shapes);
        this.oldStrokes = new ArrayList<>();
        this.oldFills = new ArrayList<>();
        this.newStroke = newStroke != null ? newStroke.copy() : null;
        this.newFill = newFill != null ? newFill.copy() : null;
        this.changeStroke = newStroke != null;
        this.changeFill = newFill != null;

        for (Shape shape : shapes) {
            oldStrokes.add(shape.getStroke().copy());
            oldFills.add(shape.getFill().copy());
        }
    }

    // Только изменение обводки
    public ChangeStyleCommand(Shape shape, StrokeProperty newStroke) {
        this(shape, newStroke, null);
    }

    // Только изменение заливки
    public ChangeStyleCommand(Shape shape, FillProperty newFill) {
        this(shape, null, newFill);
    }

    @Override
    public void execute() {
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);

            if (changeStroke) {
                shape.setStroke(newStroke);
            }
            if (changeFill) {
                // Линии не поддерживают заливку
                if (!(shape instanceof com.example.lr2_oop.model.shapes.LineShape)) {
                    shape.setFill(newFill);
                }
            }
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);

            if (changeStroke) {
                shape.setStroke(oldStrokes.get(i));
            }
            if (changeFill) {
                if (!(shape instanceof com.example.lr2_oop.model.shapes.LineShape)) {
                    shape.setFill(oldFills.get(i));
                }
            }
        }
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        if (changeStroke && changeFill) {
            desc.append("Изменение стиля (обводка + заливка)");
        } else if (changeStroke) {
            desc.append("Изменение обводки");
        } else if (changeFill) {
            desc.append("Изменение заливки");
        }

        if (shapes.size() > 1) {
            desc.append(String.format(" для %d фигур", shapes.size()));
        }

        return desc.toString();
    }
}