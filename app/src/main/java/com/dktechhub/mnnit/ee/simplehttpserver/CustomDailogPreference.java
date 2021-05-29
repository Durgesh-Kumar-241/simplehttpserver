package com.dktechhub.mnnit.ee.simplehttpserver;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class CustomDailogPreference extends DialogPreference {
    public CustomDailogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomDailogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomDailogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDailogPreference(Context context) {
        super(context);
    }
}
