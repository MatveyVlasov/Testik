package com.app.testik.presentation.adapter.answer

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemMatchingEditBinding
import com.app.testik.presentation.model.answer.MatchingDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class MatchingEditDelegateAdapter(
    val onTextChanged: (MatchingDelegateItem, String) -> Unit,
    val onTextMatchingChanged: (MatchingDelegateItem, String) -> Unit,
    val onDeleteClick: (MatchingDelegateItem) -> Unit
) : DelegateAdapter<MatchingDelegateItem, MatchingEditDelegateAdapter.ViewHolder>(
    MatchingDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemMatchingEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: MatchingDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemMatchingEditBinding) : RecyclerView.ViewHolder(binding.root) {

        var listener: TextWatcher? = null
        var listenerMatching: TextWatcher? = null

        fun bind(answer: MatchingDelegateItem) {

            binding.apply {
                etAnswer.removeTextChangedListener(listener)
                if (!etAnswer.isFocused) etAnswer.setText(answer.text)
                listener = etAnswer.addTextChangedListener { onTextChanged(answer, it.toString()) }

                etAnswerMatching.removeTextChangedListener(listenerMatching)
                if (!etAnswerMatching.isFocused) etAnswerMatching.setText(answer.textMatching)
                listenerMatching = etAnswerMatching.addTextChangedListener { onTextMatchingChanged(answer, it.toString()) }

                ivDelete.setOnClickListener {
                    llAnswer.clearFocus()
                    onDeleteClick(answer)
                }
            }
        }
    }

}



