package com.erbur.vertigallery;

import java.util.ArrayList;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.support.v7.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;

//The adapter class associated with the ChunkedImageActivity class
public class ImageAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<Bitmap> imageChunks;
	private int imageWidth, imageHeight;
	
	//constructor
	public ImageAdapter(Context c, ArrayList<Bitmap> images){
		mContext = c;
		imageChunks = images;
		imageWidth = images.get(0).getWidth();
		imageHeight = images.get(0).getHeight();
	}
	
	@Override
	public int getCount() {
		return imageChunks.size();
	}

	@Override
	public Object getItem(int position) {
		return imageChunks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView image;
		if(convertView == null){
			image = new ImageView(mContext);
			
			/*
			 * NOTE: I have set imageWidth - 10 and imageHeight
			 * as arguments to LayoutParams class. 
			 * But you can take anything as per your requirement 
			 */

            GridLayout.LayoutParams parem = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f),      GridLayout.spec(GridLayout.UNDEFINED, 1f));
            image.setLayoutParams(parem);
			//image.setLayoutParams(new GridView.LayoutParams(imageWidth - 10 , imageHeight));
			image.setPadding(0, 0, 0, 0);
		}else{
			image = (ImageView) convertView;
		}
		initView(image, position);
		return image;
	}

	private void initView(final ImageView view, int index) {
		// Set image tag (index) and image itself.
		view.setTag(index);
		view.setImageBitmap(imageChunks.get(index));

		view.setOnDragListener(new View.OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				switch(event.getAction()) {
					case DragEvent.ACTION_DRAG_STARTED:
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
						int chunkId = Integer.parseInt(event.getClipData().getItemAt(0).getText().toString());
						// Get chunk image by id and set tag and image.
						Bitmap chunk = imageChunks.get(chunkId);
						v.setTag(chunkId);
						((ImageView) v).setImageBitmap(chunk);
						return true;

					case DragEvent.ACTION_DRAG_ENDED:
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
				ClipData data = ClipData.newPlainText("value", view.getTag().toString());
				view.startDrag(data, new View.DragShadowBuilder(v), null, 0);
				view.setVisibility(View.INVISIBLE);
				return true;
			}
		});
	}
}
