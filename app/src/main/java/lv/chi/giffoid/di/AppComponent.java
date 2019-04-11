package lv.chi.giffoid.di;

import dagger.Component;
import lv.chi.giffoid.app.API;
import lv.chi.giffoid.app.SchedulerProvider;

import javax.inject.Singleton;
import java.util.Locale;

@Singleton
@Component(modules = {AppModule.class, ApiModule.class})
public interface AppComponent {
    SchedulerProvider getSchedulers();

    Locale getLocale();

    API getApi();
}
