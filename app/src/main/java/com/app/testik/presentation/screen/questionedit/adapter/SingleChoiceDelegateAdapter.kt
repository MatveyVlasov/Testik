package com.app.testik.presentation.screen.questionedit.adapter

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemSingleChoiceBinding
import com.app.testik.presentation.screen.questionedit.model.SingleChoiceDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class SingleChoiceDelegateAdapter(
    val onTextChanged: (SingleChoiceDelegateItem, String) -> Unit,
    val onSelectClick: (SingleChoiceDelegateItem) -> Unit,
    val onDeleteClick: (SingleChoiceDelegateItem) -> Unit
) : DelegateAdapter<SingleChoiceDelegateItem, SingleChoiceDelegateAdapter.ViewHolder>(
    SingleChoiceDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemSingleChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: SingleChoiceDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemSingleChoiceBinding) : RecyclerView.ViewHolder(binding.root) {

        var listener: TextWatcher? = null

        fun bind(answer: SingleChoiceDelegateItem) {

            binding.apply {
                etAnswer.removeTextChangedListener(listener)
                if (!etAnswer.isFocused) etAnswer.setText(answer.text)
                listener = etAnswer.addTextChangedListener { onTextChanged(answer, it.toString()) }

                btnSelect.setOnCheckedChangeListener(null)
                btnSelect.isChecked = answer.isCorrect
                btnSelect.setOnCheckedChangeListener { _, _ ->
                    onSelectClick(answer)
                }

                ivDelete.setOnClickListener {
                    llAnswer.clearFocus()
                    onDeleteClick(answer)
                }
            }
        }
    }

}



