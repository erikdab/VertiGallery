package com.erbur.vertigallery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Vertical Gallery Main Activity - Vertical Image Pager.
 */
public class MainActivity extends AppCompatActivity {

    // List of available images.
    static int[] mResources = {
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3,
            R.drawable.image4,
            R.drawable.image5,
            R.drawable.image6,
            R.drawable.image7,
            R.drawable.image8,
            R.drawable.image9,
            R.drawable.image10,
            R.drawable.image11,
            R.drawable.image12,
            R.drawable.image13,
            R.drawable.image14,
            R.drawable.image15,
    };

    MyAdapter mAdapter;
    VerticalViewPager mPager;
    MyPageChangeListener myPageChangeListener = new MyPageChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager = findViewById(R.id.viewpager);
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(myPageChangeListener);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    // This is to support fasting snapping than half-screen.
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        private int mCurrentSelectedScreen = 0;
        private int mNextSelectedScreen = 0;
        private int mCurrentPagerState = 0;

        @Override
        public void onPageScrollStateChanged(int state) {
            mCurrentPagerState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Ignore scroll state settling - only consider user swiping.
            if (mCurrentPagerState == ViewPager.SCROLL_STATE_SETTLING) return;

            if (position == mCurrentSelectedScreen) {
                // We are moving to next screen DOWN
                if (positionOffset > 0.1) {
                    // Closer to next screen than to current
                    if (position + 1 != mNextSelectedScreen) {
                        mNextSelectedScreen = position + 1;
                        mPager.setCurrentItem(mNextSelectedScreen);
                    }
                    // Closer to current screen than to next
                } else if (position != mNextSelectedScreen) {
                    mNextSelectedScreen = position;
                    mPager.setCurrentItem(mNextSelectedScreen);
                }
            }
            // We are moving to next screen UP
            // Closer to next screen than to current
            else if (positionOffset < 0.9 && position != mNextSelectedScreen) {
                mNextSelectedScreen = position;
                mPager.setCurrentItem(mNextSelectedScreen);
            }
        }

        @Override
        public void onPageSelected(int arg0) {
            mCurrentSelectedScreen = arg0;
            mNextSelectedScreen = arg0;
        }
    }

    // This is the setup for the pager fragments.
    public static class MyAdapter extends FragmentPagerAdapter {
        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mResources.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position >= 0 && position < getCount()) {
                return FragmentOne.newInstance(position, mResources[position]);
            }
            return null;
        }
    }

    // This is the fragment class.
    public static class FragmentOne extends Fragment {

        private static final String MY_NUM_KEY = "num";
        private static final String MY_RESOURCE_KEY = "resource";

        private int mNum;
        private int mResource;

        // You can modify the parameters to pass in whatever you want
        static FragmentOne newInstance(int num, int resource) {
            FragmentOne f = new FragmentOne();
            Bundle args = new Bundle();
            args.putInt(MY_NUM_KEY, num);
            args.putInt(MY_RESOURCE_KEY, resource);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt(MY_NUM_KEY) : 0;
            mResource = getArguments() != null ? getArguments().getInt(MY_RESOURCE_KEY) : 0;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View v = inflater.inflate(R.layout.fragment_one, container, false);

            // Set Resource Images
            final ImageView thumbnailView = v.findViewById(R.id.thumb_button_1);
            thumbnailView.setImageResource(mResource);

            // Set on Double Tap Zoom Detector
            thumbnailView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(v.getContext(),
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onDoubleTap(MotionEvent e) {
                                Intent intent = new Intent(v.getContext(), ImageZoomActivity.class);
                                intent.putExtra("puzzleResourceId", mResource);
                                startActivity(intent);

                                return super.onDoubleTap(e);
                            }

                            @Override
                            public void onLongPress(MotionEvent e) {
                                Intent intent = new Intent(v.getContext(), ImagePuzzleActivity.class);
                                intent.putExtra("puzzleResourceId", mResource);
                                startActivity(intent);

                                super.onLongPress(e);
                            }
                        });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
            return v;
        }
    }
}