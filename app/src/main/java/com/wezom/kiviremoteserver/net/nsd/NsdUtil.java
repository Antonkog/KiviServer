package com.wezom.kiviremoteserver.net.nsd;

import android.content.Context;
import android.database.ContentObserver;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.android.inputmethod.pinyin.util.PropertyHelper;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.di.qualifiers.ApplicationContext;
import com.wezom.kiviremoteserver.environment.EnvironmentFactory;

import javax.inject.Inject;

import timber.log.Timber;
import wezom.kiviremoteserver.environment.bridge.BridgeGeneral;

import static com.wezom.kiviremoteserver.common.Constants.APPLICATION_UID;

public class NsdUtil {

    private static final String TAG = "NsdUtil";
    private static final String SERVICE_MASK = "(KIVI_TV)";
    private static final String SERVICE_TYPE = "_http._tcp.";
    public static final String DEVICE_NAME_KEY = "device_name";

    private String mServiceName;

    private NsdServiceInfo nsdServiceInfo;

    private Context context;
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener mRegistrationListener;
    private NameChangeObserver nameChangeObserver = null;
    private Handler handler = null;

    @Inject
    public NsdUtil(@ApplicationContext Context context) {
        this.context = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mServiceName = setServiceName();
    }

    private String setServiceName() {
        mServiceName = Build.MODEL;
        if (App.isTVRealtek()) {
            mServiceName = PropertyHelper.getProperty("ro.product.panel");
            if (mServiceName.isEmpty()) mServiceName = Build.MODEL;
        }
        return mServiceName;
    }

    private void observeNameChange(@ApplicationContext Context context) {
        handler = new Handler(Looper.getMainLooper());
        nameChangeObserver = new NameChangeObserver(handler);
        context.getContentResolver().registerContentObserver(Settings.Global.CONTENT_URI, true, nameChangeObserver);
    }


    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Timber.d(TAG + "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Timber.d(TAG + "Service discovery success " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Timber.d(TAG + "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Timber.d(TAG + "Same machine: " + mServiceName);
                    nsdServiceInfo = service;
                    syncDeviceName();
                } else if (service.getServiceName().contains(mServiceName)) {
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                if (service != null)
                    Timber.e(TAG + "service lost" + service.toString());
                if (nsdServiceInfo != null)
                    Timber.e("\n old service :  \n" + nsdServiceInfo.toString());
                if (nsdServiceInfo == service) {
                    nsdServiceInfo = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Timber.d(TAG + "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Timber.e(TAG + "Discovery failed: Error code:" + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Timber.e(TAG + "Discovery failed: Error code:" + errorCode);
            }
        };
    }

    private void syncDeviceName() {
        String deviceName = Settings.Global.getString(context.getContentResolver(), DEVICE_NAME_KEY);
        if (BridgeGeneral.ENVIRONMENT != EnvironmentFactory.ENVIRONMENT_MOCK)
            Settings.System.putString(context.getContentResolver(), DEVICE_NAME_KEY, deviceName);
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Timber.e(TAG + "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Timber.e(TAG + "Resolve Succeeded. " + serviceInfo);
                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Timber.d(TAG + "Same IP.");
                    return;
                }
                nsdServiceInfo = serviceInfo;
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Timber.d(TAG + "Service registered: " + mServiceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Timber.d(TAG + "Service registration failed: " + arg1);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Timber.d(TAG + "Service unregistered: " + arg0.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Timber.d(TAG + "Service unregistration failed: " + errorCode);
            }
        };
    }


    String getUUIDMask() {
        String uuid = PreferenceManager.getDefaultSharedPreferences(context).getString(APPLICATION_UID, null);
        if (uuid != null)
            return " [" + uuid + "]" + SERVICE_MASK;
        return SERVICE_MASK;
    }

    public void registerService(int port) {
        String deviceName = Settings.Global.getString(context.getContentResolver(), DEVICE_NAME_KEY);
        tearDown();  // Cancel any previous registration request
        observeNameChange(context);
        initializeRegistrationListener();
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(deviceName + getUUIDMask());
        serviceInfo.setServiceType(SERVICE_TYPE);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void discoverServices() {
        stopDiscovery();  // Cancel any existing discovery request
        initializeDiscoveryListener();
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            } catch (Exception e) {

            } finally {
                mDiscoveryListener = null;
            }
        }
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return nsdServiceInfo;
    }

    public void tearDown() {
        if (mRegistrationListener != null) {
            try {
                mNsdManager.unregisterService(mRegistrationListener);
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
                nameChangeObserver = null;
                handler = null;
            } catch (Exception e) {
                Timber.d(TAG + "tearDown fails " + e.getMessage());
            } finally {
                mRegistrationListener = null;
            }
        }
    }

    private class NameChangeObserver extends ContentObserver {
        public NameChangeObserver(Handler h) {
            super(h);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "MyContentObserver.onChange(" + selfChange + ")");
            super.onChange(selfChange);
            syncDeviceName();
            // here you call the method to fill the list
        }
    }
}