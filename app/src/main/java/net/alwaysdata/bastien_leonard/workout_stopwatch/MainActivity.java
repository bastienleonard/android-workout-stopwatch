// Copyright 2012 Bastien Léonard. All rights reserved.

// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:

//    1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.

//    2. Redistributions in binary form must reproduce the above
//    copyright notice, this list of conditions and the following
//    disclaimer in the documentation and/or other materials provided
//    with the distribution.

// THIS SOFTWARE IS PROVIDED BY BASTIEN LÉONARD ``AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BASTIEN LÉONARD OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
// USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

package net.alwaysdata.bastien_leonard.workout_stopwatch;

import java.lang.Runnable;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity {
    // Delay until the next timer refresh, in milliseconds
    private static final long REFRESH_DELAY = 100L;

    private TextView mTime;
    private Button mStart;
    private Button mReset;
    private TextView mSetsCounter;
    private Button mResetSetsCountButton;
    private boolean mRunning = false;
    private long mTotalTime;
    private long mLastTick;
    private int mSetsCount;
    private Handler mHandler;

    private final Runnable mUpdater = new Runnable() {
        public void run() {
            if (mRunning) {
                long newTick = SystemClock.elapsedRealtime();
                long elapsed = newTick - mLastTick;
                mTotalTime += elapsed;
                refreshTimer();
                mLastTick = newTick;
                mHandler.postDelayed(this, REFRESH_DELAY);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mHandler = new Handler();
        mTime = (TextView) findViewById(R.id.time);
        mStart = (Button) findViewById(R.id.start);
        mReset = (Button) findViewById(R.id.reset);
        mSetsCounter = (TextView) findViewById(R.id.sets_counter);
        mResetSetsCountButton = (Button) findViewById(
                R.id.reset_sets_count_button);

        if (savedInstanceState != null) {
            mRunning = savedInstanceState.getBoolean("mRunning");
            mTotalTime = savedInstanceState.getLong("mTotalTime");
            mLastTick = savedInstanceState.getLong("mLastTick");
            mSetsCount = savedInstanceState.getInt("mSetsCount");
        }

        refreshTimer();
        refreshSetsCount();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mRunning", mRunning);
        outState.putLong("mTotalTime", mTotalTime);
        outState.putInt("mSetsCount", mSetsCount);
        outState.putLong("mLastTick", mLastTick);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mRunning) {
            mHandler.post(mUpdater);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mUpdater);
    }

    public void start(View view) {
        if (!mRunning) {
            mRunning = true;
            ++mSetsCount;
            refreshSetsCount();
            mReset.setEnabled(false);
            mLastTick = SystemClock.elapsedRealtime();
            mStart.setText(getString(R.string.pause));
            mHandler.post(mUpdater);
        } else {
            mRunning = false;
            mReset.setEnabled(true);
            mStart.setText(getString(R.string.start));
        }
    }

    public void reset(View view) {
        if (!mRunning) {
            mResetSetsCountButton.setEnabled(true);
            mTotalTime = 0;
            refreshTimer();
            mReset.setEnabled(false);
        }
    }

    public void resetSetsCount(View view) {
        mSetsCount = 0;
        mResetSetsCountButton.setEnabled(false);
        refreshSetsCount();
    }

    private void refreshTimer() {
        long ticks = mTotalTime / 1000;
        long minutes = ticks / 60;
        long seconds = ticks % 60;
        long fraction = (mTotalTime % 1000) / 100;
        mTime.setText(String.format(getString(R.string.time_format),
                minutes, seconds, fraction));
    }

    private void refreshSetsCount() {
        mSetsCounter.setText(
                String.format(getString(R.string.sets_count_format),
                        mSetsCount));
    }
}
