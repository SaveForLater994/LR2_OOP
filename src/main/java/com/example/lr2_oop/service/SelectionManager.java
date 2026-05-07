package com.example.lr2_oop.service;

import com.example.lr2_oop.listener.SelectionListener;
import com.example.lr2_oop.model.Shape;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Менеджер выделения фигур.
 * Находится в пакете service, так как управляет логикой выделения,
 * а не хранит данные (как model).
 */
public class SelectionManager {

    private final Set<Shape> selectedShapes;
    private Shape lastSelectedShape;
    private final List<SelectionListener> listeners;
    private SelectionMode mode;
    private Rectangle2D selectionRect;

    public enum SelectionMode {
        SINGLE, ADD, REMOVE, TOGGLE, RECTANGLE
    }

    public SelectionManager() {
        this.selectedShapes = new HashSet<>();
        this.listeners = new ArrayList<>();
        this.mode = SelectionMode.SINGLE;
    }

    // ----- Основные операции -----

    public void clearSelection() {
        for (Shape shape : selectedShapes) {
            shape.setSelected(false);
        }
        selectedShapes.clear();
        lastSelectedShape = null;
        notifySelectionChanged();
    }

    public void selectSingle(Shape shape) {
        if (shape == null) {
            clearSelection();
            return;
        }
        clearSelection();
        addToSelection(shape);
    }

    public void addToSelection(Shape shape) {
        if (shape == null) return;
        if (!selectedShapes.contains(shape)) {
            shape.setSelected(true);
            selectedShapes.add(shape);
            lastSelectedShape = shape;
            notifySelectionChanged();
        }
    }

    public void removeFromSelection(Shape shape) {
        if (shape == null) return;
        if (selectedShapes.remove(shape)) {
            shape.setSelected(false);
            if (lastSelectedShape == shape) {
                lastSelectedShape = selectedShapes.isEmpty() ? null :
                        selectedShapes.iterator().next();
            }
            notifySelectionChanged();
        }
    }

    public void toggleSelection(Shape shape) {
        if (shape == null) return;
        if (selectedShapes.contains(shape)) {
            removeFromSelection(shape);
        } else {
            addToSelection(shape);
        }
    }

    public void selectAll(Collection<Shape> allShapes) {
        clearSelection();
        for (Shape shape : allShapes) {
            shape.setSelected(true);
            selectedShapes.add(shape);
        }
        if (!selectedShapes.isEmpty()) {
            lastSelectedShape = selectedShapes.iterator().next();
        }
        notifySelectionChanged();
    }

    public void selectInRectangle(Rectangle2D rect, Collection<Shape> shapes, boolean additive) {
        if (!additive) {
            clearSelection();
        }
        for (Shape shape : shapes) {
            if (rect.intersects(shape.getBounds())) {
                addToSelection(shape);
            }
        }
        mode = SelectionMode.SINGLE;
        selectionRect = null;
        notifySelectionRectChanged();
    }

    public void updateSelectionRect(Rectangle2D rect) {
        this.selectionRect = rect;
        notifySelectionRectChanged();
    }

    public void finishRectangleSelection(Collection<Shape> shapes) {
        if (selectionRect != null) {
            selectInRectangle(selectionRect, shapes,
                    mode == SelectionMode.ADD || mode == SelectionMode.RECTANGLE);
        }
        selectionRect = null;
        mode = SelectionMode.SINGLE;
        notifySelectionRectChanged();
    }

    // ----- Операции над выделенными фигурами -----

    public void moveSelected(double dx, double dy) {
        for (Shape shape : selectedShapes) {
            shape.translate(dx, dy);
        }
    }

    public void scaleSelected(double factor) {
        for (Shape shape : selectedShapes) {
            shape.scale(factor);
        }
    }

    public void rotateSelected(double deltaAngle) {
        for (Shape shape : selectedShapes) {
            shape.rotate(deltaAngle);
        }
    }

    public void setStrokeForSelected(com.example.lr2_oop.property.StrokeProperty stroke) {
        for (Shape shape : selectedShapes) {
            shape.setStroke(stroke.copy());
        }
    }

    public void setFillForSelected(com.example.lr2_oop.property.FillProperty fill) {
        for (Shape shape : selectedShapes) {
            if (!(shape instanceof com.example.lr2_oop.model.shapes.LineShape)) {
                shape.setFill(fill.copy());
            }
        }
    }

    public List<Shape> deleteSelected() {
        List<Shape> deleted = new ArrayList<>(selectedShapes);
        clearSelection();
        return deleted;
    }

    public List<Shape> copySelected() {
        return selectedShapes.stream()
                .map(Shape::copy)
                .collect(Collectors.toList());
    }

    // ----- Геттеры -----

    public Set<Shape> getSelectedShapes() {
        return Collections.unmodifiableSet(selectedShapes);
    }

    public Shape getLastSelectedShape() {
        return lastSelectedShape;
    }

    public int getSelectionCount() {
        return selectedShapes.size();
    }

    public boolean isEmpty() {
        return selectedShapes.isEmpty();
    }

    public boolean isSelected(Shape shape) {
        return selectedShapes.contains(shape);
    }

    public Rectangle2D getSelectionRect() {
        return selectionRect;
    }

    public SelectionMode getMode() {
        return mode;
    }

    public void setMode(SelectionMode mode) {
        this.mode = mode;
    }

    public Rectangle2D getSelectionBounds() {
        if (selectedShapes.isEmpty()) return null;

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Shape shape : selectedShapes) {
            Rectangle2D bounds = shape.getBounds();
            minX = Math.min(minX, bounds.getMinX());
            minY = Math.min(minY, bounds.getMinY());
            maxX = Math.max(maxX, bounds.getMaxX());
            maxY = Math.max(maxY, bounds.getMaxY());
        }

        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

    public Point2D getSelectionCenter() {
        if (selectedShapes.isEmpty()) return null;

        double sumX = 0, sumY = 0;
        for (Shape shape : selectedShapes) {
            sumX += shape.getCenterX();
            sumY += shape.getCenterY();
        }
        return new Point2D(sumX / selectedShapes.size(), sumY / selectedShapes.size());
    }

    // ----- Обработка кликов -----

    public void handleShapeClick(Shape shape, boolean ctrlDown, boolean shiftDown) {
        if (shape == null) {
            if (!ctrlDown) clearSelection();
            return;
        }

        if (ctrlDown && shiftDown) {
            removeFromSelection(shape);
        } else if (ctrlDown) {
            toggleSelection(shape);
        } else {
            selectSingle(shape);
        }
    }

    // ----- Слушатели -----

    public void addListener(SelectionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SelectionListener listener) {
        listeners.remove(listener);
    }

    private void notifySelectionChanged() {
        for (SelectionListener listener : listeners) {
            listener.onSelectionChanged(
                    Collections.unmodifiableSet(selectedShapes),
                    lastSelectedShape
            );
        }
    }

    private void notifySelectionRectChanged() {
        for (SelectionListener listener : listeners) {
            listener.onSelectionRectChanged(selectionRect);
        }
    }
}