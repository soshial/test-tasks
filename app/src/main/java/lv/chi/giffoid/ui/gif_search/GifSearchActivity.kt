package lv.chi.giffoid.ui.gif_search

import android.support.v7.widget.RecyclerView
import butterknife.BindView
import lv.chi.giffoid.R
import lv.chi.giffoid.di.ActivityComponent
import lv.chi.giffoid.model.Gif
import lv.chi.giffoid.ui.mvp.BaseMvpActivity
import javax.inject.Inject

class GifSearchActivity : BaseMvpActivity(), GifSearchContract.View {
    @Inject
    lateinit var presenter: GifSearchContract.Presenter

    override fun providePresenter() = presenter

    override fun injecting(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_gif_search
    }

    @BindView(R.id.search_results_recycler_view)
    lateinit var recyclerView: RecyclerView
    var adapter = GifAdapter(emptyList())

    override fun onStart() {
        super.onStart()
        recyclerView.adapter = adapter
        presenter.loadGifs()
    }

    override fun showGifs(gifs: List<Gif>) {
        adapter.gifs = gifs
    }

}