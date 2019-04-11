package lv.chi.giffoid.ui.mvp;

public interface MvpPresenter<T extends MvpView> {
    void bind(T view);

    void unbind();
}
