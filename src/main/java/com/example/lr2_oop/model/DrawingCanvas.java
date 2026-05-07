package com.example.lr2_oop.model;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.List;

/**
 * Модель холста для рисования.
 * Содержит слои и управляет отрисовкой.
 */
public class DrawingCanvas {

    private final List<Layer> layers;
    private Layer activeLayer;
    private double width;
    private double height;
    private Color backgroundColor;

    // Временные элементы (для предпросмотра при рисовании)
    private Shape tempShape;
    private Rectangle2D selectionRect;

    // Слушатели изменений
    private final List<CanvasChangeListener> listeners;

    /**
     * Интерфейс слушателя изменений холста
     */
    public interface CanvasChangeListener {
        void onLayerAdded(Layer layer);
        void onLayerRemoved(Layer layer);
        void onActiveLayerChanged(Layer oldLayer, Layer newLayer);
        void onLayerOrderChanged();
        void onShapeAdded(Shape shape, Layer layer);
        void onShapeRemoved(Shape shape, Layer layer);
        void onRedrawRequested();
    }

    // ----- Конструкторы -----

    public DrawingCanvas(double width, double height) {
        this.layers = new ArrayList<>();
        this.width = width;
        this.height = height;
        this.backgroundColor = Color.WHITE;
        this.listeners = new ArrayList<>();

        // Создаём слой по умолчанию
        Layer defaultLayer = new Layer("Слой 1");
        addLayer(defaultLayer);
        setActiveLayer(defaultLayer);
    }

    // ----- Управление слоями -----

    /**
     * Добавляет новый слой
     */
    public void addLayer(Layer layer) {
        if (layer == null) return;

        layers.add(layer);
        notifyLayerAdded(layer);
    }

    /**
     * Добавляет слой на указанную позицию
     */
    public void addLayer(int index, Layer layer) {
        if (layer == null) return;

        layers.add(index, layer);
        notifyLayerAdded(layer);
    }

    /**
     * Удаляет слой
     */
    public boolean removeLayer(Layer layer) {
        if (layer == null || layers.size() <= 1) return false; // минимум 1 слой

        // Перемещаем фигуры с удаляемого слоя на активный
        Layer targetLayer = getActiveLayer();
        if (layer != targetLayer) {
            for (Shape shape : layer.getShapes()) {
                targetLayer.addShape(shape);
            }
        }

        boolean removed = layers.remove(layer);
        if (removed) {
            notifyLayerRemoved(layer);
        }
        return removed;
    }

    /**
     * Удаляет слой по индексу
     */
    public boolean removeLayer(int index) {
        if (index < 0 || index >= layers.size()) return false;
        return removeLayer(layers.get(index));
    }

    /**
     * Перемещает слой вверх
     */
    public void moveLayerUp(Layer layer) {
        int index = layers.indexOf(layer);
        if (index >= 0 && index < layers.size() - 1) {
            java.util.Collections.swap(layers, index, index + 1);
            notifyLayerOrderChanged();
        }
    }

    /**
     * Перемещает слой вниз
     */
    public void moveLayerDown(Layer layer) {
        int index = layers.indexOf(layer);
        if (index > 0) {
            java.util.Collections.swap(layers, index, index - 1);
            notifyLayerOrderChanged();
        }
    }

    /**
     * Устанавливает активный слой (для добавления новых фигур)
     */
    public void setActiveLayer(Layer layer) {
        if (layer == null || !layers.contains(layer)) return;

        Layer oldLayer = this.activeLayer;
        this.activeLayer = layer;
        notifyActiveLayerChanged(oldLayer, layer);
    }

    /**
     * Возвращает активный слой
     */
    public Layer getActiveLayer() {
        return activeLayer;
    }

    /**
     * Возвращает слой по имени
     */
    public Layer getLayerByName(String name) {
        for (Layer layer : layers) {
            if (layer.getName().equals(name)) {
                return layer;
            }
        }
        return null;
    }

    /**
     * Возвращает все слои
     */
    public List<Layer> getLayers() {
        return java.util.Collections.unmodifiableList(layers);
    }

    /**
     * Возвращает количество слоёв
     */
    public int getLayerCount() {
        return layers.size();
    }

    // ----- Управление фигурами -----

    /**
     * Добавляет фигуру на активный слой
     */
    public void addShape(Shape shape) {
        if (shape == null || activeLayer == null) return;

        activeLayer.addShape(shape);
        notifyShapeAdded(shape, activeLayer);
    }

    /**
     * Добавляет фигуру на указанный слой
     */
    public void addShape(Shape shape, Layer layer) {
        if (shape == null || layer == null) return;

        layer.addShape(shape);
        notifyShapeAdded(shape, layer);
    }

    /**
     * Удаляет фигуру из всех слоёв
     */
    public boolean removeShape(Shape shape) {
        for (Layer layer : layers) {
            if (layer.removeShape(shape)) {
                notifyShapeRemoved(shape, layer);
                return true;
            }
        }
        return false;
    }

