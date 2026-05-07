package com.example.lr2_oop.controller;

import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.model.shapes.LineShape;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import com.example.lr2_oop.service.SelectionManager;
import com.example.lr2_oop.service.UndoRedoService;
import com.example.lr2_oop.command.ChangeStyleCommand;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Set;

/**
 * Контроллер панели свойств
 * Отображает и позволяет изменять свойства выделенных фигур
 */
public class PropertiesController {

    private final SelectionManager selectionManager;
    private final UndoRedoService undoRedoService;

    private final VBox propertiesPanel;
    private final Label titleLabel;

    // Элементы управления обводкой
    private ColorPicker strokeColorPicker;
    private Slider strokeWidthSlider;
    private Label strokeWidthLabel;
    private CheckBox dashedCheckBox;

    // Элементы управления заливкой
    private ColorPicker fillColorPicker;
    private CheckBox transparentFillCheckBox;

    // Элементы управления трансформациями
    private TextField xField;
    private TextField yField;
    private TextField widthField;
    private TextField heightField;
    private TextField rotateField;

    private Button applyButton;

    private Shape currentShape;
    private Set<Shape> currentSelection;
    private boolean isMultiSelection;

    public PropertiesController(SelectionManager selectionManager,
                                UndoRedoService undoRedoService,
                                VBox propertiesPanel) {
        this.selectionManager = selectionManager;
        this.undoRedoService = undoRedoService;
        this.propertiesPanel = propertiesPanel;
        this.titleLabel = new Label("Свойства");

        createPropertiesPanel();

        selectionManager.addListener(new com.example.lr2_oop.listener.SelectionListener() {
            @Override
            public void onSelectionChanged(Set<Shape> selectedShapes, Shape lastSelected) {
                updateProperties(selectedShapes, lastSelected);
            }

            @Override
            public void onSelectionRectChanged(javafx.geometry.Rectangle2D rect) {}
        });
    }

    private void createPropertiesPanel() {
        propertiesPanel.getChildren().clear();
        propertiesPanel.setSpacing(10);
        propertiesPanel.setPadding(new Insets(10));
        propertiesPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 0 1;");

        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        propertiesPanel.getChildren().add(titleLabel);

        // ----- Обводка -----
        Label strokeSection = new Label("Обводка");
        strokeSection.setStyle("-fx-font-weight: bold; -fx-underline: true; -fx-font-size: 12px;");

        strokeColorPicker = new ColorPicker(Color.BLACK);
        strokeColorPicker.setPrefWidth(150);

        strokeWidthSlider = new Slider(0.5, 20, 2);
        strokeWidthSlider.setShowTickLabels(true);
        strokeWidthSlider.setShowTickMarks(true);
        strokeWidthSlider.setMajorTickUnit(5);
        strokeWidthSlider.setMinorTickCount(4);

        strokeWidthLabel = new Label();
        strokeWidthLabel.textProperty().bind(
                strokeWidthSlider.valueProperty().asString("%.1f")
        );

        dashedCheckBox = new CheckBox("Пунктирная");

        GridPane strokeGrid = new GridPane();
        strokeGrid.setHgap(10);
        strokeGrid.setVgap(5);
        strokeGrid.addRow(0, new Label("Цвет:"), strokeColorPicker);
        strokeGrid.addRow(1, new Label("Толщина:"), strokeWidthSlider, strokeWidthLabel);
        strokeGrid.addRow(2, new Label("Стиль:"), dashedCheckBox);

        // ----- Заливка -----
        Label fillSection = new Label("Заливка");
        fillSection.setStyle("-fx-font-weight: bold; -fx-underline: true; -fx-font-size: 12px;");

        fillColorPicker = new ColorPicker(Color.WHITE);
        fillColorPicker.setPrefWidth(150);

        transparentFillCheckBox = new CheckBox("Прозрачная");

        GridPane fillGrid = new GridPane();
        fillGrid.setHgap(10);
        fillGrid.setVgap(5);
        fillGrid.addRow(0, new Label("Цвет:"), fillColorPicker);
        fillGrid.addRow(1, transparentFillCheckBox);

        // ----- Трансформации -----
        Label transformSection = new Label("Трансформации");
        transformSection.setStyle("-fx-font-weight: bold; -fx-underline: true; -fx-font-size: 12px;");

        xField = new TextField();
        xField.setPrefWidth(80);
        yField = new TextField();
        yField.setPrefWidth(80);
        widthField = new TextField();
        widthField.setPrefWidth(80);
        heightField = new TextField();
        heightField.setPrefWidth(80);
        rotateField = new TextField();
        rotateField.setPrefWidth(80);

        GridPane transformGrid = new GridPane();
        transformGrid.setHgap(10);
        transformGrid.setVgap(5);
        transformGrid.addRow(0, new Label("X:"), xField, new Label("Y:"), yField);
        transformGrid.addRow(1, new Label("Ширина:"), widthField, new Label("Высота:"), heightField);
        transformGrid.addRow(2, new Label("Поворот:"), rotateField, new Label("°"), new Label());

        // ----- Кнопка применения -----
        applyButton = new Button("Применить изменения");
        applyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        applyButton.setMaxWidth(Double.MAX_VALUE);

        // Разделители
        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        Separator sep3 = new Separator();

        propertiesPanel.getChildren().addAll(
                strokeSection, strokeGrid, sep1,
                fillSection, fillGrid, sep2,
                transformSection, transformGrid, sep3,
                applyButton
        );

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        strokeColorPicker.setOnAction(e -> applyStrokeChanges());
        strokeWidthSlider.valueProperty().addListener((obs, old, val) -> applyStrokeChanges());
        dashedCheckBox.setOnAction(e -> applyStrokeChanges());

        fillColorPicker.setOnAction(e -> applyFillChanges());
        transparentFillCheckBox.setOnAction(e -> applyFillChanges());

        applyButton.setOnAction(e -> applyTransformChanges());
    }

