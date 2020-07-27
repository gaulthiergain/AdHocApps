package com.montefiore.gaulthiergain.simongameadhoc;

import java.io.Serializable;

/**
 * <p>This class represents a Msg exchanged between nodes.</p>
 *
 * @author Gaulthier Gain
 * @version 1.0
 */
public class Msg implements Serializable {

    private final int type;
    private ColorSet colorSet;
    private String msg;
    private String name;

    public Msg() {
        type = 0;
    }

    Msg(int type, ColorSet colorSet, String name) {
        this.type = type;
        this.colorSet = colorSet;
        this.name = name;
    }

    Msg(int type, String msg, String name) {
        this.type = type;
        this.msg = msg;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public ColorSet getColorSet() {
        return colorSet;
    }

    public String getMsg() {
        return msg;
    }

    public String getName() {
        return name;
    }
}
