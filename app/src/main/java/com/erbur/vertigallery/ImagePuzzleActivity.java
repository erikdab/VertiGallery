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
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class ImagePuzzleActivity extends AppCompatActivity {

    static final int CHUNK_NUMBERS = 9;
    static final int EMPTY_CHUNK = -1;

    GridLayout puzzleGrid, bagGrid;

    JSONObject puzzles;

    ArrayList<Bitmap> imageChunks;
    Bitmap emptyChunkImage;

    int puzzleResourceId;
    String puzzleResourceIdString;
    int[] puzzleChunkIds = new int[CHUNK_NUMBERS];
    int[] bagChunkIds = new int[CHUNK_NUMBERS];
    // Not to be saved, just to use.
    int[] bagChunkIdsReverse = new int[CHUNK_NUMBERS];

    private enum SLOTTYPE {
        PUZZLE,
        BAG
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_puzzle);

        Intent intent = getIntent();
        puzzleResourceId = intent.getIntExtra("puzzleResourceId", 0);
        puzzleResourceIdString = Integer.toString(puzzleResourceId);

        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(puzzleResourceId);

        imageChunks = Utils.splitImage(drawable, CHUNK_NUMBERS);

        emptyChunkImage = Bitmap.createBitmap(imageChunks.get(0).getWidth(), imageChunks.get(0).getHeight(), Bitmap.Config.ARGB_8888);
        emptyChunkImage.eraseColor(Color.TRANSPARENT);

        // Initialize Puzzle Chunk Ids and Bag Chunk Ids
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            puzzleChunkIds[i] = EMPTY_CHUNK;
            bagChunkIds[i] = i;
            bagChunkIdsReverse[i] = i;
        }

        loadPuzzle();

        puzzleGrid = findViewById(R.id.puzzlegrid);
        initGrid(puzzleGrid, SLOTTYPE.PUZZLE);

        bagGrid = findViewById(R.id.baggrid);
        initGrid(bagGrid, SLOTTYPE.BAG);

        setGridsFromData();


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
                return super.onOptionsItemSelected(item);

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
        // Return if incorrect placement.
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            if (puzzleChunkIds[i] != i) {
                return;
            }
        }

        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(R.string.puzzle_completed_message);
        dlgAlert.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void setGridsFromData() {
        // Reset Grids.
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            setSlot(bagGrid, i, bagChunkIds[i]);
            clearSlot(puzzleGrid, i);
        }

        // Place already placed slots.
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            if(puzzleChunkIds[i] != EMPTY_CHUNK) {
                setSlot(puzzleGrid, i, puzzleChunkIds[i]);
                clearSlot(bagGrid, bagChunkIdsReverse[puzzleChunkIds[i]]);
            }
        }
    }

    private void getDataFromGrids() {
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            View puzzleSlot = puzzleGrid.getChildAt(i);
            puzzleChunkIds[i] = getSlotChunkId(puzzleSlot);
        }
    }

    private void shuffleBagChunks() {
        Utils.shuffleArray(bagChunkIds);

        reverseBagChunks();
    }

    private void reverseBagChunks() {
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            bagChunkIdsReverse[bagChunkIds[i]] = i;
        }
    }

    private void resetPuzzle() {
        shuffleBagChunks();

        // Reset Puzzle Chunk Ids.
        for(int i=0;i<CHUNK_NUMBERS;i++) {
            puzzleChunkIds[i] = EMPTY_CHUNK;
        }

        setGridsFromData();

        onPuzzleChange();
    }

    private void onPuzzleChange() {
        getDataFromGrids();

        checkPuzzle();

        savePuzzle();
    }

    private void savePuzzle() {
        // Save Array.
        if(puzzles == null) {
            puzzles = new JSONObject();
        }
        JSONArray thisPuzzleChunkIds = new JSONArray(Arrays.asList(puzzleChunkIds));
        JSONArray thisBagChunkIds = new JSONArray(Arrays.asList(bagChunkIds));
        JSONObject thisPuzzle = new JSONObject();
        try {
            thisPuzzle.put("puzzleChunkIds", thisPuzzleChunkIds);
            thisPuzzle.put("bagChunkIds", thisBagChunkIds);
            puzzles.put(puzzleResourceIdString, thisPuzzle);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.puzzle_save_error), Toast.LENGTH_LONG).show();
            return;
        }
        Utils.create(this, "storage.json", puzzles.toString());
    }

    private void loadPuzzle() {
        // Load Array.
        boolean isFilePresent = Utils.isFilePresent(this, "storage.json");
        if(isFilePresent) {
            String jsonString = Utils.read(this, "storage.json");
            try {
                puzzles = new JSONObject(jsonString);
                // If it doesn't have, that's ok, its a new puzzle.
                if(puzzles.has(puzzleResourceIdString)) {
                    JSONObject thisPuzzle = puzzles.getJSONObject(puzzleResourceIdString);
                    JSONArray thisPuzzleChunkIds = ((JSONArray) thisPuzzle.get("puzzleChunkIds")).getJSONArray(0);
                    JSONArray thisBagChunkIds = ((JSONArray) thisPuzzle.get("bagChunkIds")).getJSONArray(0);

                    for (int i = 0; i < thisPuzzleChunkIds.length(); i++) {
                        puzzleChunkIds[i] = thisPuzzleChunkIds.getInt(i);
                        bagChunkIds[i] = thisBagChunkIds.getInt(i);
                    }

                    reverseBagChunks();
                }
                else {
                    shuffleBagChunks();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                shuffleBagChunks();
                Toast.makeText(this, getString(R.string.puzzle_load_error), Toast.LENGTH_LONG).show();
            }
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

    private void initView(final ImageView thisSlot, final int thisLocationId, final SLOTTYPE slotType) {
        // The Bag starts full.
        if(slotType == SLOTTYPE.BAG) {
            setSlot(thisSlot, bagChunkIds[thisLocationId]);
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

                        int thisChunkId = getSlotChunkId(thisSlot);

                        // If we are dropping into the Bag:
                        if(slotType == SLOTTYPE.BAG) {
                            // Clear the from puzzle_menu slot.
                            clearSlot(puzzleGrid, fromLocation);

                            // Reset the to bag slot to were it should be (where it started).
                            setSlot(bagGrid, bagChunkIdsReverse[fromChunkId], fromChunkId);
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
                                    setSlot(bagGrid, bagChunkIdsReverse[thisChunkId], thisChunkId);
                                }
                                // Clear the from Bag slot - The one we are dragging:
                                clearSlot(bagGrid, fromLocation);
                            }

                            // Set toPuzzleSlot (this object)
                            setSlot(thisSlot, fromChunkId);
                        }

                        onPuzzleChange();

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
