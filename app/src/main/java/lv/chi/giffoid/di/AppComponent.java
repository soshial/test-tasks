package lv.chi.giffoid.di;

import dagger.Component;
import lv.chi.giffoid.api.API;
import lv.chi.giffoid.app.AppSettings;
import lv.chi.giffoid.app.SchedulerProvider;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class, ApiModule.class})
public interface AppComponent {
    SchedulerProvider getSchedulers();

    AppSettings getAppSettings();

//    Locale getLocale();

    API getApi();
}
