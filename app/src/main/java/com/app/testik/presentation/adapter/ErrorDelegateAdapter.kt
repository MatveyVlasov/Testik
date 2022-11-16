package com.app.testik.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemErrorBinding
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class ErrorDelegateAdapter :
    DelegateAdapter<ErrorItem, ErrorDelegateAdapter.ViewHolder>(ErrorItem::class.java) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemErrorBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun bindViewHolder(model: ErrorItem, viewHolder: ViewHolder) = viewHolder.bind(model)

    inner class ViewHolder(private val binding: ItemErrorBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(error: ErrorItem) {

            binding.apply {
                tvError.text = error.message
            }
        }
    }

}