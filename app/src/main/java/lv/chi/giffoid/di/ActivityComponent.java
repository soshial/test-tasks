package lv.chi.giffoid.di;

import dagger.Component;
import lv.chi.giffoid.ui.mvp.gif_search.GifSearchActivity;

@UiScope
@Component(modules = {ActivityModule.class}, dependencies = {AppComponent.class})
public interface ActivityComponent {

    void inject(GifSearchActivity activity);

    void inject(lv.chi.giffoid.ui.mvvm.gif_search.GifSearchActivity activity);
}
