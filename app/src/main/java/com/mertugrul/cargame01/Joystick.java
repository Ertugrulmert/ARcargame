package com.mertugrul.cargame01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.util.AttributeSet;
import android.view.View;

/** Code written following the instructions at: https://www.instructables.com/id/A-Simple-Android-UI-Joystick/
 *  Virtual Joystick Implementation
 *
 */



public class Joystick extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener{
    private float baseX;
    private float baseY;
    private float baseRad;
    private float hatRad;
    private float velocityH;
    private float velocityV;
    private boolean isMoving;



    public Joystick (Context context) {
        super(context);
        getHolder().addCallback(this);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setOnTouchListener(this);
        isMoving = false;
    }
    public Joystick (Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        getHolder().addCallback(this);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setOnTouchListener(this);
        isMoving = false;
    }
    public Joystick (Context context, AttributeSet attributeSet, int style) {
        super(context, attributeSet, style);
        getHolder().addCallback(this);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setOnTouchListener(this);
        isMoving = false;
    }

    private void drawJoystick(float newX, float newY){
        if (getHolder().getSurface().isValid()){
            Canvas canvas = this.getHolder().lockCanvas();
            Paint paint = new Paint();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            paint.setARGB(190,84,206,219);
            canvas.drawCircle(baseX,baseY,baseRad,paint);
            paint.setARGB(255,48,164,242);
            canvas.drawCircle(newX,newY,hatRad,paint);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder){
        baseX = getWidth()/2.0f;
        baseY = getHeight()/2.0f;
        baseRad = Math.min(baseX,baseY)/1.6f;
        hatRad = Math.min(baseX,baseY)/2.5f;
        drawJoystick(baseX,baseY); //set joystick hat at center
        velocityH =0.0f;
        velocityV =0.0f;
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
    }



    //modify joystick position when touched
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view.equals(this)){
            if (event.getAction() != event.ACTION_UP){
                //check for out of bounds
                float distance = (float) Math.sqrt(Math.pow(event.getX()-baseX,2)+Math.pow(event.getY()-baseY,2));
                if (distance<baseRad*0.8) {
                    drawJoystick(event.getX(), event.getY());
                    velocityH = getX()-baseX;
                    velocityV = getY()-baseY;
                    isMoving = false;
                }
                else{
                    float boundedX = baseX+(event.getX()-baseX)*baseRad*0.8f/distance;
                    velocityH = boundedX-baseX;
                    float boundedY = baseY+(event.getY()-baseY)*baseRad*0.8f/distance;
                    velocityV = boundedY-baseY;
                    drawJoystick(boundedX,boundedY);
                    isMoving = true;
                }
            }
            else {
                drawJoystick(baseX, baseY);
                isMoving = false;
            }
        }
        return true;
    }

    public double getDegrees(){
        return Math.atan2(velocityH,velocityV);
    }
    public boolean getMoving() {
        return isMoving;
    }
}
