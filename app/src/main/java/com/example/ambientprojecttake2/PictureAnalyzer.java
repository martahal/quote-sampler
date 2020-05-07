package com.example.ambientprojecttake2;

import android.annotation.SuppressLint;
import android.media.Image;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

public class PictureAnalyzer implements ImageAnalysis.Analyzer {



    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        if (imageProxy == null || imageProxy.getImage() == null){
            return;
        }
        Image mediaImage = imageProxy.getImage();
        int degrees = imageProxy.getImageInfo().getRotationDegrees();
        int rotation = degreesToFirebaseRotation(degrees);
        FirebaseVisionImage image =
                FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
        //TODO: pass Image to ML kit vision API
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees){
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be either 0, 90, 180, or 270");
        }
    }
}
