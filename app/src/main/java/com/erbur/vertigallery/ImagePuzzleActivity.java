package com.erbur.vertigallery;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class ImagePuzzleActivity extends AppCompatActivity {

    static final int CHUNK_NUMBERS = 9;
    static final int EMPTY_CHUNK = -1;

    int[] puzzleChunkIds = new int[CHUNK_NUMBERS];

    GridLayout puzzleGrid, bagGrid;

    ArrayList<Bitmap> imageChunks;
    Bitmap emptyChunkImage;

    private enum SLOTTYPE {
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

        imageChunks = Utils.splitImage(drawable, CHUNK_NUMBERS);

        emptyChunkImage = Bitmap.createBitmap(imageChunks.get(0).getWidth(), imageChunks.get(0).getHeight(), Bitmap.Config.ARGB_8888);
        emptyChunkImage.eraseColor(Color.TRANSPARENT);

        puzzleGrid = findViewById(R.id.puzzlegrid);
        initGrid(puzzleGrid, SLOTTYPE.PUZZLE);

        bagGrid = findViewById(R.id.baggrid);
        initGrid(bagGrid, SLOTTYPE.BAG);

        // Initialize Puzzle Chunk Ids
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            puzzleChunkIds[0]=-1;
        }

        loadPuzzle();

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setTitle("Image Puzzle");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.puzzle_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                resetPuzzle();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void initGrid(final GridLayout grid, SLOTTYPE gridType) {
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
        setSlotChunkId(slot, chunkId);
    }

    private void setSlot(GridLayout grid, int locationId, int chunkId) {
        ImageView slot = (ImageView) grid.getChildAt(locationId);
        setSlot(slot, chunkId);
    }

    private int getSlotChunkId(View slot) {
        return (int) slot.getTag();
    }

    private void setSlotChunkId(View slot, int chunkId) {
        slot.setTag(chunkId);
    }

    private boolean isSlotEmpty(View slot) {
        return getSlotChunkId(slot) == EMPTY_CHUNK;
    }

    private void clearSlot(ImageView slot) {
        slot.setImageBitmap(emptyChunkImage);
        setSlotChunkId(slot, EMPTY_CHUNK);
    }

    private void clearSlot(GridLayout grid, int locationId) {
        ImageView slot = (ImageView) grid.getChildAt(locationId);
        clearSlot(slot);
    }

    private void checkPuzzle() {
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            if (puzzleChunkIds[i] != i) {
                return;
            }
        }

        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("You've completed the puzzle_menu!");
        dlgAlert.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    // TODO: reset the puzzle_menu and the randomization.
    private void resetPuzzle() {
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            ImageView puzzleSlot = (ImageView) puzzleGrid.getChildAt(i);

            if(! isSlotEmpty(puzzleSlot)) {
                int puzzleChunkId = getSlotChunkId(puzzleSlot);

                setSlot(bagGrid, puzzleChunkId, puzzleChunkId);
                clearSlot(puzzleSlot);
            }
        }

        savePuzzle();
    }

    //TODO: make it save per picture, not just global, also save the randomization.
    private void savePuzzle() {
        // Save array from Slots.
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            View v = puzzleGrid.getChildAt(i);
            puzzleChunkIds[i] = getSlotChunkId(v);
        }
        checkPuzzle();

        // Save Array.
        JSONArray jsonArray = new JSONArray(Arrays.asList(puzzleChunkIds));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("puzzle_menu", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Utils.create(this, "storage.json", jsonObject.toString());
    }

    private void loadPuzzle() {
        // Load Array.
        boolean isFilePresent = Utils.isFilePresent(this, "storage.json");
        if(isFilePresent) {
            String jsonString = Utils.read(this, "storage.json");
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray innerJSONArray = ((JSONArray) jsonObject.get("puzzle_menu")).getJSONArray(0);

                for (int i = 0; i < innerJSONArray.length(); i++) {
                    puzzleChunkIds[i] = innerJSONArray.getInt(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Update Bag and Puzzle.
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            if(puzzleChunkIds[i] != -1) {
                setSlot(puzzleGrid, i, puzzleChunkIds[i]);
                clearSlot(bagGrid, puzzleChunkIds[i]);
            }
        }
    }

    private void initView(final ImageView thisSlot, final int thisLocationId, final SLOTTYPE slotType) {
        // The Bag starts full.
        if(slotType == SLOTTYPE.BAG) {
            setSlot(thisSlot, thisLocationId);
        }
        // The Puzzle starts empty.
        else {
            clearSlot(thisSlot);
        }

        // Set sizing and position: row, column specification
        int columnCount = (int) Math.sqrt(CHUNK_NUMBERS);
        int rowCount = (int) Math.sqrt(CHUNK_NUMBERS);
        int row = thisLocationId/columnCount;
        int column = thisLocationId%columnCount;

        GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(row),      GridLayout.spec(column, 1f));
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;

        thisSlot.setLayoutParams(params);
        thisSlot.setScaleType(ImageView.ScaleType.FIT_XY);
        thisSlot.setAdjustViewBounds(true);

        thisSlot.setOnDragListener(new View.OnDragListener() {
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
                        SLOTTYPE fromSlotType = SLOTTYPE.valueOf(event.getClipData().getItemAt(1).getText().toString());

                        // Get type of grid from which we started dragging.
                        int fromLocation = Integer.parseInt(event.getClipData().getItemAt(2).getText().toString());

                        //TODO: make sure it's not the same location and grid and chunkid.

                        int thisChunkId = (int) getSlotChunkId(thisSlot);

                        // If we are dropping into the Bag:
                        if(slotType == SLOTTYPE.BAG) {
                            // Clear the from puzzle_menu slot.
                            clearSlot(puzzleGrid, fromLocation);

                            // Reset the to bag slot to were it should be (where it started).
                            setSlot(bagGrid, fromChunkId, fromChunkId);
                        }
                        // If we are dropping into the Puzzle:
                        else {
                            // If we dragged from a Puzzle slot into a Puzzle slot:
                            if (fromSlotType == SLOTTYPE.PUZZLE) {
                                // Clear the from puzzle_menu slot if the to puzzle_menu slot is empty.
                                if (isSlotEmpty(thisSlot)) {
                                    clearSlot(puzzleGrid, fromLocation);
                                }
                                // Set the from puzzle_menu slot to the to puzzle_menu slot. (Swap)
                                else {
                                    setSlot(puzzleGrid, fromLocation, thisChunkId);
                                }
                            }
                            // If we dragged from a Bag slot into a Puzzle slot
                            else {
                                // If we are replacing a non empty Puzzle slot with a Bag slot:
                                // Reset the correct Bag slot of the replaced Puzzle slot
                                // The one we are replacing:
                                if (! isSlotEmpty(thisSlot)) {
                                    // Reset the to bag slot to were it should be (where it started).
                                    setSlot(bagGrid, thisChunkId, thisChunkId);
                                }
                                // Clear the from Bag slot - The one we are dragging:
                                clearSlot(bagGrid, fromChunkId);
                            }

                            // Set toPuzzleSlot (this object)
                            setSlot(thisSlot, fromChunkId);
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

        thisSlot.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isSlotEmpty(v)) return false;

                // chunkId
                ClipData data = ClipData.newPlainText("value", Integer.toString(getSlotChunkId(v)));

                // gridType
                data.addItem(new ClipData.Item(slotType.toString()));

                // gridLocation
                data.addItem(new ClipData.Item(Integer.toString(thisLocationId)));

                thisSlot.startDrag(data, new View.DragShadowBuilder(v), null, 0);
                return true;
            }
        });
    }
}
