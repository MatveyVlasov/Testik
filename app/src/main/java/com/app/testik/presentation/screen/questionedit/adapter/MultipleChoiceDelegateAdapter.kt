package com.app.testik.presentation.screen.questionedit.adapter

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemMultipleChoiceBinding
import com.app.testik.presentation.screen.questionedit.model.MultipleChoiceDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class MultipleChoiceDelegateAdapter(
    val onTextChanged: (MultipleChoiceDelegateItem, String) -> Unit,
    val onSelectClick: (MultipleChoiceDelegateItem, Boolean) -> Unit,
    val onDeleteClick: (MultipleChoiceDelegateItem) -> Unit
) : DelegateAdapter<MultipleChoiceDelegateItem, MultipleChoiceDelegateAdapter.ViewHolder>(
    MultipleChoiceDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemMultipleChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: MultipleChoiceDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemMultipleChoiceBinding) : RecyclerView.ViewHolder(binding.root) {

        var listener: TextWatcher? = null

        fun bind(answer: MultipleChoiceDelegateItem) {

            binding.apply {
                etAnswer.removeTextChangedListener(listener)
                if (!etAnswer.isFocused) etAnswer.setText(answer.text)
                listener = etAnswer.addTextChangedListener { onTextChanged(answer, it.toString()) }

                btnSelect.setOnCheckedChangeListener(null)
                btnSelect.isChecked = answer.isCorrect
                btnSelect.setOnCheckedChangeListener { _, isChecked ->
                    onSelectClick(answer, isChecked)
                }

                ivDelete.setOnClickListener { onDeleteClick(answer) }
            }
        }
    }

}



