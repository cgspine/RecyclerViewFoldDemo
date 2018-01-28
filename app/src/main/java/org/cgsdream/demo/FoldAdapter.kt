package org.cgsdream.demo

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import org.cgsdream.demo.view.SectionHeaderView
import org.cgsdream.demo.view.SectionItemView
import org.cgsdream.demo.view.SectionLoadingView
import java.util.*

/**
 * Created by cgspine on 2018/1/26.
 */

class FoldAdapter(private val context: Context) : RecyclerView.Adapter<FoldViewHolder>() {
    companion object {
        val ITEM_TYPE_SECTION_HEADER = 0
        val ITEM_TYPE_SECTION_ITEM = 1
        val ITEM_TYPE_SECTION_LOADING = 2
    }

    private val mLastData: MutableList<Section<Header, Item>> = ArrayList()
    private val mData: MutableList<Section<Header, Item>> = ArrayList()

    private val mSectionIndex: SparseArray<Int> = SparseArray()
    private val mItemIndex: SparseArray<Int> = SparseArray()

    var actionListener: ActionListener? = null

    fun setData(list: MutableList<Section<Header, Item>>) {
        mData.clear()
        mData.addAll(list)
        diff(true)
    }

    private fun diff(reValue: Boolean) {
        val diffResult = DiffUtil.calculateDiff(DiffCallback(mLastData, mData), false)
        DiffCallback.generateIndex(mData, mSectionIndex, mItemIndex)
        diffResult.dispatchUpdatesTo(this)

        if (reValue) {
            mLastData.clear()
            mData.forEach { mLastData.add(it.clone()) }
        } else {
            // clone status 避免大量创建对象
            mData.forEachIndexed { index, it ->
                it.cloneStatusTo(mLastData[index])
            }
        }
    }

    override fun onBindViewHolder(holder: FoldViewHolder, position: Int) {
        val sectionIndex = mSectionIndex[position]
        val itemIndex = mItemIndex[position]
        val section = mData[sectionIndex]
        when (itemIndex) {
            DiffCallback.ITEM_INDEX_SECTION_HEADER -> (holder.itemView as SectionHeaderView).render(section)
            DiffCallback.ITEM_INDEX_LOAD_BEFORE -> (holder.itemView as SectionLoadingView).render(true, section.isLoadBeforeError)
            DiffCallback.ITEM_INDEX_LOAD_AFTER -> (holder.itemView as SectionLoadingView).render(false, section.isLoadAfterError)
            else -> {
                val view = holder.itemView as SectionItemView
                val item = section.list[itemIndex]
                view.render(item)
            }
        }
    }

    private fun onItemClick(holder: FoldViewHolder, pos: Int) {
        val itemIndex = mItemIndex[pos]
        if (itemIndex == DiffCallback.ITEM_INDEX_SECTION_HEADER) {
            toggleFold(pos)
        }
    }

    fun scrollToHeader(header: Header) {
        for (i in 0 until mSectionIndex.size()) {
            val posInSection = mItemIndex[i]
            if (posInSection == DiffCallback.ITEM_INDEX_SECTION_HEADER) {
                val data = mData[mSectionIndex[i]]
                if (data.header == header) {
                    actionListener?.scrollToPosition(i, false, true)
                    return
                }
            }
        }
    }

    fun successLoadMore(loadSection: Section<Header, Item>, data: List<Item>, loadBefore: Boolean, hasMore: Boolean){
        if(loadBefore){
            for(i in 0 until mSectionIndex.size()){
                if(mItemIndex[i] == 0){
                    if(mData[mSectionIndex[i]] == loadSection){
                        val focusVH = actionListener?.findViewHolderForAdapterPosition(i)
                        if (focusVH != null) {
                            actionListener?.requestChildFocus(focusVH.itemView)
                            break
                        }
                    }
                }
            }
            loadSection.list.addAll(0, data)
            loadSection.hasBefore = hasMore
        }else{
            loadSection.list.addAll(data)
            loadSection.hasAfter = hasMore
        }
        lock(loadSection)

        diff(true)
    }

