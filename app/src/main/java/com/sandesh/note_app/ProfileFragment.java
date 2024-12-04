package com.sandesh.note_app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProfileFragment extends Fragment {

    TextView textUsername, textEmail ,textVerification;
    ImageView uproile_image;
     Button settings, logoutBtn ;
     FirebaseAuth profile_Auth;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(profileUpdateReceiver,
                new IntentFilter("com.sandesh.note_app.PROFILE_UPDATED"));
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_fragement, container, false);

        // Initialize Firebase Auth
        profile_Auth = FirebaseAuth.getInstance();

        // Initialize views
        uproile_image = view.findViewById(R.id.profile_image);
        textUsername = view.findViewById(R.id.username);
        textEmail = view.findViewById(R.id.email);
        textVerification= view.findViewById(R.id.verification_status);
        settings = view.findViewById(R.id.edit_profile_button);
        logoutBtn = view.findViewById(R.id.logout);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        textUsername.setFocusable(false);
        textEmail.setFocusable(false);

        swipeRefreshLayout.setOnRefreshListener(this::refreshProfileData);

        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }


        settings.setOnClickListener(v -> showDialog());

        logoutBtn.setOnClickListener(view1 -> {
            profile_Auth.signOut();
            clearUserCache();

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Intent intent = new Intent(requireActivity(), signup.class);
            startActivity(intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            requireActivity().finish();

        });

        // Check email verification and show Toast
        checkEmailVerification();

        return view;
    }

    private void refreshProfileData() {
        FirebaseUser user = profile_Auth.getCurrentUser();
        if (user != null) {
            showProfile(user);
            checkEmailVerification();
            swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
        } else {
            showCustomToast(getContext(),"Unable to get user data at this moment. Try relogging after a few moments.","error");
            swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
        }
    }


    public void checkEmailVerification() {
        FirebaseUser user = profile_Auth.getCurrentUser();
        if (user != null) {
            showProfile(user);
            // Check if the user's email is verified
            if (user.isEmailVerified()) {
                textVerification.setText(R.string.verified); // Set text to "Verified"
                int verifiedGreenColor = ContextCompat.getColor(requireContext(), R.color.success_color1);
                textVerification.setTextColor(verifiedGreenColor); // Set text color to green
                textVerification.setFocusable(false);
            } else {
                textVerification.setText(R.string.not_verified); // If not verified, set text to "Not Verified"
                int notVerifiedRedColor = ContextCompat.getColor(requireContext(), R.color.red);
                textVerification.setTextColor(notVerifiedRedColor); // Set text color to red
                textVerification.setFocusable(false);
            }
        } else {
            showCustomToast(getContext(),"Unable to get user data at this moment. try relogging after few moment ","error");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showNoInternetDialog() {
        NoInternetDialogFragment dialog = new NoInternetDialogFragment();
        dialog.show(getParentFragmentManager(), "NoInternetDialog");
    }


    public void showProfile(FirebaseUser user) {
        if (!isAdded()) return; // Ensure Fragment is attached

        String userId = user.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");

        // Attempt to retrieve cached user data
        String[] cachedData = getUserDataFromCache();
        String cachedUsername = cachedData[0];
        String cachedEmail = cachedData[1];

        if (!cachedUsername.isEmpty() && !cachedEmail.isEmpty()) {
            textUsername.setText(cachedUsername);
            textUsername.setFocusable(false);
            textEmail.setText(cachedEmail);
            textEmail.setFocusable(false);

            // Update profile picture
            Uri uri = user.getPhotoUrl();
            if (uri != null) {
                Picasso.get()
                        .load(uri)
                        .placeholder(R.drawable.profile_image) // Placeholder image
                        .error(R.drawable.profile_nav) // Error image
                        .into(uproile_image);
            } else {
                uproile_image.setImageResource(R.drawable.profile_image); // Default image
            }
        } else {
            // Cache is empty or does not exist, fetch from Firebase
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isAdded()) { // Ensure Fragment is attached
                        ReadWriteUserDetails readWriteUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                        if (readWriteUserDetails != null) {
                            String username = user.getDisplayName();
                            String user_email = user.getEmail();

                            // Cache the user data
                            saveUserDataToCache(username, user_email);

                            // Update UI
                            textUsername.setText(username);
                            textEmail.setText(user_email);

                            // Update profile picture
                            Uri uri = user.getPhotoUrl();
                            if (uri != null) {
                                Picasso.get()
                                        .load(uri)
                                        .placeholder(R.drawable.profile_image) // Placeholder image
                                        .error(R.drawable.profile_image) // Error image
                                        .into(uproile_image);
                                refreshProfileImage(); //remove this if any bug//
                            } else {
                                uproile_image.setImageResource(R.drawable.profile_image); // Default image
                            }
                        } else {
                            showCustomToast(getContext(),"Oops! Something went wrong. id:190","error");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors
                }
            });
        }
    }


    private static final String CACHE_FILE_NAME = "user_data.txt";
