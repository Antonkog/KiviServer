package com.wezom.kiviremoteserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wezom.kiviremoteserver.bus.SendAppsListEvent;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.RxBus;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by andre on 06.06.2017.
 */

public class AppsChangeReceiver extends BroadcastReceiver {
   private Disposable requestAppsDisposable;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getExtras()!=null && intent.getExtras().containsKey(Intent.EXTRA_REPLACING)){
            return; // app's replaced, means that added will follow , so can ignore.
        }

        if (requestAppsDisposable != null && !requestAppsDisposable.isDisposed()) {
            requestAppsDisposable.dispose();
        }

        requestAppsDisposable = Observable
                .fromCallable(() -> DeviceUtils.getInstalledApplications(context, context.getPackageManager()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        apps -> RxBus.INSTANCE.publish(new SendAppsListEvent(apps)),
                        e -> Timber.e(e, e.getMessage()));

    }
}
