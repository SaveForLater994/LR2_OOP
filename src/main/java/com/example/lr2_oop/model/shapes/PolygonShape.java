package com.example.lr2_oop.model.shapes;

import com.example.lr2_oop.model.Shape;
import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

public class PolygonShape extends Shape {

    // Локальные координаты точек (относительно центра)
    private List<Point2D> localPoints;

    // Кэш абсолютных координат
    private transient List<Point2D> cachedAbsolutePoints;
    private transient boolean cacheValid = false;

    private transient Polygon javafxPolygon;

    // ----- Конструкторы -----

    public PolygonShape(List<Point2D> points) {
        super();

        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("Многоугольник должен содержать минимум 3 точки");
        }

        double centerX = computeCenterX(points);
        double centerY = computeCenterY(points);

        this.localPoints = new ArrayList<>();
        for (Point2D point : points) {
            localPoints.add(new Point2D(point.getX() - centerX, point.getY() - centerY));
        }

        this.transform.setPosition(centerX, centerY);
        updateBoundsFromPoints(points);

        updateNode();
        invalidateCache();
    }

    public PolygonShape(List<Point2D> points, StrokeProperty stroke, FillProperty fill) {
        this(points);
        this.stroke = stroke;
        this.fill = fill;
        updateNode();
    }

    public PolygonShape(double... coordinates) {
        this(toPointsList(coordinates));
    }

    private PolygonShape(PolygonShape original) {
        super();

        this.localPoints = new ArrayList<>();
        for (Point2D point : original.localPoints) {
            this.localPoints.add(new Point2D(point.getX(), point.getY()));
        }

        this.stroke = original.stroke.copy();
        this.fill = original.fill.copy();
        this.transform = original.transform.copy();
        this.id = java.util.UUID.randomUUID().toString();
        this.isSelected = false;

        invalidateCache();
        updateNode();
    }

    // ----- Вспомогательные методы -----

    private static List<Point2D> toPointsList(double... coordinates) {
        if (coordinates.length < 6 || coordinates.length % 2 != 0) {
            throw new IllegalArgumentException("Нечётное количество координат или меньше 6 значений (3 точки)");
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

    private void invalidateCache() {
        cacheValid = false;
        cachedAbsolutePoints = null;
    }

    private List<Point2D> getAbsolutePoints() {
        if (cacheValid && cachedAbsolutePoints != null) {
            return cachedAbsolutePoints;
        }

        List<Point2D> points = new ArrayList<>();
        double centerX = transform.getX();
        double centerY = transform.getY();
        double rotateRad = Math.toRadians(transform.getRotate());
        double scaleX = transform.getScaleX();
        double scaleY = transform.getScaleY();

        for (Point2D localPoint : localPoints) {
            double scaledX = localPoint.getX() * scaleX;
            double scaledY = localPoint.getY() * scaleY;

            double rotatedX = scaledX;
            double rotatedY = scaledY;
            if (rotateRad != 0) {
                double cos = Math.cos(rotateRad);
                double sin = Math.sin(rotateRad);
                rotatedX = scaledX * cos - scaledY * sin;
                rotatedY = scaledX * sin + scaledY * cos;
            }

            points.add(new Point2D(centerX + rotatedX, centerY + rotatedY));
        }

        cachedAbsolutePoints = points;
        cacheValid = true;
        return points;
    }

    /**
     * Получает список координат для JavaFX Polygon (Collection<Double>)
     */
    private List<Double> getCoordinatesList() {
        List<Point2D> absolutePoints = getAbsolutePoints();
        List<Double> coords = new ArrayList<>(absolutePoints.size() * 2);

        for (Point2D point : absolutePoints) {
            coords.add(point.getX());
            coords.add(point.getY());
        }

        return coords;
    }

    // ----- Обновление JavaFX узла -----

    @Override
    protected void updateNode() {
        if (javafxPolygon == null) {
            javafxPolygon = new Polygon();
        }

        // Используем setAll с Collection<Double>
        javafxPolygon.getPoints().setAll(getCoordinatesList());

        stroke.applyTo(javafxPolygon);
        fill.applyTo(javafxPolygon);

        if (isSelected) {
            javafxPolygon.setStrokeWidth(stroke.getWidth() + 3);
            javafxPolygon.getStrokeDashArray().addAll(5.0, 5.0);
        } else {
            stroke.applyTo(javafxPolygon);
            javafxPolygon.getStrokeDashArray().clear();
        }
    }

    @Override
    public Node getNode() {
        updateNode();
        return javafxPolygon;
    }

    // ----- Реализация абстрактных методов -----

    @Override
    public boolean contains(double x, double y) {
        List<Point2D> points = getAbsolutePoints();

        // Алгоритм "точка в многоугольнике" (ray casting)
        boolean inside = false;
        int n = points.size();

        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point2D pi = points.get(i);
            Point2D pj = points.get(j);

            double xi = pi.getX(), yi = pi.getY();
            double xj = pj.getX(), yj = pj.getY();

            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi);

            if (intersect) {
                inside = !inside;
            }
        }

        // Если точка не внутри, проверяем попадание на границу
        if (!inside) {
            double hitRadius = Math.max(stroke.getWidth() / 2, 5.0);
            for (int i = 0, j = n - 1; i < n; j = i++) {
                Point2D p1 = points.get(i);
                Point2D p2 = points.get(j);

                double distance = pointToSegmentDistance(x, y,
                        p1.getX(), p1.getY(), p2.getX(), p2.getY());

                if (distance <= hitRadius) {
                    return true;
                }
            }
            return false;
        }

        return true;
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
        for (int i = 0; i < localPoints.size(); i++) {
            Point2D point = localPoints.get(i);
            localPoints.set(i, new Point2D(point.getX() * factor, point.getY() * factor));
        }

        List<Point2D> absolutePoints = getAbsolutePoints();
        updateBoundsFromPoints(absolutePoints);

        invalidateCache();
        updateNode();
    }

    @Override
    public Shape copy() {
        return new PolygonShape(this);
    }

    // ----- Дополнительные методы -----

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

    public int getPointCount() {
        return localPoints.size();
    }

    public List<Point2D> getPoints() {
        return new ArrayList<>(getAbsolutePoints());
    }

    public Point2D getPoint(int index) {
        return getAbsolutePoints().get(index);
    }

    public void setPoint(int index, double x, double y) {
        List<Point2D> absolutePoints = getAbsolutePoints();
        absolutePoints.set(index, new Point2D(x, y));
        rebuildFromPoints(absolutePoints);
    }

    public void addPoint(double x, double y) {
        List<Point2D> absolutePoints = getAbsolutePoints();
        absolutePoints.add(new Point2D(x, y));
        rebuildFromPoints(absolutePoints);
    }

    public void insertPoint(int index, double x, double y) {
        List<Point2D> absolutePoints = getAbsolutePoints();
        absolutePoints.add(index, new Point2D(x, y));
        rebuildFromPoints(absolutePoints);
    }

    public void removePoint(int index) {
        if (getPointCount() <= 3) {
            throw new IllegalStateException("Многоугольник должен содержать минимум 3 точки");
        }

        List<Point2D> absolutePoints = getAbsolutePoints();
        absolutePoints.remove(index);
        rebuildFromPoints(absolutePoints);
    }

    private void rebuildFromPoints(List<Point2D> points) {
        if (points.size() < 3) {
            throw new IllegalArgumentException("Многоугольник должен содержать минимум 3 точки");
        }

        double centerX = computeCenterX(points);
        double centerY = computeCenterY(points);

        List<Point2D> newLocalPoints = new ArrayList<>();
        for (Point2D point : points) {
            newLocalPoints.add(new Point2D(point.getX() - centerX, point.getY() - centerY));
        }

        this.localPoints = newLocalPoints;
        this.transform.setPosition(centerX, centerY);
        updateBoundsFromPoints(points);

        invalidateCache();
        updateNode();
    }

    public boolean isConvex() {
        List<Point2D> points = getAbsolutePoints();
        int n = points.size();

        if (n < 3) return false;

        boolean hasPositive = false;
        boolean hasNegative = false;

        for (int i = 0; i < n; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get((i + 1) % n);
            Point2D p3 = points.get((i + 2) % n);

            double cross = crossProduct(p1, p2, p3);

            if (cross > 0) hasPositive = true;
            if (cross < 0) hasNegative = true;

            if (hasPositive && hasNegative) return false;
        }

        return true;
    }

    private double crossProduct(Point2D p1, Point2D p2, Point2D p3) {
        double x1 = p2.getX() - p1.getX();
        double y1 = p2.getY() - p1.getY();
        double x2 = p3.getX() - p2.getX();
        double y2 = p3.getY() - p2.getY();
        return x1 * y2 - y1 * x2;
    }

    @Override
    public String toString() {
        return String.format("PolygonShape[id=%s, points=%d, center=(%.1f,%.1f)]",
                id.substring(0, 8), getPointCount(), getCenterX(), getCenterY());
    }
}