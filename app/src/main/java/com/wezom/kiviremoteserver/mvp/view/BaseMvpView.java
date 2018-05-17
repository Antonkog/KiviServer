package com.wezom.kiviremoteserver.mvp.view;

import com.arellomobile.mvp.MvpView;


public interface BaseMvpView extends MvpView {
    void onError(Throwable e);

    void showMessage(String title, String message);

    void showMessage(String title);

    void showMessage(int title, int message);

    void showMessage(int title);

    void showToastMessage(int title);


}
