package com.sandesh.note_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.noties.markwon.Markwon;

public class home_fragment extends Fragment {

    private TextView textViewContent;
    private Markwon markwon;
    private static final String CACHE_FILE_NAME = "README_cache.md";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_fragment, container, false);

        // Find the TextView by its ID
        textViewContent = view.findViewById(R.id.text_content);

        // Initialize Markwon
        markwon = Markwon.create(getContext());

        // Load the content from Firebase with caching
        loadReadmeFromFirebase();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the cache file when the app is closed
        deleteCacheFile();
    }

    private void loadReadmeFromFirebase() {
        File cacheFile = new File(getContext().getCacheDir(), CACHE_FILE_NAME);

        if (cacheFile.exists()) {
            // If cache file exists, read from it
            loadFromCache(cacheFile);
        } else {
            // If cache file does not exist, load from Firebase and cache it
            downloadAndCacheReadme(cacheFile);
        }
    }

    private void loadFromCache(File cacheFile) {
        try {
            String content = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                content = new String(java.nio.file.Files.readAllBytes(cacheFile.toPath()));
            }
            // Render markdown content to the TextView
            markwon.setMarkdown(textViewContent, content);
        } catch (IOException e) {
            textViewContent.setText(R.string.failed_to_load_content);
        }
    }

    private void downloadAndCacheReadme(File cacheFile) {
        // Reference to your README.md file in Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("README.md");

        // Download and read the file
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(bytes);
                String content = new String(bytes);
                // Render markdown content to the TextView
                markwon.setMarkdown(textViewContent, content);
            } catch (IOException e) {
                e.printStackTrace();
                textViewContent.setText(R.string.failed_to_load_content);
            }
        }).addOnFailureListener(exception -> {
            // Handle any errors
            textViewContent.setText(R.string.failed_to_load_content);
        });
    }

    private void deleteCacheFile() {
        File cacheFile = new File(getContext().getCacheDir(), CACHE_FILE_NAME);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }
}