    /**
     * Перемещает фигуру из одного слоя в другой
     */
    public void moveShapeToLayer(Shape shape, Layer targetLayer) {
        if (shape == null || targetLayer == null) return;

        Layer sourceLayer = shape.getLayer();
        if (sourceLayer == targetLayer) return;

        if (sourceLayer != null) {
            sourceLayer.removeShape(shape);
        }

        targetLayer.addShape(shape);
        notifyShapeMoved(shape, sourceLayer, targetLayer);
    }

    /**
     * Находит фигуру, содержащую точку (x, y), с учётом порядка слоёв
     */
    public Shape findShapeAt(double x, double y) {
        // Идём от верхнего слоя к нижнему
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer layer = layers.get(i);
            if (!layer.isVisible() || layer.isLocked()) continue;

            Shape shape = layer.findShapeAt(x, y);
            if (shape != null) {
                return shape;
            }
        }
        return null;
    }

    /**
     * Находит все фигуры, содержащие точку (x, y)
     */
    public List<Shape> findAllShapesAt(double x, double y) {
        List<Shape> result = new ArrayList<>();
        for (Layer layer : layers) {
            if (!layer.isVisible()) continue;
            result.addAll(layer.findAllShapesAt(x, y));
        }
        return result;
    }

    /**
     * Находит все фигуры, пересекающие прямоугольник
     */
    public List<Shape> findShapesInRect(Rectangle2D rect) {
        List<Shape> result = new ArrayList<>();
        for (Layer layer : layers) {
            if (!layer.isVisible()) continue;
            result.addAll(layer.findShapesInRect(rect));
        }
        return result;
    }

    /**
     * Возвращает все фигуры со всех слоёв для отрисовки
     */
    public List<Shape> getAllShapesForRendering() {
        List<Shape> allShapes = new ArrayList<>();
        for (Layer layer : layers) {
            if (layer.isVisible()) {
                allShapes.addAll(layer.getShapesInOrder());
            }
        }
        return allShapes;
    }

    // ----- Временные элементы (предпросмотр) -----

    public void setTempShape(Shape shape) {
        this.tempShape = shape;
        notifyRedrawRequested();
    }

    public Shape getTempShape() {
        return tempShape;
    }

    public void clearTempShape() {
        this.tempShape = null;
        notifyRedrawRequested();
    }

    public void setSelectionRect(Rectangle2D rect) {
        this.selectionRect = rect;
        notifyRedrawRequested();
    }

    public Rectangle2D getSelectionRect() {
        return selectionRect;
    }

    public void clearSelectionRect() {
        this.selectionRect = null;
        notifyRedrawRequested();
    }

    // ----- Настройки холста -----

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
        notifyRedrawRequested();
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        notifyRedrawRequested();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        notifyRedrawRequested();
    }

    /**
     * Очищает холст (удаляет все фигуры со всех слоёв)
     */
    public void clear() {
        for (Layer layer : layers) {
            layer.clear();
        }
        notifyRedrawRequested();
    }

    // ----- Слушатели -----

    public void addListener(CanvasChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CanvasChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyLayerAdded(Layer layer) {
        for (CanvasChangeListener listener : listeners) {
            listener.onLayerAdded(layer);
        }
    }

    private void notifyLayerRemoved(Layer layer) {
        for (CanvasChangeListener listener : listeners) {
            listener.onLayerRemoved(layer);
        }
    }

    private void notifyActiveLayerChanged(Layer oldLayer, Layer newLayer) {
        for (CanvasChangeListener listener : listeners) {
            listener.onActiveLayerChanged(oldLayer, newLayer);
        }
    }

    private void notifyLayerOrderChanged() {
        for (CanvasChangeListener listener : listeners) {
            listener.onLayerOrderChanged();
        }
    }

    private void notifyShapeAdded(Shape shape, Layer layer) {
        for (CanvasChangeListener listener : listeners) {
            listener.onShapeAdded(shape, layer);
        }
    }

    private void notifyShapeRemoved(Shape shape, Layer layer) {
        for (CanvasChangeListener listener : listeners) {
            listener.onShapeRemoved(shape, layer);
        }
    }

    private void notifyShapeMoved(Shape shape, Layer fromLayer, Layer toLayer) {
        // Можно добавить отдельный метод в интерфейс, но для простоты используем onShapeRemoved + onShapeAdded
        notifyRedrawRequested();
    }

    private void notifyRedrawRequested() {
        for (CanvasChangeListener listener : listeners) {
            listener.onRedrawRequested();
        }
    }

    @Override
    public String toString() {
        return String.format("DrawingCanvas[%.0fx%.0f, layers=%d, activeLayer='%s']",
                width, height, layers.size(),
                activeLayer != null ? activeLayer.getName() : "null");
    }
}