package info.faraji.focusbox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> overlayPermissionLauncher;


    private boolean isOverlayActive = false;
    private Button toggleOverlayButton;
    private Button colorPickerButton;
    private SeekBar alphaSeekBar, heightSeekBar;
    private int overlayColor;
    private int overlayAlpha;
    private int overlayHeight;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overlayPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Settings.canDrawOverlays(this)) {
                        setContentView(R.layout.activity_main);
                        initUI();
                    } else {
                        Toast.makeText(this, "Permission not granted. Can't display overlay.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            overlayPermissionLauncher.launch(intent);
            return; // Wait until user grants permission
        }


        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {
        prefs = getSharedPreferences("focusbox_prefs", Context.MODE_PRIVATE);

        overlayColor = prefs.getInt("overlayColor", 0x5933B5E5);
        overlayAlpha = prefs.getInt("overlayAlpha", 35);
        overlayHeight = prefs.getInt("overlayHeight", 200);

        toggleOverlayButton = findViewById(R.id.toggleOverlayButton);
        colorPickerButton = findViewById(R.id.colorPickerButton);
        alphaSeekBar = findViewById(R.id.alphaSeekBar);
        heightSeekBar = findViewById(R.id.heightSeekBar);

        alphaSeekBar.setProgress(overlayAlpha);
        heightSeekBar.setProgress(overlayHeight);

        toggleOverlayButton.setOnClickListener(v -> {
            if (isOverlayActive) {
                stopService(new Intent(this, OverlayService.class));
                toggleOverlayButton.setText("Start Overlay");
            } else {
                startOverlayService();
                toggleOverlayButton.setText("Stop Overlay");
            }
            isOverlayActive = !isOverlayActive;
        });

        colorPickerButton.setOnClickListener(v -> showColorPickerDialog());

        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                overlayAlpha = progress;
                prefs.edit().putInt("overlayAlpha", overlayAlpha).apply();
                if (isOverlayActive) restartOverlayService();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        heightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                overlayHeight = progress;
                prefs.edit().putInt("overlayHeight", overlayHeight).apply();
                if (isOverlayActive) restartOverlayService();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        intent.putExtra("overlayColor", overlayColor);
        intent.putExtra("overlayAlpha", overlayAlpha);
        intent.putExtra("overlayHeight", overlayHeight);
        startService(intent);
    }

    private void restartOverlayService() {
        stopService(new Intent(this, OverlayService.class));
        startOverlayService();
    }

    private void showColorPickerDialog() {
        final int[] colors = new int[] {
                0xFF33B5E5, // default (light blue)
                0xFFFF0000, // red
                0xFF00FF00, // green
                0xFF0000FF, // blue
                0xFFFFFF00, // yellow
                0xFFFFFFFF  // white
        };
        final String[] colorNames = {"Sky Blue", "Red", "Green", "Blue", "Yellow", "White"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Overlay Color");
        builder.setItems(colorNames, (dialog, which) -> {
            overlayColor = colors[which];
            prefs.edit().putInt("overlayColor", overlayColor).apply();
            if (isOverlayActive) restartOverlayService();
        });
        builder.show();
    }
}
