package com.app.testik.presentation.adapter.answer

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemOrderingEditBinding
import com.app.testik.presentation.model.answer.OrderingDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class OrderingEditDelegateAdapter(
    val onTextChanged: (OrderingDelegateItem, String) -> Unit,
    val onDeleteClick: (OrderingDelegateItem) -> Unit
) : DelegateAdapter<OrderingDelegateItem, OrderingEditDelegateAdapter.ViewHolder>(
    OrderingDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemOrderingEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: OrderingDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemOrderingEditBinding) : RecyclerView.ViewHolder(binding.root) {

        var listener: TextWatcher? = null

        fun bind(answer: OrderingDelegateItem) {

            binding.apply {
                etAnswer.removeTextChangedListener(listener)
                if (!etAnswer.isFocused) etAnswer.setText(answer.text)
                listener = etAnswer.addTextChangedListener { onTextChanged(answer, it.toString()) }

                ivDelete.setOnClickListener {
                    llAnswer.clearFocus()
                    onDeleteClick(answer)
                }
            }
        }
    }

}



