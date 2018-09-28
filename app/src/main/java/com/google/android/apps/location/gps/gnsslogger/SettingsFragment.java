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

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;

/**
 * The UI fragment showing a set of configurable settings for the client to request GPS data.
 */
public class SettingsFragment extends Fragment {

    public static final String TAG = ":SettingsFragment";
    /**
     * Key in the {@link SharedPreferences} indicating whether auto-scroll has been enabled
     */
    protected static String PREFERENCE_KEY_AUTO_SCROLL = "autoScroll";
    /**
     * Position in the drop down menu of the auto ground truth mode
     */
    private static int AUTO_GROUND_TRUTH_MODE = 3;
    private GnssContainer mGnssContainer;
    private AlternativeFileLogger mAlternativeFileLogger;
    private HelpDialog helpDialog;

    /**
     * The {@link RealTimePositionVelocityCalculator} set for receiving the ground truth mode switch
     */
    private RealTimePositionVelocityCalculator mRealTimePositionVelocityCalculator;

    /**
     * User selection of ground truth mode, initially set to be disabled
     */
    private int mResidualSetting = RealTimePositionVelocityCalculator.RESIDUAL_MODE_DISABLED;

    /**
     * The reference ground truth location by user input.
     */
    private double[] mFixedReferenceLocation;

    /**
     * {@link GroundTruthModeSwitcher} to receive update from AR result broadcast
     */
    private GroundTruthModeSwitcher mModeSwitcher;

    public void setGnssContainer(GnssContainer value) {
        mGnssContainer = value;
    }

    public void setAlternativeFileLogger(AlternativeFileLogger value) {
        mAlternativeFileLogger = value;
    }

    /**
     * Set up {@link MainActivity} to receive update from AR result broadcast
     */
    public void setAutoModeSwitcher(GroundTruthModeSwitcher modeSwitcher) {
        mModeSwitcher = modeSwitcher;
    }

    /**
     * Set up {@code RealTimePositionVelocityCalculator} for receiving changes in ground truth mode
     */
    public void setRealTimePositionVelocityCalculator(
            RealTimePositionVelocityCalculator realTimePositionVelocityCalculator) {
        mRealTimePositionVelocityCalculator = realTimePositionVelocityCalculator;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false /* attachToRoot */);

