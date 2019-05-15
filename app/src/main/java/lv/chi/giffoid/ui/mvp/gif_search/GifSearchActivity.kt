package lv.chi.giffoid.ui.mvp.gif_search

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
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import lv.chi.giffoid.R
import lv.chi.giffoid.api.GlideApp
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.di.ActivityComponent
import lv.chi.giffoid.ui.mvp.base.BaseMvpActivity
import retrofit2.HttpException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class GifSearchActivity : BaseMvpActivity(), GifSearchContract.View, GifAdapter.GifClickListener {
    @Inject
    lateinit var presenter: GifSearchContract.Presenter

    override fun providePresenter() = presenter

    override fun injecting(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_gif_search_mvp
    }

    //region View elements
    //================================================================================
    @BindView(R.id.clear_search)
    lateinit var clearSearchButton: ImageView
    @BindView(R.id.giffoid_icon)
    lateinit var giffoidIcon: ImageView
    @BindView(R.id.search_field)
    lateinit var searchField: EditText
    @BindView(R.id.search_results_explanation)
    lateinit var resultsExplTv: TextView
    @BindView(R.id.search_results_recycler_view)
    lateinit var searchResultsRecyclerView: RecyclerView
    @BindView(R.id.progress_bar)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.search_results_info)
    lateinit var searchResultsInfo: ConstraintLayout
    @BindView(R.id.results_count)
    lateinit var resultsCountTv: TextView
    private lateinit var adapter: GifAdapter
    private lateinit var snackbarConnection: Snackbar

    @OnClick(R.id.clear_search)
    fun onClearSearchClicked() {
        presenter.clearSearchClicked()
    }

    override fun provideEditTextObservable(): Observable<String> =
        RxTextView.textChangeEvents(searchField).map { it.text().toString() }
    //endregion
    //================================================================================

    // TODO add SearchView to AppBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupGifRecyclerView()
        showSearchStatus(SearchStatus.START)

        snackbarConnection = Snackbar.make(
            findViewById(android.R.id.content), R.string.error_internet_connection, TimeUnit.SECONDS.toMillis(3).toInt()
        )
    }

    private fun setupGifRecyclerView() {
        adapter = GifAdapter(
            presenter.currentState.gifs,
            GlideApp.with(this),
            getScreenWidth() / resources.getInteger(R.integer.search_grid_columns),
            this
        )

        if (ConnectivityManagerCompat.isActiveNetworkMetered((getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager))) {
            // TODO implement loading of downsampled/WebP images depending if on metered network
        }

        searchResultsRecyclerView.adapter = adapter
        searchResultsRecyclerView.isSaveEnabled = true // save scrolling state
        val layoutManager = searchResultsRecyclerView.layoutManager as StaggeredGridLayoutManager

        searchResultsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy);
                // load only when scrolling down
                if (dy > 0) {
                    val lastVisibleItemId = layoutManager.findLastVisibleItemPositions(null).max() ?: 0
                    val totalItemCount = layoutManager.itemCount
                    presenter.loadMoreGifs(totalItemCount, lastVisibleItemId)
                }
            }
        })
    }

    override fun refreshSearchResults(insertedPositionStart: Int, insertedItemCount: Int, resultsCount: Int) {
        if (insertedPositionStart == 0) {
            // new search
            adapter.notifyDataSetChanged()
            // on each new search results we should scroll to the beginning
            searchResultsRecyclerView.scrollToPosition(0)
        } else {
            adapter.notifyItemRangeInserted(insertedPositionStart, insertedItemCount)
        }
        resultsCountTv.text = getString(R.string.ac_gif_search_number_results, resultsCount)
    }

    override fun showError(error: Throwable?) {
        if (!snackbarConnection.isShown) snackbarConnection.show()
        val errorMessage: String? = error?.message
        snackbarConnection.setText(
            when {
                error == null -> getString(R.string.error_internet_unknown)
                error is UnknownHostException -> getString(R.string.error_internet_connection)
                error is HttpException -> getString(R.string.error_server_problem)
                errorMessage is String -> errorMessage
                else -> getString(R.string.error_internet_unknown)
            }
        )
    }

    private fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    override fun onGifClicked(gif: Gif) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_gif_clicked_title))
            .setMessage(getString(R.string.dialog_gif_clicked_descr))
            .setPositiveButton(getString(R.string.dialog_gif_clicked_open_browser)) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(gif.url)))
            }
            .setNegativeButton(getString(R.string.dialog_gif_clicked_gallery)) { _, _ -> Unit /* TODO implement add to Gboard */ }
            .setNegativeButton(getString(R.string.dialog_gif_clicked_close)) { _, _ -> Unit }
            .show()
    }


    override fun showSearchStatus(searchStatus: SearchStatus) {
        when (searchStatus) {
            SearchStatus.START -> {
                searchField.text.clear()
                progressBar.visibility = View.GONE
                clearSearchButton.visibility = View.GONE
                resultsCountTv.visibility = View.GONE
                searchResultsInfo.visibility = View.VISIBLE
                resultsExplTv.setText(R.string.ac_gif_search_start_description)
                searchResultsRecyclerView.visibility = View.GONE
                GlideApp.with(this).load(R.drawable.giphy_logo).into(giffoidIcon)
            }
            SearchStatus.REQUESTING_DATA -> {
                progressBar.visibility = View.VISIBLE
                clearSearchButton.visibility = View.GONE
                resultsCountTv.visibility = View.GONE
                // clearSearchButton.isEnabled = false
                searchResultsInfo.visibility = View.GONE
            }
            SearchStatus.FINISHED -> {
                progressBar.visibility = View.GONE
                clearSearchButton.visibility = View.VISIBLE
                resultsCountTv.visibility = View.VISIBLE
                clearSearchButton.isEnabled = true
                // depends on result
            }
        }
    }

    override fun showSearchResult(searchResult: SearchResult) {
        when (searchResult) {
            SearchResult.NOTHING_FOUND -> {
                searchResultsInfo.visibility = View.VISIBLE
                resultsExplTv.setText(R.string.ac_gif_search_nothing_found_description)
                searchResultsRecyclerView.visibility = View.GONE
                GlideApp.with(this).load(R.drawable.not_found).into(giffoidIcon)
            }
            SearchResult.LOADED -> {
                searchResultsInfo.visibility = View.GONE
                searchResultsRecyclerView.visibility = View.VISIBLE
            }
            SearchResult.LOADED_EOF -> {
                searchResultsInfo.visibility = View.GONE
                searchResultsRecyclerView.visibility = View.VISIBLE
                // TODO show UI indicator, that the list has ended
            }
            SearchResult.ERROR -> {
                searchResultsInfo.visibility = View.GONE
                searchResultsRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}