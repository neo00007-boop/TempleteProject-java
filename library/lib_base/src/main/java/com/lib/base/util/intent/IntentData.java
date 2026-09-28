package com.lib.base.util.intent;

import android.content.Intent;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * 跳转参数。常用类型用 {@link #put}，其余类型用 {@link #extra} 直接写入 Intent。
 */
public final class IntentData {
    private final Writer writer;

    private IntentData(Writer writer) {
        this.writer = writer;
    }

    public void write(@NonNull Intent intent) {
        writer.write(intent);
    }

    /**
     * 不受内置类型限制，按 Intent 自己的方法写入。
     */
    public static IntentData extra(@NonNull Writer writer) {
        return new IntentData(writer);
    }

    public static IntentData put(String key, String value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, CharSequence value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, int value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, long value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, float value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, double value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, boolean value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, byte value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, short value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, char value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, Parcelable value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, int[] value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, long[] value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, float[] value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, double[] value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, boolean[] value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, String[] value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData put(String key, Parcelable[] value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData putSerializable(String key, Serializable value) {
        return extra(intent -> intent.putExtra(key, value));
    }

    public static IntentData putListInt(String key, ArrayList<Integer> value) {
        return extra(intent -> intent.putIntegerArrayListExtra(key, value));
    }

    public static IntentData putListStr(String key, ArrayList<String> value) {
        return extra(intent -> intent.putStringArrayListExtra(key, value));
    }

    public static IntentData putListPar(String key, ArrayList<? extends Parcelable> value) {
        return extra(intent -> putParcelableList(intent, key, value));
    }

    @SuppressWarnings("unchecked")
    private static void putParcelableList(Intent intent, String key, ArrayList<? extends Parcelable> value) {
        intent.putParcelableArrayListExtra(key, (ArrayList<Parcelable>) value);
    }

    public interface Writer {
        void write(Intent intent);
    }
}
