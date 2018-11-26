package com.erbur.vertigallery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageZoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        Intent intent = getIntent();
        int resourceId = intent.getIntExtra("resourceId", 0);

        ImageView imageZoom = findViewById(R.id.image_zoom);
        imageZoom.setImageResource(resourceId);
    }
}
