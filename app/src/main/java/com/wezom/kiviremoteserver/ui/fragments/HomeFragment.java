package com.wezom.kiviremoteserver.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.ImeUtils;
import com.wezom.kiviremoteserver.common.NetConnectionUtils;
import com.wezom.kiviremoteserver.mvp.presenter.HomeFragmentPresenter;
import com.wezom.kiviremoteserver.mvp.view.HomeFragmentView;
import com.wezom.kiviremoteserver.service.CursorService;
import com.wezom.kiviremoteserver.service.RemoteReceiverService;
import com.wezom.kiviremoteserver.service.RemoteSenderService;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Created by andre on 02.06.2017.
 */

public class HomeFragment extends BaseFragment implements HomeFragmentView {

    @InjectPresenter
    HomeFragmentPresenter presenter;
    @BindView(R.id.tvTopInfo)
    TextView tvTopInfo;
    @BindView(R.id.tvIpAddress)
    TextView tvIpAddress;
    @BindView(R.id.tvLog)
    TextView tvLog;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.container)
    RelativeLayout container;

    int screenWidth = 0;
    int screenHeight = 0;
    boolean isRunning = false;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        presenter.initConnection();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!RemoteSenderService.isStarted || !RemoteReceiverService.isStarted) {
            presenter.startServerService();
        }
        printCurrentIme();
        tvTopInfo.setText("Running on real AndroidTV " + DeviceUtils.isTvDevice(getContext()));

        initCursor();
        setPermission();
    }

    public void setPermission() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    100);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234) {
            startCursorService();
        }
    }

    //endregion

    //region Override methods
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_home;
    }

    @Override
    public void injectDependencies() {
        getFragmentComponent().inject(this);
    }

    @Override
    protected Toolbar getToolbar() {
        return null;
    }

    @Override
    public void printLog(String t) {
        if (t != null)
            tvLog.append(t + "\n");
        scrollView.smoothScrollBy(0, 100);
    }

    @Override
    public void setIpAdress(String adress) {
        tvIpAddress.setText(adress);
    }

    @Override
    public void bindService(ServiceConnection connection) {
        try {
            getActivity().bindService(new Intent(getActivity(), RemoteSenderService.class),
                    connection, Context.BIND_AUTO_CREATE);
            presenter.setBound(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unbindService(ServiceConnection connection) {
        if (presenter.isBound()) {
            getActivity().unbindService(connection);
            presenter.setBound(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isRunning = false;
        // close our cursor service
        cmdTurnCursorServiceOff();
    }

    //endregion

    public void openSetttingsIME() {
        startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
    }

    private void printCurrentIme() {
        tvTopInfo.append("\n" + ImeUtils.getCurrentImeKeyboardInfo(getContext()));
    }

    private void initCursor() {
        // get screen size
        DisplayMetrics metrics = new DisplayMetrics();
        try {
            WindowManager winMgr = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            winMgr.getDefaultDisplay().getMetrics(metrics);
            screenWidth = winMgr.getDefaultDisplay().getWidth();
            screenHeight = winMgr.getDefaultDisplay().getHeight();
        } catch (Exception e) { //default to a HVGA 320x480 and let's hope for the best
            Timber.e(e, e.getMessage());
            screenWidth = 0;
            screenHeight = 0;
        }

        // start cursor service
        cmdTurnCursorServiceOn();
    }

    //region OnClick methods
    @OnClick(R.id.btnRestart)
    public void onRestartClick() {
        presenter.startServerService();
    }

    @OnClick(R.id.btnSettings)
    public void onSettingsClick() {
        openSetttingsIME();
    }

    @OnClick(R.id.btnSetIME)
    public void onSetImeClick() {
        ImeUtils.showInputMethodPicker(getContext());
    }

    @OnClick(R.id.btnTerminate)
    public void onTerminateClick() {
        presenter.killServerService();

    }
    //endregion

    private void cmdTurnCursorServiceOn() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getActivity())) {
                if (getActivity() != null) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getActivity().getPackageName()));
                        startActivityForResult(intent, 1234);
                    } catch (Exception e) {
                    }
                }
            } else {
                startCursorService();
            }
        } else {
            startCursorService();
        }

    }

    private void startCursorService() {
        try {
            getActivity().startService(new Intent(getActivity(), CursorService.class));
        } catch (Exception e) {
        }
    }

    private void cmdTurnCursorServiceOff() {
        getActivity().stopService(new Intent(getActivity(), CursorService.class));
    }

    @Override
    public void networkNoAvailable() {
        if (!NetConnectionUtils.isNetworkAvailable(getContext())) {
            showMessage(R.string.network_failure_message);
        }
    }

    @Override
    public void networkAvailable() {

    }
}