        Switch logSensor = view.findViewById(R.id.log_sensor);
        TextView logSensorLabel =
                view.findViewById(R.id.log_sensor_label);
        //set the switch to OFF
        logSensor.setChecked(false);
        logSensorLabel.setText("Switch is OFF");
        logSensor.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        mAlternativeFileLogger.registerOrientationSensorListener();
                        logSensorLabel.setText("Switch is ON");
                    } else {
                        mAlternativeFileLogger.unregisterOrientationSensorListener();
                        logSensorLabel.setText("Switch is OFF");
                    }
                });

        Switch registerLocation = view.findViewById(R.id.register_location);
        TextView registerLocationLabel =
                view.findViewById(R.id.register_location_label);
        //set the switch to OFF
        registerLocation.setChecked(false);
        registerLocationLabel.setText("Switch is OFF");
        registerLocation.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {
                        mGnssContainer.registerLocation();
                        registerLocationLabel.setText("Switch is ON");
                    } else {
                        mGnssContainer.unregisterLocation();
                        registerLocationLabel.setText("Switch is OFF");
                    }
                });

        Switch registerMeasurements = view.findViewById(R.id.register_measurements);
        TextView registerMeasurementsLabel =
                view.findViewById(R.id.register_measurement_label);
        //set the switch to OFF
        registerMeasurements.setChecked(false);
        registerMeasurementsLabel.setText("Switch is OFF");
        registerMeasurements.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {
                        mGnssContainer.registerMeasurements();
                        registerMeasurementsLabel.setText("Switch is ON");
                    } else {
                        mGnssContainer.unregisterMeasurements();
                        registerMeasurementsLabel.setText("Switch is OFF");
                    }
                });

        Switch registerNavigation = view.findViewById(R.id.register_navigation);
        TextView registerNavigationLabel =
                view.findViewById(R.id.register_navigation_label);
        //set the switch to OFF
        registerNavigation.setChecked(false);
        registerNavigationLabel.setText("Switch is OFF");
        registerNavigation.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked) {
                            mGnssContainer.registerNavigation();
                            registerNavigationLabel.setText("Switch is ON");
                        } else {
                            mGnssContainer.unregisterNavigation();
                            registerNavigationLabel.setText("Switch is OFF");
                        }
                    }
                });

        Switch registerGpsStatus = view.findViewById(R.id.register_status);
        TextView registerGpsStatusLabel =
                view.findViewById(R.id.register_status_label);
        //set the switch to OFF
        registerGpsStatus.setChecked(false);
        registerGpsStatusLabel.setText("Switch is OFF");
        registerGpsStatus.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {
                        mGnssContainer.registerGnssStatus();
                        registerGpsStatusLabel.setText("Switch is ON");
                    } else {
                        mGnssContainer.unregisterGpsStatus();
                        registerGpsStatusLabel.setText("Switch is OFF");
                    }
                });

        Switch registerNmea = view.findViewById(R.id.register_nmea);
        TextView registerNmeaLabel = view.findViewById(R.id.register_nmea_label);
        //set the switch to OFF
        registerNmea.setChecked(false);
        registerNmeaLabel.setText("Switch is OFF");
        registerNmea.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {
                        mGnssContainer.registerNmea();
                        registerNmeaLabel.setText("Switch is ON");
                    } else {
                        mGnssContainer.unregisterNmea();
                        registerNmeaLabel.setText("Switch is OFF");
                    }
                });

        Switch gpsOnly = view.findViewById(R.id.gps_only);
        TextView gpsOnlyLabel = view.findViewById(R.id.gps_only_label);
        //set the switch to OFF
        gpsOnly.setChecked(false);
        gpsOnlyLabel.setText("Switch is OFF");
        gpsOnly.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {
                        mGnssContainer.setGpsOnly(true);
                        gpsOnlyLabel.setText("Switch is ON");
                    } else {
                        mGnssContainer.setGpsOnly(false);
                        gpsOnlyLabel.setText("Switch is OFF");
                    }
                });

        Switch autoScroll = view.findViewById(R.id.auto_scroll_on);
        TextView turnOnAutoScroll = view.findViewById(R.id.turn_on_auto_scroll);
        turnOnAutoScroll.setText("Switch is OFF");
        autoScroll.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getActivity());
                    Editor editor = sharedPreferences.edit();
                    if (isChecked) {
                        editor.putBoolean(PREFERENCE_KEY_AUTO_SCROLL, true);
                        editor.apply();
                        turnOnAutoScroll.setText("Switch is ON");
                    } else {
                        editor.putBoolean(PREFERENCE_KEY_AUTO_SCROLL, false);
                        editor.apply();
                        turnOnAutoScroll.setText("Switch is OFF");
                    }
                });

        Switch residualPlotSwitch = view.findViewById(R.id.residual_plot_enabled);
        TextView turnOnResidual = view.findViewById(R.id.turn_on_residual_plot);
        turnOnResidual.setText("Switch is OFF");
        residualPlotSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {

                        LayoutInflater inflater1 =
                                (LayoutInflater)
                                        getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = inflater1.inflate(R.layout.pop_up_window,
                                getActivity().findViewById(R.id.pop));

                        // Find UI elements in pop up window
                        Spinner residualSpinner = layout.findViewById(R.id.residual_spinner);
                        Button buttonOk = layout.findViewById(R.id.popup_button_ok);
                        Button buttonCancel = layout.findViewById(R.id.popup_button_cancel);
                        TextView longitudeInput = layout.findViewById(R.id.longitude_input);
                        TextView latitudeInput = layout.findViewById(R.id.latitude_input);
                        TextView altitudeInput = layout.findViewById(R.id.altitude_input);

                        // Set up pop up window attributes
                        PopupWindow popupWindow =
                                new PopupWindow(layout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        popupWindow.setOutsideTouchable(false);
                        popupWindow.showAtLocation(
                                view.findViewById(R.id.setting_root), Gravity.CENTER, 0, 0);
                        View container1 = (View) popupWindow.getContentView().getParent();
                        WindowManager wm =
                                (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                        WindowManager.LayoutParams params =
                                (WindowManager.LayoutParams) container1.getLayoutParams();
                        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                        params.dimAmount = 0.5f;
                        wm.updateViewLayout(container1, params);
                        mResidualSetting = RealTimePositionVelocityCalculator.RESIDUAL_MODE_DISABLED;
                        // When the window is dismissed same as cancel
                        popupWindow.setOnDismissListener(
                                () -> {
                                    if (mResidualSetting
                                            == RealTimePositionVelocityCalculator.RESIDUAL_MODE_DISABLED) {
                                        residualPlotSwitch.setChecked(false);
                                    } else {
                                        mRealTimePositionVelocityCalculator
                                                .setResidualPlotMode
                                                        (mResidualSetting,
                                                                mFixedReferenceLocation);
                                        turnOnResidual.setText("Switch is ON");
                                    }
                                }
                        );

                        buttonCancel.setOnClickListener(
                                v -> popupWindow.dismiss()
                        );

                        // Button handler to dismiss the window and store settings
                        buttonOk.setOnClickListener(
                                v -> {
                                    double longitudeDegrees =
                                            longitudeInput.getText().toString().equals("")
                                                    ? Double.NaN
                                                    : Double.parseDouble(longitudeInput.getText().toString());
                                    double latitudeDegrees =
                                            latitudeInput.getText().toString().equals("")
                                                    ? Double.NaN
                                                    : Double.parseDouble(latitudeInput.getText().toString());
                                    double altitudeMeters =
                                            altitudeInput.getText().toString().equals("")
                                                    ? Double.NaN
                                                    : Double.parseDouble(altitudeInput.getText().toString());
                                    mFixedReferenceLocation =
                                            new double[]{latitudeDegrees, longitudeDegrees, altitudeMeters};
                                    mResidualSetting = residualSpinner.getSelectedItemPosition();

                                    // If user select auto, we need to put moving first and turn on AR updates
                                    if (mResidualSetting == AUTO_GROUND_TRUTH_MODE) {
                                        mResidualSetting
                                                = RealTimePositionVelocityCalculator.RESIDUAL_MODE_MOVING;
                                        mModeSwitcher.setAutoSwitchGroundTruthModeEnabled(true);
                                    }
                                    popupWindow.dismiss();
                                }
                        );

                    } else {
                        mModeSwitcher.setAutoSwitchGroundTruthModeEnabled(false);
                        mRealTimePositionVelocityCalculator.setResidualPlotMode(
                                RealTimePositionVelocityCalculator.RESIDUAL_MODE_DISABLED,
                                mFixedReferenceLocation);
                        turnOnResidual.setText("Switch is OFF");
                    }
                }
        );
        Button help = view.findViewById(R.id.help);
        helpDialog = new HelpDialog(getContext());
        helpDialog.setTitle("Help contents");
        helpDialog.create();

        help.setOnClickListener(
                view1 -> helpDialog.show());

        Button exit = view.findViewById(R.id.exit);
        exit.setOnClickListener(
                view12 -> getActivity().finishAffinity());

        TextView swInfo = view.findViewById(R.id.sw_info);

        java.lang.reflect.Method method;
        LocationManager locationManager = mGnssContainer.getLocationManager();
        try {
            method = locationManager.getClass().getMethod("getGnssYearOfHardware");
            int hwYear = (int) method.invoke(locationManager);
            if (hwYear == 0) {
                swInfo.append("HW Year: " + "2015 or older \n");
            } else {
                swInfo.append("HW Year: " + hwYear + "\n");
            }

        } catch (NoSuchMethodException e) {
            logException("No such method exception: ", e);
            return null;
        } catch (IllegalAccessException e) {
            logException("Illegal Access exception: ", e);
            return null;
        } catch (InvocationTargetException e) {
            logException("Invocation Target Exception: ", e);
            return null;
        }

        swInfo.append("Platform: " + Build.VERSION.RELEASE + "\n");
        swInfo.append("Api Level: " + Build.VERSION.SDK_INT);

        return view;
    }

    private void logException(String errorMessage, Exception e) {
        Log.e(GnssContainer.TAG + TAG, errorMessage, e);
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }
}
