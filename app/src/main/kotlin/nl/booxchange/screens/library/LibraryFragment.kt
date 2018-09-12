package nl.booxchange.screens.library

import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_library.*
import kotlinx.android.synthetic.main.fragment_library.view.*
import nl.booxchange.R
import nl.booxchange.utilities.BaseFragment
import org.jetbrains.anko.dip

class LibraryFragment : BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_library
    override val viewModel = LibraryFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser?.photoUrl

        //userName.text = user?.displayName

//        userName.text = FirebaseAuth.getInstance().currentUser?.displayName



        Glide.with(this).load(user).into(profile_image)

        view.user_books_list.addItemDecoration(object : RecyclerView.ItemDecoration() {
            val `8dp` = view.dip(8)
            val `0dp` = view.dip(0)


            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(`8dp`, `0dp`, `8dp`, `8dp`)
            }
        })

/*        listOf(facebook_checkbox, google_checkbox, phone_checkbox).forEach(::buildBookCheckbox)

        facebook_checkbox.setOnCheckedChangedListener { checked ->
            context?.toast("FACEBOOK: $checked")
        }

        google_checkbox.setOnCheckedChangedListener { checked ->
            context?.toast("GOOGLE: $checked")
        }

        phone_checkbox.setOnCheckedChangedListener { checked ->
            context?.toast("PHONE: $checked")
        }

        val userProfileUpdates = UserProfileChangeRequest.Builder().setDisplayName("Dima‼van Cazacu").build()
        FirebaseAuth.getInstance().currentUser?.updateProfile(userProfileUpdates)
                ?.addOnSuccessListener {
                    context?.toast("YOUR SHIT WAZ UPDATED")
                }
                ?.addOnFailureListener {
                    context?.toast("FAILED TO UPDATE PROFILE")
                }
        FirebaseAuth.getInstance().currentUser?.reload()*/
    }

/*    @Suppress("UNCHECKED_CAST")
    private fun buildBookCheckbox(checkbox: AppCompatCheckedTextView) = with(checkbox) {
        val bookIconSize = 32 //dp

        val openingAnimatedDrawable = DrawableCompat.wrap(AnimatedVectorDrawableCompat.create(checkbox.context, R.drawable.checkbox_book_opening)!!)
        val closingAnimatedDrawable = DrawableCompat.wrap(AnimatedVectorDrawableCompat.create(checkbox.context, R.drawable.checkbox_book_closing)!!)

        openingAnimatedDrawable.setBounds(0, 0, dip(bookIconSize), dip(bookIconSize))
        closingAnimatedDrawable.setBounds(0, 0, dip(bookIconSize), dip(bookIconSize))

        checkMarkDrawable = if (isChecked) openingAnimatedDrawable else closingAnimatedDrawable

        checkbox.setOnClickListener {
            isChecked = !isChecked

            checkMarkDrawable = if (isChecked) openingAnimatedDrawable else closingAnimatedDrawable
            (checkMarkDrawable as AnimatedVectorDrawableCompat).start()

            (tag as? (Boolean) -> Unit)?.invoke(isChecked)
        }
    }

    private fun AppCompatCheckedTextView.setOnCheckedChangedListener(checkedChangedCallback: (Boolean) -> Unit) {
        tag = checkedChangedCallback
    }*/
}