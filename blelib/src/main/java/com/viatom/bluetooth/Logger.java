package com.viatom.bluetooth;

import android.util.Log;

public class Logger {

    /**
     *  Debug flag
     */
    public static boolean DEBUG = true;

    public static void d(Class clazz, String message) {
        if(DEBUG) {
            String tag = clazz.getSimpleName();
            Log.d(tag, message);
        }

    }

    public static void d(String tag, String message) {
        if(DEBUG) {
            Log.d(tag, message);
        }

    }

    public static <T> void d(T obj, String message ) {
        if(DEBUG) {
            String tag = obj.getClass().getSimpleName();
            Log.d(tag, message);
        }

    }

    public static void e(Class clazz, String message) {
        if(DEBUG) {
            String tag = clazz.getSimpleName();
            Log.e(tag, message);
        }

    }

    public static void e(String tag, String message) {
        if(DEBUG) {
            Log.e(tag, message);
        }
    }

    public static <T> void e(T obj, String message) {
        if(DEBUG) {
            String tag = obj.getClass().getSimpleName();
            Log.d(tag, message);
        }
    }

    public static void e(Class clazz, String message, Throwable throwable) {
        if(DEBUG) {
            String tag = clazz.getSimpleName();
            Log.e(tag, message, throwable);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if(DEBUG) {
            Log.e(tag, message, throwable);
        }
    }

    public static <T> void e(T obj, String message, Throwable throwable ) {
        if(DEBUG) {
            String tag = obj.getClass().getSimpleName();
            Log.d(tag, message, throwable);
        }

    }
}
