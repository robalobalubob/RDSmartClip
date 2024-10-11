package com.example.rdsmartclipper.shapes;

import android.opengl.GLES20;

import com.example.rdsmartclipper.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Cube shape, now that Model exists this is not needed anymore
 * Being kept around for testing purposes
 * IDEA: Set the cube to be used during debug mode
 */
public class Cube {

    // Number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final float[] cubeCoords = {
            // Front face
            -1f, 1f, 1f,  // top-left
            -1f, -1f, 1f,  // bottom-left
            1f, -1f, 1f,  // bottom-right
            1f, 1f, 1f,  // top-right
            // Back face
            -1f, 1f, -1f,
            -1f, -1f, -1f,
            1f, -1f, -1f,
            1f, 1f, -1f,
    };
    final float[] colors = {
            // Front face (red)
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            // Back face (green)
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
    };
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colorBuffer;
    private final ShortBuffer indexBuffer;
    private final short[] drawOrder = {
            // Front face
            0, 1, 2, 0, 2, 3,
            // Right face
            3, 2, 6, 3, 6, 7,
            // Back face
            7, 6, 5, 7, 5, 4,
            // Left face
            4, 5, 1, 4, 1, 0,
            // Top face
            4, 0, 3, 4, 3, 7,
            // Bottom face
            1, 5, 6, 1, 6, 2
    };
    private final int mProgram;

    /**
     * Cube Constructor
     */
    public Cube() {
        // Initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        // Initialize color byte buffer for colors
        ByteBuffer cb = ByteBuffer.allocateDirect(colors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        // Initialize index byte buffer for drawing order
        ByteBuffer ib = ByteBuffer.allocateDirect(drawOrder.length * 2); // 2 bytes per short
        ib.order(ByteOrder.nativeOrder());
        indexBuffer = ib.asShortBuffer();
        indexBuffer.put(drawOrder);
        indexBuffer.position(0);

        // Prepare shaders and OpenGL program
        String vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec4 aColor;" +
                        "varying vec4 vColor;" +
                        "void main() {" +
                        "  vColor = aColor;" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}";

        String fragmentShaderCode =
                "precision mediump float;" +
                        "varying vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Create empty OpenGL Program
        mProgram = GLES20.glCreateProgram();
        // Add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);
        // Add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);
        // Create OpenGL program executables
        GLES20.glLinkProgram(mProgram);
    }

    /**
     * Draws the cube to screen together with MyGLRenderer
     * @param mvpMatrix model-view-projection matrix
     */
    public void draw(float[] mvpMatrix) {
        // Use the program
        GLES20.glUseProgram(mProgram);

        // Get handles
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int colorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Enable vertex array
        GLES20.glEnableVertexAttribArray(positionHandle);
        // Prepare the cube coordinate data
        // Constants
        // 4 bytes per vertex
        int vertexStride = COORDS_PER_VERTEX * 4;
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Enable color array
        GLES20.glEnableVertexAttribArray(colorHandle);
        // Prepare the color data
        GLES20.glVertexAttribPointer(colorHandle, 4,
                GLES20.GL_FLOAT, false, 4 * 4, colorBuffer);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the cube
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}
