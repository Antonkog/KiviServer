package com.wezom.kiviremoteserver.interfaces;

import android.content.Context;

import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;
import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode;
import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio;
import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;
import wezom.kiviremoteserver.environment.bridge.driver_set.TemperatureValues;

public class InitialMessage {
    public String buildProp;
    public List<DriverValue> driverValueList;


    public void setDriverValueList(Context context) {
        try {
            this.driverValueList = new LinkedList();
            driverValueList.addAll(Ratio.getInstance().getAsDriverList(context));
            driverValueList.addAll(PictureMode.getInstance().getAsDriverList(context));
            driverValueList.addAll(SoundValues.getInstance().getAsDriverList(context));
            driverValueList.addAll(TemperatureValues.getInstance().getAsDriverList(context));
            driverValueList.addAll(InputSourceHelper.getAsDriverList(context));
            this.buildProp = getBuildProp();
        } catch (Exception e) {
            Timber.e(e.getMessage());
        }
    }


    private String getBuildProp() {
        Process process = null;
        try {
            process = new ProcessBuilder().command("/system/bin/getprop")
                    .redirectErrorStream(true).start();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.toLowerCase().contains(Constants.BOARD) ||
                        line.toLowerCase().contains(Constants.NAME) ||
                        line.toLowerCase().contains(Constants.SCREEN) ||
                        line.toLowerCase().contains(Constants.MODEL) ||
                        line.toLowerCase().contains(Constants.PANEL) ||
                        line.toLowerCase().contains(Constants.INCREMENTAL) ||
                        line.toLowerCase().contains(Constants.DEVICE))
                    log.append(line + "\n");
            }
            return log.toString();
        } catch (IOException e) {
            Timber.e(" error while getprop " + e.getMessage());
            e.printStackTrace();
            return "";
        } finally {
            process.destroy();
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DriverValue value : driverValueList) {
            sb.append(value.toString());
        }

        return "InitialMessage{" +
                "buildProp='" + buildProp + '\'' +
                ", driverValueList=" + sb.toString() +
                '}';
    }
}