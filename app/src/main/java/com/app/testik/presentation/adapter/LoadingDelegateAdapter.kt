package com.app.testik.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.databinding.ItemLoadingBinding
import com.app.testik.presentation.model.LoadingItem
import com.app.testik.util.delegateadapter.DelegateAdapter

class LoadingDelegateAdapter :
    DelegateAdapter<LoadingItem, LoadingDelegateAdapter.ViewHolder>(LoadingItem::class.java) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLoadingBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun bindViewHolder(model: LoadingItem, viewHolder: ViewHolder) = Unit

    inner class ViewHolder(binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)

}