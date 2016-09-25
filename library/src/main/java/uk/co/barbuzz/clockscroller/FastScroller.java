package uk.co.barbuzz.clockscroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FastScroller extends LinearLayout implements Subscriber {
    private static final String TAG = "FastScroller";

    private static final int BUBBLE_ANIMATION_DURATION = 250;
    private static final int TRACK_SNAP_RANGE = 5;
    private static final int CLOCK_EDGE_COLOR_DEFAULT = Color.parseColor("#1976D2");
    private static final int CLOCK_FACE_COLOR_DEFAULT = Color.parseColor("#FFFFFF");
    private static final int CLOCK_SCROLLBAR_COLOR_DEFAULT = Color.parseColor("#8a8a8a");
    private static final int CLOCK_SCROLLBAR_SELECTED_COLOR_DEFAULT = Color.parseColor("#5e5e5e");
    private static final float CLOCK_LINE_WIDTH_DP = Utils.dp2px(4);
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    private RecyclerView recyclerView;
    private ImageView bubble;
    private ImageView handle;
    private int height;
    private ObjectAnimator bubbleAnimator = null;

    private String bubbleText = "?";
    private boolean isHandleScrolling;
    private ClockDrawable clockDrawable;
    private int clockEdgeColor;
    private int clockFaceColor;
    private float clockLineWidth;
    private int clockScrollBarColor;
    private int clockScrollBarSelectedColor;
    private Date bubbleDate;
    private TextView handleText;
    private LinearLayout bubbleLayout;

    // CONSTRUCTORS ________________________________________________________________________________
    public FastScroller(final Context context) {
        super(context);

        initialise(context);
    }

    public FastScroller(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getAttributes(context, attrs);

        initialise(context);
    }

    public FastScroller(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        getAttributes(context, attrs);

        initialise(context);
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClockScroller);

        // a.getInteger can throw RuntimeException or UnsupportedOperationException so surround with try
        try {
            clockEdgeColor = a.getInt(R.styleable.ClockScroller_clockEdgeColor, CLOCK_EDGE_COLOR_DEFAULT);
            clockFaceColor = a.getInt(R.styleable.ClockScroller_clockFaceColor, CLOCK_FACE_COLOR_DEFAULT);
            clockScrollBarColor = a.getInt(R.styleable.ClockScroller_clockScrollBarColor, CLOCK_SCROLLBAR_COLOR_DEFAULT);
            clockScrollBarSelectedColor = a.getInt(R.styleable.ClockScroller_clockScrollBarSelectedColor, CLOCK_SCROLLBAR_SELECTED_COLOR_DEFAULT);
            clockLineWidth = a.getDimension(R.styleable.ClockScroller_clockLineWidth, CLOCK_LINE_WIDTH_DP);
        } finally {
            a.recycle();
        }
    }

    private void initialise(Context context) {
        setOrientation(HORIZONTAL);
        setClipChildren(false);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.fastscroller, this, true);

        bubbleLayout = (LinearLayout) findViewById(R.id.fastscroller_bubble_layout);
        bubble = (ImageView) findViewById(R.id.fastscroller_bubble);
        handle = (ImageView) findViewById(R.id.fastscroller_handle);
        handleText = (TextView) findViewById(R.id.fastscroller_text);

        StateListDrawable states = new StateListDrawable();
        GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.fastscroller_handle_shape);
        GradientDrawable drawableSelected = (GradientDrawable) getResources().getDrawable(R.drawable.fastscroller_handle_shape);
        drawable.setColor(clockScrollBarColor);
        drawableSelected.setColor(clockScrollBarSelectedColor);
        states.addState(new int[] {android.R.attr.state_pressed}, drawableSelected);
        states.addState(new int[] { }, drawable);
        handle.setImageDrawable(states);

        //set initial clock drawable as scroll handle
        clockDrawable = new ClockDrawable();
        clockDrawable.setFaceColor(clockFaceColor);
        clockDrawable.setRimColor(clockEdgeColor);
        clockDrawable.setAnimateDays(false);
        bubble.setImageDrawable(clockDrawable);

        bubbleLayout.setVisibility(INVISIBLE);

        handleText.setTextColor(clockEdgeColor);
    }

    // CALLBACKs ___________________________________________________________________________________
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < handle.getX()) {
                    return false;
                }

                if (bubbleAnimator != null) {
                    bubbleAnimator.cancel();
                }

                if (bubbleLayout.getVisibility() == INVISIBLE) {
                    showBubble();
                }

                handle.setSelected(true);

            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();

                setBubbleAndHandlePosition(y);
                setRecyclerViewPosition(y);

                return true;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:
                handle.setSelected(false);
                hideBubble();

                return true;
        }

        return super.onTouchEvent(event);
    }

    // GETTERS AND SETTERS _________________________________________________________________________
    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        //this.recyclerView.setOnScrollListener(scrollListener);
    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            int itemCount = recyclerView.getAdapter().getItemCount();
            float proportion;

            if (handle.getY() == 0) {
                proportion = 0f;
            } else if (handle.getY() + handle.getHeight() >= height - TRACK_SNAP_RANGE) {
                proportion = 1f;
            } else {
                proportion = y / (float) height;
            }

            int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));

            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, 0);

            if (recyclerView.getAdapter() instanceof DateGetter) {
                bubbleDate = ((DateGetter) recyclerView.getAdapter()).getDateFromAdapter(targetPos);
            }

            handleText.setText(sdf.format(bubbleDate));
            clockDrawable.start(bubbleDate);
            bubble.setImageDrawable(clockDrawable);
        }
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    public void setBubbleAndHandlePosition(float y) {
        int bubbleHeight = bubbleLayout.getHeight();
        int handleHeight = handle.getHeight();

        handle.setY(getValueInRange(0, height - handleHeight, (int) (y - handleHeight / 2)));
        bubbleLayout.setY(getValueInRange(0, height - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
    }

    public int getFastScrollHeight() {
        return height;
    }

    // UI __________________________________________________________________________________________
    private void showBubble() {
        isHandleScrolling = true;
        bubbleLayout.setVisibility(VISIBLE);

        if (bubbleAnimator != null)
            bubbleAnimator.cancel();

        bubbleAnimator = ObjectAnimator.ofFloat(bubbleLayout, "alpha", 0f, 1f).setDuration(BUBBLE_ANIMATION_DURATION);
        bubbleAnimator.start();
    }

    private void hideBubble() {
        isHandleScrolling = false;
        if (bubbleAnimator != null)
            bubbleAnimator.cancel();

        bubbleAnimator = ObjectAnimator.ofFloat(bubbleLayout, "alpha", 1f, 0f).setDuration(BUBBLE_ANIMATION_DURATION);
        bubbleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                bubbleLayout.setVisibility(INVISIBLE);
                bubbleAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                bubbleLayout.setVisibility(INVISIBLE);
                bubbleAnimator = null;
            }
        });

        bubbleAnimator.start();
    }

    // SUBSCRIBER INTERFACE ________________________________________________________________________
    @Override
    public void update(RecyclerView rv, float dx, float dy) {
        if (!isHandleScrolling) {
            View firstVisibleView = recyclerView.getChildAt(0);

            int firstVisiblePosition = recyclerView.getChildPosition(firstVisibleView);
            int visibleRange = recyclerView.getChildCount();
            int lastVisiblePosition = firstVisiblePosition + visibleRange;
            int itemCount = recyclerView.getAdapter().getItemCount();
            int position;

            if (firstVisiblePosition == 0) {
                position = 0;
            } else if (lastVisiblePosition == itemCount) {
                position = itemCount;
            } else {
                position = (int) (((float) firstVisiblePosition / (((float) itemCount - (float) visibleRange))) * (float) itemCount);
            }

            float proportion = (float) position / (float) itemCount;

            setBubbleAndHandlePosition(getFastScrollHeight() * proportion);
        }
    }
}
