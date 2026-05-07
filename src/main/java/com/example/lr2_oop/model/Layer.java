package com.example.lr2_oop.model;

import javafx.scene.Node;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Слой, содержащий набор фигур.
 * Поддерживает порядок отображения (z-order) фигур.
 */
public class Layer {

    private String id;
    private String name;
    private boolean visible;
    private boolean locked;  // заблокирован ли слой (нельзя редактировать)
    private final List<Shape> shapes;
    private double opacity;

    // Слушатели изменений слоя
    private final List<LayerChangeListener> listeners;

    /**
     * Интерфейс слушателя изменений слоя
     */
    public interface LayerChangeListener {
        void onShapeAdded(Shape shape);
        void onShapeRemoved(Shape shape);
        void onShapeOrderChanged();
        void onVisibilityChanged(boolean visible);
        void onLockChanged(boolean locked);
        void onNameChanged(String newName);
    }

    // ----- Конструкторы -----

    public Layer(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.visible = true;
        this.locked = false;
        this.shapes = new CopyOnWriteArrayList<>();  // потокобезопасный список
        this.opacity = 1.0;
        this.listeners = new ArrayList<>();
    }

    public Layer(String name, boolean visible, boolean locked) {
        this(name);
        this.visible = visible;
        this.locked = locked;
    }

    // ----- Управление фигурами -----

    /**
     * Добавляет фигуру на слой (в конец = поверх остальных)
     */
    public void addShape(Shape shape) {
        if (shape == null) return;

        shapes.add(shape);
        shape.setLayer(this);
        notifyShapeAdded(shape);
    }

    /**
     * Добавляет фигуру на слой на указанную позицию
     * @param index позиция (0 - самый нижний, size-1 - самый верхний)
     */
    public void addShape(int index, Shape shape) {
        if (shape == null) return;

        shapes.add(index, shape);
        shape.setLayer(this);
        notifyShapeAdded(shape);
    }

    /**
     * Удаляет фигуру со слоя
     */
    public boolean removeShape(Shape shape) {
        if (shape == null) return false;

        boolean removed = shapes.remove(shape);
        if (removed) {
            shape.setLayer(null);
            notifyShapeRemoved(shape);
        }
        return removed;
    }

    /**
     * Удаляет фигуру по ID
     */
    public boolean removeShapeById(String id) {
        for (Shape shape : shapes) {
            if (shape.getId().equals(id)) {
                return removeShape(shape);
            }
        }
        return false;
    }

    /**
     * Очищает слой (удаляет все фигуры)
     */
    public void clear() {
        for (Shape shape : shapes) {
            shape.setLayer(null);
        }
        shapes.clear();
        notifyShapeOrderChanged();
    }

    /**
     * Перемещает фигуру вверх по z-order
     */
    public void bringForward(Shape shape) {
        int index = shapes.indexOf(shape);
        if (index >= 0 && index < shapes.size() - 1) {
            Collections.swap(shapes, index, index + 1);
            notifyShapeOrderChanged();
        }
    }

    /**
     * Перемещает фигуру вниз по z-order
     */
    public void sendBackward(Shape shape) {
        int index = shapes.indexOf(shape);
        if (index > 0) {
            Collections.swap(shapes, index, index - 1);
            notifyShapeOrderChanged();
        }
    }

    /**
     * Перемещает фигуру на передний план
     */
    public void bringToFront(Shape shape) {
        if (shapes.remove(shape)) {
            shapes.add(shape);
            notifyShapeOrderChanged();
        }
    }

    /**
     * Перемещает фигуру на задний план
     */
    public void sendToBack(Shape shape) {
        if (shapes.remove(shape)) {
            shapes.add(0, shape);
            notifyShapeOrderChanged();
        }
    }

    /**
     * Изменяет порядок фигур (перемещает с indexFrom на indexTo)
     */
    public void reorderShape(int indexFrom, int indexTo) {
        if (indexFrom < 0 || indexFrom >= shapes.size()) return;
        if (indexTo < 0 || indexTo >= shapes.size()) return;
        if (indexFrom == indexTo) return;

        Shape shape = shapes.remove(indexFrom);
        shapes.add(indexTo, shape);
        notifyShapeOrderChanged();
    }

    // ----- Геттеры -----

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyNameChanged(name);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        notifyVisibilityChanged(visible);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        notifyLockChanged(locked);
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = Math.max(0, Math.min(1, opacity));
        // Применяем opacity ко всем фигурам (или к контейнеру)
        for (Shape shape : shapes) {
            Node node = shape.getNode();
            node.setOpacity(this.opacity);
        }
    }

    public List<Shape> getShapes() {
        return Collections.unmodifiableList(shapes);
    }

    public int getShapeCount() {
        return shapes.size();
    }

    public boolean isEmpty() {
        return shapes.isEmpty();
    }

    /**
     * Возвращает все фигуры в порядке отрисовки (снизу вверх)
     */
    public List<Shape> getShapesInOrder() {
        return new ArrayList<>(shapes);
    }

    /**
     * Возвращает все фигуры для отрисовки (только видимые)
     */
    public List<Shape> getVisibleShapes() {
        if (!visible) return Collections.emptyList();

        List<Shape> visibleShapes = new ArrayList<>();
        for (Shape shape : shapes) {
            visibleShapes.add(shape);
        }
        return visibleShapes;
    }

    // ----- Поиск фигур -----

    /**
     * Находит фигуру по ID
     */
    public Shape findShapeById(String id) {
        for (Shape shape : shapes) {
            if (shape.getId().equals(id)) {
                return shape;
            }
        }
        return null;
    }

    /**
     * Находит фигуру, содержащую точку (x, y)
     * @return верхнюю фигуру (с наибольшим z-order)
     */
    public Shape findShapeAt(double x, double y) {
        // Идём сверху вниз
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if (shape.contains(x, y)) {
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
        for (Shape shape : shapes) {
            if (shape.contains(x, y)) {
                result.add(shape);
            }
        }
        return result;
    }

    /**
     * Находит все фигуры, пересекающие прямоугольник
     */
    public List<Shape> findShapesInRect(javafx.geometry.Rectangle2D rect) {
        List<Shape> result = new ArrayList<>();
        for (Shape shape : shapes) {
            if (rect.intersects(shape.getBounds())) {
                result.add(shape);
            }
        }
        return result;
    }

    // ----- Управление слушателями -----

    public void addListener(LayerChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LayerChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyShapeAdded(Shape shape) {
        for (LayerChangeListener listener : listeners) {
            listener.onShapeAdded(shape);
        }
    }

    private void notifyShapeRemoved(Shape shape) {
        for (LayerChangeListener listener : listeners) {
            listener.onShapeRemoved(shape);
        }
    }

    private void notifyShapeOrderChanged() {
        for (LayerChangeListener listener : listeners) {
            listener.onShapeOrderChanged();
        }
    }

    private void notifyVisibilityChanged(boolean visible) {
        for (LayerChangeListener listener : listeners) {
            listener.onVisibilityChanged(visible);
        }
    }

    private void notifyLockChanged(boolean locked) {
        for (LayerChangeListener listener : listeners) {
            listener.onLockChanged(locked);
        }
    }

    private void notifyNameChanged(String newName) {
        for (LayerChangeListener listener : listeners) {
            listener.onNameChanged(newName);
        }
    }

    @Override
    public String toString() {
        return String.format("Layer[id=%s, name='%s', shapes=%d, visible=%b, locked=%b]",
                id.substring(0, 8), name, shapes.size(), visible, locked);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Layer layer = (Layer) obj;
        return Objects.equals(id, layer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}