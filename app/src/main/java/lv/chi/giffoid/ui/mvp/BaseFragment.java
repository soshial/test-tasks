package lv.chi.giffoid.ui.mvp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lv.chi.giffoid.app.GiffoidApp;
import lv.chi.giffoid.di.ActivityComponent;
import lv.chi.giffoid.di.DaggerActivityComponent;

public abstract class BaseFragment extends Fragment {
    protected Context context;
    protected Handler handler;
    private Unbinder unbinder;

    @LayoutRes
    public abstract int getContentLayout();

    public abstract void injecting(ActivityComponent activityComponent);

    @Override
    @CallSuper
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // preparing component for injection
        final ActivityComponent activityComponent = DaggerActivityComponent.builder()
                .appComponent(GiffoidApp.appComponent)
                .build();
        // we cannot inject in parent class, hence we demand {@link #injecting() and providePresenter()} to be overridden
        injecting(activityComponent);
        handler = new Handler();
    }

    /**
     * Don't forget to call super.onCreateView() in child, otherwise view() will be null
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    @CallSuper
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = getView();
        if (view == null) {
            view = inflater.inflate(getContentLayout(), container, false);
        }
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    @CallSuper
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }
}
