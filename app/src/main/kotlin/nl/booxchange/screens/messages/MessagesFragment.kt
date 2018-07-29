package nl.booxchange.screens.messages

import nl.booxchange.R
import nl.booxchange.utilities.BaseFragment

class MessagesFragment: BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_message
    override val viewModel = MessagesFragmentViewModel()

}
