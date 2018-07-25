package nl.booxchange.utilities

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vcristian.combus.dismiss
import com.vcristian.combus.expect
import nl.booxchange.BR
import nl.booxchange.R
import nl.booxchange.model.StartActivity
import nl.booxchange.widget.CustomRefreshLayout
import org.jetbrains.anko.findOptional

abstract class BaseFragment: Fragment() {
    abstract val contentViewResourceId: Int
    abstract val viewModel: BaseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, contentViewResourceId, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DataBindingUtil.getBinding<ViewDataBinding>(view)?.apply {
            setVariable(BR.viewModel, viewModel)
        }

        val swipeRefreshLayout = view.findOptional<CustomRefreshLayout>(R.id.swipe_refresh_layout)

        swipeRefreshLayout?.setOnDownRefreshListener { viewModel.onRefresh() }
        viewModel.isLoading.observe(::getLifecycle) {
            swipeRefreshLayout?.isRefreshing = it ?: false
        }

        expect(StartActivity::class.java) { (intent, requestCode, targetFragmentClass) ->
            if (this::class.java == targetFragmentClass) {
                requestCode?.let {
                    startActivityForResult(intent, requestCode)
                } ?: run {
                    startActivity(intent)
                }
            }
        }

        onFragmentReady()
    }

    open fun onFragmentReady() {

    }

    override fun onDestroy() {
        dismiss()
        super.onDestroy()
    }

    open fun onBackPressed(): Boolean { return true }
}
