package com.erbur.vertigallery;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class ImagePuzzleActivity extends AppCompatActivity {

    static final int CHUNK_NUMBERS = 9;

    int[] puzzleChunkIds = new int[CHUNK_NUMBERS];

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

        puzzleGrid = findViewById(R.id.puzzlegrid);
        initGrid(puzzleGrid, GRIDTYPE.PUZZLE);

        bagGrid = findViewById(R.id.baggrid);
        initGrid(bagGrid, GRIDTYPE.BAG);

        // Initialize Puzzle Chunk Ids
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            puzzleChunkIds[0]=0;
        }

        loadPuzzle();
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

    private void setSlot(ImageView slot, int chunkId) {
        Bitmap chunk = imageChunks.get(chunkId);
        slot.setImageBitmap(chunk);
        slot.setTag(chunkId);
    }

    private void clearSlot(ImageView slot) {
        slot.setImageResource(0);
        slot.setTag(0);
    }

    private boolean isSlotEmpty(ImageView slot) {
        return (int) slot.getTag() == 0;
    }

    private String printArray(int[] array) {
        String arr = "";
        for(int i=0;i<array.length;i++) {
            arr+=Integer.toString(array[i])+",";
        }
        return arr;
    }

    private void savePuzzle() {
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            View v = puzzleGrid.getChildAt(i);
            puzzleChunkIds[i] = (int) v.getTag();
        }

        Log.d("UIDDIDIDI", "saving");
        Log.d("UIDDIDIDI", printArray(puzzleChunkIds));
        JSONArray jsonArray = new JSONArray(Arrays.asList(puzzleChunkIds));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("puzzle", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("UIDDIDIDI", jsonObject.toString());
        Utils.create(this, "storage.json", jsonObject.toString());
    }

    private void loadPuzzle() {

        Log.d("UIDDIDIDI", "loading");
        boolean isFilePresent = Utils.isFilePresent(this, "storage.json");
        if(isFilePresent) {
            String jsonString = Utils.read(this, "storage.json");

            Log.d("UIDDIDIDI", jsonString);
            try {
                JSONObject jsonObject = new JSONObject(jsonString);

                Log.d("UIDDIDIDI", jsonObject.toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("puzzle");
                Log.d("UIDDIDIDI", jsonArray.toString());
                Log.d("UIDDIDIDI", ":)");
                if (jsonArray != null) {

                    Log.d("UIDDIDIDI", "not null");
                    JSONArray innerJSONArray = jsonArray.getJSONArray(0);
                    for (int i = 0; i < innerJSONArray.length(); i++) {
                        puzzleChunkIds[i] = innerJSONArray.getInt(i);
                    }
                }
            } catch (JSONException e) {
                Log.d("UIDDIDIDI", ":(");
            }
        }

        Log.d("UIDDIDIDI", printArray(puzzleChunkIds));
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            if(puzzleChunkIds[i] != 0) {
                ImageView puzzleSlot = (ImageView) puzzleGrid.getChildAt(i);
                setSlot(puzzleSlot, puzzleChunkIds[i]);
                ImageView bagSlot = (ImageView) bagGrid.getChildAt(puzzleChunkIds[i]);
                clearSlot(bagSlot);
            }
        }
    }

    private void initView(final ImageView view, final int gridIndex, final GRIDTYPE gridType) {
        if(gridType == GRIDTYPE.BAG) {
            setSlot(view, gridIndex);
        }
        // The Puzzle starts empty.
        else {
            if(puzzleChunkIds[gridIndex] != 0) {
                setSlot(view, puzzleChunkIds[gridIndex]);
            }
            else {
                clearSlot(view);
            }
        }

        // Set sizing and position: row, column specification
        int columnCount = (int) Math.sqrt(CHUNK_NUMBERS);
        int rowCount = (int) Math.sqrt(CHUNK_NUMBERS);
        int row = gridIndex/columnCount;
        int column = gridIndex%columnCount;

        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(row, 1f),      GridLayout.spec(column, 1f));
        params.width = 0;
        params.height = 0;

//        if(row == 0) {
//            params.setGravity(Gravity.BOTTOM);
//        }
//        if(row == rowCount-1) {
//            params.setGravity(Gravity.TOP);
//        }
        view.setLayoutParams(params);

        view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch(event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // The Bag Grid should always be highlighted? - to know that you can drop in it.
                        bagGrid.setBackgroundColor(Color.LTGRAY);
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
                        int fromChunkId = Integer.parseInt(event.getClipData().getItemAt(0).getText().toString());

                        // Get type of grid from which we started dragging.
                        GRIDTYPE fromGridType = GRIDTYPE.valueOf(event.getClipData().getItemAt(1).getText().toString());

                        // Get type of grid from which we started dragging.
                        int fromLocation = Integer.parseInt(event.getClipData().getItemAt(2).getText().toString());

                        //TODO: make sure it's not the same location and grid and chunkid.

                        // If we are dropping into the Bag:
                        if(gridType == GRIDTYPE.BAG) {
                            // Clear the from puzzle slot.
                            ImageView fromPuzzleSlot = (ImageView) puzzleGrid.getChildAt(fromLocation);
                            clearSlot(fromPuzzleSlot);

                            // Reset the to bag slot to were it should be (where it started).
                            ImageView intoBagSlot = (ImageView) bagGrid.getChildAt(fromChunkId);
                            setSlot(intoBagSlot, fromChunkId);
                        }
                        // If we are dropping into the Puzzle:
                        else {
                            // If we dragged from a Puzzle slot into a Puzzle slot:
                            if (fromGridType == GRIDTYPE.PUZZLE) {
                                ImageView fromPuzzleSlot = (ImageView) puzzleGrid.getChildAt(fromLocation);

                                // Clear the from puzzle slot if the to puzzle slot is empty.
                                if (isSlotEmpty((ImageView) v)) {
                                    clearSlot(fromPuzzleSlot);
                                }
                                // Set the from puzzle slot to the to puzzle slot. (Swap)
                                else {
                                    int chunkId = (int) v.getTag();
                                    setSlot(fromPuzzleSlot, chunkId);
                                }
                            }
                            // If we dragged from a Bag slot into a Puzzle slot
                            else {
                                // Clear the from Bag slot.
                                ImageView fromBagSlot = (ImageView) bagGrid.getChildAt(fromChunkId);
                                clearSlot(fromBagSlot);
                            }

                            // Set toPuzzleSlot (this object)
                            setSlot((ImageView) v, fromChunkId);
                        }

                        savePuzzle();

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
