package com.example.lr2_oop.service;

import com.example.lr2_oop.model.DrawingCanvas;
import com.example.lr2_oop.model.Layer;
import com.example.lr2_oop.model.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления слоями
 */
public class LayerService {

    private final DrawingCanvas drawingCanvas;
    private final List<LayerListener> listeners;

    public interface LayerListener {
        void onLayerListChanged(List<Layer> layers);
        void onActiveLayerChanged(Layer newLayer);
        void onLayerVisibilityChanged(Layer layer, boolean visible);
        void onLayerLockChanged(Layer layer, boolean locked);
    }

    public LayerService(DrawingCanvas drawingCanvas) {
        this.drawingCanvas = drawingCanvas;
        this.listeners = new ArrayList<>();
    }

    /**
     * Создаёт новый слой
     */
    public Layer createLayer(String name) {
        Layer newLayer = new Layer(name);
        drawingCanvas.addLayer(newLayer);
        notifyLayerListChanged();
        return newLayer;
    }

    /**
     * Создаёт новый слой и устанавливает его активным
     */
    public Layer createAndActivateLayer(String name) {
        Layer newLayer = createLayer(name);
        setActiveLayer(newLayer);
        return newLayer;
    }

    /**
     * Удаляет слой
     */
    public boolean deleteLayer(Layer layer) {
        if (drawingCanvas.getLayerCount() <= 1) {
            return false; // Нельзя удалить последний слой
        }

        boolean removed = drawingCanvas.removeLayer(layer);
        if (removed) {
            notifyLayerListChanged();
        }
        return removed;
    }

    /**
     * Удаляет слой по индексу
     */
    public boolean deleteLayer(int index) {
        if (index < 0 || index >= drawingCanvas.getLayerCount()) {
            return false;
        }
        return deleteLayer(drawingCanvas.getLayers().get(index));
    }

    /**
     * Устанавливает активный слой
     */
    public void setActiveLayer(Layer layer) {
        if (layer == null) return;
        drawingCanvas.setActiveLayer(layer);
        notifyActiveLayerChanged(layer);
    }

    /**
     * Устанавливает активный слой по индексу
     */
    public void setActiveLayer(int index) {
        if (index < 0 || index >= drawingCanvas.getLayerCount()) return;
        setActiveLayer(drawingCanvas.getLayers().get(index));
    }

    /**
     * Возвращает активный слой
     */
    public Layer getActiveLayer() {
        return drawingCanvas.getActiveLayer();
    }

    /**
     * Возвращает все слои
     */
    public List<Layer> getLayers() {
        return drawingCanvas.getLayers();
    }

    /**
     * Возвращает количество слоёв
     */
    public int getLayerCount() {
        return drawingCanvas.getLayerCount();
    }

    /**
     * Перемещает слой вверх
     */
    public void moveLayerUp(Layer layer) {
        drawingCanvas.moveLayerUp(layer);
        notifyLayerListChanged();
    }

    /**
     * Перемещает слой вниз
     */
    public void moveLayerDown(Layer layer) {
        drawingCanvas.moveLayerDown(layer);
        notifyLayerListChanged();
    }

    /**
     * Переименовывает слой
     */
    public void renameLayer(Layer layer, String newName) {
        if (layer != null && newName != null && !newName.trim().isEmpty()) {
            layer.setName(newName);
            notifyLayerListChanged();
        }
    }

    /**
     * Переключает видимость слоя
     */
    public void toggleLayerVisibility(Layer layer) {
        if (layer != null) {
            layer.setVisible(!layer.isVisible());
            notifyLayerVisibilityChanged(layer, layer.isVisible());
        }
    }

    /**
     * Переключает блокировку слоя
     */
    public void toggleLayerLock(Layer layer) {
        if (layer != null) {
            layer.setLocked(!layer.isLocked());
            notifyLayerLockChanged(layer, layer.isLocked());
        }
    }

    /**
     * Перемещает фигуру на другой слой
     */
    public void moveShapeToLayer(Shape shape, Layer targetLayer) {
        if (shape == null || targetLayer == null) return;

        Layer currentLayer = shape.getLayer();
        if (currentLayer == targetLayer) return;

        drawingCanvas.moveShapeToLayer(shape, targetLayer);
        notifyLayerListChanged();
    }

    /**
     * Перемещает выделенные фигуры на целевой слой
     */
    public void moveSelectedToLayer(List<Shape> selectedShapes, Layer targetLayer) {
        if (selectedShapes == null || targetLayer == null) return;

        for (Shape shape : selectedShapes) {
            if (shape.getLayer() != targetLayer) {
                moveShapeToLayer(shape, targetLayer);
            }
        }
    }

    /**
     * Возвращает слой по имени
     */
    public Optional<Layer> findLayerByName(String name) {
        return Optional.ofNullable(drawingCanvas.getLayerByName(name));
    }

    // ----- Слушатели -----

    public void addListener(LayerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LayerListener listener) {
        listeners.remove(listener);
    }

    private void notifyLayerListChanged() {
        for (LayerListener listener : listeners) {
            listener.onLayerListChanged(getLayers());
        }
    }

    private void notifyActiveLayerChanged(Layer newLayer) {
        for (LayerListener listener : listeners) {
            listener.onActiveLayerChanged(newLayer);
        }
    }

    private void notifyLayerVisibilityChanged(Layer layer, boolean visible) {
        for (LayerListener listener : listeners) {
            listener.onLayerVisibilityChanged(layer, visible);
        }
    }

    private void notifyLayerLockChanged(Layer layer, boolean locked) {
        for (LayerListener listener : listeners) {
            listener.onLayerLockChanged(layer, locked);
        }
    }
}