package com.erbur.vertigallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class ImagePuzzleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_puzzle);

        Intent intent = getIntent();
        int resourceId = intent.getIntExtra("resourceId", 0);

        ImageView imageZoom = findViewById(R.id.image_puzzle);
        imageZoom.setImageResource(resourceId);

        getSupportActionBar().setTitle("Image Puzzle");
    }
}
