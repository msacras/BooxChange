package nl.booxchange.screens.book

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_book_details.*
import nl.booxchange.BR
import nl.booxchange.BooxchangeApp.Companion.context
import nl.booxchange.R
import nl.booxchange.extension.isVisible
import nl.booxchange.extension.toGone
import nl.booxchange.extension.toVisible
import nl.booxchange.utilities.BaseActivity
import org.jetbrains.anko.contentView

class BookDetailsActivity: BaseActivity() {
    override val viewModel by lazy { ViewModelProviders.of(this).get(BookDetailsViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_book_details)

        viewModel.initializeWithConfig(intent.getStringExtra(KEY_BOOK_ID))
        viewBinding.setVariable(BR.viewModel, viewModel)

        setupViews()
    }

    private fun setupViews() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener { onBackPressed() }

        image_pager.offscreenPageLimit = 5
        book_image.setOnClickListener {
            if (viewModel.images.isNotEmpty() || viewModel.isEditModeEnabled.get()) {
                image_pager.setCurrentItem(0, false)
                image_pager.toVisible()
            }
        }

        setupTradeBooksBlock()
    }

    private fun setupTradeBooksBlock() {
        var tradeBooks = emptyList<TradeBookModel>()
        viewModel.tradeBooks.observe(::getLifecycle) {
            tradeBooks = it.filterNotNull()
            (books_for_trade_list.adapter as BaseAdapter).notifyDataSetChanged()
//            contentView?.requestLayout()
        }

        books_for_trade_list.adapter = object: BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tradeBookModel = getItem(position)

                val binding = if (viewModel.isEditModeEnabled.get()) {
                    (convertView as? AppCompatEditText)?.let { DataBindingUtil.getBinding<ViewDataBinding>(it) } ?: DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.trade_book_edit, parent, false).apply {
                        (root as AppCompatEditText).addTextChangedListener(object: TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(s: Editable?) {
                                val emptyTradeInputsCount = tradeBooks.count { it.title.get().isNullOrEmpty() }

                                when {
                                    emptyTradeInputsCount == 0 -> viewModel.addBlankTradeBookField()
                                    emptyTradeInputsCount > 1 -> viewModel.removeExtraBlankFields(root.tag as TradeBookModel)
                                }
                            }
                        })
                    }
                } else {
                    (convertView as? AppCompatTextView)?.let { DataBindingUtil.getBinding<ViewDataBinding>(it) } ?: DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.trade_book_text, parent, false)
                }

                (binding as ViewDataBinding)
                binding.root.tag = tradeBookModel
                binding.setVariable(BR.itemModel, tradeBookModel)

                return binding.root
            }

            override fun getItem(position: Int): TradeBookModel {
                return tradeBooks[position]
            }

            override fun getItemId(position: Int): Long {
                return tradeBooks[position].hashCode().toLong()
            }

            override fun getCount(): Int {
                return tradeBooks.size
            }
        }
    }

    override fun onBackPressed() {
        when {
            image_pager.isVisible -> image_pager.toGone()
            viewModel.isEditModeEnabled.get() -> viewModel.toggleEditMode(null)
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val KEY_BOOK_ID = "BOOK_ID"
    }
}

class TradeBookModel(title: String) {
    val title = ObservableField<String>(title)
}

class HeightWrappingListView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defaultStyleAttribute: Int = 0): ListView(context, attributeSet, defaultStyleAttribute) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }
}
