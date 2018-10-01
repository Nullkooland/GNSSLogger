/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.location.gps.gnsslogger;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * The UI fragment that hosts a logging view.
 */
public class ResultFragment extends Fragment {

    private final UIResultComponent mUiComponent = new UIResultComponent();
    private TextView mLogView;
    private ScrollView mScrollView;
    private RealTimePositionVelocityCalculator mPositionVelocityCalculator;

    public void setPositionVelocityCalculator(RealTimePositionVelocityCalculator value) {
        mPositionVelocityCalculator = value;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View newView = inflater.inflate(R.layout.results_log, container, false /* attachToRoot */);
        mLogView = newView.findViewById(R.id.log_view);
        mScrollView = newView.findViewById(R.id.log_scroll);

        RealTimePositionVelocityCalculator currentPositionVelocityCalculator =
                mPositionVelocityCalculator;
        if (currentPositionVelocityCalculator != null) {
            currentPositionVelocityCalculator.setUiResultComponent(mUiComponent);
        }

        Button start = newView.findViewById(R.id.start_log);
        start.setOnClickListener(
                view -> mScrollView.fullScroll(View.FOCUS_UP));

        Button end = newView.findViewById(R.id.end_log);
        end.setOnClickListener(
                view -> mScrollView.fullScroll(View.FOCUS_DOWN));

        Button clear = newView.findViewById(R.id.clear_log);
        clear.setOnClickListener(
                view -> mLogView.setText(""));
        return newView;
    }

    /**
     * A facade for UI and Activity related operations that are required for {@link GnssListener}s.
     */
    public class UIResultComponent {

        private static final int MAX_LENGTH = 12000;
        private static final int LOWER_THRESHOLD = (int) (MAX_LENGTH * 0.5);

        public synchronized void logTextResults(final String tag, final String text, int color) {
            final SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(tag).append(" | ").append(text).append("\n");

            builder.setSpan(
                    new ForegroundColorSpan(color),
                    0 /* start */,
                    builder.length(),
                    SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);

            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(
                    () -> {
                        mLogView.append(builder);
                        SharedPreferences sharedPreferences = PreferenceManager.
                                getDefaultSharedPreferences(getActivity());
                        Editable editable = mLogView.getEditableText();
                        int length = editable.length();
                        if (length > MAX_LENGTH) {
                            editable.delete(0, length - LOWER_THRESHOLD);
                        }
                        if (sharedPreferences.getBoolean(
                                SettingsFragment.PREFERENCE_KEY_AUTO_SCROLL, false /*default return value*/)) {
                            mScrollView.post(() -> mScrollView.fullScroll(View.FOCUS_DOWN));
                        }
                    });
        }

        public void startActivity(Intent intent) {
            getActivity().startActivity(intent);
        }
    }
}