    private void applyStrokeChanges() {
        StrokeProperty newStroke = new StrokeProperty(
                strokeWidthSlider.getValue(),
                strokeColorPicker.getValue()
        );

        if (dashedCheckBox.isSelected()) {
            newStroke.setDashArray(10.0, 5.0);
        }

        if (isMultiSelection && currentSelection != null && !currentSelection.isEmpty()) {
            ChangeStyleCommand command = new ChangeStyleCommand(
                    new ArrayList<>(currentSelection),
                    newStroke,
                    null
            );
            undoRedoService.executeCommand(command);
        } else if (currentShape != null) {
            ChangeStyleCommand command = new ChangeStyleCommand(
                    currentShape,
                    newStroke,
                    null
            );
            undoRedoService.executeCommand(command);
        }
    }

    private void applyFillChanges() {
        FillProperty newFill;
        if (transparentFillCheckBox.isSelected()) {
            newFill = new FillProperty(true);
        } else {
            newFill = new FillProperty(fillColorPicker.getValue());
        }

        if (isMultiSelection && currentSelection != null && !currentSelection.isEmpty()) {
            ChangeStyleCommand command = new ChangeStyleCommand(
                    new ArrayList<>(currentSelection),
                    null,
                    newFill
            );
            undoRedoService.executeCommand(command);
        } else if (currentShape != null && !(currentShape instanceof LineShape)) {
            ChangeStyleCommand command = new ChangeStyleCommand(
                    currentShape,
                    null,
                    newFill
            );
            undoRedoService.executeCommand(command);
        }
    }

