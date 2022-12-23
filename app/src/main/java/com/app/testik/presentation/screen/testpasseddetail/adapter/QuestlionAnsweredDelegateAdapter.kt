package com.app.testik.presentation.screen.testpasseddetail.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.R
import com.app.testik.databinding.ItemQuestionAnsweredBinding
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class QuestionAnsweredDelegateAdapter(
    val onClick: (QuestionDelegateItem) -> Unit
) : DelegateAdapter<QuestionDelegateItem, QuestionAnsweredDelegateAdapter.ViewHolder>(
        QuestionDelegateItem::class.java
    ) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemQuestionAnsweredBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: QuestionDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemQuestionAnsweredBinding) : RecyclerView.ViewHolder(binding.root) {

        private val colorCorrect = binding.root.resources.getColor(R.color.correct, null)
        private val colorWrong = binding.root.resources.getColor(R.color.wrong, null)

        fun bind(question: QuestionDelegateItem) {

            binding.apply {
                val color = if (question.pointsEarned == 0) colorWrong else colorCorrect
                clPoints.backgroundTintList = ColorStateList.valueOf(color)

                tvPointsMaxData.text = question.pointsMax.toString()

                tvPointsData.text = question.pointsEarned.toString()
                tvPoints.text = root.resources.getQuantityString(R.plurals.points_quantity, question.pointsEarned)

                tvTitle.text = question.title

                root.setOnClickListener { onClick(question) }
            }
        }
    }

}



