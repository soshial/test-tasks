package lv.chi.giffoid.ui.mvp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lv.chi.giffoid.app.GiffoidApp;
import lv.chi.giffoid.app.LocaleContextWrapper;
import lv.chi.giffoid.di.ActivityComponent;
import lv.chi.giffoid.di.DaggerActivityComponent;

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {
    protected Handler handler;
    private Unbinder unbinder;

    public abstract void injecting(ActivityComponent activityComponent);

    @LayoutRes
    public abstract int getContentLayout();

    @Override
    protected void attachBaseContext(final Context newBase) {
        final Locale newLocale = new Locale("EN");
        final Context context = LocaleContextWrapper.Companion.wrap(newBase, newLocale);
        super.attachBaseContext(context);
    }

    @Override
    @CallSuper
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // preparing component for injection
        final ActivityComponent activityComponent = DaggerActivityComponent.builder()
                .appComponent(GiffoidApp.appComponent)
                .build();
        // we cannot inject in parent class, hence we demand {@link #injecting() and providePresenter()} to be overridden
        injecting(activityComponent);

        handler = new Handler();
        setContentView(getContentLayout());
        unbinder = ButterKnife.bind(this);
    }

    /**
     * <ul>
     * <li>Registration of View click listeners</li>
     * <li>Subscription to observables (general observables, not necessarily Rx)</li>
     * <li>Reflect the current state into UI (UI update)</li>
     * <li>Functional flows</li>
     * <li>Initialization of asynchronous functional flows</li>
     * <li>Resources allocations</li>
     * </ul>
     */
    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
    }

    /**
     * <ul>
     * <li>unregister all observers, BroadcastReceivers and listeners</li>
     * <li>release all resources that were allocated in onStart()</li>
     * </ul>
     */
    @Override
    @CallSuper
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    /**
     * onResume() should only be used to start or resume some moving stuff on the screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * In this method you should pause or stop on-screen animations and videos that you resumed or
     * started in onResume(). Just like with onResume(), you hardly ever need to override onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    public static void hideKeyboard(final Activity activity) {
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(final Activity activity) {
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
}
