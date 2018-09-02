package nl.booxchange.screens.profile

import nl.booxchange.R
import nl.booxchange.utilities.BaseFragment

class ProfileFragment: BaseFragment() {
    override val contentViewResourceId = R.layout.fragment_profile
    override val viewModel = ProfileFragmentViewModel()
}
