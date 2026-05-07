package com.example.lr2_oop.service;

import com.example.lr2_oop.model.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с буфером обмена (копирование/вставка фигур)
 */
public class ClipboardService {

    private List<Shape> clipboardShapes;
    private final List<ClipboardListener> listeners;

    /**
     * Слушатель изменений буфера обмена
     */
    public interface ClipboardListener {
        void onClipboardChanged(List<Shape> shapes);
        void onShapesCopied(int count);
        void onShapesCut(int count);
        void onShapesPasted(int count);
    }

    public ClipboardService() {
        this.clipboardShapes = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * Копирует фигуры в буфер обмена
     * @param shapes фигуры для копирования
     */
    public void copy(List<Shape> shapes) {
        if (shapes == null || shapes.isEmpty()) return;

        clipboardShapes.clear();

        for (Shape shape : shapes) {
            clipboardShapes.add(shape.copy());
        }

        notifyClipboardChanged();
        notifyShapesCopied(shapes.size());
    }

    /**
     * Копирует одну фигуру в буфер обмена
     */
    public void copy(Shape shape) {
        if (shape == null) return;
        List<Shape> list = new ArrayList<>();
        list.add(shape);
        copy(list);
    }

    /**
     * Вырезает фигуры (копирует и удаляет оригиналы)
     * @param shapes фигуры для вырезания
     * @return список удалённых фигур
     */
    public List<Shape> cut(List<Shape> shapes) {
        if (shapes == null || shapes.isEmpty()) return new ArrayList<>();

        copy(shapes);

        List<Shape> deletedShapes = new ArrayList<>(shapes);

        // Удаляем оригиналы (фактическое удаление должен выполнять вызывающий код)
        notifyShapesCut(shapes.size());

        return deletedShapes;
    }

    /**
     * Вырезает одну фигуру
     */
    public Optional<Shape> cut(Shape shape) {
        if (shape == null) return Optional.empty();

        List<Shape> list = new ArrayList<>();
        list.add(shape);
        List<Shape> deleted = cut(list);

        return deleted.isEmpty() ? Optional.empty() : Optional.of(deleted.get(0));
    }

    /**
     * Вставляет фигуры из буфера обмена
     * @param offsetX смещение по X для вставляемых фигур
     * @param offsetY смещение по Y для вставляемых фигур
     * @return копии фигур из буфера обмена
     */
    public List<Shape> paste(double offsetX, double offsetY) {
        if (clipboardShapes.isEmpty()) return new ArrayList<>();

        List<Shape> pastedShapes = new ArrayList<>();

        for (Shape shape : clipboardShapes) {
            Shape copy = shape.copy();

            // Смещаем вставленную фигуру
            copy.translate(offsetX, offsetY);

            pastedShapes.add(copy);
        }

        notifyShapesPasted(pastedShapes.size());

        return pastedShapes;
    }

    /**
     * Вставляет фигуры без смещения
     */
    public List<Shape> paste() {
        return paste(10, 10); // Стандартное смещение 10 пикселей
    }

    /**
     * Проверяет, есть ли данные в буфере обмена
     */
    public boolean hasContent() {
        return !clipboardShapes.isEmpty();
    }

    /**
     * Очищает буфер обмена
     */
    public void clear() {
        clipboardShapes.clear();
        notifyClipboardChanged();
    }

    /**
     * Возвращает копию данных из буфера обмена
     */
    public List<Shape> getClipboardContent() {
        List<Shape> copy = new ArrayList<>();
        for (Shape shape : clipboardShapes) {
            copy.add(shape.copy());
        }
        return copy;
    }

    /**
     * Возвращает количество фигур в буфере обмена
     */
    public int getClipboardSize() {
        return clipboardShapes.size();
    }

    /**
     * Возвращает информацию о содержимом буфера
     */
    public String getClipboardInfo() {
        if (clipboardShapes.isEmpty()) {
            return "Буфер обмена пуст";
        }

        int count = clipboardShapes.size();
        String types = clipboardShapes.stream()
                .map(s -> s.getClass().getSimpleName().replace("Shape", ""))
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return String.format("Скопировано: %d фигур (%s)", count, types);
    }

    // ----- Слушатели -----

    public void addListener(ClipboardListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ClipboardListener listener) {
        listeners.remove(listener);
    }

    private void notifyClipboardChanged() {
        for (ClipboardListener listener : listeners) {
            listener.onClipboardChanged(getClipboardContent());
        }
    }

    private void notifyShapesCopied(int count) {
        for (ClipboardListener listener : listeners) {
            listener.onShapesCopied(count);
        }
    }

    private void notifyShapesCut(int count) {
        for (ClipboardListener listener : listeners) {
            listener.onShapesCut(count);
        }
    }

    private void notifyShapesPasted(int count) {
        for (ClipboardListener listener : listeners) {
            listener.onShapesPasted(count);
        }
    }
}