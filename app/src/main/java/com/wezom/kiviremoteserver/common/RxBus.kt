package com.wezom.kiviremoteserver.common

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import timber.log.Timber


object RxBus {
    private val publisher = PublishRelay.create<Any>().also { it.doOnError(Timber::e) }

    fun publish(event: Any) = publisher.accept(event)

    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}