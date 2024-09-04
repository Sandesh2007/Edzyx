// Dang this and Profile frgment took a lot of time whole 3 days ;(

package com.sandesh.note_app;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class Edit_profile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profile;
    private TextView change_passed, changeusername ,delete_acc,verify_delete_acc_txt;
    private EditText n_username, n_passwd, r_passwd,verify_delete_acc;
    private Button Change_username, change_password,uploadprofile,delete_acc_btn;
    private  Uri uriImage;
    private SwipeRefreshLayout swipeRefreshLayout;

    FirebaseAuth auth;
    StorageReference storageReference;
    FirebaseUser user;

    private static final String CACHE_FILE_NAME = "user_data.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        profile = findViewById(R.id.c_profile);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Call the method to refresh data
        swipeRefreshLayout.setOnRefreshListener(this::refreshProfileData);


        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        user= auth.getCurrentUser();

        storageReference=FirebaseStorage.getInstance().getReference("Profilepics");

        Uri uri = user.getPhotoUrl();

        //Sets users currnt in imageview
        Picasso.get().load(uri).into(profile);


        // Initialize UI elements
        delete_acc = findViewById(R.id.delete_acc);
        verify_delete_acc_txt = findViewById(R.id.verify_delete_acc_txt);
        verify_delete_acc = findViewById(R.id.verify_delete_acc);
        delete_acc_btn = findViewById(R.id.delete_acc_btn);

        uploadprofile = findViewById(R.id.u_profile);
        changeusername = findViewById(R.id.change_usrname);
        change_passed = findViewById(R.id.change_pswd);
        n_username = findViewById(R.id.new_username);
        n_passwd = findViewById(R.id.new_password);
        r_passwd = findViewById(R.id.re_password);
        Change_username = findViewById(R.id.c_username);
        change_password = findViewById(R.id.c_passwd);

        // Set initial visibility
        verify_delete_acc.setVisibility(View.GONE);
        verify_delete_acc_txt.setVisibility(View.GONE);
        delete_acc_btn.setVisibility(View.GONE);
        delete_acc_btn.setEnabled(false);
        n_username.setVisibility(View.GONE);
        n_passwd.setVisibility(View.GONE);
        r_passwd.setVisibility(View.GONE);
        Change_username.setVisibility(View.GONE);
        change_password.setVisibility(View.GONE);



        // Delete acc
        delete_acc.setOnClickListener(view -> {
            if(verify_delete_acc_txt.getVisibility() == View.GONE) {
                // Show verification UI
                verify_delete_acc_txt.setVisibility(View.VISIBLE);
                verify_delete_acc.setVisibility(View.VISIBLE);
                delete_acc_btn.setVisibility(View.VISIBLE);
                int selected = ContextCompat.getColor(Edit_profile.this, R.color.success_color);
                delete_acc.setTextColor(selected);

                // Set an OnClickListener for the delete button
                delete_acc_btn.setOnClickListener(v -> {
                    String password = verify_delete_acc.getText().toString();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    assert user != null;
                    AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), password);

                    user.reauthenticate(credential).addOnCompleteListener(task -> {
                        delete_acc_btn.setEnabled(true);
                        if (task.isSuccessful()) {
                            delete_acc_btn.setEnabled(true);
                            deleteUserData(user);
                        } else {
                            Toast.makeText(Edit_profile.this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } else {
                // Hide verification UI
                verify_delete_acc_txt.setVisibility(View.GONE);
                verify_delete_acc.setVisibility(View.GONE);
                delete_acc_btn.setVisibility(View.GONE);
                int unselected = ContextCompat.getColor(Edit_profile.this, R.color.primary_text_light);
                delete_acc.setTextColor(unselected);
                verify_delete_acc.setText("");
            }
        });


        //Change profile pic
         profile.setOnClickListener(view -> openfileChooser());

         uploadprofile.setOnClickListener(view -> uploadpic());

        // Change username visibility
        changeusername.setOnClickListener(v -> {
            if (n_username.getVisibility() == View.GONE) {
                n_username.setVisibility(View.VISIBLE);
                Change_username.setVisibility(View.VISIBLE);
                int selected = ContextCompat.getColor(Edit_profile.this, R.color.success_color);
                changeusername.setTextColor(selected);
            } else {
                n_username.setVisibility(View.GONE);
                Change_username.setVisibility(View.GONE);
                int unselected = ContextCompat.getColor(Edit_profile.this, R.color.primary_text_light);
                changeusername.setTextColor(unselected);
            }
        });

        // Change password visibility
        change_passed.setOnClickListener(v -> {
            if (n_passwd.getVisibility() == View.GONE) {
                n_passwd.setVisibility(View.VISIBLE);
                r_passwd.setVisibility(View.VISIBLE);
                change_password.setVisibility(View.VISIBLE);
                int selected = ContextCompat.getColor(Edit_profile.this, R.color.success_color);
                change_passed.setTextColor(selected);
            } else {
                n_passwd.setVisibility(View.GONE);
                r_passwd.setVisibility(View.GONE);
                change_password.setVisibility(View.GONE);
                int unselected = ContextCompat.getColor(Edit_profile.this, R.color.primary_text_light);
                change_passed.setTextColor(unselected);
            }
        });

        // Change username button click
        Change_username.setOnClickListener(v -> {
            String newUsername = n_username.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                updateUsername(newUsername);
            } else {
                Toast.makeText(Edit_profile.this, "Please enter a username.", Toast.LENGTH_SHORT).show();
            }
        });

        // Change password button click
        change_password.setOnClickListener(v -> {
            String newPassword = n_passwd.getText().toString().trim();
            String rePassword = r_passwd.getText().toString().trim();
            if (!newPassword.isEmpty() && newPassword.equals(rePassword)) {
                updatePassword(newPassword);
            } else {
                Toast.makeText(Edit_profile.this, "Passwords do not match or are empty.", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Inside this block the methods do same as their name no need to add extra line of comments <--
    private void deleteUserData(FirebaseUser user) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        databaseReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleteUserAccount(user);
            } else {
                Toast.makeText(Edit_profile.this, "Failed to delete user data. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserAccount(FirebaseUser user) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Edit_profile.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                logoutAndRedirect();
            } else {
                Toast.makeText(Edit_profile.this, "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutAndRedirect() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Edit_profile.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void refreshProfileData() {
        fetchDataFromFirebase();
    }


    private void fetchDataFromFirebase() {
        String userId = user.getUid();  // Get the current user's ID
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Update the cache and UI with the new data
                updateCacheAndUI(dataSnapshot);

                // Stop the refreshing animation
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.e("Edit_profile", "Error fetching data", databaseError.toException());

                // Stop the refreshing animation
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateCacheAndUI(DataSnapshot dataSnapshot) {
        // Update username and email in the cache
        String newUsername = dataSnapshot.child("username").getValue(String.class);
        String newEmail = dataSnapshot.child("email").getValue(String.class);

        // save the updated data to the cache
        saveUserDataToCache(newUsername, newEmail);

        // set the username
        n_username.setText(newUsername);
    }
        // -->

        // This whole long thing is used to change th profile picture ;(<--
        private void uploadpic() {
            if (uriImage != null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                // Get the original file name
                String originalFileName = getFileName(uriImage);

                Toast.makeText(this, "Uploading picture may take some time ...", Toast.LENGTH_SHORT).show();

                // Upload profile pic with the original file name
                StorageReference fileReference = storageReference.child(Objects.requireNonNull(originalFileName));

                // Upload image to storage
                fileReference.putFile(uriImage).addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {

                        user = auth.getCurrentUser();

                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        user.updateProfile(profileChangeRequest);
                    });
                    progressDialog.dismiss();
                    Toast.makeText(Edit_profile.this, "Updated profile ", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("com.sandesh.note_app.PROFILE_UPDATED");
                    LocalBroadcastManager.getInstance(Edit_profile.this).sendBroadcast(intent);

                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(Edit_profile.this, "Failed to upload picture", Toast.LENGTH_SHORT).show();
                }).addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded: " + (int) progress + "%");
                });
            } else {
                Toast.makeText(this, "Please select a picture", Toast.LENGTH_SHORT).show();
            }
        }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }



//    private String getFileExtension(Uri uriImage) {
//        ContentResolver cR = getContentResolver();
//        MimeTypeMap mime = MimeTypeMap.getSingleton();
//        return  mime.getExtensionFromMimeType(cR.getType(uriImage));
//    }

    private void openfileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data !=null &&  data.getData() !=null){
            uriImage = data.getData();
            profile.setImageURI(uriImage);
        }

    }
        // -->
    // TO change new username
    private void updateUsername(String newUsername) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.updateProfile(
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newUsername)
                                    .build())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update cache
                            String email = user.getEmail(); // Fetch the current email from user profile
                            saveUserDataToCache(newUsername, email);
                            Toast.makeText(Edit_profile.this, "Username updated successfully!", Toast.LENGTH_SHORT).show();
                            n_username.setText("");
                        } else {
                            Toast.makeText(Edit_profile.this, "Error updating username.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
        // To change new password
    private void updatePassword(String newPassword) {
        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Edit_profile.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        n_passwd.setText("");
                        r_passwd.setText("");

                    } else {
                        Toast.makeText(Edit_profile.this, "Error updating password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // This saves userdata to cache for faster displaing details

    private void saveUserDataToCache(String username, String email) {
        File file = new File(getCacheDir(), CACHE_FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write((username + "\n" + email).getBytes());
        } catch (IOException e) {
            Toast.makeText(this, "Internel error !!", Toast.LENGTH_SHORT).show();
        }
    }
}
