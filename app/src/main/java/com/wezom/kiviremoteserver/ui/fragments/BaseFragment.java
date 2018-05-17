package com.wezom.kiviremoteserver.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.wezom.kiviremoteserver.di.components.ActivityComponent;
import com.wezom.kiviremoteserver.di.components.FragmentComponent;
import com.wezom.kiviremoteserver.di.modules.FragmentModule;
import com.wezom.kiviremoteserver.mvp.view.BaseMvpView;
import com.wezom.kiviremoteserver.ui.activity.BaseActivity;
import com.wezom.kiviremoteserver.ui.activity.HomeActivity;


import butterknife.ButterKnife;


public abstract class BaseFragment extends MvpAppCompatFragment implements BaseMvpView {

    Toolbar toolbar;
    private FragmentComponent fragmentComponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentComponent = getActivityComponent()
                .providesFragmentComponent(new FragmentModule(this));
        injectDependencies();
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(getLayoutRes(), container, false);
        injectViews(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = getToolbar();
        if (toolbar != null) {
            setToolbarTitle("");
        }
    }

    protected abstract int getLayoutRes();

    public abstract void injectDependencies();

    public void injectViews(View view) {
        ButterKnife.bind(this, view);
    }


    private ActivityComponent getActivityComponent() {
        return ((BaseActivity) getActivity()).getActivityComponent();
    }

    public FragmentComponent getFragmentComponent() {
        return fragmentComponent;
    }

    public void showToastMessage(int title) {
        ((BaseActivity) getActivity()).showToastMessage(title);
    }

    @Override
    public void onError(Throwable throwable) {
        ((BaseMvpView) getActivity()).onError(throwable);
    }

    @Override
    public void showMessage(String title, String message) {
        ((HomeActivity) getActivity()).showMessage(title, message);
    }

    @Override
    public void showMessage(String title) {
        ((HomeActivity) getActivity()).showMessage(title);
    }

    @Override
    public void showMessage(int title, int message) {
        ((HomeActivity) getActivity()).showMessage(title, message);
    }

    @Override
    public void showMessage(int title) {
        ((HomeActivity) getActivity()).showMessage(title);
    }


    protected void setToolbarTitle(String toolbarTitle) {
        if (toolbar != null) {
            toolbar.setTitle(toolbarTitle);
            toolbar.setSubtitle("");
        }
    }

    protected abstract Toolbar getToolbar();
}
