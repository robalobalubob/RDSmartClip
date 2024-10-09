package com.example.rdsmartclipper;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class OpenGLFragment extends Fragment {

    private GLSurfaceView glSurfaceView;
    private MyGLRenderer renderer;

    public OpenGLFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_opengl, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        glSurfaceView = view.findViewById(R.id.gl_surface_view);

        // Set the OpenGL ES context version
        glSurfaceView.setEGLContextClientVersion(3);

        // Initialize the renderer
        renderer = new MyGLRenderer(getContext());

        // Set the Renderer for drawing on the GLSurfaceView
        glSurfaceView.setRenderer(renderer);

        // Render Continuously Comment to Disable
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Get the SharedViewModel
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe rotation data
        sharedViewModel.getRotationData().observe(getViewLifecycleOwner(), rotationData -> {
            if (rotationData != null) {
                // Update the renderer's rotation angles
                renderer.setRotationAngles(rotationData.getRoll(), rotationData.getPitch(), rotationData.getYaw());

                // Request rendering
                glSurfaceView.requestRender();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
