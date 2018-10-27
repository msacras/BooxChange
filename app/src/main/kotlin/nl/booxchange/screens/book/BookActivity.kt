package nl.booxchange.screens.book

import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_book.*
import nl.booxchange.BR
import nl.booxchange.R
import nl.booxchange.extension.setTintCompat

class BookActivity : AppCompatActivity() {
    private val viewModel = BookFragmentViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_book).apply {
            executePendingBindings()
            setVariable(BR.viewModel, viewModel)
        }

        back.setOnClickListener { onBackPressed() }
        toolbar.navigationIcon?.setTintCompat(R.color.darkGray)

/*        expect(BookOpenedEvent::class.java) {
            startActivity(Intent(this, BookActivity::class.java))
        }*/
        delete_book_button.setOnClickListener {
            viewModel.deleteBook()
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
