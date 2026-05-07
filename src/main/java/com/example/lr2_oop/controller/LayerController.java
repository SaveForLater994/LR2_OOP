package com.example.lr2_oop.controller;

import com.example.lr2_oop.model.Layer;
import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.service.LayerService;
import com.example.lr2_oop.service.SelectionManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер панели слоёв
 * Управляет отображением и взаимодействием со слоями
 */
public class LayerController {

    private final LayerService layerService;
    private final SelectionManager selectionManager;
    private final MainController mainController;

    private final ListView<Layer> layerListView;
    private final ContextMenu layerContextMenu;
    private final Button addLayerBtn;
    private final Button deleteLayerBtn;
    private final Button moveUpBtn;
    private final Button moveDownBtn;

    public LayerController(LayerService layerService,
                           SelectionManager selectionManager,
                           MainController mainController,
                           ListView<Layer> layerListView,
                           Button addLayerBtn,
                           Button deleteLayerBtn,
                           Button moveUpBtn,
                           Button moveDownBtn) {
        this.layerService = layerService;
        this.selectionManager = selectionManager;
        this.mainController = mainController;
        this.layerListView = layerListView;
        this.addLayerBtn = addLayerBtn;
        this.deleteLayerBtn = deleteLayerBtn;
        this.moveUpBtn = moveUpBtn;
        this.moveDownBtn = moveDownBtn;
        this.layerContextMenu = createContextMenu();

        setupListView();
        setupButtons();
        setupListeners();
    }

