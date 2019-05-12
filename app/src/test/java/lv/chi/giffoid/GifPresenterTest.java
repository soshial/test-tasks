package lv.chi.giffoid;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.schedulers.Schedulers;
import lv.chi.giffoid.api.GiphyResponse;
import lv.chi.giffoid.api.PaginationInfo;
import lv.chi.giffoid.app.AppSettings;
import lv.chi.giffoid.app.RxSchedulers;
import lv.chi.giffoid.data.Gif;
import lv.chi.giffoid.data.GifRepository;
import lv.chi.giffoid.data.ImageTypes;
import lv.chi.giffoid.data.UrlsAndSizes;
import lv.chi.giffoid.di.DaggerTestComponent;
import lv.chi.giffoid.ui.mvp.gif_search.GifSearchContract;
import lv.chi.giffoid.ui.mvp.gif_search.GifSearchPresenter;
import lv.chi.giffoid.ui.mvp.gif_search.SearchResult;
import lv.chi.giffoid.ui.mvp.gif_search.SearchStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class GifPresenterTest {
    @Rule
    public RxTrampolineSchedulerRule rule;
    @Mock
    GifRepository gifRepository;
    @Mock
    GifSearchContract.View view;
    private AppSettings appSettings = new AppSettings(
            2, 50, 5, 500L,
            3, "sB9wP0usZzneQq3rkYcvt25dvz1bPXIG");
    private GifSearchPresenter presenter;

    //region Data generation methods
    private UrlsAndSizes uas = new UrlsAndSizes("https://media0.giphy.com/media/MWWg5GMVCyy5i/200w.gif", "https://media0.giphy.com/media/MWWg5GMVCyy5i/200w.webp", 200, 106);
    private ImageTypes imageTypes = new ImageTypes(uas, uas, uas, uas);

    private static void testPrint(CharSequence stringToPrint) {
        System.out.print(stringToPrint);
    }

    @Before
    public void init() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
        DaggerTestComponent.create().inject(this);
        presenter = new GifSearchPresenter(gifRepository, appSettings, new RxSchedulers());
    }

    private Gif createRandomGif() {
        String randomId = UUID.randomUUID().toString().replaceAll("-", "");
        return new Gif("gif", randomId, "Some title", "https://t.tt/" + randomId, imageTypes);
    }

    private List<Gif> createRandomGifList(int gifCount) {
        Gif[] gifTestArray = new Gif[gifCount];
        for (int i = 0; i < gifCount; i++) {
            gifTestArray[i] = createRandomGif();
        }
        return Arrays.asList(gifTestArray);
    }

    private GiphyResponse<Gif> createGiphyResponse(int gifCount, int offset) {
        return new GiphyResponse<>(createRandomGifList(gifCount), new PaginationInfo(1000, gifCount, offset));
    }

    private GiphyResponse<Gif> createGiphyResponse(List<Gif> testCollection, int offset, int totalCount) {
        return new GiphyResponse<>(testCollection, new PaginationInfo(totalCount, testCollection.size(), offset));
    }

    private void prepareViewAndRepo(Observable<String> enteredSearches, List<Gif> testCollection, int offset, int totalCount) {
        Mockito.when(view.provideEditTextObservable()).thenReturn(enteredSearches);
        Mockito.when(gifRepository.loadGifs(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Single.just(createGiphyResponse(testCollection, offset, totalCount)));
    }

    private void prepareViewAndRepo(Observable<String> enteredSearches, List<Gif> testCollection) {
        prepareViewAndRepo(enteredSearches, testCollection, 0, 100000);
    }

    private long sinceStartMs(long start) {
        return (System.nanoTime() - start) / 1000000;
    }
    //endregion

    @Test
    public void current_state_SHOULD_always_have_start_status_after_initialization() {
        Assert.assertEquals(presenter.getCurrentState().getSearchStatus(), SearchStatus.START); // we created our presenter
        Mockito.when(view.provideEditTextObservable()).thenReturn(Observable.empty());
        presenter.bind(view);
        Assert.assertEquals(presenter.getCurrentState().getSearchStatus(), SearchStatus.START); // status is still START, because we user didn't enter anything
    }

    @Test
    public void current_state_SHOULD_hold_received_data() {
        Observable<String> testData = Observable.just("more people");
        List<Gif> testCollection = createRandomGifList(7);
        prepareViewAndRepo(testData, testCollection);

        presenter.bind(view);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            testPrint("INTERRUPTED");
        }
        Assert.assertArrayEquals(presenter.getCurrentState().getGifs().toArray(), testCollection.toArray()); // we received needed data
        Assert.assertEquals(presenter.getCurrentState().getSearchResult(), SearchResult.LOADED_EOF); // we received needed data
        Assert.assertEquals(presenter.getCurrentState().getSearchStatus(), SearchStatus.FINISHED); // we received needed data
    }

    @Test
    public void nothing_found_result_SHOULD_have_corresponding_state() {
        Observable<String> testData = Observable.just("LIU AJHLSJH LJSHGLHSJGFLHJS");
        List<Gif> testCollection = Collections.emptyList();
        prepareViewAndRepo(testData, testCollection, 0, 0);

        presenter.bind(view);
        Assert.assertEquals(presenter.getCurrentState().getSearchStatus(), SearchStatus.FINISHED); // we received needed data
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            testPrint("INTERRUPTED");
        }
        Assert.assertTrue(presenter.getCurrentState().getGifs().isEmpty());
        Assert.assertEquals(presenter.getCurrentState().getSearchResult(), SearchResult.NOTHING_FOUND);
        Assert.assertEquals(presenter.getCurrentState().getSearchStatus(), SearchStatus.FINISHED);
    }

    @Test
    public void clear_search_SHOULD_invoke_status_start() {
        Observable<String> testData = Observable.just("search entry");
        List<Gif> testCollection = createRandomGifList(7);
        prepareViewAndRepo(testData, testCollection);

        presenter.bind(view);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            testPrint("INTERRUPTED");
        }
        Assert.assertArrayEquals(presenter.getCurrentState().getGifs().toArray(), testCollection.toArray()); // we received needed data

        presenter.clearSearchClicked();
        Assert.assertTrue(presenter.getCurrentState().getGifs().isEmpty());
        Assert.assertEquals(presenter.getCurrentState().getSearchStatus(), SearchStatus.START);
    }

    @Test
    public void too_fast_input_data_SHOULD_return_only_latter() {
        long start = System.nanoTime();
        Observable<String> testData = Observable.just("search1", "search2", "search3", "search4", "search5")
                .concatMap(str -> Observable.just(str).delay(100, TimeUnit.MILLISECONDS)
                        .doOnNext(str2 -> testPrint(str2 + ": " + sinceStartMs(start) + "\n")));
        List<Gif> testCollection = createRandomGifList(13);
        prepareViewAndRepo(testData, testCollection);

        presenter.bind(view);
        try {
            TimeUnit.MILLISECONDS.sleep(appSettings.getKeyboardDebounceMs() + 3000L);
        } catch (InterruptedException e) {
            testPrint("INTERRUPTED");
        }
        Assert.assertArrayEquals(presenter.getCurrentState().getGifs().toArray(), testCollection.toArray());
        Assert.assertEquals(presenter.getCurrentState().getSearchStatus(), SearchStatus.FINISHED);
        Assert.assertEquals("search5", presenter.getCurrentState().getSearchQuery());
        Mockito.verify(view, Mockito.atMost(1)).refreshSearchResults(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void loading_data_SHOULD_have_corresponding_status() {
        Observable<String> testData = Observable.just("search entry");
        List<Gif> testCollection = Arrays.asList(createRandomGif(), createRandomGif(), createRandomGif(), createRandomGif());

        Mockito.when(view.provideEditTextObservable()).thenReturn(testData);
        Mockito.when(gifRepository.loadGifs("search entry", appSettings.getApiKey(), appSettings.getSearchBatchLimit(), 0))
                .thenReturn(
                        Single.just(new GiphyResponse<>(testCollection, new PaginationInfo(testCollection.size(), testCollection.size(), 0)))
                                .delay(3, TimeUnit.SECONDS)
                );
        presenter.bind(view);
        Assert.assertTrue(presenter.getCurrentState().getGifs().isEmpty()); // we haven't received data yet
        Assert.assertEquals(presenter.getCurrentState().getSearchStatus(), SearchStatus.REQUESTING_DATA);
    }
}
