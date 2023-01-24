package com.app.testik.presentation.adapter.answer

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemShortAnswerBinding
import com.app.testik.presentation.model.answer.ShortAnswerDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class ShortAnswerEditDelegateAdapter(
    val onTextChanged: (ShortAnswerDelegateItem, String) -> Unit,
    val onDeleteClick: (ShortAnswerDelegateItem) -> Unit
) : DelegateAdapter<ShortAnswerDelegateItem, ShortAnswerEditDelegateAdapter.ViewHolder>(
    ShortAnswerDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemShortAnswerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: ShortAnswerDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemShortAnswerBinding) : RecyclerView.ViewHolder(binding.root) {

        var listener: TextWatcher? = null

        fun bind(answer: ShortAnswerDelegateItem) {

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



