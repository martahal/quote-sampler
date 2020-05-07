package com.example.ambientprojecttake2;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;



public class QuoteSamplerApplication extends Application implements CameraXConfig.Provider {
    @NonNull
    @Override
    public CameraXConfig getCameraXConfig(){
        return Camera2Config.defaultConfig();
    }
}
