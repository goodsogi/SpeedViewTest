package com.seriouscompany.speedviewtest;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by johnny on 15. 10. 16.
 */
public class PlusDpPixelConverter {
    public static int  doIt(Context context, int value) {
        Resources r = context.getResources();
        float pix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                r.getDisplayMetrics());
        return (int) pix;
    }
}
