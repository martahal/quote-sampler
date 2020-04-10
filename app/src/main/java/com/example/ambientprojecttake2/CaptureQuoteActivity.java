package com.example.ambientprojecttake2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.media.Image;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

public class CaptureQuoteActivity extends AppCompatActivity {

    //private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ImageCapture imageCapture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_quote);

        PreviewView previewView = findViewById(R.id.preview_view);

        ListenableFuture cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();

                // Set up the view finder use case to display camera preview
                Preview preview = new Preview.Builder().build();

                // Set up the capture use case to allow users to take photos
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280,720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
                    @SuppressLint("UnsafeExperimentalUsageError")
                    @Override
                    public void analyze(@NonNull ImageProxy image) {
                        if (image == null || image.getImage() == null){
                            return;
                        }
                        int rotationDegrees = image.getImageInfo().getRotationDegrees();
                        Image mediaImage = image.getImage();
                        int rotation = degreesToFirebaseRotation(rotationDegrees);
                        FirebaseVisionImage firebaseImage =
                                FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

                        detectTextFromImage(firebaseImage);

                        image.close(); //Remember this when done with the analysis

                    }
                });
                        // Attach use cases to the camera with the same lifecycle owner
                Camera camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalysis);

                // Connect the preview use case to the previewView
                preview.setSurfaceProvider(
                        previewView.createSurfaceProvider(camera.getCameraInfo()));


            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown. cameraProviderFuture.get() should
                // not block since the listener is being called, so no need to
                // handle InterruptedException.
            }
        }, ContextCompat.getMainExecutor(this));
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


    private void detectTextFromImage(FirebaseVisionImage firebaseImage) {
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer =
                FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(firebaseImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                Toast.makeText(CaptureQuoteActivity.this, "Found text in image!", Toast.LENGTH_SHORT).show();
                //TODO: implement functionality to highlight text in image. Almost like in the codelab
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CaptureQuoteActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                Log.d("Error: ", e.getMessage());
            }
        });
    }

    /*public void onClick(){
        imageCapture.takePicture(new ImageCapture.OnImageCapturedCallback(){
            @Override
            public void onCaptureSuccess(ImageProxy imageProxy){
                Log.d("imageCapture", "Photo capture succeeded");
                //TODO Send picture to mlKit text recognition after analysis
                imageProxy.close();
            }
            @Override
            public void onError(ImageCaptureException exception){
                Toast.makeText(CaptureQuoteActivity.this, "Unable to take picture", Toast.LENGTH_SHORT).show();
                Log.d("imageCapture", "Photo capture failed", exception);
                exception.printStackTrace();
            }
        });
    }*/


}
