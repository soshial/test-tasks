package lv.chi.giffoid.ui.mvp;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

public abstract class BaseMvpFragment extends BaseFragment implements MvpView {

    public abstract MvpPresenter<MvpView> getPresenter();

    @Override
    @CallSuper
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresenter().bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unbind();
    }
}
