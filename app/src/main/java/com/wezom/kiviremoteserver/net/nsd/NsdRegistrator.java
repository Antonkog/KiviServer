package com.wezom.kiviremoteserver.net.nsd;

import android.content.ContentResolver;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.di.qualifiers.ApplicationContext;

import javax.inject.Inject;

import timber.log.Timber;

import static com.wezom.kiviremoteserver.common.Constants.APPLICATION_UID;

/**
 * Created by andre on 02.06.2017.
 */

public class NsdRegistrator {

    private static final String SERVICE_MASK = "(KIVI_TV)";
    private static final String SERVICE_TYPE = "_http._tcp.";
    private static final String SECURE_SETTINGS_BLUETOOTH_NAME="bluetooth_name";

    private Context context;
    private NsdManager.RegistrationListener registrationListener;

    private boolean isRegistered = false;

    @Inject
    public NsdRegistrator(@ApplicationContext Context context) {
        this.context = context;
        registrationListener = getRegistrationListener();
    }

    String getUUIDMask() {
        String uuid = PreferenceManager.getDefaultSharedPreferences(context).getString(APPLICATION_UID, null);
        if (uuid != null)
            return " [" + uuid + "]" + SERVICE_MASK;
        return SERVICE_MASK;
    }

    String getDeviceName() {
        return DeviceUtils.getDeviceName();
    }

    public void registerServiceNsd(int port, NsdManager nsdManager) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        // The name is subject to change based on conflicts
        // with other services advertised on the same network.

        ContentResolver  mContentResolver = context.getContentResolver();
        String name = Settings.Secure.getString(mContentResolver,SECURE_SETTINGS_BLUETOOTH_NAME);
        serviceInfo.setServiceName(name + getUUIDMask());
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);
        Timber.d("Register nsd %s ", getDeviceName() + getUUIDMask());
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        isRegistered = true;
    }

    private NsdManager.RegistrationListener getRegistrationListener() {
        return new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                Timber.d("NSD Service registered:" + nsdServiceInfo.getServiceName());
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Timber.d("NSD Service RegistrationFailed -> cause - $arg1 ");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Timber.d("NSD Service unRegistered");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Timber.d("NSD Service onUnregistrationFailed");
            }
        };
    }

    public void unregisterNsd(NsdManager nsdManager) {
        try {
            if (isRegistered)
                nsdManager.unregisterService(registrationListener);
        } catch (Exception e) {
            Timber.e(e, "Listener wasn't registered: " + e.getMessage());
        }
    }
}
