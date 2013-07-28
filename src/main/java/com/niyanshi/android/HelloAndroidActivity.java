package com.niyanshi.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.niyanshi.android.actionbaroverlay.R;

public class HelloAndroidActivity extends SherlockActivity {

    private static String TAG = "actionbar-overlay";

    private ViewGroup mViewWrapper;
    private ActivityCoverLayout mCoverView;

    private boolean mCovered;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewWrapper = setContentViewWithWrapper(R.layout.main);
        mCoverView = (ActivityCoverLayout)getLayoutInflater().inflate(R.layout.cover_view, null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mCoverView.setDragableResourceId(R.id.movable);
        mViewWrapper.addView(mCoverView, lp);
        mCovered = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_show_cover){
            mCoverView.slideUp();
            mCovered = true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ViewGroup setContentViewWithWrapper(int resContent) {
        ViewGroup decorView = (ViewGroup) this.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);
        decorView.removeAllViews();
        ViewGroup wrapperView = new RelativeLayout(this);
        wrapperView.setId(R.id.ACTIVITY_LAYOUT_WRAPPER);
        decorView.addView(wrapperView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams params = decorChild.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wrapperView.addView(decorChild, params);
        LayoutInflater.from(this).inflate(resContent,
                (ViewGroup) ((ViewGroup) wrapperView.getChildAt(0)).getChildAt(1), true);
        return wrapperView;
    }

    @Override
    public void onBackPressed() {
        if (mCovered){
            mCoverView.slideDown();
        } else {
            super.onBackPressed();
        }
    }
}

