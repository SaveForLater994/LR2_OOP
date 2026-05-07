package com.example.lr2_oop.command;

/**
 * Базовый интерфейс для всех команд (паттерн Command)
 * Используется для реализации Undo/Redo
 */
public interface Command {

    /**
     * Выполняет команду
     */
    void execute();

    /**
     * Отменяет команду (Undo)
     */
    void undo();

    /**
     * Повторяет команду (Redo)
     * По умолчанию просто вызывает execute()
     */
    default void redo() {
        execute();
    }

    /**
     * Возвращает описание команды для UI
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * Проверяет, можно ли объединить эту команду с другой
     * Используется для оптимизации (например, несколько перемещений подряд)
     */
    default boolean canMergeWith(Command other) {
        return false;
    }

    /**
     * Объединяет эту команду с другой
     */
    default void mergeWith(Command other) {
        // По умолчанию не поддерживается
    }
}