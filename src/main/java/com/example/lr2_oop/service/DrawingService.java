package com.example.lr2_oop.service;

import com.example.lr2_oop.model.DrawingCanvas;
import com.example.lr2_oop.model.Layer;
import com.example.lr2_oop.model.Shape;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

/**
 * Сервис отрисовки холста
 */
public class DrawingService {

    private final DrawingCanvas drawingCanvas;
    private final Canvas canvas;
    private GraphicsContext gc;

    public DrawingService(DrawingCanvas drawingCanvas, Canvas canvas) {
        this.drawingCanvas = drawingCanvas;
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        // Подписываемся на изменения холста
        drawingCanvas.addListener(new DrawingCanvas.CanvasChangeListener() {
            @Override
            public void onLayerAdded(Layer layer) {
                redraw();
            }

            @Override
            public void onLayerRemoved(Layer layer) {
                redraw();
            }

            @Override
            public void onActiveLayerChanged(Layer oldLayer, Layer newLayer) {
                redraw();
            }

            @Override
            public void onLayerOrderChanged() {
                redraw();
            }

            @Override
            public void onShapeAdded(Shape shape, Layer layer) {
                redraw();
            }

            @Override
            public void onShapeRemoved(Shape shape, Layer layer) {
                redraw();
            }

            @Override
            public void onRedrawRequested() {
                redraw();
            }
        });
    }

    /**
     * Полная перерисовка холста
     */
    public void redraw() {
        clearCanvas();
        drawBackground();
        drawAllShapes();
        drawTempShape();
        drawSelectionRect();
    }

