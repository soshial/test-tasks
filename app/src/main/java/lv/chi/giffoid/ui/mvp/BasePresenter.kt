package lv.chi.giffoid.ui.mvp

import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<V : MvpView> : MvpPresenter<V> {
    protected var compositeDisposable = CompositeDisposable()
    protected var view: V? = null

    @CallSuper
    override fun bind(view: V) {
        this.view = view
    }

    @CallSuper
    override fun unbind() {
        compositeDisposable.clear()
        this.view = null
    }
}
