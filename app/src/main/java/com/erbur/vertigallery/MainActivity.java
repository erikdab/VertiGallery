package com.erbur.vertigallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Vertical Gallery Main Activity - Vertical Image Pager.
 */
public class MainActivity extends FragmentActivity {

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

        // Hold a reference to the current animator,
        // so that it can be canceled mid-way.
        private Animator mCurrentAnimator;

        // The system "short" animation time duration, in milliseconds. This
        // duration is ideal for subtle animations or animations that occur
        // very frequently.
        private int mShortAnimationDuration;

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

            // Retrieve and cache the system's default "short" animation time.
            mShortAnimationDuration = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View v = inflater.inflate(R.layout.fragment_one, container, false);


            final ImageView thumbnailView = v.findViewById(R.id.thumb_button_1);
            thumbnailView.setImageResource(mResource);

            final ImageView expandedView = v.findViewById(R.id.expanded_image);
            expandedView.setImageResource(mResource);

            thumbnailView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    zoomImageFromThumb(thumbnailView, expandedView, v);
                }
            });

            // Here we set the image by resource id.
//            ImageView imageView = v.findViewById(R.id.thumb_button_1);
//            imageView.setImageResource(mResource);

//            thumbnailView = v.findViewById(R.id.expanded_image);
//            thumbnailView.setImageResource(mResource);
            return v;
        }

        private void zoomImageFromThumb(final View thumbView, final View expandedImageView, final View container) {
            // If there's an animation in progress, cancel it
            // immediately and proceed with this one.
            if (mCurrentAnimator != null) {
                mCurrentAnimator.cancel();
            }

            // Load the high-resolution "zoomed-in" image.
//            final ImageView expandedImageView = (ImageView) findViewById(
//                    R.id.expanded_image);

            // Calculate the starting and ending bounds for the zoomed-in image.
            // This step involves lots of math. Yay, math.
            final Rect startBounds = new Rect();
            final Rect finalBounds = new Rect();
            final Point globalOffset = new Point();

            // The start bounds are the global visible rectangle of the thumbnail,
            // and the final bounds are the global visible rectangle of the container
            // view. Also set the container view's offset as the origin for the
            // bounds, since that's the origin for the positioning animation
            // properties (X, Y).
            thumbView.getGlobalVisibleRect(startBounds);
            container.findViewById(R.id.container)
                    .getGlobalVisibleRect(finalBounds, globalOffset);
            startBounds.offset(-globalOffset.x, -globalOffset.y);
            finalBounds.offset(-globalOffset.x, -globalOffset.y);

            // Adjust the start bounds to be the same aspect ratio as the final
            // bounds using the "center crop" technique. This prevents undesirable
            // stretching during the animation. Also calculate the start scaling
            // factor (the end scaling factor is always 1.0).
            float startScale;
            if ((float) finalBounds.width() / finalBounds.height()
                    > (float) startBounds.width() / startBounds.height()) {
                // Extend start bounds horizontally
                startScale = (float) startBounds.height() / finalBounds.height();
                float startWidth = startScale * finalBounds.width();
                float deltaWidth = (startWidth - startBounds.width()) / 2;
                startBounds.left -= deltaWidth;
                startBounds.right += deltaWidth;
            } else {
                // Extend start bounds vertically
                startScale = (float) startBounds.width() / finalBounds.width();
                float startHeight = startScale * finalBounds.height();
                float deltaHeight = (startHeight - startBounds.height()) / 2;
                startBounds.top -= deltaHeight;
                startBounds.bottom += deltaHeight;
            }

            // Hide the thumbnail and show the zoomed-in view. When the animation
            // begins, it will position the zoomed-in view in the place of the
            // thumbnail.
            thumbView.setAlpha(0f);
            expandedImageView.setVisibility(View.VISIBLE);

            // Set the pivot point for SCALE_X and SCALE_Y transformations
            // to the top-left corner of the zoomed-in view (the default
            // is the center of the view).
            expandedImageView.setPivotX(0f);
            expandedImageView.setPivotY(0f);

            // Construct and run the parallel animation of the four translation and
            // scale properties (X, Y, SCALE_X, and SCALE_Y).
            AnimatorSet set = new AnimatorSet();
            set
                    .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                            startBounds.left, finalBounds.left))
                    .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                            startBounds.top, finalBounds.top))
                    .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                            startScale, 1f))
                    .with(ObjectAnimator.ofFloat(expandedImageView,
                            View.SCALE_Y, startScale, 1f));
            set.setDuration(mShortAnimationDuration);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCurrentAnimator = null;
                }
            });
            set.start();
            mCurrentAnimator = set;

            // Upon clicking the zoomed-in image, it should zoom back down
            // to the original bounds and show the thumbnail instead of
            // the expanded image.
            final float startScaleFinal = startScale;
            expandedImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCurrentAnimator != null) {
                        mCurrentAnimator.cancel();
                    }

                    // Animate the four positioning/sizing properties in parallel,
                    // back to their original values.
                    AnimatorSet set = new AnimatorSet();
                    set.play(ObjectAnimator
                            .ofFloat(expandedImageView, View.X, startBounds.left))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView,
                                            View.Y,startBounds.top))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView,
                                            View.SCALE_X, startScaleFinal))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView,
                                            View.SCALE_Y, startScaleFinal));
                    set.setDuration(mShortAnimationDuration);
                    set.setInterpolator(new DecelerateInterpolator());
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }
                    });
                    set.start();
                    mCurrentAnimator = set;
                }
            });
        }
    }
}