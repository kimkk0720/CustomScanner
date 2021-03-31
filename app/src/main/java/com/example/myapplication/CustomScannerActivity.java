package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

/**
 * Created by samsung on 2017-08-28.
 */

public class CustomScannerActivity extends Activity {
    private final String packageName_safepass = "com.jinsit.safepass";
    boolean detectSound = true;

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private BackPressCloseHandler backPressCloseHandler;
    private ImageView setting_btn, switchFlashlightButton;
    private Boolean switchFlashlightButtonCheck;
    private Button safepass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        switchFlashlightButtonCheck = true;

        backPressCloseHandler = new BackPressCloseHandler(this);

        setting_btn = findViewById(R.id.setting_btn);
        switchFlashlightButton = findViewById(R.id.switch_flashlight);
        safepass = (Button) findViewById(R.id.safepass);

        if (!hasFlash()) {
            switchFlashlightButton.setVisibility(View.GONE);
        }

        barcodeScannerView = (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        safepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAppInstalled(packageName_safepass)) {
                    Intent intent_safepass = getPackageManager().getLaunchIntentForPackage(packageName_safepass);
                    startActivity(intent_safepass);
                    finish();
                    detectSound = true;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("market://details?id=" + packageName_safepass));
                    startActivity(intent);
                }
            }
        });

        //AudioManager.registerAudioPlaybackCallback 앱에서 나는 소리를 감지함
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Handler handler = new Handler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.registerAudioPlaybackCallback(new AudioManager.AudioPlaybackCallback() {
                @Override
                public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> configs) {
                    super.onPlaybackConfigChanged(configs);
                    if (detectSound) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        startActivity(intent);
                        detectSound = false;
                    }
                }
            }, handler);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public void switchFlashlight(View view) {
        if (switchFlashlightButtonCheck) {
            barcodeScannerView.setTorchOn();
        } else {
            barcodeScannerView.setTorchOff();
        }
    }

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    //어플리케이션이 설치되었는지 확인
    boolean isAppInstalled(String packageName) {
        PackageManager pm = getApplication().getPackageManager();
        PackageInfo pi;

        try {
            pi = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}