    private void setupListView() {
        layerListView.setCellFactory(lv -> new LayerCell());

        layerListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                Layer selected = layerListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    layerService.setActiveLayer(selected);
                }
            }
        });

        layerListView.setContextMenu(layerContextMenu);
    }

    private void setupButtons() {
        addLayerBtn.setOnAction(e -> mainController.onCreateLayer());
        deleteLayerBtn.setOnAction(e -> mainController.onDeleteLayer());
        moveUpBtn.setOnAction(e -> mainController.onMoveLayerUp());
        moveDownBtn.setOnAction(e -> mainController.onMoveLayerDown());
    }

    private void setupListeners() {
        layerService.addListener(new LayerService.LayerListener() {
            @Override
            public void onLayerListChanged(List<Layer> layers) {
                refreshLayerList();
                updateButtonsState();
            }

            @Override
            public void onActiveLayerChanged(Layer newLayer) {
                refreshLayerList();
                layerListView.getSelectionModel().select(newLayer);
                updateButtonsState();
            }

            @Override
            public void onLayerVisibilityChanged(Layer layer, boolean visible) {
                refreshLayerList();
            }

            @Override
            public void onLayerLockChanged(Layer layer, boolean locked) {
                refreshLayerList();
            }
        });
    }

    private void updateButtonsState() {
        Layer selected = layerListView.getSelectionModel().getSelectedItem();
        int layerCount = layerService.getLayerCount();
        int selectedIndex = layerService.getLayers().indexOf(selected);

        deleteLayerBtn.setDisable(selected == null || layerCount <= 1);
        moveUpBtn.setDisable(selected == null || selectedIndex >= layerCount - 1);
        moveDownBtn.setDisable(selected == null || selectedIndex <= 0);
    }

    private ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem renameItem = new MenuItem("Переименовать");
        renameItem.setOnAction(e -> renameSelectedLayer());

        MenuItem duplicateItem = new MenuItem("Дублировать");
        duplicateItem.setOnAction(e -> duplicateSelectedLayer());

        MenuItem mergeItem = new MenuItem("Объединить с предыдущим");
        mergeItem.setOnAction(e -> mergeWithPreviousLayer());

        MenuItem selectAllItem = new MenuItem("Выделить всё на слое");
        selectAllItem.setOnAction(e -> selectAllOnLayer());

        MenuItem lockItem = new MenuItem("Заблокировать/Разблокировать");
        lockItem.setOnAction(e -> toggleLockSelectedLayer());

        SeparatorMenuItem separator = new SeparatorMenuItem();

        MenuItem moveUpItem = new MenuItem("Переместить вверх");
        moveUpItem.setOnAction(e -> mainController.onMoveLayerUp());

        MenuItem moveDownItem = new MenuItem("Переместить вниз");
        moveDownItem.setOnAction(e -> mainController.onMoveLayerDown());

        SeparatorMenuItem separator2 = new SeparatorMenuItem();

        MenuItem deleteItem = new MenuItem("Удалить слой");
        deleteItem.setOnAction(e -> mainController.onDeleteLayer());

        menu.getItems().addAll(renameItem, duplicateItem, mergeItem, selectAllItem, lockItem,
                separator, moveUpItem, moveDownItem, separator2, deleteItem);

        return menu;
    }

    private void renameSelectedLayer() {
        Layer selected = layerListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Переименовать слой");
        dialog.setHeaderText("Введите новое имя слоя");
        dialog.setContentText("Имя:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                layerService.renameLayer(selected, newName);
            }
        });
    }

    private void duplicateSelectedLayer() {
        Layer selected = layerListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Layer newLayer = layerService.createLayer(selected.getName() + " (копия)");

        for (Shape shape : selected.getShapes()) {
            Shape copy = shape.copy();
            copy.setStroke(shape.getStroke().copy());
            copy.setFill(shape.getFill().copy());
            newLayer.addShape(copy);
        }

        layerService.setActiveLayer(newLayer);
    }

    private void mergeWithPreviousLayer() {
        Layer selected = layerListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        int index = layerService.getLayers().indexOf(selected);
        if (index <= 0) return;

        Layer previous = layerService.getLayers().get(index - 1);

        for (Shape shape : selected.getShapes()) {
            layerService.moveShapeToLayer(shape, previous);
        }

        layerService.deleteLayer(selected);
    }

    private void selectAllOnLayer() {
        Layer activeLayer = layerService.getActiveLayer();
        if (activeLayer != null && !activeLayer.getShapes().isEmpty()) {
            selectionManager.clearSelection();
            for (Shape shape : activeLayer.getShapes()) {
                if (!activeLayer.isLocked()) {
                    selectionManager.addToSelection(shape);
                }
            }
        }
    }

    private void toggleLockSelectedLayer() {
        Layer selected = layerListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            layerService.toggleLayerLock(selected);
        }
    }

    public void refreshLayerList() {
        layerListView.getItems().clear();
        for (Layer layer : layerService.getLayers()) {
            layerListView.getItems().add(layer);
        }
        updateButtonsState();
    }

    public void selectLayer(Layer layer) {
        if (layer != null) {
            layerListView.getSelectionModel().select(layer);
        }
    }

    /**
     * Кастомная ячейка для отображения слоя
     */
    private class LayerCell extends ListCell<Layer> {

        private final HBox content;
        private final CheckBox visibleCheckBox;
        private final CheckBox lockedCheckBox;
        private final Label nameLabel;
        private final Label countLabel;

        public LayerCell() {
            visibleCheckBox = new CheckBox();
            visibleCheckBox.setStyle("-fx-padding: 0 5 0 0;");

            lockedCheckBox = new CheckBox();
            lockedCheckBox.setStyle("-fx-padding: 0 5 0 0;");

            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-weight: bold;");

            countLabel = new Label();
            countLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 10; -fx-padding: 0 0 0 5;");

            content = new HBox(5);
            content.getChildren().addAll(visibleCheckBox, lockedCheckBox, nameLabel, countLabel);
            content.setPadding(new Insets(5, 5, 5, 5));

            visibleCheckBox.setOnAction(e -> {
                Layer layer = getItem();
                if (layer != null) {
                    layer.setVisible(visibleCheckBox.isSelected());
                }
            });

            lockedCheckBox.setOnAction(e -> {
                Layer layer = getItem();
                if (layer != null) {
                    layer.setLocked(lockedCheckBox.isSelected());
                }
            });
        }

        @Override
        protected void updateItem(Layer layer, boolean empty) {
            super.updateItem(layer, empty);

            if (empty || layer == null) {
                setGraphic(null);
                setText(null);
            } else {
                visibleCheckBox.setSelected(layer.isVisible());
                lockedCheckBox.setSelected(layer.isLocked());
                nameLabel.setText(layer.getName());
                countLabel.setText("(" + layer.getShapeCount() + ")");

                // Стиль для активного слоя
                if (layerService.getActiveLayer() == layer) {
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2a82da;");
                    content.setStyle("-fx-background-color: #e8f0fe; -fx-border-color: #2a82da; -fx-border-width: 0 0 0 2;");
                } else {
                    nameLabel.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                    content.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                }

                // Стиль для заблокированного слоя
                if (layer.isLocked()) {
                    nameLabel.setStyle("-fx-text-fill: gray;");
                    lockedCheckBox.setStyle("-fx-opacity: 1;");
                }

                setGraphic(content);
                setText(null);
            }
        }
    }
}