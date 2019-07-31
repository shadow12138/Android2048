package com.pdun.a2048;

import android.view.View;

/**
 * Created by Dun on 2019/7/28.
 */

public class Action {
    public static final int DIR_HORIZONTAL = 0;
    public static final int DIR_VERTICAL = 1;

    public static final int TYPE_MOVE = 0;
    public static final int TYPE_MERGE = 1;

    public int dir;
    public int type;
    public int from;
    public int to;
    public View target;

    public Action(int dir, int type, int from, int to, View target) {
        this.dir = dir;
        this.type = type;
        this.from = from;
        this.to = to;
        this.target = target;
    }
}
