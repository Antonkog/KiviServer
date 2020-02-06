package com.wezom.kiviremoteserver.mvp.presenter;

import androidx.annotation.NonNull;

import com.arellomobile.mvp.MvpPresenter;
import com.arellomobile.mvp.MvpView;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


abstract class BasePresenter<View extends MvpView> extends MvpPresenter<View> {


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected void unsubscribeOnDestroy(@NonNull Disposable subscription) {
        compositeDisposable.add(subscription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
