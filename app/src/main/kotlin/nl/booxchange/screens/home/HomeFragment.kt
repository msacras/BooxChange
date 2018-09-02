package nl.booxchange.screens.home

import nl.booxchange.R
import nl.booxchange.utilities.BaseFragment

class HomeFragment: BaseFragment() {
    override val contentViewResourceId = R.layout.fragment_home
    override val viewModel = HomeFragmentViewModel()
}
