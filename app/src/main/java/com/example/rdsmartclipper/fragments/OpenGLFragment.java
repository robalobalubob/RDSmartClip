package com.example.rdsmartclipper.fragments;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.rdsmartclipper.MyGLRenderer;
import com.example.rdsmartclipper.R;
import com.example.rdsmartclipper.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OpenGLFragment extends Fragment {

    private GLSurfaceView glSurfaceView;
    private MyGLRenderer renderer;

    public OpenGLFragment() {
        // Required empty public constructor
    }

    /**
     * Called when view needs to be created
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The inflated view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_opengl, container, false);
    }

    /**
     * Called when view is created
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Find the view
        glSurfaceView = view.findViewById(R.id.gl_surface_view);

        // Set the OpenGL ES context version
        glSurfaceView.setEGLContextClientVersion(3); // Version 3

        // Initialize the renderer
        renderer = new MyGLRenderer(getContext());

        // Set the Renderer for drawing on the GLSurfaceView
        glSurfaceView.setRenderer(renderer);

        // Render when updated
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
        handleToolbar();
    }

    /**
     * Called when view is resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    /**
     * Called when view is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    private void handleToolbar() {
        boolean isFullscreen = getArguments() != null && getArguments().getBoolean("isFullscreen", false);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Model View");

            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back when the back arrow is clicked
                activity.getOnBackPressedDispatcher().onBackPressed();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Reset toolbar to default state
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().setTitle("SmartClip");
            toolbar.setNavigationOnClickListener(null);
        }

        bottomNavigationView.setVisibility(View.VISIBLE);
    }
}
