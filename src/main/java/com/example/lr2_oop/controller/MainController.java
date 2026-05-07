package com.example.lr2_oop.controller;

import com.example.lr2_oop.command.*;
import com.example.lr2_oop.listener.KeyboardEventListener;
import com.example.lr2_oop.listener.MouseEventListener;
import com.example.lr2_oop.listener.SelectionListener;
import com.example.lr2_oop.model.DrawingCanvas;
import com.example.lr2_oop.model.Layer;
import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.model.shapes.*;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import com.example.lr2_oop.service.*;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainController implements SelectionListener, KeyboardEventListener, MouseEventListener {

    // Сервисы
    private final DrawingCanvas drawingCanvas;
    private final DrawingService drawingService;
    private final SelectionManager selectionManager;
    private final UndoRedoService undoRedoService;
    private final ClipboardService clipboardService;
    private final LayerService layerService;

    // UI компоненты
    private final Canvas canvas;
    private final Label statusLabel;
    private final ListView<Layer> layerListView;
    private final ColorPicker strokeColorPicker;
    private final ColorPicker fillColorPicker;
    private final Slider strokeWidthSlider;

    // Состояние рисования
    private ToolType currentTool;
    private Point2D startPoint;
    private Point2D currentPoint;
    private boolean isDrawing;
    private boolean isSelecting;

    // Временные данные для рисования
    private List<Point2D> polylinePoints;
    private List<Point2D> polygonPoints;

    // Текущие стили
    private StrokeProperty currentStroke;
    private FillProperty currentFill;

    public enum ToolType {
        SELECT, LINE, POLYLINE, ELLIPSE, RECTANGLE, POLYGON
    }

    public MainController(Canvas canvas, Label statusLabel,
                          ListView<Layer> layerListView,
                          ColorPicker strokeColorPicker,
                          ColorPicker fillColorPicker,
                          Slider strokeWidthSlider) {
        this.canvas = canvas;
        this.statusLabel = statusLabel;
        this.layerListView = layerListView;
        this.strokeColorPicker = strokeColorPicker;
        this.fillColorPicker = fillColorPicker;
        this.strokeWidthSlider = strokeWidthSlider;

        // Инициализация сервисов
        this.drawingCanvas = new DrawingCanvas(canvas.getWidth(), canvas.getHeight());
        this.drawingService = new DrawingService(drawingCanvas, canvas);
        this.selectionManager = new SelectionManager();
        this.undoRedoService = new UndoRedoService();
        this.clipboardService = new ClipboardService();
        this.layerService = new LayerService(drawingCanvas);

        // Инициализация состояния
        this.currentTool = ToolType.SELECT;
        this.isDrawing = false;
        this.isSelecting = false;

        // Инициализация стилей по умолчанию
        this.currentStroke = new StrokeProperty(2.0, Color.BLACK);
        this.currentFill = new FillProperty(Color.WHITE);

        // Настройка слоёв
        setupLayers();

        // Регистрация слушателей
        registerListeners();

        // Настройка UI
        setupUI();
    }

    private void setupLayers() {
        layerService.addListener(new LayerService.LayerListener() {
            @Override
            public void onLayerListChanged(List<Layer> layers) {
                updateLayerListView();
            }

            @Override
            public void onActiveLayerChanged(Layer newLayer) {
                updateLayerListView();
                statusLabel.setText("Активный слой: " + newLayer.getName());
            }

            @Override
            public void onLayerVisibilityChanged(Layer layer, boolean visible) {
                updateLayerListView();
                drawingService.redraw();
            }

            @Override
            public void onLayerLockChanged(Layer layer, boolean locked) {
                updateLayerListView();
            }
        });
    }

    private void registerListeners() {
        selectionManager.addListener(this);

        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
        canvas.setOnMouseMoved(this::onMouseMoved);
        canvas.setOnMouseClicked(this::onMouseClicked);
        canvas.setOnKeyPressed(this::onKeyPressed);
        canvas.setOnKeyReleased(this::onKeyReleased);
        canvas.setFocusTraversable(true);

        undoRedoService.addListener(new UndoRedoService.UndoRedoListener() {
            @Override
            public void onUndoStackChanged(boolean canUndo) {}

            @Override
            public void onRedoStackChanged(boolean canRedo) {}

            @Override
            public void onCommandExecuted(Command command) {
                statusLabel.setText("Выполнено: " + command.getDescription());
                drawingService.redraw();
            }

            @Override
            public void onCommandUndone(Command command) {
                statusLabel.setText("Отменено: " + command.getDescription());
                drawingService.redraw();
            }

            @Override
            public void onCommandRedone(Command command) {
                statusLabel.setText("Повторено: " + command.getDescription());
                drawingService.redraw();
            }
        });

        clipboardService.addListener(new ClipboardService.ClipboardListener() {
            @Override
            public void onClipboardChanged(List<Shape> shapes) {
                statusLabel.setText(clipboardService.getClipboardInfo());
            }

            @Override
            public void onShapesCopied(int count) {
                statusLabel.setText("Скопировано фигур: " + count);
            }

            @Override
            public void onShapesCut(int count) {
                statusLabel.setText("Вырезано фигур: " + count);
            }

            @Override
            public void onShapesPasted(int count) {
                statusLabel.setText("Вставлено фигур: " + count);
                drawingService.redraw();
            }
        });
    }

    private void setupUI() {
        strokeColorPicker.setValue(Color.BLACK);
        fillColorPicker.setValue(Color.WHITE);

        strokeColorPicker.setOnAction(e -> updateStrokeProperty(strokeColorPicker.getValue()));
        fillColorPicker.setOnAction(e -> updateFillProperty(fillColorPicker.getValue()));
        strokeWidthSlider.valueProperty().addListener((obs, old, val) ->
                updateStrokeProperty(strokeWidthSlider.getValue(), strokeColorPicker.getValue()));

        updateLayerListView();
    }

    // ===== НОВЫЕ МЕТОДЫ ДЛЯ ОБНОВЛЕНИЯ СТИЛЕЙ =====

    /**
     * Обновляет свойство обводки
     */
    public void updateStrokeProperty(StrokeProperty newStroke) {
        this.currentStroke = newStroke;

        if (!selectionManager.isEmpty()) {
            ChangeStyleCommand command = new ChangeStyleCommand(
                    new ArrayList<>(selectionManager.getSelectedShapes()),
                    newStroke.copy(),
                    null
            );
            undoRedoService.executeCommand(command);
        }
    }

    /**
     * Обновляет свойство обводки (перегрузка)
     */
    public void updateStrokeProperty(double width, Color color) {
        updateStrokeProperty(new StrokeProperty(width, color));
    }

    /**
     * Обновляет свойство обводки (только цвет)
     */
    public void updateStrokeProperty(Color color) {
        updateStrokeProperty(strokeWidthSlider.getValue(), color);
    }

    /**
     * Обновляет свойство заливки
     */
    public void updateFillProperty(FillProperty newFill) {
        this.currentFill = newFill;

        if (!selectionManager.isEmpty()) {
            ChangeStyleCommand command = new ChangeStyleCommand(
                    new ArrayList<>(selectionManager.getSelectedShapes()),
                    null,
                    newFill.copy()
            );
            undoRedoService.executeCommand(command);
        }
    }

    /**
     * Обновляет свойство заливки (перегрузка)
     */
    public void updateFillProperty(Color color) {
        updateFillProperty(new FillProperty(color));
    }

    /**
     * Обновляет свойство заливки на прозрачную
     */
    public void updateFillPropertyTransparent() {
        updateFillProperty(new FillProperty(true));
    }

    // ===== ОСТАЛЬНЫЕ МЕТОДЫ =====

    private void updateLayerListView() {
        layerListView.getItems().clear();
        for (Layer layer : layerService.getLayers()) {
            layerListView.getItems().add(layer);
        }
    }

    public void setTool(ToolType tool) {
        this.currentTool = tool;
        this.isDrawing = false;
        this.isSelecting = false;
        this.polylinePoints = null;
        this.polygonPoints = null;
        statusLabel.setText("Инструмент: " + tool.name());
    }

    public void setToolSelect() { setTool(ToolType.SELECT); }
    public void setToolLine() { setTool(ToolType.LINE); }
    public void setToolPolyline() { setTool(ToolType.POLYLINE); }
    public void setToolEllipse() { setTool(ToolType.ELLIPSE); }
    public void setToolRectangle() { setTool(ToolType.RECTANGLE); }
    public void setToolPolygon() { setTool(ToolType.POLYGON); }

    public void onUndo() { undoRedoService.undo(); }
    public void onRedo() { undoRedoService.redo(); }

    public void onCopy() {
        clipboardService.copy(new ArrayList<>(selectionManager.getSelectedShapes()));
    }

    public void onCut() {
        List<Shape> cutShapes = clipboardService.cut(
                new ArrayList<>(selectionManager.getSelectedShapes())
        );
        for (Shape shape : cutShapes) {
            RemoveShapeCommand command = new RemoveShapeCommand(drawingCanvas, shape);
            undoRedoService.executeCommand(command);
        }
    }

    public void onPaste() {
        List<Shape> pastedShapes = clipboardService.paste();
        Layer activeLayer = layerService.getActiveLayer();

        for (Shape shape : pastedShapes) {
            AddShapeCommand command = new AddShapeCommand(drawingCanvas, shape, activeLayer);
            undoRedoService.executeCommand(command);
        }
        selectionManager.clearSelection();
    }

    public void onDelete() {
        List<Shape> selected = new ArrayList<>(selectionManager.getSelectedShapes());
        if (!selected.isEmpty()) {
            CompositeCommand composite = new CompositeCommand("Удаление выделенных");
            for (Shape shape : selected) {
                composite.addCommand(new RemoveShapeCommand(drawingCanvas, shape));
            }
            undoRedoService.executeCommand(composite);
        }
    }

    public void onCreateLayer() {
        String name = "Слой " + (layerService.getLayerCount() + 1);
        layerService.createAndActivateLayer(name);
    }

    public void onDeleteLayer() {
        Layer selected = layerListView.getSelectionModel().getSelectedItem();
        if (selected != null && layerService.getLayerCount() > 1) {
            layerService.deleteLayer(selected);
        }
    }

    public void onMoveLayerUp() {
        Layer selected = layerListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            layerService.moveLayerUp(selected);
            drawingService.redraw();
        }
    }

    public void onMoveLayerDown() {
        Layer selected = layerListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            layerService.moveLayerDown(selected);
            drawingService.redraw();
        }
    }

    // ----- Реализация SelectionListener -----

    @Override
    public void onSelectionChanged(Set<Shape> selectedShapes, Shape lastSelected) {
        statusLabel.setText("Выбрано фигур: " + selectedShapes.size());

        if (selectedShapes.size() == 1 && lastSelected != null) {
            strokeColorPicker.setValue(lastSelected.getStroke().getColor());
            strokeWidthSlider.setValue(lastSelected.getStroke().getWidth());
            fillColorPicker.setValue(lastSelected.getFill().getColor());
        }

        drawingService.redraw();
    }

    @Override
    public void onSelectionRectChanged(Rectangle2D rect) {
        drawingCanvas.setSelectionRect(rect);
        drawingService.redraw();
    }

    // ----- Реализация MouseEventListener -----

    @Override
    public void onMousePressed(javafx.scene.input.MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;

        double x = event.getX();
        double y = event.getY();

        if (currentTool == ToolType.SELECT) {
            startSelection(event);
        } else {
            startDrawing(x, y);
        }
    }

    @Override
    public void onMouseDragged(javafx.scene.input.MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        if (currentTool == ToolType.SELECT && isSelecting) {
            updateSelectionRect(x, y);
        } else if (isDrawing) {
            updateDrawing(x, y);
        }
    }

    @Override
    public void onMouseReleased(javafx.scene.input.MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        if (currentTool == ToolType.SELECT && isSelecting) {
            finishSelection();
        } else if (isDrawing) {
            finishDrawing(x, y);
        }
    }

    @Override
    public void onMouseMoved(javafx.scene.input.MouseEvent event) {
        statusLabel.setText(String.format("(%.0f, %.0f) | Инструмент: %s",
                event.getX(), event.getY(), currentTool.name()));
    }

    @Override
    public void onMouseClicked(javafx.scene.input.MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;

        if (event.getClickCount() == 2) {
            if (currentTool == ToolType.POLYLINE && polylinePoints != null && polylinePoints.size() >= 2) {
                finishPolyline();
            } else if (currentTool == ToolType.POLYGON && polygonPoints != null && polygonPoints.size() >= 3) {
                finishPolygon();
            }
        }
    }

    // ----- Методы выделения -----

    private void startSelection(javafx.scene.input.MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        Shape clicked = drawingCanvas.findShapeAt(x, y);

        if (clicked != null) {
            selectionManager.handleShapeClick(clicked, event.isControlDown(), event.isShiftDown());
            isSelecting = false;
        } else {
            selectionManager.clearSelection();
            isSelecting = true;
            startPoint = new Point2D(x, y);
        }
    }

    private void updateSelectionRect(double x, double y) {
        if (startPoint != null) {
            Rectangle2D rect = new Rectangle2D(
                    Math.min(startPoint.getX(), x),
                    Math.min(startPoint.getY(), y),
                    Math.abs(x - startPoint.getX()),
                    Math.abs(y - startPoint.getY())
            );
            selectionManager.updateSelectionRect(rect);
        }
    }

    private void finishSelection() {
        if (startPoint != null && drawingCanvas.getSelectionRect() != null) {
            List<Shape> shapesInRect = drawingCanvas.findShapesInRect(drawingCanvas.getSelectionRect());
            selectionManager.selectInRectangle(drawingCanvas.getSelectionRect(), shapesInRect, false);
        }
        selectionManager.setMode(SelectionManager.SelectionMode.SINGLE);
        drawingCanvas.setSelectionRect(null);
        startPoint = null;
        isSelecting = false;
        drawingService.redraw();
    }

    // ----- Методы рисования -----

    private void startDrawing(double x, double y) {
        isDrawing = true;
        startPoint = new Point2D(x, y);
        currentPoint = new Point2D(x, y);

        if (currentTool == ToolType.POLYLINE) {
            if (polylinePoints == null) {
                polylinePoints = new ArrayList<>();
                polylinePoints.add(startPoint);
            }
            polylinePoints.add(new Point2D(x, y));
        } else if (currentTool == ToolType.POLYGON) {
            if (polygonPoints == null) {
                polygonPoints = new ArrayList<>();
                polygonPoints.add(startPoint);
            }
            polygonPoints.add(new Point2D(x, y));
        }
    }

    private void updateDrawing(double x, double y) {
        currentPoint = new Point2D(x, y);

        Shape tempShape = null;

        switch (currentTool) {
            case LINE:
                tempShape = new LineShape(startPoint.getX(), startPoint.getY(), x, y);
                break;
            case ELLIPSE:
                double centerX = (startPoint.getX() + x) / 2;
                double centerY = (startPoint.getY() + y) / 2;
                double radiusX = Math.abs(x - startPoint.getX()) / 2;
                double radiusY = Math.abs(y - startPoint.getY()) / 2;
                tempShape = new EllipseShape(centerX, centerY, radiusX, radiusY);
                break;
            case RECTANGLE:
                tempShape = new RectangleShape(startPoint.getX(), startPoint.getY(), x, y);
                break;
            case POLYLINE:
                if (polylinePoints != null && polylinePoints.size() >= 1) {
                    List<Point2D> points = new ArrayList<>(polylinePoints);
                    points.add(new Point2D(x, y));
                    tempShape = new PolylineShape(points);
                }
                break;
            case POLYGON:
                if (polygonPoints != null && polygonPoints.size() >= 1) {
                    List<Point2D> points = new ArrayList<>(polygonPoints);
                    points.add(new Point2D(x, y));
                    tempShape = new PolygonShape(points);
                }
                break;
            default:
                break;
        }

        if (tempShape != null) {
            tempShape.setStroke(currentStroke.copy());
            tempShape.setFill(currentFill.copy());
            drawingCanvas.setTempShape(tempShape);
        }
    }

    private void finishDrawing(double x, double y) {
        if (currentTool != ToolType.POLYLINE && currentTool != ToolType.POLYGON) {
            createShape(startPoint.getX(), startPoint.getY(), x, y);
            isDrawing = false;
            drawingCanvas.clearTempShape();
        }
        currentPoint = null;
        startPoint = null;
    }

    private void finishPolyline() {
        if (polylinePoints != null && polylinePoints.size() >= 2) {
            PolylineShape polyline = new PolylineShape(polylinePoints);
            polyline.setStroke(currentStroke.copy());
            polyline.setFill(currentFill.copy());

            AddShapeCommand command = new AddShapeCommand(drawingCanvas, polyline);
            undoRedoService.executeCommand(command);
        }
        polylinePoints = null;
        isDrawing = false;
        drawingCanvas.clearTempShape();
    }

    private void finishPolygon() {
        if (polygonPoints != null && polygonPoints.size() >= 3) {
            PolygonShape polygon = new PolygonShape(polygonPoints);
            polygon.setStroke(currentStroke.copy());
            polygon.setFill(currentFill.copy());

            AddShapeCommand command = new AddShapeCommand(drawingCanvas, polygon);
            undoRedoService.executeCommand(command);
        }
        polygonPoints = null;
        isDrawing = false;
        drawingCanvas.clearTempShape();
    }

    private void createShape(double x1, double y1, double x2, double y2) {
        Shape shape = null;

        switch (currentTool) {
            case LINE:
                shape = new LineShape(x1, y1, x2, y2);
                break;
            case ELLIPSE:
                double centerX = (x1 + x2) / 2;
                double centerY = (y1 + y2) / 2;
                double radiusX = Math.abs(x2 - x1) / 2;
                double radiusY = Math.abs(y2 - y1) / 2;
                shape = new EllipseShape(centerX, centerY, radiusX, radiusY);
                break;
            case RECTANGLE:
                shape = new RectangleShape(x1, y1, x2, y2);
                break;
            default:
                return;
        }

        if (shape != null) {
            shape.setStroke(currentStroke.copy());
            shape.setFill(currentFill.copy());

            AddShapeCommand command = new AddShapeCommand(drawingCanvas, shape);
            undoRedoService.executeCommand(command);
        }
    }

    // ----- Реализация KeyboardEventListener -----

    @Override
    public void onKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.isControlDown()) {
            switch (event.getCode()) {
                case Z -> { undoRedoService.undo(); event.consume(); }
                case Y -> { undoRedoService.redo(); event.consume(); }
                case C -> { onCopy(); event.consume(); }
                case X -> { onCut(); event.consume(); }
                case V -> { onPaste(); event.consume(); }
                case A -> { selectionManager.selectAll(drawingCanvas.getAllShapesForRendering()); event.consume(); }
                case D -> { onDelete(); event.consume(); }
                default -> {}
            }
        } else {
            double dx = 0, dy = 0;
            switch (event.getCode()) {
                case LEFT -> dx = -1;
                case RIGHT -> dx = 1;
                case UP -> dy = -1;
                case DOWN -> dy = 1;
                case DELETE -> { onDelete(); event.consume(); }
                default -> {}
            }

            if (dx != 0 || dy != 0) {
                if (!selectionManager.isEmpty()) {
                    CompositeCommand composite = new CompositeCommand("Перемещение выделенных");
                    for (Shape shape : selectionManager.getSelectedShapes()) {
                        composite.addCommand(new MoveShapeCommand(shape, dx, dy));
                    }
                    undoRedoService.executeCommand(composite);
                }
                event.consume();
            }
        }
    }

    @Override
    public void onKeyReleased(javafx.scene.input.KeyEvent event) {}
}