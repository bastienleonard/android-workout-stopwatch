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
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity
{
    private TextView time;
    private Button start;
    private Button reset;
    private TextView setsCounter;
    private Button resetSetsCountButton;
    private boolean running = false;
    private long totalTime;
    private long lastTick;
    private int setsCount;

    // Delay until the next timer refresh, in milliseconds
    private int refreshDelay = 100;

    private final Runnable updater = new Runnable()
    {
        public void run()
        {
            if (running)
            {
                long newTick = SystemClock.elapsedRealtime();
                long elapsed = newTick - lastTick;
                totalTime += elapsed;
                refreshTimer();
                lastTick = newTick;
                time.postDelayed(this, refreshDelay);
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        time = (TextView)findViewById(R.id.time);
        start = (Button)findViewById(R.id.start);
        reset = (Button)findViewById(R.id.reset);
        setsCounter = (TextView)findViewById(R.id.sets_counter);
        resetSetsCountButton = (Button)findViewById(
            R.id.reset_sets_count_button);

        if (savedInstanceState != null)
        {
            running = savedInstanceState.getBoolean("running");
            totalTime = savedInstanceState.getLong("totalTime");
            lastTick = savedInstanceState.getLong("lastTick");
            setsCount = savedInstanceState.getInt("setsCount");

            if (running)
            {
                time.post(updater);
            }
        }

        refreshTimer();
        refreshSetsCount();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("running", running);
        outState.putLong("totalTime", totalTime);
        outState.putInt("setsCount", setsCount);
        outState.putLong("lastTick", lastTick);
    }

    public void start(View view)
    {
        if (!running)
        {
            running = true;
            ++setsCount;
            refreshSetsCount();
            reset.setEnabled(false);
            lastTick = SystemClock.elapsedRealtime();
            start.setText(getString(R.string.pause));
            time.post(updater);
        }
        else
        {
            running = false;
            reset.setEnabled(true);
            start.setText(getString(R.string.start));
        }
    }

    public void reset(View view)
    {
        if (!running)
        {
            resetSetsCountButton.setEnabled(true);
            totalTime = 0;
            refreshTimer();
            reset.setEnabled(false);
        }
    }

    public void resetSetsCount(View view)
    {
        setsCount = 0;
        resetSetsCountButton.setEnabled(false);
        refreshSetsCount();
    }

    private void refreshTimer()
    {
        long ticks = totalTime / 1000;
        long minutes = ticks / 60;
        long seconds = ticks % 60;
        long fraction = (totalTime % 1000) / 100;
        time.setText(String.format(getString(R.string.time_format),
                                   minutes, seconds, fraction));
    }

    private void refreshSetsCount()
    {
        setsCounter.setText(
            String.format(getString(R.string.sets_count_format),
                          setsCount));
    }
}
