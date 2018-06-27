package nl.booxchange.screens

import android.os.Bundle
import android.view.View
import com.vcristian.combus.expect
import kotlinx.android.synthetic.main.fragment_chat.view.*
import nl.booxchange.R
import nl.booxchange.extension.setTintCompat
import nl.booxchange.model.ChatOpenedEvent
import nl.booxchange.utilities.BaseFragment

class MessageFragment: BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_message
    override val viewModel = MessageFragmentViewModel()

}
