package nl.booxchange.utilities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_book_details.*

abstract class BaseActivity: AppCompatActivity() {
    protected abstract val viewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.isLoading.observe(::getLifecycle) { isLoading ->
            if (isLoading) loading_view?.show() else loading_view?.hide()
        }
    }
}
