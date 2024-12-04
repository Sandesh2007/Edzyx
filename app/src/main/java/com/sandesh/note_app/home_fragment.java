package com.sandesh.note_app;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import io.noties.markwon.Markwon;

public class home_fragment extends Fragment {
    private TextView textViewContent;
    boolean dont_show_again=false;
    String C_versionName;
    CheckBox checkBox;
    private Markwon markwon;
    private static final String CACHE_FILE_NAME = "README_cache.md";
    private static final String VERSION_URL = "https://github.com/Sandesh2007/Edzyx/raw/main/version.json";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ThemeUtils.applyTheme(getContext());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_fragment, container, false);

        // Find the TextView by its ID
        textViewContent = view.findViewById(R.id.text_content);

        //ask for notification permission and if not granted ask again
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (getContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
        }


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM token failed", task.getException());
                        return;
                    }

                    // Get the FCM token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                });

        // Initialize Markwon
        markwon = Markwon.create(getContext());

        // Load the content from Firebase with caching
        loadReadmeFromFirebase();

        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }

        try {
            C_versionName = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


        checkForUpdates();


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the cache file when the app is closed
        deleteCacheFile();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showNoInternetDialog() {
        NoInternetDialogFragment dialog = new NoInternetDialogFragment();
        dialog.show(getParentFragmentManager(), "NoInternetDialog");
        textViewContent.setText(R.string.no_internet);
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

    private void checkForUpdates() {
        SharedPreferences preferences = getContext().getSharedPreferences("UpdatePrefs", Context.MODE_PRIVATE);
        dont_show_again = preferences.getBoolean("dont_show_again", false);

        if (dont_show_again) {
            // Skip showing the update dialog if "Don't show again" was selected
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, VERSION_URL, null,
                response -> {
                    try {
                        String latestVersion = response.getString("version");
                        String apkUrl = response.getString("apk_url");

                        if (!C_versionName.equals(latestVersion)) {
                            showUpdateDialog(apkUrl);
                        } else {
                            // No update available
                            showCustomToast(getContext(),"App is at latest version.");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

        queue.add(jsonObjectRequest);
    }


    private void showUpdateDialog(final String apkUrl) {

        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);

        // Find the TextViews and Buttons in the dialog layout
        TextView updateInfo = dialogView.findViewById(R.id.update_info);
        TextView updateDetails = dialogView.findViewById(R.id.update_details);
        TextView info = dialogView.findViewById(R.id.info);
        checkBox = dialogView.findViewById(R.id.checkbox);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        Button confirmButton = dialogView.findViewById(R.id.btn_confirm);

        // Set info about update
        updateInfo.setText("Update Available v" + C_versionName);
        updateDetails.setText("A new version of the app is available. Would you like to update?");
        info.setText("» Added new features and bug fixes.");

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialogTheme);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Load the previous state of `dont_show_again` from SharedPreferences
        SharedPreferences preferences = getContext().getSharedPreferences("UpdatePrefs", Context.MODE_PRIVATE);
        dont_show_again = preferences.getBoolean("dont_show_again", false);

        // If the checkbox is checked, store the preference in SharedPreferences
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dont_show_again = true;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("dont_show_again", true);
                editor.apply();
                }
        });

        // Set Cancel button behavior
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Set Confirm button behavior
        confirmButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
            startActivity(intent);
            dialog.dismiss();
        });

        // Create and show the dialog
        dialog.show();
    }


    private void showCustomToast(Context context, String message) {
        // inflate the custom toast layout
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, getView().findViewById(R.id.custm_toast));

        // set the text and image in the custom toast
        TextView toastText = toastLayout.findViewById(R.id.toast_text);

        toastText.setText(message);

        // create and display the toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout); // Set custom view
        toast.show();
    }
}