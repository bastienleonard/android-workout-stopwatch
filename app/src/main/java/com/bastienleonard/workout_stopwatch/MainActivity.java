// Copyright 2012, 2015 Bastien Léonard. All rights reserved.

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

package com.bastienleonard.workout_stopwatch;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.lang.Runnable;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {
    // Delay until the next timer refresh, in milliseconds
    private static final long REFRESH_DELAY = 100L;

    private static final String STATE_RUNNING = "running";
    private static final String STATE_TOTAL_TIME = "totalTime";
    private static final String STATE_LAST_TICK = "lastTick";
    private static final String STATE_SETS_COUNT = "setsCount";

    private TextView mTime;
    private Button mStart;
    private Button mReset;
    private TextView mSetsCounter;
    private Button mResetSetsCount;
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
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler = new Handler();
        mTime = (TextView) findViewById(R.id.time);
        mStart = (Button) findViewById(R.id.start);
        mReset = (Button) findViewById(R.id.reset);
        mSetsCounter = (TextView) findViewById(R.id.sets_counter);
        mResetSetsCount = (Button) findViewById(
                R.id.reset_sets_count_button);

        if (savedInstanceState != null) {
            mRunning = savedInstanceState.getBoolean(STATE_RUNNING);
            mTotalTime = savedInstanceState.getLong(STATE_TOTAL_TIME);
            mLastTick = savedInstanceState.getLong(STATE_LAST_TICK);
            mSetsCount = savedInstanceState.getInt(STATE_SETS_COUNT);
        }

        refreshButtons();
        refreshTimer();
        refreshSetsCount();

        mStart.setOnClickListener(this);
        mReset.setOnClickListener(this);
        mResetSetsCount.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                start();
                break;
            case R.id.reset:
                reset();
                break;
            case R.id.reset_sets_count_button:
                resetSetsCount();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RUNNING, mRunning);
        outState.putLong(STATE_TOTAL_TIME, mTotalTime);
        outState.putInt(STATE_SETS_COUNT, mSetsCount);
        outState.putLong(STATE_LAST_TICK, mLastTick);
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

    public void start() {
        if (mRunning) {
            mRunning = false;
        } else {
            mRunning = true;
            ++mSetsCount;
            refreshSetsCount();
            mLastTick = SystemClock.elapsedRealtime();
            mHandler.post(mUpdater);
        }

        refreshButtons();
    }

    public void reset() {
        if (!mRunning) {
            mResetSetsCount.setEnabled(true);
            mTotalTime = 0;
            refreshTimer();
            mReset.setEnabled(false);
        }
    }

    public void resetSetsCount() {
        mSetsCount = 0;
        mResetSetsCount.setEnabled(false);
        refreshSetsCount();
    }

    private void refreshButtons() {
        if (mRunning) {
            mReset.setEnabled(false);
            mStart.setText(getString(R.string.pause));
        } else {
            mReset.setEnabled(true);
            mStart.setText(getString(R.string.start));
        }
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
