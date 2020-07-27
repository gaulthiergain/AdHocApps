package com.montefiore.gaulthiergain.simongameadhoc;

import java.io.Serializable;

/**
 * <p>This class represents a simply a color.</p>
 *
 * @author Gaulthier Gain
 * @version 1.0
 */
public class Color implements Serializable {
    public static int RED = 0;
    public static int GREEN = 1;
    public static int BLUE = 2;
    public static int ORANGE = 3;

    private int color;

    public Color() {
    }

    Color(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
