package com.sandesh.note_app;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;

public class CustomProgressDialog {
    private Dialog dialog;
    private LottieAnimationView lottieAnimation;
    private TextView progressText;
    private boolean isDismissed = false; // Flag to manage dismissal state

    public CustomProgressDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_progress_dialog);
        dialog.setCancelable(false);

        lottieAnimation = dialog.findViewById(R.id.lottie_animation);
        progressText = dialog.findViewById(R.id.progress_text);

        // Set custom size programmatically
        int customSize = 100;
        int pixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, customSize, context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pixels, pixels);
        lottieAnimation.setLayoutParams(params);
    }

    public void show() {
        dialog.show();
        lottieAnimation.playAnimation();  // Start animation when dialog is shown
        isDismissed = false; // Reset the dismissal state
    }

    public void dismiss() {
        if (isDismissed) return; // Prevent multiple dismiss calls

        isDismissed = true; // Set the flag
        lottieAnimation.cancelAnimation();
        dialog.dismiss();
    }

    public void setProgress(int progress, long bytesTransferred, long totalBytes) {
        double mbTransferred = bytesTransferred / (1024.0 * 1024.0);
        double mbTotal = totalBytes / (1024.0 * 1024.0);

        // Format the progress information to show megabytes
        String progressInfo = String.format("Uploaded: %d%% (%.2fMb/%.2fMb)", progress, mbTransferred, mbTotal);
        progressText.setText(progressInfo);
    }

    public void changeAnimation(int animationResId, Runnable onAnimationEnd) {
        lottieAnimation.cancelAnimation(); // Stop current animation
        lottieAnimation.setAnimation(animationResId); // Set the new animation
        lottieAnimation.playAnimation(); // Play the new animation
        lottieAnimation.setRepeatCount(0);

        // Add listener to detect when the animation ends
        lottieAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                // No action needed
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                if (!isDismissed) { // Check if already dismissed
                    onAnimationEnd.run();
                    dismiss(); // Dismiss the dialog
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
                // No action needed
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {
                // No action needed
            }
        });
    }
}
