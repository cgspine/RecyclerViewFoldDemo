package org.cgsdream.demo

import android.support.v7.util.DiffUtil
import android.util.SparseArray

/**
 * Created by cgspine on 2018/1/26.
 */

class DiffCallback<H: Cloneable<H>, T: Cloneable<T>>(private val oldList: List<Section<H, T>>, private val newList: List<Section<H, T>>) : DiffUtil.Callback() {

    private val mOldSectionIndex: SparseArray<Int> = SparseArray()
    private val mOldItemIndex: SparseArray<Int> = SparseArray()

    private val mNewSectionIndex: SparseArray<Int> = SparseArray()
    private val mNewItemIndex: SparseArray<Int> = SparseArray()

    init {
        generateIndex(oldList, mOldSectionIndex, mOldItemIndex)
        generateIndex(newList, mNewSectionIndex, mNewItemIndex)

    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        val oldSectionIndex = mOldSectionIndex[oldItemPosition]
        val oldItemIndex = mOldItemIndex[oldItemPosition]
        val oldModel = oldList[oldSectionIndex]

        val newSectionIndex = mNewSectionIndex[newItemPosition]
        val newItemIndex = mNewItemIndex[newItemPosition]
        val newModel = newList[newSectionIndex]

        if (oldModel.header != newModel.header) {
            return false
        }

        if (oldItemIndex < 0 && oldItemIndex == newItemIndex) {
            return true
        }

        if (oldItemIndex < 0 || newItemIndex < 0) {
            return false
        }
        return oldModel.list[oldItemIndex] == newModel.list[newItemIndex]
    }

    override fun getOldListSize() = mOldSectionIndex.size()

    override fun getNewListSize() = mNewSectionIndex.size()

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        val oldSectionIndex = mOldSectionIndex[oldItemPosition]
        val oldItemIndex = mOldItemIndex[oldItemPosition]
        val oldModel = oldList[oldSectionIndex]

        val newSectionIndex = mNewSectionIndex[newItemPosition]
        val newModel = newList[newSectionIndex]

        if (oldItemIndex == ITEM_INDEX_SECTION_HEADER) {
            return oldModel.isFold == newModel.isFold
        }

        if (oldItemIndex == ITEM_INDEX_LOAD_BEFORE || oldItemIndex == ITEM_INDEX_LOAD_AFTER) {
            // load more 强制返回 false，这样可以通过 FolderAdapter.onViewAttachedToWindow 触发 load more
            return false
        }

        return true
    }

    companion object {
        val ITEM_INDEX_SECTION_HEADER = -1
        val ITEM_INDEX_LOAD_BEFORE = -2
        val ITEM_INDEX_LOAD_AFTER = -3

        fun <H: Cloneable<H>, T: Cloneable<T>> generateIndex(list: List<Section<H, T>>,
                                 sectionIndex: SparseArray<Int>,
                                 itemIndex: SparseArray<Int>){
            sectionIndex.clear()
            itemIndex.clear()
            var i = 0
            list.forEachIndexed { index, it ->
                if (!it.isLocked) {
                    sectionIndex.append(i, index)
                    itemIndex.append(i, ITEM_INDEX_SECTION_HEADER)
                    i++
                    if (!it.isFold && it.count() > 0) {
                        if (it.hasBefore) {
                            sectionIndex.append(i, index)
                            itemIndex.append(i, ITEM_INDEX_LOAD_BEFORE)
                            i++
                        }

                        for (j in 0 until it.count()) {
                            sectionIndex.append(i, index)
                            itemIndex.append(i, j)
                            i++
                        }

                        if (it.hasAfter) {
                            sectionIndex.append(i, index)
                            itemIndex.append(i, ITEM_INDEX_LOAD_AFTER)
                            i++
                        }
                    }
                }
            }
        }
    }
}