/**
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.systemui.biometrics;

import android.content.pm.PackageManager;
import android.hardware.display.ColorDisplayManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Slog;
import android.view.View;

import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.CommandQueue.Callbacks;


public class FODCircleViewImpl extends SystemUI implements CommandQueue.Callbacks {
    private static final String TAG = "FODCircleViewImpl";

    private FODCircleView mFodCircleView;
    private boolean mNightMode;
    private int mNightModeTemp;
    private static final boolean sIsOnePlus7t = android.os.Build.DEVICE.equals("oneplus7t");

    @Override
    public void start() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FOD)) {
            return;
        }
        getComponent(CommandQueue.class).addCallback(this);
        try {
            mFodCircleView = new FODCircleView(mContext);
        } catch (RuntimeException e) {
            Slog.e(TAG, "Failed to initialize FODCircleView", e);
        }
    }

    @Override
    public void showInDisplayFingerprintView() {
        if (mFodCircleView != null) {
            if (sIsOnePlus7t) {
                setNightMode(true);
            }
            mFodCircleView.show();
        }
    }

    @Override
    public void hideInDisplayFingerprintView() {
        if (mFodCircleView != null) {
            if (sIsOnePlus7t) {
                setNightMode(false);
            }
            mFodCircleView.hide();
        }
    }

    private void setNightMode(boolean state) {
        if (!SystemProperties.getBoolean("persist.fod.night_mode_enabled", true)) {
            return;
        }
        ColorDisplayManager colorDisplayManager = mContext.getSystemService(ColorDisplayManager.class);
        if (state) {
            mNightMode = colorDisplayManager.isNightDisplayActivated();
            colorDisplayManager.setNightDisplayActivated(true);
            mNightModeTemp = colorDisplayManager.getNightDisplayColorTemperature();
            colorDisplayManager.setNightDisplayColorTemperature(ColorDisplayManager.getMaximumColorTemperature(mContext));
        } else {
            colorDisplayManager.setNightDisplayActivated(mNightMode);
            colorDisplayManager.setNightDisplayColorTemperature(mNightModeTemp + 1);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    colorDisplayManager.setNightDisplayColorTemperature(mNightModeTemp);
                }
            }, 1500);
        }
    }
}
