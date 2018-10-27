package nl.booxchange.screens.book

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.databinding.ViewDataBinding
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.database.FirebaseDatabase
import com.vcristian.combus.expect
import kotlinx.android.synthetic.main.activity_book.*
import kotlinx.android.synthetic.main.fragment_book.view.*
import kotlinx.android.synthetic.main.trade_books.*
import nl.booxchange.BR
import nl.booxchange.R
import nl.booxchange.model.BookModel
import nl.booxchange.model.BookOpenedEvent
import nl.booxchange.screens.MainFragmentActivity

class BookFragment: Fragment() {
//    private val contentViewResourceId = R.layout.fragment_book
    private val viewModel = BookFragmentViewModel()
    var bookArray = ArrayList<String>()
    private var hashMap = HashMap<String, String>()
    val color = Color.parseColor("#939393")
    val dbref = FirebaseDatabase.getInstance().reference
    val bookModel = ObservableField<BookModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout.activity_book, null, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DataBindingUtil.getBinding<ViewDataBinding>(view)?.apply {
            executePendingBindings()
            setVariable(BR.viewModel, viewModel)
        }

/*        add_book_label.setOnClickListener {
            createBookLabel()
        }*/

        back.setOnClickListener{ onBackPressed() }
//        toolbar.navigationIcon?.setTintCompat(R.color.darkGray)

        expect(BookOpenedEvent::class.java) {
            (activity as? MainFragmentActivity)?.showFragment("book_view", false)
//            startActivity(Intent(context, BookActivity::class.java))
        }
/*        view.image_pager.offscreenPageLimit = 5
        view.book_image.setOnClickListener {
            view.image_pager.setCurrentItem(0, false)
            view.image_pager.toVisible()
//            view.app_bar_layout.animate().alpha(0.5f).start()
        }*/


        view.delete_book_button.setOnClickListener {
            viewModel.deleteBook()
            onBackPressed()
        }

/*        add_book_label.setOnClickListener({
            val country = textOut.text.toString().trim()
            val user = User(country)
        })*/

/*        add_book_label.setOnClickListener({
            val layoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val addView = layoutInflater.inflate(R.layout.trade_books, null)
            val textOut = addView.findViewById(R.id.textOut) as TextView
            textOut.text = books_to_trade.text.toString()
            hashMap[dbref.push().toString()] = books_to_trade.text.toString()
            val buttonRemove = addView.findViewById(R.id.remove) as ImageView
            val thisListener = View.OnClickListener {
                (addView.parent as LinearLayout).removeView(addView)
            }
            buttonRemove.setOnClickListener(thisListener)
            parentLayout.addView(addView)
        })*/
    }

/*    private fun listAllAddView() {
        val childCount = parentLayout.childCount
        for (i in 0 until childCount)
        {
            val thisChild = parentLayout.getChildAt(i)
            reList.append(thisChild + "\n")
        }
    }*/

    private fun onBackPressed(): Boolean {
        val view = view ?: return true

/*        if (view.image_pager.isVisible) {
            view.image_pager.toGone()
            view.app_bar_layout.animate().alpha(1f).start()
        } else {
            if (viewModel.isEditModeEnabled.get()) {
                viewModel.toggleEditMode(null)
            }
            (activity as? MainFragmentActivity)?.hideFragment("book_view")
        }*/

        (activity as? MainFragmentActivity)?.hideFragment("book_view")
        return isHidden
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