//    THis is used to cache the userdadta to a cache dir/file which will show the user details faster rather than retrieving  from firebase again and again

    private void saveUserDataToCache(String username, String email) {
        File file = new File(requireContext().getCacheDir(), CACHE_FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write((username + "\n" + email).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] getUserDataFromCache() {
        File file = new File(requireContext().getCacheDir(), CACHE_FILE_NAME);
        String[] cachedData = new String[2];
        cachedData[0] = "";
        cachedData[1] = "";

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                cachedData[0] = reader.readLine(); // username
                cachedData[1] = reader.readLine(); // email
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cachedData;
    }


    //    This will clear the cache if user logs out
    private void clearUserCache() {
        File file = new File(requireContext().getCacheDir(), CACHE_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }

    private final BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Re-fetch user profile to update the UI after profile picture change
            FirebaseUser user = profile_Auth.getCurrentUser();
            if (user != null) {
                showProfile(user);
            }
        }
    };
public void refreshProfileImage() {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user != null) {
        Uri photoUri = user.getPhotoUrl();
        if (photoUri != null) {
            Picasso.get().load(photoUri).into(uproile_image);
        } else {
            uproile_image.setImageResource(R.drawable.profile_image); // Replace with your default image
        }
    }
}
    @Override
    public void onResume() {
        super.onResume();
        // Re-fetch user profile data when the fragment is visible again
        FirebaseUser user = profile_Auth.getCurrentUser();
        if (user != null) {
            showProfile(user);
        }
    }

    private void showDialog() {

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogue_layout);

        LinearLayout editLayout = dialog.findViewById(R.id.layoutEdit);
        LinearLayout themeLayout = dialog.findViewById(R.id.layoutTheme);
        LinearLayout aboutLayout = dialog.findViewById(R.id.layoutAbout);
        LinearLayout helpLayout = dialog.findViewById(R.id.layoutHelp);
        LinearLayout adminLayout = dialog.findViewById(R.id.layoutAdmin);
        ImageView editImage = dialog.findViewById(R.id.edit_icon);
        ImageView themeImage = dialog.findViewById(R.id.theme_icon);
        ImageView aboutImage = dialog.findViewById(R.id.about_icon);
        ImageView helpImage = dialog.findViewById(R.id.help_icon);
        ImageView adminImage = dialog.findViewById(R.id.admin_icon);
        TextView admintxt = dialog.findViewById(R.id.admin_text);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // Get the user's email
            String email = currentUser.getEmail();
            // Check if the email matches the admin email
            if ("sandeshsharma924@gmail.com".equals(email) || "sandeshgojo@gmail.com".equals(email) ) {
                adminLayout.setVisibility(View.VISIBLE);
                adminImage.setVisibility(View.VISIBLE);
                admintxt.setVisibility(View.VISIBLE);
            }
        }

        if (isDarkMode()) {
            editImage.setImageResource(R.drawable.edit_dark);
            themeImage.setImageResource(R.drawable.theme_dark_icon);
            aboutImage.setImageResource(R.drawable.about_dark);
            helpImage.setImageResource(R.drawable.help_dark);
            adminImage.setImageResource(R.drawable.admin_dark);
        }

        editLayout.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getContext(), Edit_profile.class);
            startActivity(intent);

        });

        themeLayout.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getContext(), theme_switcher.class);
            startActivity(intent);

        });

        aboutLayout.setOnClickListener(v -> {

            dialog.dismiss();
            Intent intent = new Intent(getContext(),about.class);
            startActivity(intent);

        });

        helpLayout.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getContext(),help.class);
            startActivity(intent);

        });

        adminLayout.setOnClickListener(v -> {

            dialog.dismiss();
            Intent intent = new Intent(getContext(),admin.class);
            startActivity(intent);

        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

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



    private void showCustomToast(Context context, String message, String messageType) {
        // Inflate the custom toast layout
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, getView().findViewById(R.id.custm_toast));

        TextView toastText = toastLayout.findViewById(R.id.toast_text);
        toastText.setText(message);

        switch (messageType.toLowerCase()) {
            case "error":
                toastText.setTextColor(ContextCompat.getColor(context, R.color.red));
                break;
            case "success":
                toastText.setTextColor(ContextCompat.getColor(context, R.color.success_color));
                break;
            default:
                toastText.setTextColor(ContextCompat.getColor(context, R.color.white));
                break;
        }

        // Create and display the toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout); // Set custom view
        toast.show();
    }
}
