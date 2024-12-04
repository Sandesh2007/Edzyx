package com.sandesh.note_app;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.airbnb.lottie.LottieAnimationView;

public class NoInternetDialogFragment extends DialogFragment {

    private LottieAnimationView loadingAnimation;
    private Button retryButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_no_internet, container, false);

        retryButton = view.findViewById(R.id.retry_button);
        loadingAnimation = view.findViewById(R.id.loading_animation);

        retryButton.setOnClickListener(v -> {
            showLoading(true);  // Show the animation and hide the retry button
            retryNetworkCheck();
        });

        return view;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            retryButton.setVisibility(View.GONE);
            loadingAnimation.setVisibility(View.VISIBLE);
            loadingAnimation.playAnimation();
        } else {
            retryButton.setVisibility(View.VISIBLE);
            loadingAnimation.setVisibility(View.GONE);
            loadingAnimation.cancelAnimation(); // Stop the animation
        }
    }

    private void retryNetworkCheck() {
        // Simulate a delay for network checking (e.g., 2 seconds)
        new Handler().postDelayed(() -> {
            if (isNetworkAvailable()) {
                dismiss();
            } else {
                showLoading(false);  // No connection, show the retry button again
            }
        }, 5600);  // Delay of 2 seconds to simulate checking network connection
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        return super.onCreateDialog(savedInstanceState);
    }
}
