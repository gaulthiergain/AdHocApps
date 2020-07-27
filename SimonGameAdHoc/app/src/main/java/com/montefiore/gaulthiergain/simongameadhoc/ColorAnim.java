package com.montefiore.gaulthiergain.simongameadhoc;

import android.view.animation.Animation;

/**
 * <p>This class represents a Color animation and is used to perform animation.</p>
 *
 * @author Gaulthier Gain
 * @version 1.0
 */
public class ColorAnim {
    private Color color;
    private Animation anim;

    public ColorAnim() {
    }

    ColorAnim(Color color, Animation anim) {
        this.color = color;
        this.anim = anim;
    }

    public Color getColor() {
        return color;
    }

    public Animation getAnim() {
        return anim;
    }
}