    fun scrollToItem(item: Item) {
        for (i in mData.indices) {

            val find = mData[i].list.find { it == item }
            if (find != null) {
                if (mData[i].isFold) {
                    mData[i].isFold = false
                    lock(mData[i])
                    diff(false)
                    realScrollToItem(item)
                } else {
                    realScrollToItem(item)
                }
            }
        }

    }

    private fun realScrollToItem(item: Item) {
        for (i in 0 until mSectionIndex.size()) {
            val posInSection = mItemIndex[i]
            if (posInSection >= 0) {
                val section = mData[mSectionIndex[i]]
                if (section.list[posInSection] == item) {
                    actionListener?.scrollToPosition(i, true, true)
                    return
                }
            }
        }
    }

    private fun toggleFold(pos: Int) {
        val section = mData[mSectionIndex[pos]]
        section.isFold = !section.isFold
        lock(section)
        diff(false)

        if (!section.isFold) {
            for (i in 0 until mSectionIndex.size()) {
                val index = mSectionIndex[i]
                val inner = mItemIndex[i]
                if (inner == DiffCallback.ITEM_INDEX_SECTION_HEADER) {
                    if (section.header == mData[index].header) {
                        actionListener?.scrollToPosition(i, false, true)
                        break
                    }
                }
            }
        }
    }

    // 遍历整个列表，以 index 为中心，锁住/解锁前后所有的数据
    private fun lock(section: Section<Header, Item>) {
        val lockPrevious = !section.isFold && section.hasBefore && !section.isLoadBeforeError
        val lockAfter = !section.isFold && section.hasAfter && !section.isLoadAfterError
        val index = mData.indexOf(section)
        section.isLocked = false
        for (i in mData.indices) {
            if (i < index) {
                val data = mData[i]
                data.isLocked = lockPrevious
            } else if (i > index) {
                val data = mData[i]
                data.isLocked = lockAfter
            }
        }
    }

    fun getRelativeFixedItemPosition(pos: Int): Int {
        var position = pos
        while (getItemViewType(position) != ITEM_TYPE_SECTION_HEADER) {
            position--
            if (position < 0) {
                return RecyclerView.NO_POSITION
            }
        }
        return position
    }

    override fun getItemCount(): Int = mItemIndex.size()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FoldViewHolder {
        val view = when (viewType) {
            ITEM_TYPE_SECTION_HEADER -> SectionHeaderView(context)
            ITEM_TYPE_SECTION_LOADING -> SectionLoadingView(context)
            else -> SectionItemView(context)
        }
        val viewHolder = FoldViewHolder(view)
        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(viewHolder, position)
            }
        }
        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        val itemIndex = mItemIndex[position]
        return when (itemIndex) {
            DiffCallback.ITEM_INDEX_SECTION_HEADER -> ITEM_TYPE_SECTION_HEADER
            DiffCallback.ITEM_INDEX_LOAD_AFTER -> ITEM_TYPE_SECTION_LOADING
            DiffCallback.ITEM_INDEX_LOAD_BEFORE -> ITEM_TYPE_SECTION_LOADING
            else -> ITEM_TYPE_SECTION_ITEM
        }
    }

    override fun onViewAttachedToWindow(holder: FoldViewHolder) {
        if (holder.itemView is SectionLoadingView) {
            val layout = holder.itemView
            if (!layout.isLoadError()) {
                val section = mData[mSectionIndex.get(holder.adapterPosition)]
                actionListener?.loadMore(section, layout.isLoadBefore())
            }
        }
    }

    fun afterBindFixedViewHolder(viewHolder: FoldViewHolder, pos: Int) {
        viewHolder.itemView.setOnClickListener {
            onItemClick(viewHolder, pos)
        }
    }

    interface ActionListener {
        fun loadMore(section: Section<Header, Item>, loadBefore: Boolean)
        fun scrollToPosition(position: Int, underSection: Boolean, forceInScreen: Boolean)
        fun findViewHolderForAdapterPosition(position: Int): RecyclerView.ViewHolder?
        fun requestChildFocus(view: View)
    }
}