    private void applyTransformChanges() {
        try {
            double x = Double.parseDouble(xField.getText());
            double y = Double.parseDouble(yField.getText());
            double width = Double.parseDouble(widthField.getText());
            double height = Double.parseDouble(heightField.getText());
            double rotate = Double.parseDouble(rotateField.getText());

            if (isMultiSelection && currentSelection != null && !currentSelection.isEmpty()) {
                // Находим центр масс для перемещения
                double sumX = 0, sumY = 0;
                for (Shape shape : currentSelection) {
                    sumX += shape.getCenterX();
                    sumY += shape.getCenterY();
                }
                double centerX = sumX / currentSelection.size();
                double centerY = sumY / currentSelection.size();

                double dx = x - centerX;
                double dy = y - centerY;

                for (Shape shape : currentSelection) {
                    shape.translate(dx, dy);
                }
            } else if (currentShape != null) {
                currentShape.setPosition(x, y);
                currentShape.setWidth(width);
                currentShape.setHeight(height);
                currentShape.setRotate(rotate);
            }

            // Обновляем поля после применения
            if (currentShape != null) {
                updateFieldsFromShape(currentShape);
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректные числовые значения");
        }
    }

    private void updateFieldsFromShape(Shape shape) {
        xField.setText(String.format("%.1f", shape.getCenterX()));
        yField.setText(String.format("%.1f", shape.getCenterY()));
        widthField.setText(String.format("%.1f", shape.getWidth()));
        heightField.setText(String.format("%.1f", shape.getHeight()));
        rotateField.setText(String.format("%.1f", shape.getRotate()));
    }

    private void updateProperties(Set<Shape> selectedShapes, Shape lastSelected) {
        int count = selectedShapes.size();

        if (count == 0) {
            propertiesPanel.setVisible(false);
            currentShape = null;
            currentSelection = null;
            isMultiSelection = false;
            return;
        }

        propertiesPanel.setVisible(true);

        if (count == 1 && lastSelected != null) {
            // Одиночное выделение
            isMultiSelection = false;
            currentShape = lastSelected;
            currentSelection = null;

            titleLabel.setText("Свойства: " + getShapeTypeName(lastSelected));

            // Обводка
            StrokeProperty stroke = lastSelected.getStroke();
            strokeColorPicker.setValue(stroke.getColor());
            strokeWidthSlider.setValue(stroke.getWidth());
            dashedCheckBox.setSelected(stroke.isDashed());

            // Заливка
            FillProperty fill = lastSelected.getFill();
            if (fill.isTransparent()) {
                transparentFillCheckBox.setSelected(true);
                fillColorPicker.setDisable(true);
            } else {
                transparentFillCheckBox.setSelected(false);
                fillColorPicker.setDisable(false);
                fillColorPicker.setValue(fill.getColor());
            }

            // Трансформации
            updateFieldsFromShape(lastSelected);

            // Включаем поля трансформаций
            xField.setDisable(false);
            yField.setDisable(false);
            widthField.setDisable(false);
            heightField.setDisable(false);
            rotateField.setDisable(false);
            applyButton.setDisable(false);

            // Блокировка заливки для линии
            boolean isLine = lastSelected instanceof LineShape;
            fillColorPicker.setDisable(isLine || fill.isTransparent());
            transparentFillCheckBox.setDisable(isLine);

        } else {
            // Мультиселект
            isMultiSelection = true;
            currentShape = null;
            currentSelection = selectedShapes;

            titleLabel.setText("Свойства: " + count + " фигур");

            // Проверяем общие значения для обводки
            StrokeProperty firstStroke = selectedShapes.iterator().next().getStroke();
            boolean sameStroke = selectedShapes.stream()
                    .allMatch(s -> s.getStroke().getColor().equals(firstStroke.getColor()) &&
                            Math.abs(s.getStroke().getWidth() - firstStroke.getWidth()) < 0.1 &&
                            s.getStroke().isDashed() == firstStroke.isDashed());

            if (sameStroke) {
                strokeColorPicker.setValue(firstStroke.getColor());
                strokeWidthSlider.setValue(firstStroke.getWidth());
                dashedCheckBox.setSelected(firstStroke.isDashed());
            } else {
                strokeColorPicker.setValue(Color.GRAY);
                strokeWidthSlider.setValue(0);
                dashedCheckBox.setSelected(false);
            }

            // Проверяем общие значения для заливки
            FillProperty firstFill = selectedShapes.iterator().next().getFill();
            boolean sameFill = selectedShapes.stream()
                    .allMatch(s -> {
                        if (s instanceof LineShape) return true;
                        return s.getFill().isTransparent() == firstFill.isTransparent() &&
                                s.getFill().getColor().equals(firstFill.getColor());
                    });

            if (sameFill && !(selectedShapes.iterator().next() instanceof LineShape)) {
                if (firstFill.isTransparent()) {
                    transparentFillCheckBox.setSelected(true);
                } else {
                    transparentFillCheckBox.setSelected(false);
                    fillColorPicker.setValue(firstFill.getColor());
                }
            } else {
                transparentFillCheckBox.setSelected(false);
                fillColorPicker.setValue(Color.GRAY);
            }

            // Для мультиселекта отключаем поля трансформаций
            xField.setDisable(true);
            yField.setDisable(true);
            widthField.setDisable(true);
            heightField.setDisable(true);
            rotateField.setDisable(true);
            applyButton.setDisable(true);

            // Очищаем поля
            xField.clear();
            yField.clear();
            widthField.clear();
            heightField.clear();
            rotateField.clear();
        }
    }

    private String getShapeTypeName(Shape shape) {
        String className = shape.getClass().getSimpleName();
        return className.replace("Shape", "");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setVisible(boolean visible) {
        propertiesPanel.setVisible(visible);
    }

    public boolean isVisible() {
        return propertiesPanel.isVisible();
    }
}