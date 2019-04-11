package lv.chi.giffoid.ui.mvp;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BasePresenter<V extends MvpView> implements MvpPresenter<V> {
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    private V view;

    @Override
    public void bind(final V view) {
        this.view = view;
    }

    @Override
    public void unbind() {
        compositeDisposable.clear();
        view = null;
    }

    public V view() {
        return view;
    }

}
