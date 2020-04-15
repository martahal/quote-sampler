package com.example.ambientprojecttake2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Button captureImageBtn, detectTextBtn;
    private ImageView imageView;
    private TextView textView;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureImageBtn = findViewById(R.id.capture_image_btn);


        captureImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCaptureQuoteActivity();
            }
        });
    }


    public void openCaptureQuoteActivity(){
        try{
            Intent intent = new Intent(this, CaptureQuoteActivity.class);
            startActivity(intent);
        }
        catch (Exception e){
            Log.d("activityFlow", "unable to change activity", e);
            e.printStackTrace();
        }


    }





    /*
    This method probably need modification for the text to be shown in context with the usage of the app.

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        if (blockList.size() == 0){
            Toast.makeText(this, "No text found in image", Toast.LENGTH_SHORT).show();
        }
        else{
            for (FirebaseVisionText.TextBlock block : blockList){
                String text = block.getText();
                textView.setText(text);
            }
        }
    }
*/

}
