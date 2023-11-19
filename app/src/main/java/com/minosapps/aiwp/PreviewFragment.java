package com.minosapps.aiwp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.minosapps.aiwp.databinding.TabPreviewBinding;

import java.io.IOException;
import java.io.InputStream;

public class PreviewFragment extends Fragment {

    private Activity activityContext;
    private TabPreviewBinding binding;

    private OpenAIManager openAIManager;
    private StorageManager storageManager;

    private AIWPWallpaperManager aiwpWallpaperManager;

    private Bitmap curBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = TabPreviewBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        openAIManager = new OpenAIManager(getContext());
        aiwpWallpaperManager = new AIWPWallpaperManager(getContext());
        storageManager = new StorageManager(getContext());

        showLastStoredBitmap();

        binding.testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchAndDisplayImage();
            }
        });

        binding.wpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.wpButton.setVisibility(View.GONE);
                binding.wpSpinner.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        aiwpWallpaperManager.setWallpaper(curBitmap);
                        if(activityContext != null && binding != null) {
                            activityContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.wpSpinner.setVisibility(View.GONE);
                                    showLastStoredBitmap();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        return view;
    }


    private void showLastStoredBitmap(){
        try {
            Bitmap lastWp = storageManager.getLastStoredImage();
            if(lastWp == null){return;}
            binding.imageView.setImageBitmap(lastWp);
            binding.imageView.setVisibility(View.VISIBLE);
            curBitmap = lastWp;
        } catch (IOException e) {
            binding.imageView.setVisibility(View.GONE);
        }
    }

    private void fetchAndDisplayImage() {
        showLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final InputStream imageStream = openAIManager.fetchImage();
                    final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    curBitmap = bitmap;
                    showImage(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    showFailure(e.getMessage());
                }
            }
        }).start();
    }

    private void showLoading(){
        if (activityContext != null && binding != null) {
            activityContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.imageView.setImageBitmap(null);
                    binding.imageView.setVisibility(View.GONE);
                    binding.testButton.setVisibility(View.GONE);
                    binding.wpButton.setVisibility(View.GONE);
                    binding.loadingText.setVisibility(View.VISIBLE);
                    binding.loadingSubtext.setVisibility(View.VISIBLE);
                    binding.loadingSpinner.setVisibility(View.VISIBLE);
                    binding.failureSection.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showFailure(String message) {
        if (activityContext != null && binding != null) {
            activityContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.imageView.setVisibility(View.GONE);
                    binding.testButton.setVisibility(View.VISIBLE);
                    binding.loadingText.setVisibility(View.GONE);
                    binding.loadingSubtext.setVisibility(View.GONE);
                    binding.loadingSpinner.setVisibility(View.GONE);
                    binding.failureSection.setVisibility(View.VISIBLE);
                    binding.failureSubtext.setText(message);
                }
            });
        }
    }

    private void showImage(Bitmap bitmap){
        if (activityContext != null && binding != null) {
            activityContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.imageView.setImageBitmap(bitmap);
                    binding.imageView.setVisibility(View.VISIBLE);
                    binding.testButton.setVisibility(View.VISIBLE);
                    binding.wpButton.setVisibility(View.VISIBLE);
                    binding.loadingText.setVisibility(View.GONE);
                    binding.loadingSubtext.setVisibility(View.GONE);
                    binding.loadingSpinner.setVisibility(View.GONE);
                    binding.failureSection.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            activityContext =(Activity) context;
        }
    }
}
