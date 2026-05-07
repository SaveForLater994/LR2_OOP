package com.example.lr2_oop.service;

import com.example.lr2_oop.command.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Сервис для управления Undo/Redo операциями
 * Объединяет функционал CommandManager и UndoRedoService
 */
public class UndoRedoService {

    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;
    private final List<UndoRedoListener> listeners;

    private int maxHistorySize;
    private boolean isExecutingCommand;
    private Command lastCommand;  // Для объединения команд

    /**
     * Слушатель изменений состояния Undo/Redo
     */
    public interface UndoRedoListener {
        void onUndoStackChanged(boolean canUndo);
        void onRedoStackChanged(boolean canRedo);
        void onCommandExecuted(Command command);
        void onCommandUndone(Command command);
        void onCommandRedone(Command command);
    }

    public UndoRedoService() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.listeners = new ArrayList<>();
        this.maxHistorySize = 100;
        this.isExecutingCommand = false;
        this.lastCommand = null;
    }

    /**
     * Выполняет команду и добавляет её в историю
     * Поддерживает объединение последовательных команд одного типа
     */
    public void executeCommand(Command command) {
        if (command == null) return;
        if (isExecutingCommand) return;

        try {
            isExecutingCommand = true;

            // Проверяем возможность объединения с предыдущей командой
            if (lastCommand != null && lastCommand.canMergeWith(command)) {
                lastCommand.mergeWith(command);
                // Обновляем историю
                undoStack.pop();
                undoStack.push(lastCommand);
                notifyUndoStackChanged();
                notifyCommandExecuted(command);
                return;
            }

            // Выполняем команду
            command.execute();

            // Добавляем в стек Undo
            undoStack.push(command);
            lastCommand = command;

            // Очищаем Redo стек (при новом действии)
            redoStack.clear();

            // Ограничиваем размер истории
            trimHistory();

            // Уведомляем слушателей
            notifyUndoStackChanged();
            notifyRedoStackChanged();
            notifyCommandExecuted(command);

        } finally {
            isExecutingCommand = false;
        }
    }

    /**
     * Отменяет последнюю операцию (Undo)
     */
    public void undo() {
        if (!canUndo()) return;

        Command command = undoStack.pop();
        lastCommand = null;  // Сбрасываем lastCommand при undo

        try {
            isExecutingCommand = true;
            command.undo();
            redoStack.push(command);

            notifyUndoStackChanged();
            notifyRedoStackChanged();
            notifyCommandUndone(command);

        } finally {
            isExecutingCommand = false;
        }
    }

    /**
     * Повторяет отменённую операцию (Redo)
     */
    public void redo() {
        if (!canRedo()) return;

        Command command = redoStack.pop();

        try {
            isExecutingCommand = true;
            command.redo();
            undoStack.push(command);
            lastCommand = command;

            notifyUndoStackChanged();
            notifyRedoStackChanged();
            notifyCommandRedone(command);

        } finally {
            isExecutingCommand = false;
        }
    }

    /**
     * Проверяет, можно ли отменить операцию
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Проверяет, можно ли повторить операцию
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Очищает всю историю
     */
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        lastCommand = null;

        notifyUndoStackChanged();
        notifyRedoStackChanged();
    }

    /**
     * Очищает Redo стек (вызывается после нового действия вне executeCommand)
     */
    public void clearRedoStack() {
        redoStack.clear();
        lastCommand = null;
        notifyRedoStackChanged();
    }

    /**
     * Сбрасывает lastCommand (например, при щелчке по холсту)
     */
    public void resetLastCommand() {
        lastCommand = null;
    }

    /**
     * Устанавливает максимальный размер истории
     */
    public void setMaxHistorySize(int maxSize) {
        this.maxHistorySize = Math.max(1, maxSize);
        trimHistory();
    }

    /**
     * Ограничивает размер стека истории
     */
    private void trimHistory() {
        while (undoStack.size() > maxHistorySize) {
            undoStack.remove(0);
        }
    }

    /**
     * Возвращает количество команд в Undo стеке
     */
    public int getUndoStackSize() {
        return undoStack.size();
    }

    /**
     * Возвращает количество команд в Redo стеке
     */
    public int getRedoStackSize() {
        return redoStack.size();
    }

    /**
     * Возвращает последнюю Undo команду (без удаления)
     */
    public Command getLastUndoCommand() {
        return undoStack.isEmpty() ? null : undoStack.peek();
    }

    /**
     * Возвращает последнюю Redo команду (без удаления)
     */
    public Command getLastRedoCommand() {
        return redoStack.isEmpty() ? null : redoStack.peek();
    }

    /**
     * Возвращает описание последней операции для UI
     */
    public String getLastOperationDescription() {
        if (!undoStack.isEmpty()) {
            return undoStack.peek().getDescription();
        }
        return "Нет операций";
    }

    // ----- Слушатели -----

    public void addListener(UndoRedoListener listener) {
        listeners.add(listener);
    }

    public void removeListener(UndoRedoListener listener) {
        listeners.remove(listener);
    }

    private void notifyUndoStackChanged() {
        boolean canUndo = canUndo();
        for (UndoRedoListener listener : listeners) {
            listener.onUndoStackChanged(canUndo);
        }
    }

    private void notifyRedoStackChanged() {
        boolean canRedo = canRedo();
        for (UndoRedoListener listener : listeners) {
            listener.onRedoStackChanged(canRedo);
        }
    }

    private void notifyCommandExecuted(Command command) {
        for (UndoRedoListener listener : listeners) {
            listener.onCommandExecuted(command);
        }
    }

    private void notifyCommandUndone(Command command) {
        for (UndoRedoListener listener : listeners) {
            listener.onCommandUndone(command);
        }
    }

    private void notifyCommandRedone(Command command) {
        for (UndoRedoListener listener : listeners) {
            listener.onCommandRedone(command);
        }
    }
}