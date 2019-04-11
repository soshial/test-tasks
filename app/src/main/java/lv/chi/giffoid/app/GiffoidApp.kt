package lv.chi.giffoid.app

import android.app.Application
import com.facebook.stetho.Stetho
import lv.chi.giffoid.BuildConfig
import lv.chi.giffoid.di.AppComponent
import lv.chi.giffoid.di.AppModule
import lv.chi.giffoid.di.DaggerAppComponent
import timber.log.Timber

class GiffoidApp : Application() {
    companion object {
        lateinit var instance: GiffoidApp
        lateinit var appComponent: AppComponent
    }

    init {
        instance = this@GiffoidApp
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().appModule(AppModule(applicationContext)).build()

        // Timber initialization
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // logging requests
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }
}