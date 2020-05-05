package com.example.ambientprojecttake2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.media.Image;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;



public class CaptureQuoteActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 333;
    private String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private ImageCapture imageCapture;
    private PreviewView previewView;
    private GraphicOverlay mGraphicOverlay;
    private ImageButton captureButton;

    private Context context = CaptureQuoteActivity.this;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_quote);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        previewView = findViewById(R.id.view_finder);
        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        captureButton = findViewById(R.id.imageButton);



        if (allPermissionGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }


    private void startCamera(){

        /*
        int aspectRatio = previewView.getWidth() / previewView.getHeight();
        Size resolution =  new Size(previewView.getWidth(), previewView.getHeight());
        */
        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Set up the view finder use case to display camera preview
                Preview preview = new Preview.Builder()
                        .build();

                // Set up the capture use case to allow users to take photos
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();



                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                captureButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        imageCapture.takePicture(executor, callback);
                    }
                });


                /*ImageAnalysis imageAnalysis =
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

                    }
                });*/
                        // Attach use cases to the camera with the same lifecycle owner
                Camera camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview,
                        imageCapture);
                        //imageAnalysis);

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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionGranted() {
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }




    ImageCapture.OnImageCapturedCallback callback =
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
                        Log.d("imageCapture", "Photo capture succeeded");
                        Toast.makeText(CaptureQuoteActivity.this, "Picture taken!", Toast.LENGTH_SHORT).show();
                        //TODO Show captured image on screen

                        //TODO Send picture to mlKit text recognition after analysis (done in MarkQuoteActivity)
                        image.close();
                    }
                    @Override
                    public void onError(ImageCaptureException exception){
                        Toast.makeText(CaptureQuoteActivity.this, "Unable to take picture", Toast.LENGTH_SHORT).show();
                        Log.d("imageCapture", "Photo capture failed", exception);
                        exception.printStackTrace();
                    }

                };







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
        FirebaseVisionTextRecognizer textRecognizer =
                FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        textRecognizer.processImage(firebaseImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processTextRecognitionResult(firebaseVisionText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void processTextRecognitionResult(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        if (blocks.size() ==0 ){
            Toast.makeText(this, "No text found", Toast.LENGTH_SHORT).show();
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++){
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++){

                GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, lines.get(j));
                mGraphicOverlay.add(textGraphic);
            }

        }


    }


    public void openMarkQuoteActivity(File imageFile) {
        //TODO Pass an image (or representation) to new activity for analysis
        /*try {
            Intent intent = new Intent(this, MarkQuoteActivity.class);
            intent.putExtra();
            startActivity(intent);
        } catch (Exception e) {
            Log.d("activityFlow", "unable to change activity", e);
            e.printStackTrace();
        }*/
    }
}
