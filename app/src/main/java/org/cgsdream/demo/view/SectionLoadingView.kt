package org.cgsdream.demo.view

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.util.QMUIResHelper
import com.qmuiteam.qmui.widget.QMUILoadingView
import org.cgsdream.demo.R

/**
 * Created by cgspine on 2018/1/28.
 */

class SectionLoadingView(context: Context) : FrameLayout(context) {
    private var mQMUILoadingView: QMUILoadingView
    private var mErrorTextView: TextView? = null

    private var mIsLoadBefore: Boolean = false
    private var mIsLoadError = false

    var reLoadAction: ((Boolean) -> Unit)? = null

    init {
        val paddingVer = QMUIDisplayHelper.dp2px(getContext(), 24)
        val paddingHor = QMUIDisplayHelper.dp2px(getContext(), 16)
        setPadding(paddingHor, paddingVer, paddingHor, paddingVer)

        mQMUILoadingView = QMUILoadingView(context)
        val lp = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.gravity = Gravity.CENTER
        addView(mQMUILoadingView, lp)
    }

    private fun showError(isError: Boolean) {
        if (isError) {
            if (mErrorTextView == null) {
                initTextView()
            }
            mErrorTextView?.visibility = VISIBLE
            mQMUILoadingView.visibility = GONE
        } else {
            mErrorTextView?.visibility = GONE
            mQMUILoadingView.visibility = VISIBLE
        }
    }

    private fun initTextView() {
        val errTv = TextView(context)
        errTv.setTextColor(QMUIResHelper.getAttrColor(context, R.attr.qmui_config_color_gray_4))
        errTv.textSize = 14f
        errTv.gravity = Gravity.CENTER
        val builder = SpannableStringBuilder()
        builder.append("加载失败 ")
        val start = builder.length
        builder.append("重试")
        val linkNormalColor = QMUIResHelper.getAttrColor(context, R.attr.qmui_config_color_blue)
        builder.setSpan(ForegroundColorSpan(linkNormalColor), start, builder.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        errTv.text = builder
        errTv.visibility = GONE
        errTv.setOnClickListener {
            showError(false)
            reLoadAction?.invoke(isLoadBefore())
        }
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, QMUIDisplayHelper.dpToPx(32))
        lp.gravity = Gravity.CENTER
        addView(mErrorTextView, lp)
        mErrorTextView = errTv
    }

    fun render(isLoadBefore: Boolean, isLoadError: Boolean) {
        mIsLoadBefore = isLoadBefore
        mIsLoadError = isLoadError
        showError(isLoadError)
    }

    fun isLoadBefore(): Boolean {
        return mIsLoadBefore
    }

    fun isLoadError(): Boolean {
        return mIsLoadError
    }
}