package com.app.testik.presentation.screen.gradingsystem.adapter

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemGradeBinding
import com.app.testik.presentation.screen.gradingsystem.model.GradeDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class GradeDelegateAdapter(
    val onGradeTextChanged: (GradeDelegateItem, String) -> Unit,
    val onFromTextChanged: (GradeDelegateItem, String) -> Unit,
    val onToTextChanged: (GradeDelegateItem, String) -> Unit,
    val onDeleteClick: (GradeDelegateItem) -> Unit
) : DelegateAdapter<GradeDelegateItem, GradeDelegateAdapter.ViewHolder>(
    GradeDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemGradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: GradeDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemGradeBinding) : RecyclerView.ViewHolder(binding.root) {

        var gradeListener: TextWatcher? = null
        var toListener: TextWatcher? = null
        var fromListener: TextWatcher? = null

        fun bind(grade: GradeDelegateItem) {

            binding.apply {
                etGrade.removeTextChangedListener(gradeListener)
                if (!etGrade.isFocused) etGrade.setText(grade.grade)
                gradeListener = etGrade.addTextChangedListener { onGradeTextChanged(grade, it.toString()) }

                etPointsFrom.removeTextChangedListener(fromListener)
                if (!etPointsFrom.isFocused) etPointsFrom.setText(grade.pointsFrom)
                fromListener = etPointsFrom.addTextChangedListener { onFromTextChanged(grade, it.toString()) }

                etPointsTo.removeTextChangedListener(toListener)
                if (!etPointsTo.isFocused) etPointsTo.setText(grade.pointsTo)
                toListener = etPointsTo.addTextChangedListener { onToTextChanged(grade, it.toString()) }

                ivDelete.setOnClickListener {
                    llGrade.clearFocus()
                    onDeleteClick(grade)
                }
            }
        }
    }

}



