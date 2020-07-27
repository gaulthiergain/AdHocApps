package com.montefiore.gaulthiergain.simongameadhoc;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <p>This class represents a set of colors.</p>
 *
 * @author Gaulthier Gain
 * @version 1.0
 */
public class ColorSet implements Serializable {

    private ArrayList<Color> arrayList;

    ColorSet() {
        this.arrayList = new ArrayList<>();
    }

    public void addColor(Color color) {
        arrayList.add(color);
    }

    public ArrayList<Color> getArrayList() {
        return arrayList;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < arrayList.size(); i++) {
            str.append("Color: ").append(switchColor(arrayList.get(i).getColor())).append("\n");
        }
        return str.toString();
    }

    private String switchColor(int color) {
        switch (color) {
            case 0:
                return "RED";
            case 1:
                return "GREEN";
            case 2:
                return "BLUE";
            case 3:
                return "ORANGE";
        }
        return "NA";
    }
}
