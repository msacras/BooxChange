package nl.booxchange.screens.library

import android.graphics.Rect
import android.os.Bundle
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatCheckedTextView
import android.support.v7.widget.RecyclerView
import android.view.View
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_library.*
import kotlinx.android.synthetic.main.fragment_library.view.*
import nl.booxchange.R
import nl.booxchange.utilities.BaseFragment
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast

class LibraryFragment: BaseFragment() {

    override val contentViewResourceId = R.layout.fragment_library
    override val viewModel = LibraryFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            GoogleSignIn.getClient(view.context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            LoginManager.getInstance().logOut()
            view.context.toast("Logging you out");
            (view.context as AppCompatActivity).finish()
        }

        view.user_books_list.addItemDecoration(object: RecyclerView.ItemDecoration() {
            val bookSpacing = view.dip(8)

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(bookSpacing, bookSpacing, bookSpacing, bookSpacing)
            }
        })

        listOf(facebook_checkbox, google_checkbox, phone_checkbox).forEach(::buildBookCheckbox)
    }

    private fun buildBookCheckbox(checkbox: AppCompatCheckedTextView) = with(checkbox) {
        val openingAnimatedDrawable = DrawableCompat.wrap(AnimatedVectorDrawableCompat.create(checkbox.context, R.drawable.animation_book_opening)!!)
        val closingAnimatedDrawable = DrawableCompat.wrap(AnimatedVectorDrawableCompat.create(checkbox.context, R.drawable.animation_book_closing)!!)

        checkMarkDrawable = if (isChecked) openingAnimatedDrawable else closingAnimatedDrawable

        checkbox.setOnClickListener {
            isChecked = !isChecked
            checkMarkDrawable = if (isChecked) openingAnimatedDrawable else closingAnimatedDrawable
            (checkMarkDrawable as? AnimatedVectorDrawableCompat)?.start()
        }
    }
}
