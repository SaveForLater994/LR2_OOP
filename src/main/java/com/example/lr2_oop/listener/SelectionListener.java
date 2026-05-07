package com.example.lr2_oop.listener;

import com.example.lr2_oop.model.Shape;
import javafx.geometry.Rectangle2D;

import java.util.Set;

/**
 * Слушатель изменений выделения фигур.
 * Используется для уведомления компонентов UI об изменениях в SelectionManager.
 */
public interface SelectionListener {

    /**
     * Вызывается при изменении набора выделенных фигур
     * (выделение/снятие выделения, очистка, выделение всех)
     *
     * @param selectedShapes текущий набор выделенных фигур (неизменяемый)
     * @param lastSelected последняя выделенная фигура, может быть null если выделение пусто
     */
    void onSelectionChanged(Set<Shape> selectedShapes, Shape lastSelected);

    /**
     * Вызывается при изменении прямоугольника выделения (рамки)
     * Используется для отрисовки рамки во время выделения мышью
     *
     * @param rect текущий прямоугольник выделения в координатах холста,
     *             null если рамка не активна
     */
    void onSelectionRectChanged(Rectangle2D rect);

    /**
     * Вызывается, когда был изменён порядок слоёв или фигура перемещена между слоями,
     * что повлияло на то, какие фигуры считаются выделенными
     *
     * @param selectedShapes обновлённый набор выделенных фигур
     */
    default void onSelectionOrderChanged(Set<Shape> selectedShapes) {
        // Пустая реализация по умолчанию
    }

    /**
     * Вызывается при изменении свойств выделенных фигур
     * (толщина, цвет, заливка, размер, поворот)
     *
     * @param changedShape фигура, у которой изменились свойства (может быть null для всех)
     * @param propertyType тип изменённого свойства
     */
    default void onSelectedPropertiesChanged(Shape changedShape, PropertyType propertyType) {
        // Пустая реализация по умолчанию
    }

    /**
     * Типы свойств для onSelectedPropertiesChanged
     */
    enum PropertyType {
        STROKE_COLOR,
        STROKE_WIDTH,
        FILL_COLOR,
        POSITION,
        SIZE,
        ROTATION,
        LAYER
    }
}