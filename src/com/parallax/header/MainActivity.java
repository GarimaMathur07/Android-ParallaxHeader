package com.parallax.header;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.parallax.header.pager.CustomViewPager;
import com.parallax.header.pager.PagerSlidingTabStrip;


@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

    public static final boolean NEEDS_PROXY = Integer.valueOf(Build.VERSION.SDK_INT).intValue() < 11;

    private View mHeader;

    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private CustomViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    private int mMinHeaderHeight;
    private int mHeaderHeight;
    private int mMinHeaderTranslation;

    private int mLastY;
    private String frontEndScreen = "LIST";
    private boolean isFirstTime = true;

    Animator slideUpAnimation, slideDownAnimation;
    LinearLayout rl;
    EditText searchLocation, search;
    RelativeLayout searchConatiner;
    Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // mMinHeaderHeight = getResources().getDimensionPixelSize(R.dimen.min_header_height);
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        //    mMinHeaderTranslation = -mMinHeaderHeight;

        setContentView(R.layout.activity_main);


        slideDownAnimation = AnimatorInflater.loadAnimator(this, R.animator.slide_down_animation);

        slideUpAnimation = AnimatorInflater.loadAnimator(this, R.animator.slide_up_animation);

        mHeader = findViewById(R.id.header);
        searchConatiner = (RelativeLayout) findViewById(R.id.search_container);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        rl = (LinearLayout) findViewById(R.id.list_container);
        searchLocation = (EditText) findViewById(R.id.searchLocation);
        search = (EditText) findViewById(R.id.search);

        mHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (frontEndScreen.equals("LIST")) {
                    startSlideDownAnimation(v);
                }

            }
        });

        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (frontEndScreen.equals("MAP")) {
                    startSlideUpAnimation(v);
                }
            }
        });

        mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mPagerSlidingTabStrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSlideUpAnimation(v);
            }
        });

        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(3);

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        //mPagerAdapter.setTabHolderScrollingContent(this);

        mViewPager.setAdapter(mPagerAdapter);

        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setOnPageChangeListener(this);
        mLastY = 0;
        searchLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mPagerSlidingTabStrip.setVisibility(View.GONE);
                    searchConatiner.setVisibility(View.VISIBLE);
                    searchLocation.setVisibility(View.GONE);
                    mViewPager.setPagingEnabled(false);
                    mViewPager.setAdapter(mPagerAdapter);

                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(search, InputMethodManager.SHOW_FORCED);
                }
            }
        });
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
                    if (frontEndScreen.equals("MAP")) {
                        startSlideUpAnimation(v);
                    }
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenReset();

            }
        });
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // nothing
    }

    public void startSlideUpAnimation(View view) {
        frontEndScreen = "LIST";
        slideUpAnimation.setTarget(rl);
        slideUpAnimation.start();
    }

    public void startSlideDownAnimation(View view) {
        frontEndScreen = "MAP";
        slideDownAnimation.setTarget(rl);
        slideDownAnimation.start();

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        if (position == 0) {
            if (isFirstTime) {
                isFirstTime = false;
            } else {
                if (frontEndScreen.equals("MAP")) {
                    frontEndScreen = "LIST";
                    slideUpAnimation.setTarget(rl);
                    slideUpAnimation.start();

                }
            }

        } else {
            if (frontEndScreen.equals("MAP")) {
                frontEndScreen = "LIST";
                slideUpAnimation.setTarget(rl);
                slideUpAnimation.start();
                //rl.startAnimation(slideUpAnimation);
            }
        }

        if (positionOffsetPixels > 0) {

            int currentItem = mViewPager.getCurrentItem();

            SparseArrayCompat<ScrollTabHolder> scrollTabHolders = mPagerAdapter.getScrollTabHolders();
            ScrollTabHolder currentHolder;

            if (position < currentItem) {
                currentHolder = scrollTabHolders.valueAt(position);
            } else {
                currentHolder = scrollTabHolders.valueAt(position + 1);
            }

            if (NEEDS_PROXY) {
                // TODO is not good
                currentHolder.adjustScroll(mHeader.getHeight() - mLastY);
                mHeader.postInvalidate();
            } else {
                currentHolder.adjustScroll((int) (mHeader.getHeight() + mHeader.getTranslationY()));
            }

        }

    }

    @Override
    public void onPageSelected(int position) {

        SparseArrayCompat<ScrollTabHolder> scrollTabHolders = mPagerAdapter.getScrollTabHolders();
        ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);

        if (NEEDS_PROXY) {
            //TODO is not good
            currentHolder.adjustScroll(mHeader.getHeight() - mLastY);
            mHeader.postInvalidate();
        } else {
            currentHolder.adjustScroll((int) (mHeader.getHeight() + mHeader.getTranslationY()));
        }

    }

	/*@Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
		if (mViewPager.getCurrentItem() == pagePosition) {
			int scrollY = getScrollY(view);
			if(NEEDS_PROXY){
				//TODO is not good
				mLastY=-Math.max(-scrollY, mMinHeaderTranslation);
				//info.setText(String.valueOf(scrollY));
				mHeader.scrollTo(0, mLastY);
				mHeader.postInvalidate();
			}else{
				mHeader.setTranslationY(Math.max(-scrollY, mMinHeaderTranslation));
			}
		}
	}

	@Override
	public void adjustScroll(int scrollHeight) {
		// nothing
	}*/

    public int getScrollY(AbsListView view) {
        View c = view.getChildAt(0);
        if (c == null) {
            return 0;
        }

        int firstVisiblePosition = view.getFirstVisiblePosition();
        int top = c.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mHeaderHeight;
        }

        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    public static float clamp(float value, float max, float min) {
        return Math.max(Math.min(value, min), max);
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        private SparseArrayCompat<ScrollTabHolder> mScrollTabHolders;
        private final String[] TITLES = {"26\nLOCATIONS", "7\nWith IN 5MI", "4\nFAVORITE"};
        private ScrollTabHolder mListener;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            mScrollTabHolders = new SparseArrayCompat<ScrollTabHolder>();
        }

        public void setTabHolderScrollingContent(ScrollTabHolder listener) {
            mListener = listener;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            ScrollTabHolderFragment fragment = (ScrollTabHolderFragment) SampleListFragment.newInstance(position);

            mScrollTabHolders.put(position, fragment);
            if (mListener != null) {
                fragment.setScrollTabHolder(mListener);
            }

            return fragment;
        }

        public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
            return mScrollTabHolders;
        }

    }

    @Override
    public void onBackPressed() {
        if (searchConatiner.getVisibility() == View.VISIBLE) {
            screenReset();
        } else {
            super.onBackPressed();
        }

    }

    private void screenReset() {
        searchConatiner.setVisibility(View.GONE);
        mPagerSlidingTabStrip.setVisibility(View.VISIBLE);
        searchLocation.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        mViewPager.setPagingEnabled(true);
    }

}