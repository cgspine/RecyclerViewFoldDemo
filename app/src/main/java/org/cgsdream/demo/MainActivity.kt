package org.cgsdream.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.QMUITopBarLayout

/**
 * Created by cgspine on 2018/1/26.
 */

class MainActivity : AppCompatActivity() {

    @BindView(R.id.topbar) lateinit var mTopBar: QMUITopBarLayout
    @BindView(R.id.recycler_view) lateinit var mRecyclerView: RecyclerView
    @BindView(R.id.section_header_container) lateinit var mSectionHeaderView: FrameLayout

    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: FoldAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        QMUIStatusBarHelper.translucent(this)
        ButterKnife.bind(this)
        initTopBar()
        initRecyclerView()

        mAdapter.setData(loadData())
        mAdapter.scrollToItem(Item("section 4, item 5"))
    }

    private fun initTopBar() {
        mTopBar.setTitle(R.string.app_name)
        mTopBar.setTitleGravity(Gravity.CENTER)
    }


    private fun initRecyclerView() {
        mAdapter = FoldAdapter(this)
        mAdapter.actionListener = object : FoldAdapter.ActionListener {
            override fun loadMore(section: Section<Header, Item>, loadBefore: Boolean) {
                mRecyclerView.postDelayed({
                    val list = ArrayList<Item>()
                    val count = 1 + (Math.random() * 20).toInt()
                    val hasMore = count <= 10
                    (0..count).mapTo(list) { Item("${section.header.title} load more $it in ${System.currentTimeMillis()}") }
                    successLoadMore(section, list, loadBefore, hasMore)
                },2000)
            }

            override fun scrollToPosition(position: Int, underSection: Boolean, forceInScreen: Boolean) {
                scrollToPos(position, underSection, forceInScreen)
            }

            override fun findViewHolderForAdapterPosition(position: Int): RecyclerView.ViewHolder? {
                return mRecyclerView.findViewHolderForAdapterPosition(position)
            }

            override fun requestChildFocus(view: View) {
                mRecyclerView.requestChildFocus(view, null)
            }
        }
        mRecyclerView.adapter = mAdapter
        mLayoutManager = object : LinearLayoutManager(this) {
            override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
                return RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
                try {
                    super.onLayoutChildren(recycler, state)
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
        }
        mRecyclerView.layoutManager = mLayoutManager
        val callback = object : PinnedSectionItemDecoration.Callback<FoldViewHolder> {
            override fun getRelativeFixedItemPosition(pos: Int): Int {
                return mAdapter.getRelativeFixedItemPosition(pos)
            }

            override fun isHeader(pos: Int): Boolean {
                return mAdapter.getItemViewType(pos) == FoldAdapter.ITEM_TYPE_SECTION_HEADER
            }

            override fun afterCreateFixedViewHolder(viewHolder: FoldViewHolder, viewType: Int) {

            }

            override fun afterBindFixedViewHolder(viewHolder: FoldViewHolder, pos: Int) {
                mAdapter.afterBindFixedViewHolder(viewHolder, pos)
            }

            override fun createViewHolder(parent: ViewGroup, viewType: Int): FoldViewHolder {
                return mAdapter.createViewHolder(parent, viewType)
            }

            override fun bindViewHolder(holder: FoldViewHolder, position: Int) {
                mAdapter.bindViewHolder(holder, position)
            }

            override fun getItemViewType(position: Int): Int {
                return mAdapter.getItemViewType(position)
            }

            override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
                mAdapter.registerAdapterDataObserver(observer)
            }

            override fun onHeaderVisibilityChanged(visible: Boolean) {

            }

        }
        mRecyclerView.addItemDecoration(PinnedSectionItemDecoration(mSectionHeaderView, callback))
    }

    private fun successLoadMore(loadSection: Section<Header, Item>, data: List<Item>, loadBefore: Boolean, hasMore: Boolean) {
        if (mRecyclerView.isComputingLayout) {
            mRecyclerView.postDelayed({
                successLoadMore(loadSection, data, loadBefore, hasMore)
            }, 250)
        } else {
            mAdapter.successLoadMore(loadSection, data, loadBefore, hasMore)
        }
    }


    private fun scrollToPos(pos: Int, underSection: Boolean = true, force: Boolean = false) {
        if (pos < 0 || pos >= mAdapter.itemCount) {
            return
        }
        val firstVPos = mLayoutManager.findFirstCompletelyVisibleItemPosition()
        val lastVPos = mLayoutManager.findLastCompletelyVisibleItemPosition()
        if (pos < firstVPos || pos > lastVPos || force) {
            if (underSection) {
                mLayoutManager.scrollToPositionWithOffset(pos, QMUIDisplayHelper.dp2px(this, 60)) // 滚到不被 SectionHeader 盖住的位置
            } else {
                mLayoutManager.scrollToPositionWithOffset(pos, 0)
            }

        }
    }

    private fun loadData(): MutableList<Section<Header, Item>> {
        val data = ArrayList<Section<Header, Item>>()
        for (i in 1..10) {
            val header = Header("section $i")
            val items = ArrayList<Item>()
            (1..10).mapTo(items) { Item("section $i, item $it") }
            val section = Section(header, items, true, true, true, false)
            data.add(section)
        }
        return data
    }
}