    /**
     * Очищает холст
     */
    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Рисует фон
     */
    private void drawBackground() {
        gc.setFill(drawingCanvas.getBackgroundColor());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Рисует все фигуры со всех слоёв
     */
    private void drawAllShapes() {
        for (Shape shape : drawingCanvas.getAllShapesForRendering()) {
            drawShape(shape);
        }
    }

    /**
     * Рисует одну фигуру
     */
    private void drawShape(Shape shape) {
        javafx.scene.Node node = shape.getNode();

        // Если это JavaFX Shape, можно добавить на временный холст
        // или использовать GraphicsContext для рисования
        if (node instanceof javafx.scene.shape.Shape) {
            drawJavaFXShape((javafx.scene.shape.Shape) node);
        } else if (node instanceof javafx.scene.Group) {
            drawGroup((javafx.scene.Group) node);
        }
    }

    /**
     * Рисует JavaFX Shape (альтернативный способ без добавления в Scene)
     */
    private void drawJavaFXShape(javafx.scene.shape.Shape shape) {
        // Сохраняем текущее состояние
        gc.save();

        // Применяем трансформации
        gc.setTransform(new Affine());

        // Рисуем заливку
        if (shape.getFill() != null) {
            gc.setFill(shape.getFill());
            fillShape(gc, shape);
        }

        // Рисуем обводку
        if (shape.getStroke() != null) {
            gc.setStroke(shape.getStroke());
            gc.setLineWidth(shape.getStrokeWidth());
            gc.setLineDashes(shape.getStrokeDashArray().stream()
                    .mapToDouble(Double::doubleValue).toArray());
            strokeShape(gc, shape);
        }

        gc.restore();
    }

    /**
     * Рисует Group (для PolylineShape)
     */
    private void drawGroup(javafx.scene.Group group) {
        for (javafx.scene.Node node : group.getChildren()) {
            if (node instanceof javafx.scene.shape.Shape) {
                drawJavaFXShape((javafx.scene.shape.Shape) node);
            }
        }
    }

    /**
     * Заливка фигуры
     */
    private void fillShape(GraphicsContext gc, javafx.scene.shape.Shape shape) {
        if (shape instanceof javafx.scene.shape.Rectangle) {
            javafx.scene.shape.Rectangle r = (javafx.scene.shape.Rectangle) shape;
            gc.fillRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        } else if (shape instanceof javafx.scene.shape.Ellipse) {
            javafx.scene.shape.Ellipse e = (javafx.scene.shape.Ellipse) shape;
            gc.fillOval(e.getCenterX() - e.getRadiusX(),
                    e.getCenterY() - e.getRadiusY(),
                    e.getRadiusX() * 2, e.getRadiusY() * 2);
        } else if (shape instanceof javafx.scene.shape.Polygon) {
            javafx.scene.shape.Polygon p = (javafx.scene.shape.Polygon) shape;
            fillPolygon(gc, p);
        }
    }

    /**
     * Обводка фигуры
     */
    private void strokeShape(GraphicsContext gc, javafx.scene.shape.Shape shape) {
        if (shape instanceof javafx.scene.shape.Line) {
            javafx.scene.shape.Line l = (javafx.scene.shape.Line) shape;
            gc.strokeLine(l.getStartX(), l.getStartY(), l.getEndX(), l.getEndY());
        } else if (shape instanceof javafx.scene.shape.Rectangle) {
            javafx.scene.shape.Rectangle r = (javafx.scene.shape.Rectangle) shape;
            gc.strokeRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        } else if (shape instanceof javafx.scene.shape.Ellipse) {
            javafx.scene.shape.Ellipse e = (javafx.scene.shape.Ellipse) shape;
            gc.strokeOval(e.getCenterX() - e.getRadiusX(),
                    e.getCenterY() - e.getRadiusY(),
                    e.getRadiusX() * 2, e.getRadiusY() * 2);
        } else if (shape instanceof javafx.scene.shape.Polygon) {
            javafx.scene.shape.Polygon p = (javafx.scene.shape.Polygon) shape;
            strokePolygon(gc, p);
        }
    }

    /**
     * Рисует залитый многоугольник
     */
    private void fillPolygon(GraphicsContext gc, javafx.scene.shape.Polygon polygon) {
        double[] points = polygon.getPoints().stream()
                .mapToDouble(Double::doubleValue).toArray();
        gc.fillPolygon(
                getXCoords(points),
                getYCoords(points),
                points.length / 2
        );
    }

    /**
     * Рисует контур многоугольника
     */
    private void strokePolygon(GraphicsContext gc, javafx.scene.shape.Polygon polygon) {
        double[] points = polygon.getPoints().stream()
                .mapToDouble(Double::doubleValue).toArray();
        gc.strokePolygon(
                getXCoords(points),
                getYCoords(points),
                points.length / 2
        );
    }

    private double[] getXCoords(double[] points) {
        double[] x = new double[points.length / 2];
        for (int i = 0; i < x.length; i++) {
            x[i] = points[i * 2];
        }
        return x;
    }

    private double[] getYCoords(double[] points) {
        double[] y = new double[points.length / 2];
        for (int i = 0; i < y.length; i++) {
            y[i] = points[i * 2 + 1];
        }
        return y;
    }

    /**
     * Рисует временную фигуру (предпросмотр)
     */
    private void drawTempShape() {
        Shape tempShape = drawingCanvas.getTempShape();
        if (tempShape != null) {
            gc.save();
            gc.setGlobalAlpha(0.5);
            drawShape(tempShape);
            gc.restore();
        }
    }

    /**
     * Рисует рамку выделения
     */
    private void drawSelectionRect() {
        javafx.geometry.Rectangle2D rect = drawingCanvas.getSelectionRect();
        if (rect != null) {
            gc.save();
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1.5);
            gc.setLineDashes(5, 5);
            gc.strokeRect(rect.getMinX(), rect.getMinY(),
                    rect.getWidth(), rect.getHeight());
            gc.restore();
        }
    }

    /**
     * Обновляет размер холста
     */
    public void resizeCanvas(double width, double height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
        drawingCanvas.setWidth(width);
        drawingCanvas.setHeight(height);
        redraw();
    }
}