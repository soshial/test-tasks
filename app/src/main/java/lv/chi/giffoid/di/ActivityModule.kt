package lv.chi.giffoid.di

import dagger.Binds
import dagger.Module
import lv.chi.giffoid.data.GifRepository
import lv.chi.giffoid.data.GifRepositoryImpl
import lv.chi.giffoid.ui.mvp.gif_search.GifSearchContract
import lv.chi.giffoid.ui.mvp.gif_search.GifSearchPresenter

// TODO If a module has only abstract methods, then it can be implemented via an interface
@Module
abstract class ActivityModule {

    @Binds
    abstract fun providesGifSearchPresenter(presenter: GifSearchPresenter): GifSearchContract.Presenter

    @Binds
    abstract fun providesGifRepository(gifRepository: GifRepositoryImpl): GifRepository
}
