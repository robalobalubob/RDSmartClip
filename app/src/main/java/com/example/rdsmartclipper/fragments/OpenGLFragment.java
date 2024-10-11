package com.example.rdsmartclipper.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.rdsmartclipper.MyGLRenderer;
import com.example.rdsmartclipper.MyGLSurfaceView;
import com.example.rdsmartclipper.R;
import com.example.rdsmartclipper.SharedViewModel;

public class OpenGLFragment extends Fragment {

    private MyGLSurfaceView glSurfaceView;
    private MyGLRenderer renderer;

    public OpenGLFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout that includes the GLSurfaceView
        View view = inflater.inflate(R.layout.fragment_opengl, container, false);

        glSurfaceView = view.findViewById(R.id.gl_surface_view);
        renderer = glSurfaceView.getRenderer();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (glSurfaceView != null) {
            glSurfaceView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        handleToolbar();

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

    private void handleToolbar() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        Toolbar toolbar = activity.findViewById(R.id.toolbar);

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

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().setTitle("SmartClip");
            toolbar.setNavigationOnClickListener(null);
        }
    }
}
