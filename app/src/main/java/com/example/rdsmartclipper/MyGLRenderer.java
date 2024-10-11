package com.example.rdsmartclipper;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.rdsmartclipper.shapes.Model;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";

    // Matrices
    private final float[] mMVPMatrix = new float[16];      // Model View Projection Matrix
    private final float[] mProjectionMatrix = new float[16]; // Projection Matrix
    private final float[] mViewMatrix = new float[16];     // View Matrix
    private final float[] mModelMatrix = new float[16];    // Model Matrix

    // Model and context
    private Model model;
    private final Context context;

    // Rotation angles (from rotation data)
    public volatile float angleX;
    public volatile float angleY;
    public volatile float angleZ;

    // Scaling factor
    private float scaleFactor = 1.0f;

    // Panning offsets
    private float panX = 0.0f;
    private float panY = 0.0f;

    // Constructor
    public MyGLRenderer(Context context) {
        this.context = context;
    }

    // Load and compile shader code
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        // Add the source code and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        // Check for compilation errors
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }

    // Initialize OpenGL settings and load the model
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0f, 0f, 0f, 1f); // Black

        // Enable depth testing and back-face culling
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        // Initialize the model
        model = new Model(context, "assembly.obj");
    }

    // Adjust the viewport and projection matrix based on surface changes
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport
        GLES20.glViewport(0, 0, width, height);

        // Calculate the aspect ratio
        float ratio = (float) width / height;

        // Set up the projection matrix (Perspective Projection)
        Matrix.perspectiveM(mProjectionMatrix, 0, 45.0f, ratio, 1f, 100f);
    }

    // Render the frame
    @Override
    public void onDrawFrame(GL10 unused) {
        // Clear the color and depth buffers
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set up the camera (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                0f, 0f, -5f,  // Camera position
                0f, 0f, 0f,   // Look at point
                0f, 1f, 0f    // Up vector
        );

        // Initialize the model matrix
        Matrix.setIdentityM(mModelMatrix, 0);

        // Apply panning
        Matrix.translateM(mModelMatrix, 0, panX, panY, 0f);

        // Apply scaling
        Matrix.scaleM(mModelMatrix, 0, scaleFactor, scaleFactor, scaleFactor);

        // Apply rotation from rotation data
        Matrix.rotateM(mModelMatrix, 0, angleX, 1f, 0f, 0f);
        Matrix.rotateM(mModelMatrix, 0, angleY, 0f, 1f, 0f);
        Matrix.rotateM(mModelMatrix, 0, angleZ, 0f, 0f, 1f);

        // Calculate the Model-View-Projection matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Draw the model
        model.draw(mMVPMatrix);
    }

    // Method to update rotation angles safely
    public void setRotationAngles(float angleX, float angleY, float angleZ) {
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;
    }

    // Method to adjust scale factor
    public void adjustScale(float scaleFactor) {
        this.scaleFactor *= scaleFactor;
        // Limit the scale factor
        this.scaleFactor = Math.max(0.1f, Math.min(this.scaleFactor, 5.0f));
    }

    // Method to adjust panning offsets
    public void adjustPan(float deltaX, float deltaY) {
        // Adjust panning offsets
        this.panX += deltaX;
        this.panY += deltaY;
    }

    // Set absolute scale factor
    public void setScale(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    // Error checking utility
    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
