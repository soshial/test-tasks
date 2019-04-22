package lv.chi.giffoid.ui.gif_search

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.net.ConnectivityManagerCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import butterknife.OnTextChanged
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import lv.chi.giffoid.R
import lv.chi.giffoid.app.AppSettings
import lv.chi.giffoid.app.GlideApp
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.di.ActivityComponent
import lv.chi.giffoid.ui.mvp.BaseMvpActivity
import retrofit2.adapter.rxjava2.HttpException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class GifSearchActivity : BaseMvpActivity(), GifSearchContract.View, GifAdapter.GifClickListener {
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
    @BindView(R.id.clear_search)
    lateinit var clearSearchButton: ImageView
    @BindView(R.id.search_field)
    lateinit var searchField: EditText
    @BindView(R.id.search_results_recycler_view)
    lateinit var searchResultsRecyclerView: RecyclerView
    @BindView(R.id.nothing_found)
    lateinit var nothingFoundInfo: ConstraintLayout
    private lateinit var adapter: GifAdapter
    private lateinit var snackbarConnection: Snackbar
    private lateinit var snackbarServerError: Snackbar
    @OnTextChanged(R.id.search_field)
    fun searchTextEntered(text: CharSequence) {
        presenter.loadGifs(text.trim().toString())
    }
    //endregion
    //================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOrHideSearchResults(false)
        adapter = GifAdapter(
            presenter.currentState.gifs,
            GlideApp.with(this),
            getScreenWidth() / resources.getInteger(R.integer.search_grid_columns),
            this
        )
        val g: Observable<CharSequence> = RxTextView.textChanges(searchField)
        val k: Observable<Any> = RxView.clicks(clearSearchButton)

        // TODO implement loading of downsampled/WebP images depending on metered network
        if (ConnectivityManagerCompat.isActiveNetworkMetered((getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager))) {
            // Checks if the device is on a metered network
//            searchField.text = "METERED"
        }

        searchResultsRecyclerView.adapter = adapter
        val layoutManager = searchResultsRecyclerView.layoutManager as StaggeredGridLayoutManager

        searchResultsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy);
                // load only when scrolling down
                if (dy > 0) {
                    val lastVisibleItemId = layoutManager.findLastVisibleItemPositions(null).max() ?: 0
                    val totalItemCount = layoutManager.itemCount
                    if (!presenter.currentState.isLoading && totalItemCount <= lastVisibleItemId + appSettings.visibleThreshold) {
                        presenter.loadMoreGifs()
                    }
                }
            }
        })
//        TODO implement restoring scrolling state
//        val scrollToElement = savedInstanceState?.getInt(scrollingState) ?: 10
//        Timber.d("LENNY scroll $scrollToElement")
//        layoutManager.scrollToPosition(scrollToElement)

        snackbarConnection = Snackbar.make(
            searchResultsRecyclerView, R.string.error_internet_connection, TimeUnit.SECONDS.toMillis(7).toInt()
        ).setAction(getString(R.string.snackbar_search_retry)) { presenter.loadMoreGifs() }
        snackbarServerError = Snackbar.make(
            searchResultsRecyclerView, R.string.error_server_problem, TimeUnit.SECONDS.toMillis(7).toInt()
        ).setAction(getString(R.string.snackbar_search_retry)) { presenter.loadMoreGifs() }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(scrollingState, 23)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun showOrHideSearchResults(showResults: Boolean) {
        nothingFoundInfo.visibility = if (showResults) View.GONE else View.VISIBLE
        searchResultsRecyclerView.visibility = if (showResults) View.VISIBLE else View.GONE
    }

    override fun showAllGifs(gifs: List<Gif>) {
        showOrHideSearchResults(true)
        adapter.gifs = gifs
        adapter.notifyDataSetChanged()
    }

    override fun showLoadedGifs(gifs: List<Gif>, sizeOfAdded: Int) {
        showOrHideSearchResults(true)
        adapter.gifs = gifs
        adapter.notifyItemRangeInserted(adapter.itemCount, gifs.size)
    }

    override fun showError(error: Throwable) {
        when {
            error is UnknownHostException -> if (!snackbarConnection.isShown) snackbarConnection.show()
            error is HttpException -> if (!snackbarServerError.isShown) snackbarServerError.show()
            error.message is String -> Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
            else -> Snackbar.make(searchResultsRecyclerView, getString(R.string.error_internet_unknown), 5000)
        }
    }

    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    override fun onGifClicked(gif: Gif) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_gif_clicked_title))
            .setMessage(getString(R.string.dialog_gif_clicked_descr))
            .setPositiveButton(getString(R.string.dialog_gif_clicked_open_browser)) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(gif.url)))
            }
            .setNegativeButton(getString(R.string.dialog_gif_clicked_gallery)) { _, _ -> Unit }
            .setNegativeButton(getString(R.string.dialog_gif_clicked_close)) { _, _ -> Unit }
            .show()
    }
}