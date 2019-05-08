package lv.chi.giffoid.di;

import dagger.Component;
import lv.chi.giffoid.GifPresenterTest;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ApiModule.class})
public interface TestComponent {
    // void inject(MapsPresenterTest test);

    void inject(GifPresenterTest test);
}
