package org.cgsdream.demo

import android.graphics.Canvas
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import java.lang.ref.WeakReference

/**
 * Created by cgspine on 2018/1/28.
 */

class PinnedSectionItemDecoration<VH : RecyclerView.ViewHolder>(sectionContainer: ViewGroup, private val mCallback: Callback<VH>) : RecyclerView.ItemDecoration() {
    private var mFixedHeaderViewHolder: VH? = null
    private var mFixedHeaderViewPosition = RecyclerView.NO_POSITION
    private val mWeakSectionContainer: WeakReference<ViewGroup> = WeakReference(sectionContainer)

    init {

        mCallback.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            // adapter 里的 ViewHolder 更新的话，fixedHeaderViewHolder 也要更新
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                if (mFixedHeaderViewPosition >= positionStart && mFixedHeaderViewPosition < positionStart + itemCount) {
                    if (mFixedHeaderViewHolder != null) {
                        if (mWeakSectionContainer.get() != null) {
                            bindFixedViewHolder(mWeakSectionContainer.get(), mFixedHeaderViewHolder!!, mFixedHeaderViewPosition)
                        }
                    }
                }
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                if (mFixedHeaderViewPosition >= positionStart && mFixedHeaderViewPosition < positionStart + itemCount) {
                    mFixedHeaderViewPosition = RecyclerView.NO_POSITION
                }
            }
        })
    }

    private fun setHeaderVisibility(visibility: Boolean) {
        val sectionContainer = mWeakSectionContainer.get() ?: return
        sectionContainer.visibility = if (visibility) View.VISIBLE else View.GONE
        mCallback.onHeaderVisibilityChanged(visibility)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        super.onDrawOver(c, parent, state)
        val sectionContainer = mWeakSectionContainer.get() ?: return

        val layoutManager = parent.layoutManager
        if (layoutManager == null || layoutManager !is LinearLayoutManager) {
            setHeaderVisibility(false)
            return
        }
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (firstVisibleItemPosition == RecyclerView.NO_POSITION) {
            setHeaderVisibility(false)
            return
        }

        val headerPos = mCallback.getRelativeFixedItemPosition(firstVisibleItemPosition)
        if (headerPos == RecyclerView.NO_POSITION) {
            setHeaderVisibility(false)
            return
        }
        if (mFixedHeaderViewHolder == null || mFixedHeaderViewHolder!!.itemViewType != mCallback.getItemViewType(headerPos)) {
            mFixedHeaderViewHolder = createFixedViewHolder(parent, headerPos)
        }

        if (mFixedHeaderViewPosition != headerPos) {
            mFixedHeaderViewPosition = headerPos
            bindFixedViewHolder(sectionContainer, mFixedHeaderViewHolder!!, headerPos)
        }

        setHeaderVisibility(true)

        val contactPoint = sectionContainer.height
        val childInContact = parent.findChildViewUnder((parent.width / 2).toFloat(), contactPoint.toFloat())
        if (childInContact == null) {
            sectionContainer.offsetTopAndBottom(parent.top - sectionContainer.top)
            return
        }

        if (mCallback.isHeader(parent.getChildAdapterPosition(childInContact))) {
            sectionContainer.offsetTopAndBottom(childInContact.top + parent.top - sectionContainer.height - sectionContainer.top)
            return
        }

        sectionContainer.offsetTopAndBottom(parent.top - sectionContainer.top)
    }


    private fun createFixedViewHolder(recyclerView: RecyclerView, position: Int): VH {
        val viewType = mCallback.getItemViewType(position)
        val vh = mCallback.createViewHolder(recyclerView, viewType)
        mCallback.afterCreateFixedViewHolder(vh, viewType)
        return vh
    }

    private fun bindFixedViewHolder(sectionContainer: ViewGroup?, viewHolder: VH, position: Int) {
        mCallback.bindViewHolder(viewHolder, position)
        mCallback.afterBindFixedViewHolder(viewHolder, position)
        sectionContainer!!.removeAllViews()
        sectionContainer.addView(viewHolder!!.itemView)
    }


    interface Callback<ViewHolder> {
        /**
         * @param pos 任意 pos
         * @return 获取 pos 对应的 sectionHeader 的 pos
         */
        fun getRelativeFixedItemPosition(pos: Int): Int


        fun isHeader(pos: Int): Boolean

        /**
         * 在 RecyclerView.Adapter#onCreateViewHolder 之后的方法
         */
        fun afterCreateFixedViewHolder(viewHolder: ViewHolder, viewType: Int)

        /**
         * 在 RecyclerView.Adapter#bindViewHolder 之后的渲染方法
         */
        fun afterBindFixedViewHolder(viewHolder: ViewHolder, pos: Int)

        fun createViewHolder(parent: ViewGroup, viewType: Int): ViewHolder

        fun bindViewHolder(holder: ViewHolder, position: Int)

        fun getItemViewType(position: Int): Int

        fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver)

        fun onHeaderVisibilityChanged(visible: Boolean)
    }
}