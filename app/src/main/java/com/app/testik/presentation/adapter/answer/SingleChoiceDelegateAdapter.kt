package com.app.testik.presentation.adapter.answer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.R
import com.app.testik.databinding.ItemSingleChoiceBinding
import com.app.testik.presentation.model.answer.SingleChoiceDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter
import com.app.testik.util.toABC

class SingleChoiceDelegateAdapter(
    val onSelectClick: (SingleChoiceDelegateItem) -> Unit
) : DelegateAdapter<SingleChoiceDelegateItem, SingleChoiceDelegateAdapter.ViewHolder>(
    SingleChoiceDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemSingleChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: SingleChoiceDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemSingleChoiceBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(answer: SingleChoiceDelegateItem) {

            binding.apply {
                etAnswer.setText(answer.text)

                btnSelect.setOnCheckedChangeListener(null)
                btnSelect.isChecked = answer.isSelected
                btnSelect.setOnCheckedChangeListener { _, _ ->
                    onSelectClick(answer)
                }

                tilAnswer.hint = root.context.getString(R.string.answer_option_abc, absoluteAdapterPosition.toABC())
                etAnswer.isFocusable = false
                ivDelete.isVisible = false
            }
        }
    }

}



