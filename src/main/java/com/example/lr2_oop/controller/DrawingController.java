package com.example.lr2_oop.controller;

import com.example.lr2_oop.model.DrawingCanvas;
import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.model.shapes.*;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import com.example.lr2_oop.service.UndoRedoService;
import com.example.lr2_oop.command.AddShapeCommand;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер для управления рисованием на холсте
 */
public class DrawingController {

    private final DrawingCanvas drawingCanvas;
    private final UndoRedoService undoRedoService;
    private final Canvas canvas;

    // Текущие стили
    private StrokeProperty currentStroke;
    private FillProperty currentFill;

    // Состояние рисования
    private Point2D startPoint;
    private Point2D currentPoint;
    private boolean isDrawing;
    private List<Point2D> polylinePoints;
    private List<Point2D> polygonPoints;

    public enum DrawTool {
        LINE, POLYLINE, ELLIPSE, RECTANGLE, POLYGON, SELECT
    }

    private DrawTool currentTool;

    public DrawingController(DrawingCanvas drawingCanvas, UndoRedoService undoRedoService, Canvas canvas) {
        this.drawingCanvas = drawingCanvas;
        this.undoRedoService = undoRedoService;
        this.canvas = canvas;
        this.currentTool = DrawTool.SELECT;
        this.currentStroke = new StrokeProperty(2.0, javafx.scene.paint.Color.BLACK);
        this.currentFill = new FillProperty(javafx.scene.paint.Color.WHITE);
        this.isDrawing = false;
    }

    public void setTool(DrawTool tool) {
        this.currentTool = tool;
        resetDrawingState();
    }

    public void setCurrentStroke(StrokeProperty stroke) {
        this.currentStroke = stroke;
    }

    public void setCurrentFill(FillProperty fill) {
        this.currentFill = fill;
    }

    public void resetDrawingState() {
        isDrawing = false;
        polylinePoints = null;
        polygonPoints = null;
        startPoint = null;
        currentPoint = null;
        drawingCanvas.clearTempShape();
    }

    public void startDrawing(double x, double y) {
        if (currentTool == DrawTool.SELECT) return;

        isDrawing = true;
        startPoint = new Point2D(x, y);
        currentPoint = new Point2D(x, y);

        if (currentTool == DrawTool.POLYLINE) {
            if (polylinePoints == null) {
                polylinePoints = new ArrayList<>();
                polylinePoints.add(startPoint);
            }
            polylinePoints.add(new Point2D(x, y));
        } else if (currentTool == DrawTool.POLYGON) {
            if (polygonPoints == null) {
                polygonPoints = new ArrayList<>();
                polygonPoints.add(startPoint);
            }
            polygonPoints.add(new Point2D(x, y));
        }
    }

    public void updateDrawing(double x, double y) {
        if (!isDrawing || currentTool == DrawTool.SELECT) return;

        currentPoint = new Point2D(x, y);

        Shape tempShape = createTempShape(x, y);
        if (tempShape != null) {
            tempShape.setStroke(currentStroke.copy());
            tempShape.setFill(currentFill.copy());
            drawingCanvas.setTempShape(tempShape);
        }
    }

    public void finishDrawing(double x, double y) {
        if (!isDrawing) return;

        if (currentTool != DrawTool.POLYLINE && currentTool != DrawTool.POLYGON) {
            createAndAddShape(startPoint.getX(), startPoint.getY(), x, y);
            isDrawing = false;
            drawingCanvas.clearTempShape();
        }
        startPoint = null;
        currentPoint = null;
    }

    public void addPolylinePoint(double x, double y) {
        if (currentTool != DrawTool.POLYLINE) return;

        if (polylinePoints == null) {
            polylinePoints = new ArrayList<>();
        }
        polylinePoints.add(new Point2D(x, y));

        // Обновляем временную фигуру
        if (polylinePoints.size() >= 2) {
            Shape tempShape = new PolylineShape(polylinePoints);
            tempShape.setStroke(currentStroke.copy());
            tempShape.setFill(currentFill.copy());
            drawingCanvas.setTempShape(tempShape);
        }
    }

    public void addPolygonPoint(double x, double y) {
        if (currentTool != DrawTool.POLYGON) return;

        if (polygonPoints == null) {
            polygonPoints = new ArrayList<>();
        }
        polygonPoints.add(new Point2D(x, y));

        // Обновляем временную фигуру
        if (polygonPoints.size() >= 2) {
            Shape tempShape = new PolygonShape(polygonPoints);
            tempShape.setStroke(currentStroke.copy());
            tempShape.setFill(currentFill.copy());
            drawingCanvas.setTempShape(tempShape);
        }
    }

    public void finishPolyline() {
        if (polylinePoints != null && polylinePoints.size() >= 2) {
            PolylineShape polyline = new PolylineShape(polylinePoints);
            polyline.setStroke(currentStroke.copy());
            polyline.setFill(currentFill.copy());
            addShape(polyline);
        }
        polylinePoints = null;
        isDrawing = false;
        drawingCanvas.clearTempShape();
    }

    public void finishPolygon() {
        if (polygonPoints != null && polygonPoints.size() >= 3) {
            PolygonShape polygon = new PolygonShape(polygonPoints);
            polygon.setStroke(currentStroke.copy());
            polygon.setFill(currentFill.copy());
            addShape(polygon);
        }
        polygonPoints = null;
        isDrawing = false;
        drawingCanvas.clearTempShape();
    }

    public void cancelDrawing() {
        isDrawing = false;
        polylinePoints = null;
        polygonPoints = null;
        startPoint = null;
        currentPoint = null;
        drawingCanvas.clearTempShape();
    }

    private Shape createTempShape(double x, double y) {
        switch (currentTool) {
            case LINE:
                return new LineShape(startPoint.getX(), startPoint.getY(), x, y);
            case ELLIPSE:
                double centerX = (startPoint.getX() + x) / 2;
                double centerY = (startPoint.getY() + y) / 2;
                double radiusX = Math.abs(x - startPoint.getX()) / 2;
                double radiusY = Math.abs(y - startPoint.getY()) / 2;
                return new EllipseShape(centerX, centerY, radiusX, radiusY);
            case RECTANGLE:
                return new RectangleShape(startPoint.getX(), startPoint.getY(), x, y);
            case POLYLINE:
                if (polylinePoints != null && !polylinePoints.isEmpty()) {
                    List<Point2D> points = new ArrayList<>(polylinePoints);
                    points.add(new Point2D(x, y));
                    return new PolylineShape(points);
                }
                break;
            case POLYGON:
                if (polygonPoints != null && !polygonPoints.isEmpty()) {
                    List<Point2D> points = new ArrayList<>(polygonPoints);
                    points.add(new Point2D(x, y));
                    return new PolygonShape(points);
                }
                break;
            default:
                break;
        }
        return null;
    }

    private void createAndAddShape(double x1, double y1, double x2, double y2) {
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
            addShape(shape);
        }
    }

    private void addShape(Shape shape) {
        AddShapeCommand command = new AddShapeCommand(drawingCanvas, shape);
        undoRedoService.executeCommand(command);
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public DrawTool getCurrentTool() {
        return currentTool;
    }

    public Point2D getStartPoint() {
        return startPoint;
    }

    public Point2D getCurrentPoint() {
        return currentPoint;
    }

    public List<Point2D> getPolylinePoints() {
        return polylinePoints != null ? new ArrayList<>(polylinePoints) : null;
    }

    public List<Point2D> getPolygonPoints() {
        return polygonPoints != null ? new ArrayList<>(polygonPoints) : null;
    }
}