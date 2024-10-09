package com.example.rdsmartclipper;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.rdsmartclipper.shapes.Cube;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    // Rotation angles (volatile for thread safety)
    public volatile float angleX;
    public volatile float angleY;
    public volatile float angleZ;
    private Cube cube;

    public MyGLRenderer(Context context) {
    }

    // Utility method for compiling a shader
    public static int loadShader(int type, String shaderCode) {
        // Create a shader
        int shader = GLES20.glCreateShader(type);

        // Add the source code and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        // Initialize a cube
        cube = new Cube();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                0f, 0f, -5f,  // Camera position
                0f, 0f, 0f,   // Look at point
                0f, 1.0f, 0f  // Up vector
        );

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Apply rotation transformations
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleX, 1f, 0f, 0f);
        Matrix.rotateM(mModelMatrix, 0, angleY, 0f, 1f, 0f);
        Matrix.rotateM(mModelMatrix, 0, angleZ, 0f, 0f, 1f);

        // Combine the model matrix with the projection and camera view
        float[] scratch = new float[16];
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mModelMatrix, 0);

        // Draw the cube
        cube.draw(scratch);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // This projection matrix is applied to object coordinates in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    // Method to update rotation angles safely
    public void setRotationAngles(float angleX, float angleY, float angleZ) {
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;
    }
}
