package com.seriouscompany.speedviewtest;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;



public class SpeedView extends View {

    private final int SPEED_TEXT_SIZE_DP = 100;
    private final int ROAD_TEXT_SIZE_DP = 20;
    private final int TEXT_Y_OFFSET_DP = 10;
    private final int MAJOR_TICK_LENGTH = 20;


    public static final double DEFAULT_MAX_SPEED = 100.0;
    public static final double DEFAULT_MAJOR_TICK_STEP = 20.0;
    public static final int DEFAULT_MINOR_TICKS = 1;


    private Context mContext;
    private double maxSpeed = DEFAULT_MAX_SPEED;
    private double speed = 0;
    private int defaultColor = Color.rgb(180, 180, 180);
    private double majorTickStep = DEFAULT_MAJOR_TICK_STEP;
    private int minorTicks = DEFAULT_MINOR_TICKS;

    private Paint ticksPaint;
    private Paint speedIndicatorPaint;
    private Paint speedTextPaint;
    private Paint roadTextPaint;
    private Paint colorLinePaint;



    public SpeedView(Context context) {
        super(context);
        init(context);


    }

    public SpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);
        init(context);

    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        if (maxSpeed <= 0)
            throw new IllegalArgumentException("Non-positive value specified as max speed.");
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed < 0)
            throw new IllegalArgumentException("Non-positive value specified as a speed.");
        if (speed > maxSpeed)
            speed = maxSpeed;
        this.speed = speed;
        invalidate();
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double progress, long duration, long startDelay) {
        //이 부분을 수정해야 할 것 같니더!!
        if (progress < 0)
            throw new IllegalArgumentException("Non-positive value specified as a speed.");

        if (progress > maxSpeed)
            progress = maxSpeed;

        ValueAnimator va = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return startValue + fraction * (endValue - startValue);
            }
        }, Double.valueOf(getSpeed()), Double.valueOf(progress));

        va.setDuration(duration);
        va.setStartDelay(startDelay);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Double value = (Double) animation.getAnimatedValue();
                if (value != null)
                    setSpeed(value);
            }
        });
        va.start();
        return va;
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double progress, boolean animate) {
        return setSpeed(progress, 1500, 200);
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
        invalidate();
    }

    public double getMajorTickStep() {
        return majorTickStep;
    }

    public void setMajorTickStep(double majorTickStep) {
        if (majorTickStep <= 0)
            throw new IllegalArgumentException("Non-positive value specified as a major tick step.");
        this.majorTickStep = majorTickStep;
        invalidate();
    }

    public int getMinorTicks() {
        return minorTicks;
    }

    public void setMinorTicks(int minorTicks) {
        this.minorTicks = minorTicks;
        invalidate();
    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        clearCanvas(canvas);

        drawTicks(canvas);

        drawSpeedIndicator(canvas);

        drawSpeedLimitText(canvas);


        drawRoadTypeText(canvas);


    }

    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
    }

    private void drawRoadTypeText(Canvas canvas) {
        RectF oval = getOval(canvas, 1);
        String roadTypeText = "고속도로";

        float txtX = oval.centerX() ;
        float txtY = oval.bottom - PlusDpPixelConverter.doIt(mContext,ROAD_TEXT_SIZE_DP) - PlusDpPixelConverter.doIt(mContext,TEXT_Y_OFFSET_DP);

        canvas.drawText(roadTypeText, txtX, txtY, roadTextPaint);
    }

    private void drawSpeedLimitText(Canvas canvas) {
        RectF oval = getOval(canvas, 1);
       String speedText = "70";

        //텍스트를 중간에 쓰기 위한 코드니더!!
        //speedTextPaint.setTextAlign(Paint.Align.CENTER);도 필요하니더!!
        float txtX = oval.centerX() ;
        float txtY =  oval.centerY() - ((speedTextPaint.descent() + speedTextPaint.ascent()) / 2) - PlusDpPixelConverter.doIt(mContext,TEXT_Y_OFFSET_DP);
        canvas.drawText(speedText, txtX, txtY, speedTextPaint);
    }

    private void drawSpeedIndicator(Canvas canvas) {

        RectF oval = getOval(canvas, 1);
        float radius = oval.width() * 0.35f;
        float circleRadius = 40;

        //float angle = 10 + (float) (getSpeed() / getMaxSpeed() * 160); //이거 수정해야 하니더!!
        float angle = -40;
        float x = (float) (oval.centerX() + Math.sin((angle) / 180 * Math.PI) * radius);
        float y =  (float) (oval.centerY() - Math.cos(angle / 180 * Math.PI) * radius);

        canvas.drawCircle(x, y, circleRadius, speedIndicatorPaint);



    }






    private void drawTicks(Canvas canvas) {
        float availableAngle = 240;
        float majorStep = (float) (majorTickStep / maxSpeed * availableAngle);
        float minorStep = majorStep / (1 + minorTicks);

        float majorTicksLength = PlusDpPixelConverter.doIt(mContext,MAJOR_TICK_LENGTH);
        float minorTicksLength = majorTicksLength / 2;

        RectF oval = getOval(canvas, 1);
        float radius = oval.width() * 0.35f;

        float currentAngle = -125;
        while (currentAngle <= 125) {

            canvas.drawLine(
                    (float) (oval.centerX() + Math.sin((currentAngle) / 180 * Math.PI) * (radius - majorTicksLength / 2)),
                    (float) (oval.centerY() - Math.cos(currentAngle / 180 * Math.PI) * (radius - majorTicksLength / 2)),
                    (float) (oval.centerX() + Math.sin((currentAngle) / 180 * Math.PI) * (radius + majorTicksLength / 2)),
                    (float) (oval.centerY() - Math.cos(currentAngle / 180 * Math.PI) * (radius + majorTicksLength / 2)),
                    ticksPaint
            );

            for (int i = 1; i <= minorTicks; i++) {
                float angle = currentAngle + i * minorStep;
                if (angle >= 170 + minorStep / 2) {
                    break;
                }
                canvas.drawLine(
                        (float) (oval.centerX() + Math.sin((angle) / 180 * Math.PI) * radius),
                        (float) (oval.centerY() - Math.cos(angle / 180 * Math.PI) * radius),
                        (float) (oval.centerX() + Math.sin((angle) / 180 * Math.PI) * (radius + minorTicksLength)),
                        (float) (oval.centerY() - Math.cos(angle / 180 * Math.PI) * (radius + minorTicksLength)),
                        ticksPaint
                );
            }


            currentAngle += majorStep;
        }


        //canvas.drawArc(smallOval, 185, 170, false, colorLinePaint);
        RectF smallOval = getOval(canvas, 0.7f);
        //시작각도는 동쪽(오른쪽)부터 0도 시계방향으로 계산하니더. 그리고 이동할 각도는 고정된 위치가 아니니더
        canvas.drawArc(smallOval, 135, 270, false, colorLinePaint);

    }


    private RectF getOval(Canvas canvas, float factor) {
        RectF oval;
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
                if (canvasHeight  >= canvasWidth) {
            oval = new RectF(0, 0, canvasWidth * factor, canvasWidth * factor);
        } else {
            oval = new RectF(0, 0, canvasHeight * factor, canvasHeight * factor);
        }

        oval.offset((canvasWidth - oval.width()) / 2 + getPaddingLeft(), (canvasHeight - oval.height()) / 2 + getPaddingTop());

        return oval;
    }



    @SuppressWarnings("NewApi")
    private void init(Context context) {

        mContext = context;

        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        initPaint();
        initTick();



    }

    private void initTick() {
       setMaxSpeed(200);
       setMajorTickStep(10);
       setMinorTicks(4);
    }

    private void initAttributes(Context context,AttributeSet attrs) {
        float density = getResources().getDisplayMetrics().density;
//        TypedArray attributes = context.getTheme().obtainStyledAttributes(
//                attrs,
//                R.styleable.SpeedView,
//                0, 0);
//
//        try {
//            // read attributes
//            setMaxSpeed(attributes.getFloat(R.styleable.SpeedView_maxSpeed, (float) DEFAULT_MAX_SPEED));
//            setSpeed(attributes.getFloat(R.styleable.SpeedView_speed, 0));
//        } finally {
//            attributes.recycle();
//        }
    }

    private void initPaint() {
        Typeface digitalFont = Typeface.createFromAsset(mContext.getAssets(), "digital-7.ttf");



        speedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedTextPaint.setColor(Color.RED);
        speedTextPaint.setTextSize(PlusDpPixelConverter.doIt(mContext,SPEED_TEXT_SIZE_DP));
        speedTextPaint.setTextAlign(Paint.Align.CENTER);
        speedTextPaint.setTypeface(digitalFont);


        ticksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ticksPaint.setStrokeWidth(3.0f);
        ticksPaint.setStyle(Paint.Style.STROKE);
        ticksPaint.setColor(defaultColor);

        colorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorLinePaint.setStyle(Paint.Style.STROKE);
        colorLinePaint.setStrokeWidth(5);
        colorLinePaint.setColor(defaultColor);


        speedIndicatorPaint =  new Paint();
        speedIndicatorPaint.setStyle(Paint.Style.FILL);
        speedIndicatorPaint.setColor(Color.BLUE);

        roadTextPaint =  new Paint();
        roadTextPaint.setStyle(Paint.Style.FILL);
        roadTextPaint.setColor(Color.BLACK);
        roadTextPaint.setTextSize(PlusDpPixelConverter.doIt(mContext,ROAD_TEXT_SIZE_DP));
        roadTextPaint.setTextAlign(Paint.Align.CENTER);
        roadTextPaint.setTypeface(digitalFont);

    }


}
