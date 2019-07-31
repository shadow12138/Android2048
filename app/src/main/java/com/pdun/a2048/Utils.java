package com.pdun.a2048;

import android.content.Context;

/**
 * Created by Dun on 2019/7/27.
 */

public class Utils {

    public static int dip2px(Context context, float dip) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f);
    }
}
