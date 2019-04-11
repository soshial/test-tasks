package lv.chi.giffoid.di;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import lv.chi.giffoid.BuildConfig;
import lv.chi.giffoid.app.API;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Singleton;
import java.lang.reflect.Modifier;

@Module
public class ApiModule {

    @Provides
    @Singleton
        // Retrofit provideRetrofit(Context context, Settings settings, Gson gson) {
    Retrofit.Builder provideRetrofitBuilder(Gson gson) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        // clientBuilder.addNetworkInterceptor(new HeaderInterceptor(getApplicationName(context), settings));
        if (BuildConfig.DEBUG) {
            clientBuilder.addNetworkInterceptor(new StethoInterceptor());
            // clientBuilder.addInterceptor(new OfflineMockInterceptor(context.getAssets(), gson, 1)); // fixme crash when no internet + Rx!!!
        }
        return new Retrofit.Builder()
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()));
    }

    @Provides
    @Singleton
    API provideApi(Retrofit.Builder retrofitBuilder) {
        return retrofitBuilder.baseUrl("https://api.giphy.com/")
                .build()
                .create(API.class);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }
}
