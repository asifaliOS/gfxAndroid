package com.gfx;

import android.app.Activity;
import android.os.Bundle;


public class maths {

    public static float DEGTORAD=(float) 0.017453292519943295;
    public static void vec3_cross(float[] v1, float[] v2, float[] r) {
        r[0] = v1[1] * v2[2] - v2[1] * v1[2];
        r[1] = v1[2] * v2[0] - v2[2] * v1[0];
        r[2] = v1[0] * v2[1] - v2[0] * v1[1];
    }

    public static void scalarMultiply(float[] v, float s) {
        for (int i = 0; i < v.length; i++) {
            v[i] *= s;
        }
    }

    public static float vec3_magnitude(float[] v) {
        return (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    public static void vec3_normalize(float[] v) {
        scalarMultiply(v, 1 / vec3_magnitude(v));
    }

}