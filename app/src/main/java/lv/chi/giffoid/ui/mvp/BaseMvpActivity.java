package lv.chi.giffoid.ui.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;

public abstract class BaseMvpActivity extends BaseActivity implements MvpView {
    public abstract MvpPresenter providePresenter();

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        providePresenter().bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        providePresenter().unbind();
    }
}
