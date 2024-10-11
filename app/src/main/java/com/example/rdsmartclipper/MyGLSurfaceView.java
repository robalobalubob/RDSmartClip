package com.example.rdsmartclipper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class MyGLSurfaceView extends GLSurfaceView {

    private MyGLRenderer mRenderer;

    private ScaleGestureDetector scaleGestureDetector;

    // Previous touch coordinates
    private float previousX;
    private float previousY;

    // Flags to distinguish between pinch zoom and pan
    private boolean isScaling = false;

    public MyGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    // Constructor for inflating from XML
    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Initialization code
    private void init(Context context) {
        // Set OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer(context);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render when updated
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Initialize the scale detector
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean needRender = false;

        // Handle pinch zoom
        if (scaleGestureDetector.onTouchEvent(event)) {
            isScaling = true;
            needRender = true;
        }

        // Handle panning
        if (!isScaling) {
            int action = event.getActionMasked();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    previousX = event.getX();
                    previousY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    float y = event.getY();

                    float deltaX = x - previousX;
                    float deltaY = y - previousY;

                    // Adjust panning (normalize based on view size)
                    float width = getWidth();
                    float height = getHeight();

                    float normalizedDeltaX = deltaX / width * 2f; // Scale factor can be adjusted
                    float normalizedDeltaY = -deltaY / height * 2f; // Invert Y-axis

                    mRenderer.adjustPan(normalizedDeltaX, normalizedDeltaY);

                    previousX = x;
                    previousY = y;

                    needRender = true;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isScaling = false;
                    break;
            }
        }

        if (needRender) {
            requestRender();
        }

        return true;
    }

    // Scale gesture listener for pinch zoom
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mRenderer.adjustScale(detector.getScaleFactor());
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScaling = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaling = false;
        }
    }

    public MyGLRenderer getRenderer() {
        return mRenderer;
    }
}
