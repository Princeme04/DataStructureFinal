package eu.hansolo.fx.charts.data;

import javafx.scene.paint.Color;

public class ChartItem {
    private String name;
    private double value;
    private Color fill;
    private Color stroke;

    public ChartItem(final String name, final double value, final Color fill) {
        this.name = name;
        this.value = value;
        this.fill = fill;
        this.stroke = Color.WHITE;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(final double value) {
        this.value = value;
    }

    public Color getFill() {
        return fill;
    }

    public void setFill(final Color fill) {
        this.fill = fill;
    }

    public Color getStroke() {
        return stroke;
    }

    public void setStroke(final Color stroke) {
        this.stroke = stroke;
    }
}
