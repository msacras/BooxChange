package nl.booxchange.utilities.databinding

import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.ViewPager
import nl.booxchange.model.entities.ImageModel
import nl.booxchange.utilities.PhotosPagerAdapter

@BindingAdapter("photoHandler")
fun ViewPager.setupPhotoViewPagerItems(handler: Any?) {
    adapter = PhotosPagerAdapter(handler)
}

@BindingAdapter("photoItems")
fun ViewPager.updatePhotoViewPagerItems(itemsList: List<ImageModel>?) {
    (adapter as? PhotosPagerAdapter)?.swapItems(itemsList ?: emptyList())
}
