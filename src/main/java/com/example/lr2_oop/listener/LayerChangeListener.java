package com.example.lr2_oop.listener;

import com.example.lr2_oop.model.Layer;
import com.example.lr2_oop.model.Shape;

/**
 * Слушатель изменений слоёв
 */
public interface LayerChangeListener {

    void onLayerAdded(Layer layer);
    void onLayerRemoved(Layer layer);
    void onLayerOrderChanged();
    void onLayerSelected(Layer layer);
    void onShapeMovedToLayer(Shape shape, Layer fromLayer, Layer toLayer);
}