package lv.chi.giffoid.ui.mvvm.gif_search

import android.content.Intent
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import lv.chi.giffoid.R
import lv.chi.giffoid.app.AppSettings
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.databinding.ActivityGifSearchMvvmBinding
import lv.chi.giffoid.ui.mvp.gif_search.GifAdapter
import lv.chi.giffoid.ui.mvp.gif_search.GifSearchContract
import retrofit2.adapter.rxjava2.HttpException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class GifSearchActivity : AppCompatActivity(), GifSearchContract.View, GifAdapter.GifClickListener {
    @Inject
    lateinit var presenter: GifSearchContract.Presenter
    @Inject
    lateinit var appSettings: AppSettings

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

    @OnClick(R.id.clear_search)
    fun onClearSearchClicked() {
        presenter.clearSearchClicked()
    }

    override fun clearSearch() {
        searchField.text.clear()
    }

    override fun provideEditTextObservable(): Observable<String> =
        RxTextView.textChangeEvents(searchField).map { it.text().trim().toString() }
    //endregion
    //================================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityGifSearchMvvmBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_gif_search_mvvm)
        ButterKnife.bind(this)
        showOrHideSearchResults(false)
//        val textView :TextView= findViewById(R.id.sample_text);
//textView.setText(binding.getUserName());

//        adapter = GifAdapter(
//            presenter.currentState.gifs,
//            GlideApp.with(this),
//            // TODO check that it works on all devices
//            getScreenWidth() / resources.getInteger(R.integer.search_grid_columns),
//            this
//        )
//
//        // TODO implement loading of downsampled/WebP images depending on metered network
//        if (ConnectivityManagerCompat.isActiveNetworkMetered((getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager))) {
//            // Checks if the device is on a metered network
////            searchField.text = "METERED"
//        }
//
//        searchResultsRecyclerView.adapter = adapter
//        searchResultsRecyclerView.isSaveEnabled = true // save scrolling state
//        val layoutManager = searchResultsRecyclerView.layoutManager as StaggeredGridLayoutManager
//
//        // TODO refactor
//        searchResultsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy);
//                // load only when scrolling down
//                if (dy > 0) {
//                    val lastVisibleItemId = layoutManager.findLastVisibleItemPositions(null).max() ?: 0
//                    val totalItemCount = layoutManager.itemCount
//                    if (!presenter.isLoading() && totalItemCount <= lastVisibleItemId + appSettings.visibleThreshold) {
//                        presenter.loadMoreGifs()
//                    }
//                }
//            }
//        })
//
//        snackbarConnection = Snackbar.make(
//            findViewById(android.R.id.content), R.string.error_internet_connection, TimeUnit.SECONDS.toMillis(7).toInt()
//        )
//            .setAction(getString(R.string.snackbar_search_retry)) { presenter.loadMoreGifs() } // TODO should work with both loaders
//        snackbarServerError = Snackbar.make(
//            findViewById(android.R.id.content), R.string.error_server_problem, TimeUnit.SECONDS.toMillis(7).toInt()
//        ).setAction(getString(R.string.snackbar_search_retry)) { presenter.loadMoreGifs() }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt(scrollingState, 23)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun showOrHideSearchResults(showResults: Boolean) {
        nothingFoundInfo.visibility = if (showResults) View.GONE else View.VISIBLE
        searchResultsRecyclerView.visibility = if (showResults) View.VISIBLE else View.GONE
    }

    /**
     * Shows results of a new search
     */
    override fun showAllGifs(gifs: List<Gif>) {
        showOrHideSearchResults(gifs.isNotEmpty())
        adapter.gifs = gifs
        adapter.notifyDataSetChanged()
        // on each new search results we should scroll to the beginning
        searchResultsRecyclerView.scrollToPosition(0)
    }

    /**
     * Shows more paginated results
     */
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
            else -> Snackbar.make(
                searchResultsRecyclerView,
                getString(R.string.error_internet_unknown),
                TimeUnit.SECONDS.toMillis(5).toInt()
            )
        }
    }

    private fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    override fun hideSearchButton(hide: Boolean) {
        clearSearchButton.visibility = if (hide) View.INVISIBLE else View.VISIBLE
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