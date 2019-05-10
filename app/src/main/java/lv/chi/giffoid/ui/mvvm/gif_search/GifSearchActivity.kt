package lv.chi.giffoid.ui.mvvm.gif_search

import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import io.reactivex.Observable
import lv.chi.giffoid.R
import lv.chi.giffoid.app.GiffoidApp
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.ui.mvp.gif_search.GifAdapter
import lv.chi.giffoid.ui.mvp.gif_search.SearchResult
import lv.chi.giffoid.ui.mvp.gif_search.SearchStatus
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class GifSearchActivity : AppCompatActivity(), GifAdapter.GifClickListener {

    //region elements related to View
    //================================================================================
    private lateinit var adapter: GifAdapter
    private lateinit var snackbarConnection: Snackbar
    private lateinit var binding: ActivityGifSearchMvvmBinding
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gif_search_mvvm)
        binding.lifecycleOwner = this
        // TODO inject using ViewModel factory to avoid reloading all data
        // viewmodel = ViewModelProviders.of(this).get(GifViewModel::class.java)
        // viewmodel = ViewModelProviders.of(this, viewModelFactory)[GifViewModel::class.java]
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

        binding.searchResultsRecyclerView.adapter = adapter
        binding.searchResultsRecyclerView.isSaveEnabled = true // save scrolling state
        val layoutManager = binding.searchResultsRecyclerView.layoutManager as StaggeredGridLayoutManager

        binding.searchResultsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

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
        snackbarConnection = Snackbar.make(
            findViewById<View>(android.R.id.content), "", TimeUnit.SECONDS.toMillis(3).toInt()
        )
    }

    private fun refreshSearchResults(insertedPositionStart: Int) {
        if (insertedPositionStart == 0) {
            // new search
            adapter.notifyDataSetChanged()
            // on each new search results we should scroll to the beginning
            binding.searchResultsRecyclerView.scrollToPosition(0)
        } else {
            adapter.notifyItemRangeInserted(insertedPositionStart, adapter.itemCount - insertedPositionStart)
        }
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
            .setNegativeButton(getString(R.string.dialog_gif_clicked_gallery)) { _, _ -> Unit }
            .setNegativeButton(getString(R.string.dialog_gif_clicked_close)) { _, _ -> Unit }
            .show()
    }

    private fun showSearchStatus(searchStatus: SearchStatus) {
        Timber.d("LENNY $searchStatus")
        if (searchStatus == SearchStatus.START) {
            binding.searchField.text.clear()
            GlideApp.with(this).load(R.drawable.giphy_logo).into(binding.giffoidIcon)
        }
    }

    private fun showSearchResult(searchResult: SearchResult) {
        Timber.d("LENNY $searchResult")
        if (searchResult == SearchResult.NOTHING_FOUND) {
            GlideApp.with(this).load(R.drawable.not_found).into(binding.giffoidIcon)
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