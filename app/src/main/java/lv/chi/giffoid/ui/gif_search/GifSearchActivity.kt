package lv.chi.giffoid.ui.gif_search

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import lv.chi.giffoid.R
import lv.chi.giffoid.di.ActivityComponent
import lv.chi.giffoid.model.Gif
import lv.chi.giffoid.ui.mvp.BaseMvpActivity
import lv.chi.giffoid.ui.mvp.MvpPresenter
import lv.chi.giffoid.ui.mvp.MvpView
import javax.inject.Inject

class GifSearchActivity : BaseMvpActivity(), GifSearchContract.View {
    @Inject
    lateinit var presenter: GifSearchContract.Presenter
    @BindView(R.id.search_results_grid_view)
    lateinit var grid: RecyclerView
    var adapter = GifAdapter(emptyList())

    override fun injecting(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_gif_search
    }

    override fun getPresenter(): MvpPresenter<MvpView> = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        grid.layoutManager = GridLayoutManager(this, 3)
        grid.adapter = adapter
    }

    override fun showGifs(gifs: List<Gif>) {
        adapter.gifs = gifs
    }

}