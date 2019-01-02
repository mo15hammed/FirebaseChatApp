package com.example.mo15h.firebasechatapp;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    /**
     * The total number of items in the dataset after the last load
     */
    public static int mPreviousTotal = 0;
    /**
     * True if we are still waiting for the last set of data to load.
     */
    private boolean mLoading = true;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = recyclerView.getLayoutManager().getItemCount();
        int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

        Log.d(TAG, "onScrolled: mLoading 1 = " + mLoading);

        if (mLoading) {
            if (totalItemCount > mPreviousTotal) {
                Log.d(TAG, "onScrolled: IF 2");
                mLoading = false;
                mPreviousTotal = totalItemCount;
            }
        }
        int visibleThreshold = 0;
        Log.d(TAG, "onScrolled: visibleItemCount = " + visibleItemCount);
        Log.d(TAG, "onScrolled: totalItemCount = " + totalItemCount);
        Log.d(TAG, "onScrolled: firstVisibleItem = " + firstVisibleItem);
        Log.d(TAG, "onScrolled: mLoading 2 = " + mLoading);


        if (!mLoading && firstVisibleItem <= 0) {
            // End has been reached
            Log.d(TAG, "onScrolled: IF 1");
            onLoadMore();

            mLoading = true;
        }
    }

    public abstract void onLoadMore();

}