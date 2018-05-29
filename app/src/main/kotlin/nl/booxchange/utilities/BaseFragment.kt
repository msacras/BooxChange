package nl.booxchange.utilities

import android.support.v4.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.View
import nl.booxchange.api.APIClient.RequestManager

open class BaseFragment: Fragment() {
    lateinit var activity: BaseActivity
    lateinit var requestManager: RequestManager

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = (context as? BaseActivity) ?: throw Exception("Activity must be an instance of BaseActivity")
        requestManager = RequestManager(activity)
    }
}
