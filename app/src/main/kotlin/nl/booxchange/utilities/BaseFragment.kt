package nl.booxchange.utilities

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vcristian.combus.dismiss
import com.vcristian.combus.expect
import nl.booxchange.BR
import nl.booxchange.model.events.StartActivity

abstract class BaseFragment: Fragment() {
    abstract val contentViewResourceId: Int
    abstract val viewModel: BaseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, contentViewResourceId, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DataBindingUtil.getBinding<ViewDataBinding>(view)?.apply {
            executePendingBindings()
            setVariable(BR.viewModel, viewModel)
        }

/*        expect(StartActivity::class.java) { (intent, requestCode, targetFragmentClass) ->
            if (this::class.java == targetFragmentClass) {
                requestCode?.let {
                    startActivityForResult(intent, requestCode)
                } ?: run {
                    startActivity(intent)
                }
            }
        }*/

        onFragmentReady(view)
    }

    open fun onFragmentReady(view: View) {}

    override fun onDestroy() {
        dismiss()
        super.onDestroy()
    }

    open fun onBackPressed() = true
}
