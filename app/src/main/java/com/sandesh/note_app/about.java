package com.sandesh.note_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

public class about extends AppCompatActivity {
    String C_versionName;
    String latestVersion;


    private static final String VERSION_URL = "https://github.com/Sandesh2007/Edzyx/raw/main/version.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageView githubRepo = findViewById(R.id.github_repo);


        // Display the current app version
        try {
           C_versionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        TextView currentVersion = findViewById(R.id.current_version);
        currentVersion.setText("Current version: v"+C_versionName);

        if (isDarkMode()) {
            githubRepo.setImageResource(R.drawable.github_icon_dark);
        }

        // Facebook Link
        ImageView facebookLink = findViewById(R.id.facebook_link);
        facebookLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=100060033657219"));
            startActivity(browserIntent);
        });

        // Instagram Link
        ImageView instagramLink = findViewById(R.id.instagram_link);
        instagramLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/sandesh_sharma07/"));
            startActivity(browserIntent);
        });

        // GitHub Link
        TextView githubLink = findViewById(R.id.github_link);
        githubLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sandesh2007/Edzyx/issues"));
            startActivity(browserIntent);
        });

        githubRepo.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sandesh2007/"));
            startActivity(browserIntent);
        });

        // Check for Update Button
        Button checkUpdateButton = findViewById(R.id.check_update_button);
        checkUpdateButton.setOnClickListener(v -> checkForUpdates());
    }

    private void checkForUpdates() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, VERSION_URL, null,
                response -> {
                    try {
                        latestVersion = response.getString("version");
                        String apkUrl = response.getString("apk_url");

                        if (!C_versionName.equals(latestVersion)) {
                            showUpdateDialog(apkUrl);
                        } else {
                            // No update available
                            showCustomToast(about.this,"App is at latest version.");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

        queue.add(jsonObjectRequest);
    }
    public boolean isDarkMode() {
        int nightModeFlags =
                getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return false;
            default:
                return false;
        }
    }

    private void showUpdateDialog(final  String apkUrl) {

        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialogue2, null);

        // Find the TextViews and Buttons in the dialog layout
        TextView updateInfo = dialogView.findViewById(R.id.update_info);
        TextView updateDetails = dialogView.findViewById(R.id.update_details);
        TextView info = dialogView.findViewById(R.id.info);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        Button confirmButton = dialogView.findViewById(R.id.btn_confirm);

        //set info about update
        updateInfo.setText("Update Available v"+latestVersion);
        updateDetails.setText(R.string.update_available);
        info.setText(R.string.update_info);

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.CustomDialogTheme);
        builder.setView(dialogView);
        AlertDialog dialog=builder.create();;

        // Set dialog title
        builder.setTitle("Update Available");

        // Set Cancel button behavior
        cancelButton.setOnClickListener(v -> {
            showCustomToast(about.this,"Update cancled by user!!");
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
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custm_toast));
        TextView toastText = toastLayout.findViewById(R.id.toast_text);
        toastText.setText(message);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout); // Set custom view
        toast.show();
    }
}
