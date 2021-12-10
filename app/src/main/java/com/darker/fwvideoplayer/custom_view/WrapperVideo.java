package com.darker.fwvideoplayer.custom_view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WrapperVideo extends FrameLayout {


    public WrapperVideo(@NonNull Context context) {
        super(context);
    }

    public WrapperVideo(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
