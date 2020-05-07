package com.example.ambientprojecttake2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class MarkQuoteActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_quote);
        imageView = findViewById(R.id.image_view);
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");
        Uri imageUri = Uri.parse(imagePath);
        imageView.setImageURI(imageUri);
        Log.d("MarkQuoteActivity", "Activity created with image saved at: " + imagePath);
    }
}
