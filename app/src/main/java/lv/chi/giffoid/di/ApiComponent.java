package lv.chi.giffoid.di;

import dagger.Component;
import lv.chi.giffoid.app.API;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ApiModule.class})
public interface ApiComponent {
    API api();
}
