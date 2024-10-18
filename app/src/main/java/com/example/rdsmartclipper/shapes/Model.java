package com.example.rdsmartclipper.shapes;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
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
import java.nio.IntBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {

    private static final String TAG = "Model";

    // VBO and IBO IDs
    private int[] vboIds = new int[3]; // 0: Vertex, 1: TexCoord, 2: Normal
    private int iboId;

    private int numIndices;

    // OpenGL handles
    private int mProgram;

    // Texture ID
    private int textureId;

    // Shader code with improved lighting
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec2 aTexCoord;" +
                    "attribute vec3 aNormal;" +
                    "varying vec2 vTexCoord;" +
                    "varying vec3 vNormal;" +
                    "varying vec3 vPosition;" +
                    "void main() {" +
                    "  vTexCoord = aTexCoord;" +
                    "  vNormal = aNormal;" +
                    "  vPosition = vec3(aPosition);" +
                    "  gl_Position = uMVPMatrix * aPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "varying vec2 vTexCoord;" +
                    "varying vec3 vNormal;" +
                    "varying vec3 vPosition;" +
                    "void main() {" +
                    "  vec3 normal = normalize(vNormal);" +
                    "  vec3 lightDir = normalize(vec3(0.0, 0.0, 1.0));" +
                    "  vec3 viewDir = normalize(-vPosition);" +
                    "  vec3 reflectDir = reflect(-lightDir, normal);" +
                    "  float diff = max(dot(normal, lightDir), 0.1);" +
                    "  float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);" +
                    "  vec4 texColor = texture2D(uTexture, vTexCoord);" +
                    "  vec3 ambient = 0.1 * texColor.rgb;" +
                    "  vec3 finalColor = ambient + diff * texColor.rgb + spec * vec3(1.0, 1.0, 1.0);" +
                    "  gl_FragColor = vec4(finalColor, texColor.a);" +
                    "}";

    /**
     * Constructor
     *
     * @param context  Application context
     * @param fileName OBJ file name in assets
     */
    public Model(Context context, String fileName) {
        loadOBJ(context, fileName);
        setupShaders();
        //setupBuffers();
    }

    /**
     * Loads the OBJ file and parses vertex, texture coordinate, and normal data.
     *
     * @param context  Application context
     * @param fileName OBJ file name in assets
     */
    private void loadOBJ(Context context, String fileName) {
        // Temporary lists to hold data
        List<float[]> tempVertices = new ArrayList<>();
        List<float[]> tempTexCoords = new ArrayList<>();
        List<float[]> tempNormals = new ArrayList<>();

        // Maps and lists for indexed drawing
        Map<VertexKey, Integer> vertexMap = new HashMap<>();
        List<Float> verticesList = new ArrayList<>();
        List<Float> texCoordsList = new ArrayList<>();
        List<Float> normalsList = new ArrayList<>();
        List<Integer> indicesList = new ArrayList<>();

        // Material handling
        Map<String, Material> materialMap = new HashMap<>();
        String currentMaterialName;
        Material currentMaterial = null;

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("mtllib ")) {
                    // Material library
                    String mtlFileName = line.substring(7).trim();
                    Log.d(TAG, "Loading mtllib: " + mtlFileName);
                    loadMTL(context, mtlFileName+".mtl", materialMap);
                } else if (line.startsWith("usemtl ")) {
                    // Use material
                    currentMaterialName = line.substring(7).trim();
                    currentMaterial = materialMap.get(currentMaterialName);
                    if (currentMaterial == null) {
                        Log.w(TAG, "Material not found: " + currentMaterialName);
                    } else {
                        Log.d(TAG, "Using material: " + currentMaterialName);
                    }
                } else if (line.startsWith("v ")) {
                    // Vertex position
                    String[] tokens = line.split("\\s+");
                    float x = Float.parseFloat(tokens[1]);
                    float y = Float.parseFloat(tokens[2]);
                    float z = Float.parseFloat(tokens[3]);
                    tempVertices.add(new float[]{x, y, z});
                } else if (line.startsWith("vt ")) {
                    // Texture coordinate
                    String[] tokens = line.split("\\s+");
                    float u = Float.parseFloat(tokens[1]);
                    float v = Float.parseFloat(tokens[2]);
                    tempTexCoords.add(new float[]{u, 1.0f - v}); // Flip V coordinate
                } else if (line.startsWith("vn ")) {
                    // Normal vector
                    String[] tokens = line.split("\\s+");
                    float nx = Float.parseFloat(tokens[1]);
                    float ny = Float.parseFloat(tokens[2]);
                    float nz = Float.parseFloat(tokens[3]);
                    tempNormals.add(new float[]{nx, ny, nz});
                } else if (line.startsWith("f ")) {
                    // Face
                    String[] tokens = line.split("\\s+");
                    int[] faceIndices = new int[tokens.length - 1];
                    for (int i = 1; i < tokens.length; i++) {
                        String token = tokens[i];
                        String[] parts = token.split("/");
                        int vertexIndex = Integer.parseInt(parts[0]) - 1;
                        int texCoordIndex = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) - 1 : -1;
                        int normalIndex = parts.length > 2 && !parts[2].isEmpty() ? Integer.parseInt(parts[2]) - 1 : -1;

                        VertexKey key = new VertexKey(vertexIndex, texCoordIndex, normalIndex);
                        Integer index = vertexMap.get(key);
                        if (index == null) {
                            index = verticesList.size() / 3;

                            // Add vertex position
                            float[] vertex = tempVertices.get(vertexIndex);
                            verticesList.add(vertex[0]);
                            verticesList.add(vertex[1]);
                            verticesList.add(vertex[2]);

                            // Add texture coordinate
                            if (texCoordIndex >= 0 && texCoordIndex < tempTexCoords.size()) {
                                float[] texCoord = tempTexCoords.get(texCoordIndex);
                                texCoordsList.add(texCoord[0]);
                                texCoordsList.add(texCoord[1]);
                            } else {
                                texCoordsList.add(0.0f);
                                texCoordsList.add(0.0f);
                            }

                            // Add normal vector
                            if (normalIndex >= 0 && normalIndex < tempNormals.size()) {
                                float[] normal = tempNormals.get(normalIndex);
                                normalsList.add(normal[0]);
                                normalsList.add(normal[1]);
                                normalsList.add(normal[2]);
                            } else {
                                normalsList.add(0.0f);
                                normalsList.add(0.0f);
                                normalsList.add(1.0f);
                            }

                            vertexMap.put(key, index);
                        }
                        indicesList.add(index);
                        faceIndices[i - 1] = index;
                    }

                    // Triangulate faces if necessary
                    if (faceIndices.length > 3) {
                        for (int i = 2; i < faceIndices.length; i++) {
                            indicesList.add(faceIndices[0]);
                            indicesList.add(faceIndices[i - 1]);
                            indicesList.add(faceIndices[i]);
                        }
                    }
                }
            }
            reader.close();

            // Convert lists to arrays
            float[] verticesArray = toFloatArray(verticesList);
            float[] texCoordsArray = toFloatArray(texCoordsList);
            float[] normalsArray = toFloatArray(normalsList);
            int[] indicesArray = toIntArray(indicesList);

            numIndices = indicesArray.length;

            // Prepare buffers
            setupBuffers(verticesArray, texCoordsArray, normalsArray, indicesArray);

            // Load texture if available
            if (currentMaterial != null && currentMaterial.textureFileName != null) {
                Log.d(TAG, "Loading texture: " + currentMaterial.textureFileName);
                loadTexture(context, currentMaterial.textureFileName);
            } else {
                // Load a default white texture
                Log.d(TAG, "No texture found, loading default texture");
                loadDefaultTexture();
            }

        } catch (IOException e) {
            Log.e(TAG, "Failed to load OBJ file: " + e.getMessage());
        }
    }

    /**
     * Loads the MTL file and parses materials.
     *
     * @param context       Application context
     * @param mtlFileName   MTL file name
     * @param materialMap   Map to store materials
     */
    private void loadMTL(Context context, String mtlFileName, Map<String, Material> materialMap) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(mtlFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            Material currentMaterial = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("newmtl ")) {
                    // Start a new material
                    String materialName = line.substring(7).trim();
                    currentMaterial = new Material(materialName);
                    materialMap.put(materialName, currentMaterial);
                } else if (currentMaterial != null) {
                    if (line.startsWith("Kd ")) {
                        // Diffuse color
                        String[] tokens = line.split("\\s+");
                        if (tokens.length >= 4) {
                            float r = Float.parseFloat(tokens[1]);
                            float g = Float.parseFloat(tokens[2]);
                            float b = Float.parseFloat(tokens[3]);
                            currentMaterial.diffuseColor = new float[]{r, g, b};
                        }
                    } else if (line.startsWith("Ka ")) {
                        // Ambient color
                        String[] tokens = line.split("\\s+");
                        if (tokens.length >= 4) {
                            float r = Float.parseFloat(tokens[1]);
                            float g = Float.parseFloat(tokens[2]);
                            float b = Float.parseFloat(tokens[3]);
                            currentMaterial.ambientColor = new float[]{r, g, b};
                        }
                    } else if (line.startsWith("Ks ")) {
                        // Specular color
                        String[] tokens = line.split("\\s+");
                        if (tokens.length >= 4) {
                            float r = Float.parseFloat(tokens[1]);
                            float g = Float.parseFloat(tokens[2]);
                            float b = Float.parseFloat(tokens[3]);
                            currentMaterial.specularColor = new float[]{r, g, b};
                        }
                    } else if (line.startsWith("Ns ")) {
                        // Specular exponent (shininess)
                        String[] tokens = line.split("\\s+");
                        if (tokens.length >= 2) {
                            currentMaterial.shininess = Float.parseFloat(tokens[1]);
                        }
                    } else if (line.startsWith("Tr ") || line.startsWith("d ")) {
                        // Transparency (Tr or d - inverse of transparency)
                        String[] tokens = line.split("\\s+");
                        if (tokens.length >= 2) {
                            float transparency = Float.parseFloat(tokens[1]);
                            // OpenGL uses `d` for transparency and `Tr` for transparency, but they're inverses
                            if (line.startsWith("Tr ")) {
                                transparency = 1.0f - transparency; // Inverse of Tr
                            }
                            currentMaterial.transparency = transparency;
                        }
                    } else if (line.startsWith("illum ")) {
                        // Illumination model
                        String[] tokens = line.split("\\s+");
                        if (tokens.length >= 2) {
                            currentMaterial.illuminationModel = Integer.parseInt(tokens[1]);
                        }
                    } else if (line.startsWith("map_Kd ")) {
                        // Diffuse texture map
                        currentMaterial.textureFileName = line.substring(7).trim();
                    }
                    // Add other material properties here if needed
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to load MTL file: " + e.getMessage());
        }
    }

    /**
     * Sets up shaders and compiles them.
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
     * Sets up VBOs and IBOs.
     */
    private void setupBuffers(float[] verticesArray, float[] texCoordsArray, float[] normalsArray, int[] indicesArray) {
        // Generate VBO and IBO IDs
        vboIds = new int[3];
        int[] buffers = new int[4]; // 3 VBOs + 1 IBO
        GLES20.glGenBuffers(4, buffers, 0);

        vboIds[0] = buffers[0]; // Vertex VBO
        vboIds[1] = buffers[1]; // TexCoord VBO
        vboIds[2] = buffers[2]; // Normal VBO
        iboId = buffers[3];     // IBO

        // Vertex Buffer
        FloatBuffer vertexBuffer = createFloatBuffer(verticesArray);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboIds[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);

        // Texture Coordinate Buffer
        FloatBuffer texCoordBuffer = createFloatBuffer(texCoordsArray);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboIds[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texCoordBuffer.capacity() * 4, texCoordBuffer, GLES20.GL_STATIC_DRAW);

        // Normal Buffer
        FloatBuffer normalBuffer = createFloatBuffer(normalsArray);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboIds[2]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, normalBuffer.capacity() * 4, normalBuffer, GLES20.GL_STATIC_DRAW);

        // Index Buffer
        IntBuffer indexBuffer = createIntBuffer(indicesArray);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboId);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 4, indexBuffer, GLES20.GL_STATIC_DRAW);

        // Unbind buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Draws the model using the provided MVP matrix.
     *
     * @param mvpMatrix Model-View-Projection matrix
     */
    public void draw(float[] mvpMatrix) {
        if (mProgram == 0) return;

        GLES20.glUseProgram(mProgram);

        // Get attribute and uniform locations
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int texCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        int normalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        int textureHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");

        // Bind VBOs and set vertex attributes
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboIds[0]);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboIds[1]);
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboIds[2]);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);

        // Bind IBO
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboId);

        // Apply scaling
        float[] scaledMVPMatrix = new float[16];
        float[] scalingMatrix = new float[16];
        Matrix.setIdentityM(scalingMatrix, 0);
        // Scaling factor
        float scale = 1.0f;
        Matrix.scaleM(scalingMatrix, 0, scale, scale, scale);
        Matrix.multiplyMM(scaledMVPMatrix, 0, mvpMatrix, 0, scalingMatrix, 0);

        // Set the MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, scaledMVPMatrix, 0);

        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(textureHandle, 0);

        // Draw the model using indexed drawing
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_INT, 0);

        // Disable vertex attributes and unbind buffers
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Loads a texture from assets.
     *
     * @param context         Application context
     * @param textureFileName Texture file name in assets
     */
    private void loadTexture(Context context, String textureFileName) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(textureFileName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            textureId = textures[0];

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle();
        } catch (IOException e) {
            Log.e(TAG, "Failed to load texture: " + e.getMessage());
            loadDefaultTexture();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close texture input stream: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Loads a default white texture.
     */
    private void loadDefaultTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFFFFFFFF); // White color
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();
    }

    // Utility methods

    private float[] toFloatArray(List<Float> list) {
        int size = list.size();
        float[] array = new float[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private int[] toIntArray(List<Integer> list) {
        int size = list.size();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private FloatBuffer createFloatBuffer(float[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(array);
        fb.position(0);
        return fb;
    }

    private IntBuffer createIntBuffer(int[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer ib = bb.asIntBuffer();
        ib.put(array);
        ib.position(0);
        return ib;
    }

    // Inner classes

    /**
     * Represents a unique combination of vertex attributes.
     */
    private static class VertexKey {
        int vertexIndex;
        int texCoordIndex;
        int normalIndex;

        VertexKey(int vertexIndex, int texCoordIndex, int normalIndex) {
            this.vertexIndex = vertexIndex;
            this.texCoordIndex = texCoordIndex;
            this.normalIndex = normalIndex;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof VertexKey)) return false;
            VertexKey other = (VertexKey) obj;
            return vertexIndex == other.vertexIndex &&
                    texCoordIndex == other.texCoordIndex &&
                    normalIndex == other.normalIndex;
        }

        @Override
        public int hashCode() {
            int result = vertexIndex;
            result = 31 * result + texCoordIndex;
            result = 31 * result + normalIndex;
            return result;
        }
    }

    /**
     * Represents material properties.
     */
    private static class Material {
        String name;
        float[] diffuseColor = {1.0f, 1.0f, 1.0f}; // Default white diffuse
        float[] ambientColor = {1.0f, 1.0f, 1.0f}; // Default white ambient
        float[] specularColor = {1.0f, 1.0f, 1.0f}; // Default white specular
        float shininess = 32.0f; // Default shininess
        float transparency = 1.0f; // Default fully opaque
        int illuminationModel = 2; // Default Phong shading
        String textureFileName;

        public Material(String name) {
            this.name = name;
        }
    }
}
