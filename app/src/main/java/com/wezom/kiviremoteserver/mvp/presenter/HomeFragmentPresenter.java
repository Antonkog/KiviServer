package com.wezom.kiviremoteserver.mvp.presenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.arellomobile.mvp.InjectViewState;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.bus.NetworkStateEvent;
import com.wezom.kiviremoteserver.bus.RestartServerEvent;
import com.wezom.kiviremoteserver.bus.StopReceivingEvent;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.di.qualifiers.ApplicationContext;
import com.wezom.kiviremoteserver.mvp.view.HomeFragmentView;
import com.wezom.kiviremoteserver.service.KiviRemoteService;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * Created by andre on 02.06.2017.
 */
@InjectViewState
public class HomeFragmentPresenter extends BasePresenter<HomeFragmentView> {

    @Inject
    @ApplicationContext
    Context context;

    private CompositeDisposable compDisp = new CompositeDisposable();
    private KiviRemoteService.ServiceBinder binder;
    private Handler msgHandler = new Handler();

    private ServiceConnection connection;
    private boolean isBound;

    public HomeFragmentPresenter() {
        App.getApplicationComponent().inject(this);
    }

    public void killServerService() {
        getViewState().unbindService(connection);
        KiviRemoteService.stop(context);
        RxBus.INSTANCE.publish(new StopReceivingEvent());

        if (Constants.DEBUG)
            getViewState().showToastMessage(R.string.server_terminate_message);
    }

    public void startServerService() {
        if (KiviRemoteService.isStarted) {
            killServerService();
        }
        KiviRemoteService.launch(context);
        getViewState().bindService(connection);
    }

    public void sendDisplaySize(int x, int y) {
//        bus.post(new SendDisplaySize(x, y));
    }

    public void initConnection() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                binder = (KiviRemoteService.ServiceBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder = null;
            }
        };
    }

    public void onStart() {
        getViewState().bindService(connection);

        msgHandler.postDelayed(() -> {
            if (binder != null) {
                getViewState().setIpAdress(binder.getIpAddress());
            }
        }, 400);

        RxBus.INSTANCE.listen(NetworkStateEvent.class).subscribe(event -> {
            if (event.isNetworkAvailable()) {
                getViewState().networkAvailable();
            } else {
                getViewState().networkNoAvailable();
            }
        }, Timber::e);

        RxBus.INSTANCE.listen(RestartServerEvent.class).subscribe(event -> {
            startServerService();
        }, Timber::e);
    }

    public void onStop() {
        getViewState().unbindService(connection);
    }

    @Override
    public void onDestroy() {
        compDisp.clear();
        super.onDestroy();
    }

    public boolean isBound() {
        return isBound;
    }

    public void setBound(boolean bound) {
        isBound = bound;
    }
}
