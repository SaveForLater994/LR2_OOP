package com.example.lr2_oop.model.shapes;

import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

public class PolylineShape extends Shape {

    // Список отрезков, составляющих ломаную
    private List<LineShape> segments;

    // Кэш абсолютных координат точек
    private transient List<Point2D> cachedPoints;
    private transient boolean cacheValid = false;

    private transient Group javafxGroup;

    // ----- Конструкторы -----

    /**
     * Конструктор ломаной по списку точек (абсолютные координаты)
     * @param points список точек в порядке соединения
     */
    public PolylineShape(List<Point2D> points) {
        super();

        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("Ломаная должна содержать минимум 2 точки");
        }

        // Вычисляем центр как среднее арифметическое всех точек
        double centerX = computeCenterX(points);
        double centerY = computeCenterY(points);

        // Создаём отрезки в локальных координатах (относительно центра)
        this.segments = new ArrayList<>();
        this.cachedPoints = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);

            // Переводим в локальные координаты (относительно центра)
            double localX1 = p1.getX() - centerX;
            double localY1 = p1.getY() - centerY;
            double localX2 = p2.getX() - centerX;
            double localY2 = p2.getY() - centerY;

            // Создаём отрезок в локальных координатах
            LineShape segment = new LineShape(localX1, localY1, localX2, localY2);
            segments.add(segment);
        }

        // Устанавливаем центр фигуры
        this.transform.setPosition(centerX, centerY);

        // Вычисляем размеры для bounds
        updateBoundsFromPoints(points);

        // Копируем стили на все сегменты
        updateSegmentsStyles();

        updateNode();
        invalidateCache();
    }

    /**
     * Конструктор ломаной по списку точек с заданными стилями
     */
    public PolylineShape(List<Point2D> points, StrokeProperty stroke, FillProperty fill) {
        this(points);
        this.stroke = stroke;
        this.fill = fill;
        updateSegmentsStyles();
        updateNode();
    }

    /**
     * Конструктор ломаной по массиву координат (x1, y1, x2, y2, ...)
     */
    public PolylineShape(double... coordinates) {
        this(toPointsList(coordinates));
    }

    // Приватный конструктор для copy()
    private PolylineShape(PolylineShape original) {
        super();

        // Копируем все сегменты
        this.segments = new ArrayList<>();
        for (LineShape segment : original.segments) {
            this.segments.add((LineShape) segment.copy());
        }

        this.stroke = original.stroke.copy();
        this.fill = original.fill.copy();
        this.transform = original.transform.copy();
        this.id = java.util.UUID.randomUUID().toString();
        this.isSelected = false;

        updateSegmentsStyles();
        invalidateCache();
        updateNode();
    }

    // ----- Вспомогательные методы -----

    private static List<Point2D> toPointsList(double... coordinates) {
        if (coordinates.length < 4 || coordinates.length % 2 != 0) {
            throw new IllegalArgumentException("Нечётное количество координат или меньше 4 значений");
        }

        List<Point2D> points = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i += 2) {
            points.add(new Point2D(coordinates[i], coordinates[i + 1]));
        }
        return points;
    }

    private double computeCenterX(List<Point2D> points) {
        double sumX = 0;
        for (Point2D point : points) {
            sumX += point.getX();
        }
        return sumX / points.size();
    }

    private double computeCenterY(List<Point2D> points) {
        double sumY = 0;
        for (Point2D point : points) {
            sumY += point.getY();
        }
        return sumY / points.size();
    }

    private void updateBoundsFromPoints(List<Point2D> points) {
        if (points.isEmpty()) return;

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point2D point : points) {
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
        }

        transform.setSize(maxX - minX, maxY - minY);
    }

    private void updateSegmentsStyles() {
        for (LineShape segment : segments) {
            segment.setStroke(stroke);
            segment.setFill(fill);
        }
    }

    private void invalidateCache() {
        cacheValid = false;
        cachedPoints = null;
    }

    /**
     * Получает абсолютные координаты всех точек (с учётом трансформаций)
     */
    private List<Point2D> getAbsolutePoints() {
        if (cacheValid && cachedPoints != null) {
            return cachedPoints;
        }

        List<Point2D> points = new ArrayList<>();
        double centerX = transform.getX();
        double centerY = transform.getY();
        double rotateRad = Math.toRadians(transform.getRotate());
        double scaleX = transform.getScaleX();
        double scaleY = transform.getScaleY();

        // Добавляем первую точку
        if (segments.isEmpty()) {
            return points;
        }

        // Получаем первую точку первого сегмента
        Point2D firstPoint = getTransformedPoint(
                segments.get(0).getStartX(), segments.get(0).getStartY(),
                centerX, centerY, rotateRad, scaleX, scaleY
        );
        points.add(firstPoint);

        // Добавляем конечные точки всех сегментов
        for (LineShape segment : segments) {
            Point2D endPoint = getTransformedPoint(
                    segment.getEndX(), segment.getEndY(),
                    centerX, centerY, rotateRad, scaleX, scaleY
            );
            points.add(endPoint);
        }

        cachedPoints = points;
        cacheValid = true;
        return points;
    }

    private Point2D getTransformedPoint(double localX, double localY,
                                        double centerX, double centerY,
                                        double rotateRad, double scaleX, double scaleY) {
        // Применяем масштаб
        double scaledX = localX * scaleX;
        double scaledY = localY * scaleY;

        // Применяем поворот
        double rotatedX = scaledX;
        double rotatedY = scaledY;
        if (rotateRad != 0) {
            double cos = Math.cos(rotateRad);
            double sin = Math.sin(rotateRad);
            rotatedX = scaledX * cos - scaledY * sin;
            rotatedY = scaledX * sin + scaledY * cos;
        }

        // Применяем смещение к центру
        return new Point2D(centerX + rotatedX, centerY + rotatedY);
    }

    // ----- Обновление JavaFX узла -----

    @Override
    protected void updateNode() {
        if (javafxGroup == null) {
            javafxGroup = new Group();
        }

        javafxGroup.getChildren().clear();

        // Обновляем каждый сегмент и добавляем в группу
        for (LineShape segment : segments) {
            // Временно отключаем выделение у сегментов (выделяется вся ломаная)
            boolean wasSelected = segment.isSelected();
            segment.setSelected(false);

            // Применяем трансформации к сегменту
            segment.getTransform().setPosition(transform.getX(), transform.getY());
            segment.getTransform().setRotate(transform.getRotate());
            segment.getTransform().setScale(transform.getScaleX(), transform.getScaleY());

            segment.updateNode();
            javafxGroup.getChildren().add(segment.getNode());

            segment.setSelected(wasSelected);
        }

        // Эффект выделения для всей ломаной
        if (isSelected) {
            // Добавляем эффект выделения (можно обвести все сегменты)
            for (javafx.scene.Node node : javafxGroup.getChildren()) {
                if (node instanceof Line) {
                    Line line = (Line) node;
                    line.setStrokeWidth(stroke.getWidth() + 3);
                    line.getStrokeDashArray().addAll(5.0, 5.0);
                }
            }
        }
    }

    @Override
    public Node getNode() {
        updateNode();
        return javafxGroup;
    }

    // ----- Реализация абстрактных методов -----

    @Override
    public boolean contains(double x, double y) {
        // Проверяем попадание на любой из сегментов
        List<Point2D> points = getAbsolutePoints();

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);

            double distance = pointToSegmentDistance(x, y, p1.getX(), p1.getY(), p2.getX(), p2.getY());
            double hitRadius = Math.max(stroke.getWidth() / 2, 5.0);

            if (distance <= hitRadius) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Rectangle2D getBounds() {
        List<Point2D> points = getAbsolutePoints();

        if (points.isEmpty()) {
            return new Rectangle2D(0, 0, 0, 0);
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point2D point : points) {
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
        }

        double padding = stroke.getWidth() / 2;

        return new Rectangle2D(
                minX - padding,
                minY - padding,
                (maxX - minX) + padding * 2,
                (maxY - minY) + padding * 2
        );
    }

    @Override
    public void translate(double dx, double dy) {
        transform.translate(dx, dy);
        invalidateCache();
        updateNode();
    }

    @Override
    public void scale(double factor) {
        // Масштабируем локальные координаты всех сегментов
        for (LineShape segment : segments) {
            double startX = segment.getStartX() * factor;
            double startY = segment.getStartY() * factor;
            double endX = segment.getEndX() * factor;
            double endY = segment.getEndY() * factor;

            segment.setStartPoint(startX, startY);
            segment.setEndPoint(endX, endY);
        }

        // Обновляем размер
        List<Point2D> points = getAbsolutePoints();
        updateBoundsFromPoints(points);

        invalidateCache();
        updateNode();
    }

    @Override
    public void rotate(double deltaAngle) {
        transform.rotate(deltaAngle);
        invalidateCache();
        updateNode();
    }

    @Override
    public void setRotate(double angle) {
        transform.setRotate(angle);
        invalidateCache();
        updateNode();
    }

    @Override
    public Shape copy() {
        return new PolylineShape(this);
    }

    // ----- Дополнительные методы -----

    /**
     * Вычисляет расстояние от точки до отрезка
     */
    private double pointToSegmentDistance(double px, double py,
                                          double ax, double ay,
                                          double bx, double by) {
        double abX = bx - ax;
        double abY = by - ay;
        double apX = px - ax;
        double apY = py - ay;

        double dot = apX * abX + apY * abY;
        double abLenSq = abX * abX + abY * abY;

        if (abLenSq == 0) {
            return Math.hypot(apX, apY);
        }

        double t = dot / abLenSq;

        if (t < 0) {
            return Math.hypot(apX, apY);
        } else if (t > 1) {
            double bpX = px - bx;
            double bpY = py - by;
            return Math.hypot(bpX, bpY);
        } else {
            double closestX = ax + t * abX;
            double closestY = ay + t * abY;
            return Math.hypot(px - closestX, py - closestY);
        }
    }

    /**
     * Возвращает количество сегментов в ломаной
     */
    public int getSegmentCount() {
        return segments.size();
    }

    /**
     * Возвращает количество точек в ломаной
     */
    public int getPointCount() {
        return segments.size() + 1;
    }

    /**
     * Возвращает все точки ломаной (абсолютные координаты)
     */
    public List<Point2D> getPoints() {
        return new ArrayList<>(getAbsolutePoints());
    }

    /**
     * Добавляет точку в конец ломаной
     */
    public void addPoint(double x, double y) {
        List<Point2D> points = getAbsolutePoints();
        points.add(new Point2D(x, y));

        // Перестраиваем ломаную с новым центром
        rebuildFromPoints(points);
    }

    /**
     * Вставляет точку в указанную позицию
     */
    public void insertPoint(int index, double x, double y) {
        List<Point2D> points = getAbsolutePoints();
        points.add(index, new Point2D(x, y));

        rebuildFromPoints(points);
    }

    /**
     * Удаляет точку по индексу
     */
    public void removePoint(int index) {
        if (getPointCount() <= 2) {
            throw new IllegalStateException("Ломаная должна содержать минимум 2 точки");
        }

        List<Point2D> points = getAbsolutePoints();
        points.remove(index);

        rebuildFromPoints(points);
    }

    /**
     * Перестраивает ломаную из нового списка точек
     */
    private void rebuildFromPoints(List<Point2D> points) {
        if (points.size() < 2) {
            throw new IllegalArgumentException("Ломаная должна содержать минимум 2 точки");
        }

        // Вычисляем новый центр
        double centerX = computeCenterX(points);
        double centerY = computeCenterY(points);

        // Создаём новые сегменты
        List<LineShape> newSegments = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);

            double localX1 = p1.getX() - centerX;
            double localY1 = p1.getY() - centerY;
            double localX2 = p2.getX() - centerX;
            double localY2 = p2.getY() - centerY;

            LineShape segment = new LineShape(localX1, localY1, localX2, localY2);
            segment.setStroke(stroke);
            segment.setFill(fill);
            newSegments.add(segment);
        }

        this.segments = newSegments;
        this.transform.setPosition(centerX, centerY);
        updateBoundsFromPoints(points);

        invalidateCache();
        updateNode();
    }

    @Override
    public void setStroke(StrokeProperty stroke) {
        super.setStroke(stroke);
        updateSegmentsStyles();
        updateNode();
    }

    @Override
    public void setFill(FillProperty fill) {
        super.setFill(fill);
        updateSegmentsStyles();
        updateNode();
    }

    @Override
    public String toString() {
        return String.format("PolylineShape[id=%s, points=%d, center=(%.1f,%.1f)]",
                id.substring(0, 8), getPointCount(), getCenterX(), getCenterY());
    }
}