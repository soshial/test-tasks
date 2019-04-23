package lv.chi.giffoid.ui.mvvm.gif_search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class GifViewModel : ViewModel() {
    var searchButtonVisibility: Boolean = true
    var searchResultsVisibility: Boolean = false
    val searchString: MutableLiveData<String> = MutableLiveData()

    fun onClearSearch() {
        searchString.value = ""
    }

    fun update(result: String) {
        searchString.value = result
    }
}

