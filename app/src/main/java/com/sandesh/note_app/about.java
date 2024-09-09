package com.sandesh.note_app;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
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

    private static final String VERSION_URL = "https://github.com/Sandesh2007/Edzyx/raw/main/version.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Display the current app version
        try {
           C_versionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        TextView currentVersion = findViewById(R.id.current_version);
        currentVersion.setText("Current Version: " + "v"+C_versionName);

        // Facebook Link
        TextView facebookLink = findViewById(R.id.facebook_link);
        facebookLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=100060033657219"));
            startActivity(browserIntent);
        });

        // Instagram Link
        TextView instagramLink = findViewById(R.id.instagram_link);
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

        TextView githubRepo = findViewById(R.id.github_repo);
        githubRepo.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sandesh2007/Edzyx/"));
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
                        String latestVersion = response.getString("version");
                        String apkUrl = response.getString("apk_url");

                        if (!C_versionName.equals(latestVersion)) {
                            promptUpdate(apkUrl);
                        } else {
                            // No update available
                            Toast.makeText(this, "App is at latest version", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

        queue.add(jsonObjectRequest);
    }

    private void promptUpdate(final String apkUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Available")
                .setMessage("A new version of the app is available. Would you like to update?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // User canceled the dialog
                    Toast.makeText(this, "Update cancled by user!!", Toast.LENGTH_SHORT).show();
                });
        builder.create().show();
    }
}
