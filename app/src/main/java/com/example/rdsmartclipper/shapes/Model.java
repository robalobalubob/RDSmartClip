package com.example.rdsmartclipper.shapes;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.rdsmartclipper.MyGLRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Model class
 * Loads and renders a 3D model from an OBJ file
 */
public class Model {
    private static final String TAG = "Model";

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    private int numVertices;

    private int mProgram;

    private float scale = 1.0f;

    // Shader code (simple shaders with normals for basic lighting)
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec3 aNormal;" +
                    "varying vec3 vNormal;" +
                    "void main() {" +
                    "  vNormal = aNormal;" +
                    "  gl_Position = uMVPMatrix * aPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec3 vNormal;" +
                    "void main() {" +
                    "  float light = max(dot(normalize(vNormal), vec3(0.0, 0.0, 1.0)), 0.2);" + // Basic lighting
                    "  gl_FragColor = vec4(light, light, light, 1.0);" +
                    "}";

    /**
     * Constructor
     * @param context Application context
     * @param fileName OBJ file name in assets
     */
    public Model(Context context, String fileName) {
        loadOBJ(context, fileName);
        setupShaders();
    }

    /**
     * Loads the OBJ file and parses vertex and normal data
     * @param context Application context
     * @param fileName OBJ file name in assets
     */
    private void loadOBJ(Context context, String fileName) {
        ArrayList<Float> tempVertices = new ArrayList<>();
        ArrayList<Float> tempNormals = new ArrayList<>();
        ArrayList<Integer> vertexIndices = new ArrayList<>();
        ArrayList<Integer> normalIndices = new ArrayList<>();
        Log.d(TAG, "About to begin loading file: " + fileName);

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            Log.d(TAG, "Begin loading OBJ file: " + fileName);

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    // Vertex position
                    String[] tokens = line.split("\\s+");
                    if (tokens.length >= 4) {
                        tempVertices.add(Float.parseFloat(tokens[1]));
                        tempVertices.add(Float.parseFloat(tokens[2]));
                        tempVertices.add(Float.parseFloat(tokens[3]));
                    }
                } else if (line.startsWith("vn ")) {
                    // Vertex normal
                    String[] tokens = line.split("\\s+");
                    if (tokens.length >= 4) {
                        tempNormals.add(Float.parseFloat(tokens[1]));
                        tempNormals.add(Float.parseFloat(tokens[2]));
                        tempNormals.add(Float.parseFloat(tokens[3]));
                    }
                } else if (line.startsWith("f ")) {
                    // Face
                    String[] tokens = line.split("\\s+");
                    for (int i = 1; i < tokens.length; i++) {
                        String[] parts = tokens[i].split("//"); // Assuming 'v//vn' format, or other formats below
                        if (parts.length == 2) {
                            // Format: v//vn (vertex and normal)
                            int vertexIndex = Integer.parseInt(parts[0]) - 1;
                            int normalIndex = Integer.parseInt(parts[1]) - 1;
                            vertexIndices.add(vertexIndex);
                            normalIndices.add(normalIndex);
                        } else if (parts.length == 1) {
                            // Format: v (only vertex indices, no texture, no normal)
                            try {
                                int vertexIndex = Integer.parseInt(parts[0]) - 1; // Get vertex index
                                vertexIndices.add(vertexIndex);
                                // No normal or texture index, so we'll use default normals later
                                normalIndices.add(-1); // Placeholder for missing normal
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Invalid face format (v): " + tokens[i] + " in line: " + line);
                            }
                        }
                    }
                }
                // Ignoring texture coordinates and other elements
            }
            Log.d(TAG, "Finished loading OBJ file: " + fileName);
            reader.close();

            // Convert lists to arrays
            numVertices = vertexIndices.size();
            float[] vertices = new float[numVertices * 3];
            float[] normals = new float[numVertices * 3];

            for (int i = 0; i < numVertices; i++) {
                int vertexIndex = vertexIndices.get(i);
                if (vertexIndex < 0 || vertexIndex >= tempVertices.size() / 3) {
                    Log.e(TAG, "Vertex index out of bounds: " + vertexIndex);
                    // Assign a default vertex position if index is invalid
                    vertices[i * 3] = 0.0f;
                    vertices[i * 3 + 1] = 0.0f;
                    vertices[i * 3 + 2] = 0.0f;
                } else {
                    // Copy vertex data
                    vertices[i * 3] = tempVertices.get(vertexIndex * 3);
                    vertices[i * 3 + 1] = tempVertices.get(vertexIndex * 3 + 1);
                    vertices[i * 3 + 2] = tempVertices.get(vertexIndex * 3 + 2);
                }

                // Check for normal data
                int normalIndex = normalIndices.get(i);
                if (normalIndex >= 0 && normalIndex < tempNormals.size() / 3) {
                    // Use provided normal data
                    normals[i * 3] = tempNormals.get(normalIndex * 3);
                    normals[i * 3 + 1] = tempNormals.get(normalIndex * 3 + 1);
                    normals[i * 3 + 2] = tempNormals.get(normalIndex * 3 + 2);
                } else {
                    // Assign a default normal (pointing along Z-axis)
                    normals[i * 3] = 0.0f;
                    normals[i * 3 + 1] = 0.0f;
                    normals[i * 3 + 2] = 1.0f;
                }
            }

            // Prepare buffers
            ByteBuffer vb = ByteBuffer.allocateDirect(vertices.length * 4);
            vb.order(ByteOrder.nativeOrder());
            vertexBuffer = vb.asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);

            ByteBuffer nb = ByteBuffer.allocateDirect(normals.length * 4);
            nb.order(ByteOrder.nativeOrder());
            normalBuffer = nb.asFloatBuffer();
            normalBuffer.put(normals);
            normalBuffer.position(0);

        } catch (IOException e) {
            Log.e(TAG, "Failed to load OBJ file: " + e.getMessage());
        }
    }

    /**
     * Sets up shaders and compiles them
     */
    private void setupShaders() {
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();    // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables

        // Check for linking errors
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Error linking program: " + GLES20.glGetProgramInfoLog(mProgram));
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }

    /**
     * Draws the model using the provided MVP matrix
     * @param mvpMatrix Model-View-Projection matrix
     */
    public void draw(float[] mvpMatrix) {
        if (mProgram == 0) return;

        GLES20.glUseProgram(mProgram);

        // Create a scaling matrix
        float[] scalingMatrix = new float[16];
        Matrix.setIdentityM(scalingMatrix, 0);
        Matrix.scaleM(scalingMatrix, 0, scale, scale, scale);

        // Combine the scaling matrix with the MVP matrix
        float[] scaledMVPMatrix = new float[16];
        Matrix.multiplyMM(scaledMVPMatrix, 0, mvpMatrix, 0, scalingMatrix, 0);

        // Get attribute and uniform locations
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int normalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Enable vertex array
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false,
                3 * 4, vertexBuffer);

        // Enable normal array
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glVertexAttribPointer(normalHandle, 3,
                GLES20.GL_FLOAT, false,
                3 * 4, normalBuffer);

        // Pass the transformation matrix to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, scaledMVPMatrix, 0);

        // Draw the model
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numVertices);

        // Disable arrays
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
    }

    /**
     * Sets the scale for the model
     * @param scale Float to scale by
     */
    public void setScale(float scale) {
        this.scale = scale;
    }
}
