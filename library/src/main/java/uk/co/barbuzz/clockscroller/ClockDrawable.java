package uk.co.barbuzz.clockscroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Cap.ROUND;
import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.STROKE;
import static android.util.Log.d;

/**
 * Created by evelina on 15/07/2016.
 */

public class ClockDrawable extends Drawable implements Animatable {

    private static final String TAG = "ClockDrawable";

    private final static int ANIMATION_DURATION = 500;

    private static int RIM_COLOR = Color.parseColor("#1976D2");
    private static int FACE_COLOR = Color.parseColor("#FFFFFF");
    private static final float CLOCK_LINE_WIDTH_DP = Utils.dp2px(4);

    private Paint facePaint;
    private Paint rimPaint;
    private final ValueAnimator minAnimator;
    private final ValueAnimator hourAnimator;
    private int faceColor = RIM_COLOR;
    private int rimColor = FACE_COLOR;

    private float rimRadius;
    private float faceRadius;
    private float screwRadius;

    private final Path hourHandPath;
    private final Path minuteHandPath;

    private float remainingHourRotation = 0f;
    private float remainingMinRotation = 0f;

    private float targetHourRotation = 0f;
    private float targetMinRotation = 0f;

    private float currentHourRotation = 0f;
    private float currentMinRotation;

    private boolean hourAnimInterrupted;
    private boolean minAnimInterrupted;

    private Date previousTime;

    private boolean animateDays = true;
    private float clockLineWidth = CLOCK_LINE_WIDTH_DP;

    public ClockDrawable() {
        initFacePaint(faceColor);

        initRimPaint(rimColor);

        hourHandPath = new Path();
        minuteHandPath = new Path();

        hourAnimator = ValueAnimator.ofFloat(0, 0);
        hourAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        hourAnimator.setDuration(ANIMATION_DURATION);
        hourAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = (float) valueAnimator.getAnimatedValue();
                //d("ANIM", "Hfraction = " + fraction + ", remaining hour rotation = " + remainingHourRotation);
                remainingHourRotation = targetHourRotation - fraction;
                currentHourRotation = fraction;
                invalidateSelf();
            }
        });
        hourAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!hourAnimInterrupted) {
                    remainingHourRotation = 0f;
                }
                //i("ANIM", "END! remaining hour rotation = " + remainingHourRotation);
            }
        });


        minAnimator = ValueAnimator.ofFloat(0, 0);
        minAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        minAnimator.setDuration(ANIMATION_DURATION);
        minAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = (float) valueAnimator.getAnimatedValue();
                //d("ANIM", "Mfraction = " + fraction + ", remaining minute rotation = " + remainingMinRotation);
                remainingMinRotation = targetMinRotation - fraction;
                currentMinRotation = fraction;
                invalidateSelf();
            }
        });
        minAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!minAnimInterrupted) {
                    remainingMinRotation = 0f;
                }
                //i("ANIM", "END! remaining minute rotation = " + remainingMinRotation);
            }
        });

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        previousTime = cal.getTime();

    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        rimRadius = Math.min(bounds.width(), bounds.height()) / 2f - rimPaint.getStrokeWidth();
        faceRadius = rimRadius - rimPaint.getStrokeWidth();
        screwRadius = rimPaint.getStrokeWidth() * 1;
        float hourHandLength = (float) (0.5 * faceRadius);
        float minuteHandLength = (float) (0.7 * faceRadius);
        float top = bounds.centerY() - screwRadius;

        hourHandPath.reset();
        hourHandPath.moveTo(bounds.centerX(), bounds.centerY());
        hourHandPath.addRect(bounds.centerX(), top, bounds.centerX(), top - hourHandLength, Direction.CCW);
        hourHandPath.close();

        minuteHandPath.reset();
        minuteHandPath.moveTo(bounds.centerX(), bounds.centerY());
        minuteHandPath.addRect(bounds.centerX(), top, bounds.centerX(), top - minuteHandLength, Direction.CCW);
        minuteHandPath.close();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        // draw the outer rim of the clock
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), rimRadius, rimPaint);
        // draw the face of the clock
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), faceRadius, facePaint);
        // draw the little rim in the middle of the clock
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), screwRadius, rimPaint);

        int saveCount = canvas.save();
        canvas.rotate(currentHourRotation, bounds.centerX(), bounds.centerY());
        // draw hour hand
        canvas.drawPath(hourHandPath, rimPaint);
        canvas.restoreToCount(saveCount);

        saveCount = canvas.save();
        canvas.rotate(currentMinRotation, bounds.centerX(), bounds.centerY());
        // draw minute hand
        canvas.drawPath(minuteHandPath, rimPaint);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void setAlpha(int alpha) {
        rimPaint.setAlpha(alpha);
        facePaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        rimPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void start() {
        hourAnimInterrupted = false;
        minAnimInterrupted = false;
        hourAnimator.start();
        minAnimator.start();
    }

    private void initRimPaint(int rimColor) {
        rimPaint = new Paint(ANTI_ALIAS_FLAG);
        rimPaint.setColor(rimColor);
        rimPaint.setStyle(STROKE);
        rimPaint.setStrokeCap(ROUND);
        rimPaint.setStrokeWidth(clockLineWidth);
    }

    private void initFacePaint(int faceColor) {
        facePaint = new Paint(ANTI_ALIAS_FLAG);
        facePaint.setColor(faceColor);
        facePaint.setStyle(FILL);
    }

    public void setAnimateDays(boolean animateDays) {
        this.animateDays = animateDays;
    }

    public void start(Date newTime) {
        long diff = newTime.getTime() - previousTime.getTime();//as given
        long minDiff = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minDiff == -59) minDiff = -60;
        Log.i(TAG, "diff mins  - " + minDiff);

        // 60min ... 360grade
        // minDif .. minDelta
        float minDeltaRotation = ((float) minDiff * 360f) / 60f;
        // 720min ... 360grade = 12h ... 360grade
        // minDif ... hourDelta
        float hourDeltaRotation = ((float) minDiff * 360f) / 720f;

        remainingMinRotation += minDeltaRotation;
        remainingHourRotation += hourDeltaRotation;

        d("ANIM", "current hour rotation = " + currentHourRotation + ", current min rotation = " + currentMinRotation);

        if (isRunning()) {
            stop();
        }

        targetHourRotation = currentHourRotation + remainingHourRotation;
        hourAnimator.setFloatValues(currentHourRotation, targetHourRotation);

        targetMinRotation = currentMinRotation + remainingMinRotation;
        minAnimator.setFloatValues(currentMinRotation, targetMinRotation);

        start();

        previousTime = newTime;
    }

    @Override
    public void stop() {
        hourAnimInterrupted = true;
        minAnimInterrupted = true;
        hourAnimator.cancel();
        minAnimator.cancel();
    }

    @Override
    public boolean isRunning() {
        return hourAnimator.isRunning() || minAnimator.isRunning();
    }

    public int getFaceColor() {
        return faceColor;
    }

    public void setFaceColor(int faceColor) {
        this.faceColor = faceColor;
        initFacePaint(faceColor);
    }

    public int getRimColor() {
        return rimColor;
    }

    public void setRimColor(int rimColor) {
        this.rimColor = rimColor;
        initRimPaint(rimColor);
    }

    public float getClockLineWidth() {
        return clockLineWidth;
    }

    public void setClockLineWidth(float clockLineWidth) {
        this.clockLineWidth = clockLineWidth;
        initFacePaint(faceColor);
    }
}
