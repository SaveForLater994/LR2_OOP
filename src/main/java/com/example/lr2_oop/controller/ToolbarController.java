package com.example.lr2_oop.controller;

import com.example.lr2_oop.controller.MainController.ToolType;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

/**
 * Контроллер панели инструментов
 * Управляет кнопками инструментов, выбором цвета и толщины линии
 */
public class ToolbarController {

    private final MainController mainController;

    // Кнопки инструментов
    private final ToggleButton selectBtn;
    private final ToggleButton lineBtn;
    private final ToggleButton polylineBtn;
    private final ToggleButton ellipseBtn;
    private final ToggleButton rectangleBtn;
    private final ToggleButton polygonBtn;

    // Группа для кнопок инструментов
    private final ToggleGroup toolGroup;

    // Элементы управления стилями
    private final ColorPicker strokeColorPicker;
    private final ColorPicker fillColorPicker;
    private final Slider strokeWidthSlider;
    private final Label strokeWidthLabel;
    private final CheckBox transparentFillCheckBox;

    // Кнопки действий
    private final Button undoBtn;
    private final Button redoBtn;
    private final Button copyBtn;
    private final Button cutBtn;
    private final Button pasteBtn;
    private final Button deleteBtn;

    // Кнопки управления слоями
    private final Button addLayerBtn;
    private final Button deleteLayerBtn;
    private final Button layerUpBtn;
    private final Button layerDownBtn;

    public ToolbarController(MainController mainController,
                             ToggleButton selectBtn,
                             ToggleButton lineBtn,
                             ToggleButton polylineBtn,
                             ToggleButton ellipseBtn,
                             ToggleButton rectangleBtn,
                             ToggleButton polygonBtn,
                             ColorPicker strokeColorPicker,
                             ColorPicker fillColorPicker,
                             Slider strokeWidthSlider,
                             Label strokeWidthLabel,
                             CheckBox transparentFillCheckBox,
                             Button undoBtn,
                             Button redoBtn,
                             Button copyBtn,
                             Button cutBtn,
                             Button pasteBtn,
                             Button deleteBtn,
                             Button addLayerBtn,
                             Button deleteLayerBtn,
                             Button layerUpBtn,
                             Button layerDownBtn) {

        this.mainController = mainController;
        this.selectBtn = selectBtn;
        this.lineBtn = lineBtn;
        this.polylineBtn = polylineBtn;
        this.ellipseBtn = ellipseBtn;
        this.rectangleBtn = rectangleBtn;
        this.polygonBtn = polygonBtn;
        this.strokeColorPicker = strokeColorPicker;
        this.fillColorPicker = fillColorPicker;
        this.strokeWidthSlider = strokeWidthSlider;
        this.strokeWidthLabel = strokeWidthLabel;
        this.transparentFillCheckBox = transparentFillCheckBox;
        this.undoBtn = undoBtn;
        this.redoBtn = redoBtn;
        this.copyBtn = copyBtn;
        this.cutBtn = cutBtn;
        this.pasteBtn = pasteBtn;
        this.deleteBtn = deleteBtn;
        this.addLayerBtn = addLayerBtn;
        this.deleteLayerBtn = deleteLayerBtn;
        this.layerUpBtn = layerUpBtn;
        this.layerDownBtn = layerDownBtn;

        this.toolGroup = new ToggleGroup();

        setupToolButtons();
        setupStyleControls();
        setupActionButtons();
    }

    private void setupToolButtons() {
        // Добавляем кнопки в группу
        selectBtn.setToggleGroup(toolGroup);
        lineBtn.setToggleGroup(toolGroup);
        polylineBtn.setToggleGroup(toolGroup);
        ellipseBtn.setToggleGroup(toolGroup);
        rectangleBtn.setToggleGroup(toolGroup);
        polygonBtn.setToggleGroup(toolGroup);

        // Устанавливаем SELECT по умолчанию
        selectBtn.setSelected(true);

        // Обработчики выбора инструмента
        selectBtn.setOnAction(e -> mainController.setToolSelect());
        lineBtn.setOnAction(e -> mainController.setToolLine());
        polylineBtn.setOnAction(e -> mainController.setToolPolyline());
        ellipseBtn.setOnAction(e -> mainController.setToolEllipse());
        rectangleBtn.setOnAction(e -> mainController.setToolRectangle());
        polygonBtn.setOnAction(e -> mainController.setToolPolygon());
    }

