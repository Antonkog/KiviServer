package com.wezom.kiviremoteserver.ui.activity;

import android.os.Bundle;
import android.view.Window;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.ui.fragments.HomeFragment;

public class HomeActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstance) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstance);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, HomeFragment.newInstance())
                .commit();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_home;
    }

    @Override
    public void injectDependency() {
        getActivityComponent().inject(this);
    }
}
