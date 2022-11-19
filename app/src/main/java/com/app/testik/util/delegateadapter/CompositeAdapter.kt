package com.app.testik.util.delegateadapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.presentation.model.LoadingItem

@Suppress("UNCHECKED_CAST")
class CompositeAdapter(
    private val delegates: SparseArray<DelegateAdapter<DelegateAdapterItem, RecyclerView.ViewHolder>>,
    private val onUpdateCallback: (() -> Unit)?
) : ListAdapter<DelegateAdapterItem, RecyclerView.ViewHolder>(DelegateAdapterItemDiffCallback()) {
    private var positionForUpdate: Int = -1

    override fun getItemViewType(position: Int): Int {
        for (i in 0 until delegates.size()) {
            if (delegates[i].modelClass == getItem(position).javaClass) {
                return delegates.keyAt(i)
            }
        }
        throw NullPointerException("Can not get viewType for position $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        delegates[viewType].createViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        onBindViewHolder(holder, position, mutableListOf())

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val delegateAdapter = delegates[getItemViewType(position)]

        if (delegateAdapter != null) {
            delegateAdapter.bindViewHolder(getItem(position), holder)
        } else {
            throw NullPointerException("can not find adapter for position $position")
        }

        if (positionForUpdate == position && position != 0) {
            onUpdateCallback?.invoke()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        delegates[holder.itemViewType].onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        delegates[holder.itemViewType].onViewDetachedFromWindow(holder)
        super.onViewDetachedFromWindow(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        delegates[holder.itemViewType].onViewAttachedToWindow(holder)
        super.onViewAttachedToWindow(holder)
    }

    override fun submitList(list: List<DelegateAdapterItem>?) {
        var newList = list

        list?.let {
            if (it.isNotEmpty() && it.last() !is LoadingItem) {
                onUpdateCallback?.let { _ ->
                    val newItemCount = it.size

                    positionForUpdate = maxOf(0, newItemCount - 3)

                    (it.last() as? ErrorItem?).let { error ->
                        if (error?.message?.contains("empty") == true) {
                            positionForUpdate = -1
                            newList = list.subList(0, list.lastIndex)
                        }
                    }

                }
            }
        }

        super.submitList(newList)
    }

    class Builder {
        private var onUpdateCallback: (() -> Unit)? = null
        private var count: Int = 0
        private val delegates: SparseArray<DelegateAdapter<DelegateAdapterItem, RecyclerView.ViewHolder>> = SparseArray()

        fun add(delegateAdapter: DelegateAdapter<out DelegateAdapterItem, *>): Builder {
            delegates.put(count++, delegateAdapter as DelegateAdapter<DelegateAdapterItem, RecyclerView.ViewHolder>)
            return this
        }

        fun setOnUpdateCallback(callback: () -> Unit): Builder {
            onUpdateCallback = callback
            return this
        }

        fun build(): CompositeAdapter {
            require(count != 0) { "Register at least one adapter" }
            return CompositeAdapter(
                delegates = delegates,
                onUpdateCallback = onUpdateCallback
            )
        }
    }
}