package com.app.testik.presentation.screen.testspassedusers.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.R
import com.app.testik.databinding.ItemTestPassedUserBinding
import com.app.testik.presentation.screen.testspassedusers.model.TestPassedUserDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapter
import com.app.testik.util.loadAvatar

class TestPassedUserDelegateAdapter(
    val onClick: (String, String) -> Unit
) : DelegateAdapter<TestPassedUserDelegateItem, TestPassedUserDelegateAdapter.ViewHolder>(
    TestPassedUserDelegateItem::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ViewHolder(
            ItemTestPassedUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun bindViewHolder(model: TestPassedUserDelegateItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemTestPassedUserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(test: TestPassedUserDelegateItem) {

            val isPointsVisible = test.isFinished || test.pointsCalculated

            binding.apply {
                loadAvatar(context = root.context, imageView = binding.ivAvatar, url = test.avatar)

                tvUsername.text = test.username
                tvDateData.text = test.date
                tvPointsData.text = root.resources.getString(R.string.points_earned, test.pointsEarned, test.pointsMax)

                tvPoints.isVisible = isPointsVisible
                tvPointsData.isVisible = isPointsVisible
                tvNotFinished.isVisible = !test.isFinished

                root.setOnClickListener { onClick(test.recordId, test.username) }
            }
        }
    }

}



