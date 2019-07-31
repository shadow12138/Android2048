package com.pdun.a2048;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 2048
 * R.id.position记录view的位置
 * R.id.number记录view的数值
 */
public class MainActivity extends AppCompatActivity {
    private static final int POS = R.id.position;
    private static final int VAL = R.id.number;

    private LinearLayout llBackground;
    private AbsoluteLayout alNumbers;
    private int size;
    private int margin;
    private boolean isAnimatorStopped = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        size = Utils.dip2px(this, 50);
        margin = Utils.dip2px(this, 10);

        initBackground();
        findViewById(R.id.btn_new).performClick();
    }

    private void initBackground() {
        llBackground = findViewById(R.id.ll_background);
        alNumbers = findViewById(R.id.al);

        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(-1, -2);
        lineParams.bottomMargin = margin;
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(size, size);
        gridParams.rightMargin = margin;

        for (int i = 0; i < 4; i++) {
            LinearLayout line = new LinearLayout(this);

            for (int j = 0; j < 4; j++) {
                View view = new View(this);
                view.setBackgroundColor(getResources().getColor(R.color.color_background));
                line.addView(view, gridParams);
            }

            llBackground.addView(line, lineParams);
        }
    }

    public void newGame(View view) {
        //清除所有控件
        alNumbers.removeAllViews();

        //生成两个2
        addNewBlock();
        addNewBlock();
    }

    private void addNewBlock() {
        //查找可以还没有数字的位置
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 4 * 4; i++) {
            if (findViewByPosition(i) == null)
                positions.add(i);
        }

        //如果都满了，游戏就结束啦
        if (positions.size() == 0)
            return;

        Collections.shuffle(positions);
        int position = positions.get(0);

        int i = position / 4;
        int j = position % 4;
        int x = j * (size + margin) + margin;
        int y = i * (size + margin) + margin;
        int number = 2;

        TextView textView = new TextView(this);
        textView.setBackgroundColor(getResources().getColor(R.color.color_2));
        textView.setGravity(Gravity.CENTER);
        textView.setText(String.valueOf(number));
        textView.setTag(POS, position);
        textView.setTag(VAL, number);
        textView.getPaint().setFakeBoldText(true);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setLayoutParams(new AbsoluteLayout.LayoutParams(size, size, x, y));
        alNumbers.addView(textView);

        ObjectAnimator.ofFloat(textView, "scaleX", 0, 1).setDuration(200).start();
        ObjectAnimator.ofFloat(textView, "scaleY", 0, 1).setDuration(200).start();
    }

    private List<Action> moveLeft() {

        List<Action> actions = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int prev = 0;
            for (int j = 1; j < 4; j++) {
                if (j == prev)
                    continue;

                int currPosition = i * 4 + j;
                int prevPosition = i * 4 + prev;

                View currView = findViewByPosition(currPosition);
                View prevView = findViewByPosition(prevPosition);
                if (currView != null) {
                    TextView currTextView = (TextView) currView;
                    int number = (int) currTextView.getTag(VAL);

                    int type = Action.TYPE_MOVE;
                    if (prevView != null) {
                        TextView prevTextView = (TextView) prevView;
                        int prevNumber = (int) prevTextView.getTag(VAL);

                        //当前数字与前一数字相同，可以合并
                        if (prevNumber == number) {
                            type = Action.TYPE_MERGE;
                        }
                    }

                    if (type == Action.TYPE_MERGE) {
                        // 0。合并
                        actions.add(new Action(Action.DIR_HORIZONTAL, type, j, prev, currTextView));

                        // 1。要移除prevView（动画结束后才能移除）
                        prevView.setTag(POS, -1);
                        // 2。要改变currView的R.id.number
                        currView.setTag(VAL, number * 2);
                        // 3。要改变currView的tag
                        currTextView.setTag(POS, currPosition + (prev - j));
                        // 4。合并后就不能再再被合并，所以left + 1
                        prev += 1;
                    } else {
                        // 0。移动
                        if (prevView == null) {
                            // 1。如果prevView是空，则可以移动到left
                            actions.add(new Action(Action.DIR_HORIZONTAL, type, j, prev, currTextView));
                            currTextView.setTag(POS, currPosition + (prev - j));
                        } else {
                            prev += 1;
                            if (prev != j) {
                                // 1。否则，只能移动到left + 1
                                actions.add(new Action(Action.DIR_HORIZONTAL, type, j, prev, currTextView));
                                currTextView.setTag(POS, currPosition + (prev - j));
                            }
                        }
                    }

                }
            }
        }


        return actions;
    }

    private List<Action> moveRight() {

        List<Action> actions = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int right = 3;
            for (int j = 2; j >= 0; j--) {
                if (j == right)
                    continue;

                int currPosition = i * 4 + j;
                int prevPosition = i * 4 + right;

                View currView = findViewByPosition(currPosition);
                View prevView = findViewByPosition(prevPosition);
                if (currView != null) {
                    TextView currTextView = (TextView) currView;
                    int number = (int) currTextView.getTag(VAL);

                    int type = Action.TYPE_MOVE;
                    if (prevView != null) {
                        TextView prevTextView = (TextView) prevView;
                        int prevNumber = (int) prevTextView.getTag(VAL);

                        if (prevNumber == number) {
                            type = Action.TYPE_MERGE;
                        }
                    }

                    if (type == Action.TYPE_MERGE) {
                        actions.add(new Action(Action.DIR_HORIZONTAL, type, j, right, currTextView));

                        prevView.setTag(POS, -1);
                        currView.setTag(VAL, number * 2);
                        currTextView.setTag(POS, currPosition + (right - j));
                        right -= 1;
                    } else {
                        if (prevView == null) {
                            actions.add(new Action(Action.DIR_HORIZONTAL, type, j, right, currTextView));
                            currTextView.setTag(POS, currPosition + (right - j));
                        } else {
                            right -= 1;
                            if (right != j) {
                                actions.add(new Action(Action.DIR_HORIZONTAL, type, j, right, currTextView));
                                currTextView.setTag(POS, currPosition + (right - j));
                            }
                        }
                    }

                }
            }
        }


        return actions;
    }

    private List<Action> moveUp() {

        List<Action> actions = new ArrayList<>();
        for (int j = 0; j < 4; j++) {
            int up = 0;
            for (int i = 1; i < 4; i++) {
                if (i == up)
                    continue;

                int currPosition = i * 4 + j;
                int prevPosition = up * 4 + j;

                View currView = findViewByPosition(currPosition);
                View prevView = findViewByPosition(prevPosition);
                if (currView != null) {
                    TextView currTextView = (TextView) currView;
                    int number = (int) currTextView.getTag(VAL);

                    int type = Action.TYPE_MOVE;
                    if (prevView != null) {
                        TextView prevTextView = (TextView) prevView;
                        int prevNumber = (int) prevTextView.getTag(VAL);

                        if (prevNumber == number) {
                            type = Action.TYPE_MERGE;
                        }
                    }

                    if (type == Action.TYPE_MERGE) {
                        actions.add(new Action(Action.DIR_VERTICAL, type, i, up, currTextView));

                        prevView.setTag(POS, -1);
                        currView.setTag(VAL, number * 2);
                        currTextView.setTag(POS, currPosition + (up - i) * 4);
                        up += 1;
                    } else {
                        if (prevView == null) {
                            actions.add(new Action(Action.DIR_VERTICAL, type, i, up, currTextView));
                            currTextView.setTag(POS, currPosition + (up - i) * 4);
                        } else {
                            up += 1;
                            if (up != i) {
                                actions.add(new Action(Action.DIR_VERTICAL, type, i, up, currTextView));
                                currTextView.setTag(POS, currPosition + (up - i) * 4);
                            }
                        }
                    }

                }
            }
        }


        return actions;
    }

    private List<Action> moveDown() {

        List<Action> actions = new ArrayList<>();
        for (int j = 0; j < 4; j++) {
            int down = 3;
            for (int i = 2; i >= 0; i--) {
                if (i == down)
                    continue;

                int currPosition = i * 4 + j;
                int prevPosition = down * 4 + j;

                View currView = findViewByPosition(currPosition);
                View prevView = findViewByPosition(prevPosition);
                if (currView != null) {
                    TextView currTextView = (TextView) currView;
                    int number = (int) currTextView.getTag(VAL);

                    int type = Action.TYPE_MOVE;
                    if (prevView != null) {
                        TextView prevTextView = (TextView) prevView;
                        int prevNumber = (int) prevTextView.getTag(VAL);

                        if (prevNumber == number) {
                            type = Action.TYPE_MERGE;
                        }
                    }

                    if (type == Action.TYPE_MERGE) {
                        actions.add(new Action(Action.DIR_VERTICAL, type, i, down, currTextView));

                        prevView.setTag(POS, -1);
                        currView.setTag(VAL, number * 2);
                        currTextView.setTag(POS, currPosition + (down - i) * 4);
                        down -= 1;
                    } else {
                        if (prevView == null) {
                            actions.add(new Action(Action.DIR_VERTICAL, type, i, down, currTextView));
                            currTextView.setTag(POS, currPosition + (down - i) * 4);
                        } else {
                            down -= 1;
                            if (down != i) {
                                actions.add(new Action(Action.DIR_VERTICAL, type, i, down, currTextView));
                                currTextView.setTag(POS, currPosition + (down - i) * 4);
                            }
                        }
                    }

                }
            }
        }


        return actions;
    }

    private View findViewByPosition(int pos) {
        for (int i = 0; i < alNumbers.getChildCount(); i++) {
            View view = alNumbers.getChildAt(i);
            if ((int) view.getTag(POS) == pos) {
                return view;
            }
        }
        return null;
    }

    private void removeAllPrevView() {
        List<View> removeViews = new ArrayList<>();
        for (int i = 0; i < alNumbers.getChildCount(); i++) {
            View view = alNumbers.getChildAt(i);
            if ((int) view.getTag(POS) == -1) {
                removeViews.add(view);
            }
        }
        for (View view : removeViews) {
            alNumbers.removeView(view);
        }
    }

    private void playTogether(final List<Action> actions) {
        isAnimatorStopped = false;

        List<Animator> animators = new ArrayList<>();
        for (Action action : actions) {
            int from = action.from * (size + margin) + margin;
            int to = action.to * (size + margin) + margin;
            String attr = action.dir == Action.DIR_HORIZONTAL ? "x" : "y";
            animators.add(ObjectAnimator.ofFloat(action.target, attr, from, to));
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                for (Action action : actions) {


                    if (action.type == Action.TYPE_MERGE) {

                        // 合并后修改view的值
                        TextView textView = (TextView) action.target;
                        int number = (int) textView.getTag(VAL);
                        textView.setText(String.valueOf(number));
                        textView.setBackgroundColor(getNumberColor(number));

                        // 合并动画
                        ObjectAnimator.ofFloat(textView, "scaleX", 1.2f, 1).setDuration(200).start();
                        ObjectAnimator.ofFloat(textView, "scaleY", 1.2f, 1).setDuration(200).start();
                    }

                }

                // 移除所有被合并的view
                removeAllPrevView();

                // 一波移动后要生成新数字
                addNewBlock();

                isAnimatorStopped = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }



    public void move(View view) {
        move(view.getId());
    }

    private void move(int id) {
        if (!isAnimatorStopped)
            return;
        List<Action> actions = null;
        switch (id) {
            case R.id.btn_left:
                actions = moveLeft();
                break;
            case R.id.btn_right:
                actions = moveRight();
                break;
            case R.id.btn_up:
                actions = moveUp();
                break;
            case R.id.btn_down:
                actions = moveDown();
                break;
        }
        if (actions != null)
            playTogether(actions);
    }

    private int getNumberColor(int number) {
        int id = getResources().getIdentifier(String.format("color_%d", number), "color", getPackageName());
        return getResources().getColor(id);
    }

    private float x1, x2, y1, y2;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
            y1 = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            y2 = event.getY();
            if (Math.abs(x1 - x2) > Math.abs(y1 - y2)) {
                if (x1 - x2 > 50) {
                    move(R.id.btn_left);
                } else if (x2 - x1 > 50) {
                    move(R.id.btn_right);
                }
            } else {
                if (y1 - y2 > 50) {
                    move(R.id.btn_up);
                } else if (y2 - y1 > 50) {
                    move(R.id.btn_down);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                move(R.id.btn_left);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                move(R.id.btn_right);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                move(R.id.btn_up);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                move(R.id.btn_down);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