    private void setupStyleControls() {
        // Настройка ползунка толщины линии
        strokeWidthSlider.setMin(0.5);
        strokeWidthSlider.setMax(20.0);
        strokeWidthSlider.setValue(2.0);
        strokeWidthSlider.setShowTickLabels(true);
        strokeWidthSlider.setShowTickMarks(true);
        strokeWidthSlider.setMajorTickUnit(5.0);
        strokeWidthSlider.setMinorTickCount(4);

        strokeWidthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            strokeWidthLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });

        // Настройка прозрачной заливки
        transparentFillCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                fillColorPicker.setDisable(true);
                mainController.updateFillProperty(new FillProperty(true));
            } else {
                fillColorPicker.setDisable(false);
                mainController.updateFillProperty(new FillProperty(fillColorPicker.getValue()));
            }
        });

        // Обработчики изменения стилей
        strokeColorPicker.setOnAction(e -> {
            mainController.updateStrokeProperty(
                    new StrokeProperty(strokeWidthSlider.getValue(), strokeColorPicker.getValue())
            );
        });

        fillColorPicker.setOnAction(e -> {
            if (!transparentFillCheckBox.isSelected()) {
                mainController.updateFillProperty(
                        new FillProperty(fillColorPicker.getValue())
                );
            }
        });
    }

    private void setupActionButtons() {
        undoBtn.setOnAction(e -> mainController.onUndo());
        redoBtn.setOnAction(e -> mainController.onRedo());
        copyBtn.setOnAction(e -> mainController.onCopy());
        cutBtn.setOnAction(e -> mainController.onCut());
        pasteBtn.setOnAction(e -> mainController.onPaste());
        deleteBtn.setOnAction(e -> mainController.onDelete());

        addLayerBtn.setOnAction(e -> mainController.onCreateLayer());
        deleteLayerBtn.setOnAction(e -> mainController.onDeleteLayer());
        layerUpBtn.setOnAction(e -> mainController.onMoveLayerUp());
        layerDownBtn.setOnAction(e -> mainController.onMoveLayerDown());
    }

    // ----- Методы для обновления UI состояния -----

    public void updateUndoRedoState(boolean canUndo, boolean canRedo) {
        undoBtn.setDisable(!canUndo);
        redoBtn.setDisable(!canRedo);
    }

    public void updateSelectionState(boolean hasSelection) {
        copyBtn.setDisable(!hasSelection);
        cutBtn.setDisable(!hasSelection);
        deleteBtn.setDisable(!hasSelection);
    }

    public void updateLayerButtonsState(boolean canDeleteLayer, boolean canMoveUp, boolean canMoveDown) {
        deleteLayerBtn.setDisable(!canDeleteLayer);
        layerUpBtn.setDisable(!canMoveUp);
        layerDownBtn.setDisable(!canMoveDown);
    }

    public void updateStyleControls(StrokeProperty stroke, FillProperty fill) {
        if (stroke != null) {
            strokeColorPicker.setValue(stroke.getColor());
            strokeWidthSlider.setValue(stroke.getWidth());
        }

        if (fill != null) {
            if (fill.isTransparent()) {
                transparentFillCheckBox.setSelected(true);
            } else {
                transparentFillCheckBox.setSelected(false);
                fillColorPicker.setValue(fill.getColor());
            }
        }
    }

    public void setTool(ToolType tool) {
        switch (tool) {
            case SELECT -> selectBtn.setSelected(true);
            case LINE -> lineBtn.setSelected(true);
            case POLYLINE -> polylineBtn.setSelected(true);
            case ELLIPSE -> ellipseBtn.setSelected(true);
            case RECTANGLE -> rectangleBtn.setSelected(true);
            case POLYGON -> polygonBtn.setSelected(true);
        }
    }

    public ToolType getCurrentTool() {
        if (selectBtn.isSelected()) return ToolType.SELECT;
        if (lineBtn.isSelected()) return ToolType.LINE;
        if (polylineBtn.isSelected()) return ToolType.POLYLINE;
        if (ellipseBtn.isSelected()) return ToolType.ELLIPSE;
        if (rectangleBtn.isSelected()) return ToolType.RECTANGLE;
        if (polygonBtn.isSelected()) return ToolType.POLYGON;
        return ToolType.SELECT;
    }
}