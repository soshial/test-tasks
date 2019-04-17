package lv.chi.giffoid.ui.gif_search

import android.content.res.Resources
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import lv.chi.giffoid.R
import lv.chi.giffoid.app.AppSettings
import lv.chi.giffoid.app.GlideApp
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.di.ActivityComponent
import lv.chi.giffoid.ui.mvp.BaseMvpActivity
import timber.log.Timber
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class GifSearchActivity : BaseMvpActivity(), GifSearchContract.View {
    @Inject
    lateinit var presenter: GifSearchContract.Presenter
    @Inject
    lateinit var appSettings: AppSettings

    override fun providePresenter() = presenter

    override fun injecting(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_gif_search
    }

    //region View elements
    //================================================================================
    private val scrollingState = "SCROLLING_STATE"
    @BindView(R.id.search_field)
    lateinit var searchField: EditText
    @BindView(R.id.search_results_recycler_view)
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: GifAdapter
    private val snackbarConnection = Snackbar.make(
        recyclerView, R.string.error_internet_connection, TimeUnit.SECONDS.toMillis(7).toInt()
    ).setAction(getString(R.string.snackbar_search_retry)) { presenter.loadMoreGifs() }
    //endregion
    //================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = GifAdapter(
            emptyList(),
            GlideApp.with(this),
            getScreenWidth() / resources.getInteger(R.integer.search_grid_columns)
        )
        handler.post {
            recyclerView.adapter = adapter
            val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy);
                    // load only when scrolling down
                    if (dy > 0) {
                        val lastVisibleItemId = layoutManager.findLastVisibleItemPositions(null).max() ?: 0
                        val totalItemCount = layoutManager.itemCount
                        if (!presenter.isLoading && totalItemCount <= lastVisibleItemId + appSettings.visibleThreshold) {
                            presenter.loadMoreGifs()
                        }
                    }
                }
            })
            // todo FIX
            val scrollToElement = savedInstanceState?.getInt(scrollingState) ?: 10
            Timber.d("LENNY scroll $scrollToElement")
            layoutManager.scrollToPosition(scrollToElement)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.loadGifs()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(scrollingState, 23)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun showAllGifs(gifs: List<Gif>) {
        adapter.gifs = gifs
        adapter.notifyDataSetChanged()
    }

    override fun showLoadedGifs(gifs: List<Gif>, sizeOfAdded: Int) {
        adapter.gifs = gifs
        adapter.notifyItemRangeInserted(adapter.itemCount, gifs.size)
    }

    override fun showError(error: Throwable) {
        when {
            error is UnknownHostException -> if (!snackbarConnection.isShown) snackbarConnection.show()
            error.message is String -> Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
            else -> Snackbar.make(recyclerView, getString(R.string.error_internet_unknown), 5000)
        }
    }

    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }
}