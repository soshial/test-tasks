package lv.chi.giffoid.di;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import dagger.Module;
import dagger.Provides;
import lv.chi.giffoid.app.RxSchedulers;
import lv.chi.giffoid.app.SchedulerProvider;

import javax.inject.Singleton;
import java.util.Locale;

@Module
public class AppModule {

    private Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    private static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String appNameString = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
        return appNameString.replaceAll("\\s+", "_");
    }

    @Provides
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    SchedulerProvider provideSchedulers() {
        return new RxSchedulers();
    }

    @Provides
    Locale provideLocale() {
        // TODO switch to getLocales() after minSdkVersion >= 24
        // TODO or create app-specific locale: context.getResources().getConfiguration().locale
        return Resources.getSystem().getConfiguration().locale;
    }
}
