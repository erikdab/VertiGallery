package com.erbur.vertigallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImagePuzzleActivity extends AppCompatActivity {

    static final int SNAP_GRID_INTERVAL = 20;
    static final int CHUNK_NUMBERS = 16;

    ArrayList<Bitmap> imageChunks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_puzzle);

        Intent intent = getIntent();
        int resourceId = intent.getIntExtra("resourceId", 0);

        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(resourceId);

        getSupportActionBar().setTitle("Image Puzzle");

        imageChunks = splitImage(drawable, CHUNK_NUMBERS);

        //Getting the grid view and setting an adapter to it
        GridView grid = findViewById(R.id.gridview);
        grid.setAdapter(new ImageAdapter(this, imageChunks));
        grid.setNumColumns((int) Math.sqrt(imageChunks.size()));
    }

    /**
     * Splits the source image and show them all into a grid in a new activity
     *  @param image The source image to split
     * @param chunkNumbers The target number of small image chunks to be formed from the source image
     */
    private static ArrayList<Bitmap> splitImage(ImageView image, int chunkNumbers) {

        //Getting the scaled bitmap of the source image
        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();

        return splitImage(drawable, chunkNumbers);
    }

    /**
     * Splits the source image and show them all into a grid in a new activity
     *  @param drawable The source image to split
     * @param chunkNumbers The target number of small image chunks to be formed from the source image
     */
    private static ArrayList<Bitmap> splitImage(BitmapDrawable drawable, int chunkNumbers) {

        //For the number of rows and columns of the grid to be displayed
        int rows,cols;

        //For height and width of the small image chunks
        int chunkHeight,chunkWidth;

        //To store all the small image chunks in bitmap format in this list
        ArrayList<Bitmap> chunkedImages = new ArrayList<Bitmap>(chunkNumbers);

        //Getting the scaled bitmap of the source image
        Bitmap bitmap = drawable.getBitmap();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        rows = cols = (int) Math.sqrt(chunkNumbers);
        chunkHeight = bitmap.getHeight()/rows;
        chunkWidth = bitmap.getWidth()/cols;

        //xCoord and yCoord are the pixel positions of the image chunks
        int yCoord = 0;
        for(int x=0; x<rows; x++){
            int xCoord = 0;
            for(int y=0; y<cols; y++){
                chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }

        return chunkedImages;
    }
}
