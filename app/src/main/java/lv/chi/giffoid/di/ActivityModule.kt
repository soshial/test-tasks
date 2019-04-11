package lv.chi.giffoid.di

import dagger.Binds
import dagger.Module
import lv.chi.giffoid.ui.gif_search.GifSearchContract
import lv.chi.giffoid.ui.gif_search.GifSearchPresenter

// Also note that if a module has only abstract methods, then it can be implemented via an interface
@Module
abstract class ActivityModule {

    @Binds
    abstract fun providesGifSearchPresenter(presenter: GifSearchPresenter): GifSearchContract.Presenter

}
