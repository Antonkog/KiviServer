package com.wezom.kiviremoteserver.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.di.components.ActivityComponent;
import com.wezom.kiviremoteserver.di.components.ApplicationComponent;
import com.wezom.kiviremoteserver.di.modules.ActivityModule;
import com.wezom.kiviremoteserver.mvp.view.BaseMvpView;
import com.wezom.kiviremoteserver.ui.dialogs.MessageDialog;

import butterknife.ButterKnife;


public abstract class BaseActivity extends MvpAppCompatActivity implements BaseMvpView {

    private ActivityComponent activityComponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activityComponent = getApplicationComponent().providesActivityComponent(new ActivityModule(this));
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
        setContentView(getLayoutRes());
        ButterKnife.bind(this);
    }

    ApplicationComponent getApplicationComponent() {
        return App.getApplicationComponent();
    }

    public abstract int getLayoutRes();

    public abstract void injectDependency();

    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public void onError(Throwable throwable) {
        MessageDialog dialog = new MessageDialog();
        dialog.setCancelable(true);
        dialog.setTitle(getString(R.string.error));
        dialog.show(getSupportFragmentManager(), "message_dialog");
    }

    @Override
    public void showMessage(String title, String message) {
        MessageDialog dialog = new MessageDialog();
        dialog.setCancelable(true);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show(getSupportFragmentManager(), "message_dialog");
    }

    @Override
    public void showMessage(String title) {
        MessageDialog dialog = new MessageDialog();
        dialog.setCancelable(true);
        dialog.setTitle(title);
        dialog.setMessage("");
        dialog.show(getSupportFragmentManager(), "message_dialog");
    }

    @Override
    public void showMessage(int title, int message) {
        showMessage(getString(title), getString(message));
    }

    @Override
    public void showMessage(int title) {
        showMessage(getString(title));
    }

    @Override
    public void showToastMessage(int title) {
        Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();
    }
}
