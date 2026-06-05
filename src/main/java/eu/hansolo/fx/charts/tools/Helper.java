package eu.hansolo.fx.charts.tools;

import javafx.scene.Node;

public class Helper {
    public static int clamp(final int min, final int max, final int value) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static long clamp(final long min, final long max, final long value) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static double clamp(final double min, final double max, final double value) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static void enableNode(final Node node, final boolean enable) {
        node.setVisible(enable);
        node.setManaged(enable);
    }
}
