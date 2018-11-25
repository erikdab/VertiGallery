package com.erbur.vertigallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Vertical Gallery Main Activity - Vertical Image Pager.
 */
public class MainActivity extends AppCompatActivity {

    // List of available images.
    static int[] mResources = {
            R.drawable.barash,
            R.drawable.elenas,
            R.drawable.erundil,
            R.drawable.ryusume,
            R.drawable.sigrun
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
        public void onPageScrolled( int position , float positionOffset , int positionOffsetPixels )
        {
            // Ignore scroll state settling - only consider user swiping.
            if(mCurrentPagerState != ViewPager.SCROLL_STATE_SETTLING)
            {
                if ( position == mCurrentSelectedScreen )
                {
                    // We are moving to next screen DOWN
                    if ( positionOffset > 0.05 )
                    {
                        // Closer to next screen than to current
                        if ( position + 1 != mNextSelectedScreen )
                        {
                            mNextSelectedScreen = position + 1;
                            mPager.setCurrentItem(mNextSelectedScreen);
                        }
                    }
                    else
                    {
                        // Closer to current screen than to next
                        if ( position != mNextSelectedScreen )
                        {
                            mNextSelectedScreen = position;
                            mPager.setCurrentItem(mNextSelectedScreen);
                        }
                    }
                }
                else
                {
                    // We are moving to next screen UP
                    if ( positionOffset < 0.95 )
                    {
                        // Closer to next screen than to current
                        if ( position != mNextSelectedScreen )
                        {
                            mNextSelectedScreen = position;
                            mPager.setCurrentItem(mNextSelectedScreen);
                        }
                    }
                }
            }
        }

        @Override
        public void onPageSelected( int arg0 )
        {
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
            if( position >= 0 && position < getCount()) {
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_one, container, false);

            // Here we set the image by resource id.
            ImageView imageView = v.findViewById(R.id.imageView);
            imageView.setImageResource(mResource);
            return v;
        }
    }
}