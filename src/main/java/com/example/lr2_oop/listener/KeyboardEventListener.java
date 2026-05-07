package com.example.lr2_oop.listener;

import javafx.scene.input.KeyEvent;

/**
 * Слушатель событий клавиатуры
 */
public interface KeyboardEventListener {

    void onKeyPressed(KeyEvent event);
    void onKeyReleased(KeyEvent event);

    // Удобные методы для конкретных клавиш
    default void onDeletePressed() {}
    default void onCtrlZPressed() {}
    default void onCtrlYPressed() {}
    default void onArrowPressed(double dx, double dy) {}
}