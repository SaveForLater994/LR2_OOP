package com.example.lr2_oop.listener;

import javafx.scene.input.MouseEvent;

/**
 * Слушатель событий мыши на холсте
 */
public interface MouseEventListener {

    void onMousePressed(MouseEvent event);
    void onMouseDragged(MouseEvent event);
    void onMouseReleased(MouseEvent event);
    void onMouseMoved(MouseEvent event);
    void onMouseClicked(MouseEvent event);
}