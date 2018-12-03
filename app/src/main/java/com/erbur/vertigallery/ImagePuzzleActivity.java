package com.erbur.vertigallery;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImagePuzzleActivity extends AppCompatActivity {

    static final int CHUNK_NUMBERS = 9;

    // Puzzle target views.
    private View[] views;

    GridLayout puzzleGrid, bagGrid;

    ArrayList<Bitmap> imageChunks;

    private enum GRIDTYPE {
        PUZZLE,
        BAG
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_puzzle);

        Intent intent = getIntent();
        int resourceId = intent.getIntExtra("resourceId", 0);

        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(resourceId);

        getSupportActionBar().setTitle("Image Puzzle");

        imageChunks = splitImage(drawable, CHUNK_NUMBERS);

        views = new View[CHUNK_NUMBERS];

        // FrameLayout group - Puzzle Grid.
        //ViewGroup rootView = findViewById(R.id.piecesGrid);

//        views = new View[rootView.getChildCount()];
//        for (int i = 0; i < rootView.getChildCount(); i++) {
//            final ImageView view = (ImageView) rootView.getChildAt(i);
//            initView(view, i);
//            views[i] = view;
//        }

        puzzleGrid = findViewById(R.id.puzzlegrid);
        initGrid(puzzleGrid, GRIDTYPE.PUZZLE);

        bagGrid = findViewById(R.id.baggrid);
        initGrid(bagGrid, GRIDTYPE.BAG);

        //Getting the grid view and setting an adapter to it
//        GridView grid = findViewById(R.id.gridview);
//        grid.setAdapter(new ImageAdapter(this, imageChunks));
//        grid.setNumColumns((int) Math.sqrt(imageChunks.size()));
    }

    private void initGrid(final GridLayout grid, GRIDTYPE gridType) {
        grid.setColumnCount((int) Math.sqrt(CHUNK_NUMBERS));
        grid.setRowCount((int) Math.sqrt(CHUNK_NUMBERS));

        for (int i = 0; i < CHUNK_NUMBERS; i++) {
            ImageView view = new ImageView(this);
            initView(view, i, gridType);
            grid.addView(view);
        }
    }

    private void initView(final ImageView view, final int gridIndex, final GRIDTYPE gridType) {
        // The Puzzle starts empty.
        if(gridType == GRIDTYPE.BAG) {
            // Set image
            Bitmap chunk = imageChunks.get(gridIndex);
            view.setImageBitmap(chunk);
            // Set image tag (index)
            view.setTag(gridIndex);
        }
        else {
            view.setTag(0);
        }

        // Set sizing and position: row, column specification
        int columnCount = (int) Math.sqrt(CHUNK_NUMBERS);
        int row = gridIndex/columnCount;
        int column = gridIndex%columnCount;
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(row, 1f),      GridLayout.spec(column, 1f));
        params.width = 0;
        params.height = 0;
        view.setLayoutParams(params);

        view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch(event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // The Bag Grid should always be highlighted? - to know that you can drop in it.
                        bagGrid.setBackgroundColor(Color.rgb(250, 250, 250));
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackgroundColor(Color.LTGRAY);
                        return true;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setBackgroundColor(Color.TRANSPARENT);
                        return true;

                    case DragEvent.ACTION_DROP:
                        v.setBackgroundColor(Color.TRANSPARENT);

                        // Get number of chunk we are dropping.
                        int draggedChunkId = Integer.parseInt(event.getClipData().getItemAt(0).getText().toString());

                        // Get type of grid from which we started dragging.
                        GRIDTYPE draggedGridType = GRIDTYPE.valueOf(event.getClipData().getItemAt(1).getText().toString());

                        // Get type of grid from which we started dragging.
                        int draggedLocation = Integer.parseInt(event.getClipData().getItemAt(2).getText().toString());

                        Bitmap draggedChunk = imageChunks.get(draggedChunkId);
                        //Get current chunk and chunkid.

                        // If we are dropping into the Bag, show the chunk.
                        if(gridType == GRIDTYPE.BAG) {
                            // Get ImageView in Bag which will now be made visible.
                            ImageView bagSlot = (ImageView) bagGrid.getChildAt(draggedChunkId);
                            bagSlot.setTag(draggedChunkId);
                            bagSlot.setImageBitmap(draggedChunk);

                            // Clear puzzle slot.
                            ImageView puzzleSlot = (ImageView) puzzleGrid.getChildAt(draggedLocation);
                            puzzleSlot.setImageResource(0);
                            puzzleSlot.setTag(0);
                        }

                        // If we are dropping into the Puzzle, set it.
                        else {
                            // If we dragged from Puzzle to Puzzle, swap.
                            if (draggedGridType == GRIDTYPE.PUZZLE) {
                                // Get ImageView in Bag which will now be made visible.
                                ImageView puzzleSlot = (ImageView) puzzleGrid.getChildAt(draggedLocation);
                                int chunkId = (int) v.getTag();
                                Bitmap chunk = imageChunks.get(chunkId);
                                puzzleSlot.setTag(chunkId);
                                puzzleSlot.setImageBitmap(chunk);
                            } else {
                                ImageView bagSlot = (ImageView) bagGrid.getChildAt(draggedChunkId);
                                bagSlot.setImageResource(0);
                                bagSlot.setTag(0);
                            }

                            // Get new chunk image by id and set tag and image.
                            //Bitmap draggedChunk = imageChunks.get(draggedChunkId);
                            v.setTag(draggedChunkId);
                            ((ImageView) v).setImageBitmap(draggedChunk);
                        }

                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        bagGrid.setBackgroundColor(Color.TRANSPARENT);
                        return true;

                    default:
                        break;
                }
                return false;
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if ((int) view.getTag() == 0) return false;

                // chunkId
                ClipData data = ClipData.newPlainText("value", view.getTag().toString());

                // gridType
                data.addItem(new ClipData.Item(gridType.toString()));

                // gridLocation
                data.addItem(new ClipData.Item(Integer.toString(gridIndex)));

                view.startDrag(data, new View.DragShadowBuilder(v), null, 0);
                return true;
            }
        });
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
