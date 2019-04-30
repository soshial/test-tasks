package lv.chi.giffoid.ui.mvvm.gif_search

import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Observer
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
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import io.reactivex.Observable
import lv.chi.giffoid.R
import lv.chi.giffoid.api.GlideApp
import lv.chi.giffoid.app.GiffoidApp
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.databinding.ActivityGifSearchMvvmBinding
import lv.chi.giffoid.di.DaggerActivityComponent
import lv.chi.giffoid.ui.mvp.gif_search.GifAdapter
import lv.chi.giffoid.ui.mvp.gif_search.SearchResult
import lv.chi.giffoid.ui.mvp.gif_search.SearchStatus
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class GifSearchActivity : AppCompatActivity(), GifAdapter.GifClickListener {

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
    @BindView(R.id.search_results_info)
    lateinit var searchResultsInfo: ConstraintLayout
    private lateinit var adapter: GifAdapter
    private lateinit var snackbarConnection: Snackbar

    //endregion
    //================================================================================
    @Inject
    lateinit var viewmodel: GifViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerActivityComponent.builder()
            .appComponent(GiffoidApp.appComponent)
            .build()
            .inject(this)
        val binding: ActivityGifSearchMvvmBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_gif_search_mvvm)
        ButterKnife.bind(this)
        binding.lifecycleOwner = this
        binding.viewmodel = viewmodel

        viewmodel.currentState.searchStatus.observe(this, Observer { searchStatus -> showSearchStatus(searchStatus!!) })
        viewmodel.currentState.searchResult.observe(this, Observer { searchResult -> showSearchResult(searchResult!!) })
        viewmodel.currentState.errorResult.observe(this, Observer { showError(it) })
        viewmodel.init(
            Observable.fromPublisher(
                LiveDataReactiveStreams.toPublisher(
                    this,
                    viewmodel.currentState.searchQuery
                )
            )
        )
        adapter = GifAdapter(
            viewmodel.currentState.gifs,
            GlideApp.with(this),
            getScreenWidth() / resources.getInteger(R.integer.search_grid_columns),
            this
        )
        viewmodel.currentState.gifsUpdatedIndex.observe(this) { index -> refreshSearchResults(index) }

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
                    viewmodel.loadMoreGifs(totalItemCount, lastVisibleItemId)
                }
            }
        })
        val view = findViewById<View>(android.R.id.content)
        val parent = view.parent
        val nested = view.hasNestedScrollingParent()
        snackbarConnection = Snackbar.make(
            view, "", TimeUnit.SECONDS.toMillis(3).toInt()
        )
    }

    private fun refreshSearchResults(insertedPositionStart: Int) {
        if (insertedPositionStart == 0) {
            // new search
            adapter.notifyDataSetChanged()
            // on each new search results we should scroll to the beginning
            searchResultsRecyclerView.scrollToPosition(0)
        } else {
            adapter.notifyItemRangeInserted(insertedPositionStart, adapter.itemCount - insertedPositionStart)
        }
    }

    /**
     * TODO check that it works on all devices
     */
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
            .setNegativeButton(getString(R.string.dialog_gif_clicked_gallery)) { _, _ -> Unit }
            .setNegativeButton(getString(R.string.dialog_gif_clicked_close)) { _, _ -> Unit }
            .show()
    }

    private fun showSearchStatus(searchStatus: SearchStatus) {
        Timber.d("LENNY $searchStatus")
        if (searchStatus == SearchStatus.START) {
            searchField.text.clear()
            GlideApp.with(this).load(R.drawable.giphy_logo).into(giffoidIcon)
        }
    }

    private fun showSearchResult(searchResult: SearchResult) {
        Timber.d("LENNY $searchResult")
        if (searchResult == SearchResult.NOTHING_FOUND) {
            GlideApp.with(this).load(R.drawable.not_found).into(giffoidIcon)
        }
    }

    private fun showError(error: Throwable?) {
        if (!snackbarConnection.isShown) snackbarConnection.show()
        val errorMessage: String? = error?.message
        snackbarConnection.setText(
            when {
                error == null -> "WARNING: null"
                error is UnknownHostException -> getString(R.string.error_internet_connection)
                error is HttpException -> getString(R.string.error_server_problem)
                errorMessage is String -> errorMessage
                else -> getString(R.string.error_internet_unknown)
            }
        )
    }
}