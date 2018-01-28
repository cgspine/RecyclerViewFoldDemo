package org.cgsdream.demo.view

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.qmuiteam.qmui.alpha.QMUIAlphaLinearLayout
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.util.QMUIResHelper
import com.qmuiteam.qmui.widget.QMUIRadiusImageView
import org.cgsdream.demo.Section
import org.cgsdream.demo.Header
import org.cgsdream.demo.Item
import org.cgsdream.demo.R

/**
 * Created by cgspine on 2018/1/28.
 */
class SectionHeaderView(context: Context) : LinearLayout(context) {

    private val iconView: QMUIRadiusImageView
    private val titleView: TextView
    private val foldView: AppCompatImageView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val paddingHor = QMUIDisplayHelper.dp2px(context, 16)
        val paddingVer = QMUIDisplayHelper.dp2px(context, 11)
        setPadding(paddingHor, paddingVer, paddingHor, paddingVer)
        setBackgroundColor(ContextCompat.getColor(context, R.color.qmui_config_color_white))

        iconView = QMUIRadiusImageView(context)
        iconView.borderWidth = 1
        iconView.isCircle = true
        iconView.borderColor = QMUIResHelper.getAttrColor(context, R.attr.qmui_config_color_gray_3)
        iconView.setImageResource(R.mipmap.ic_launcher)
        val iconSize = QMUIDisplayHelper.dp2px(context, 32)
        val iconLp = LinearLayout.LayoutParams(iconSize, iconSize)
        iconLp.rightMargin = QMUIDisplayHelper.dp2px(context, 12)
        addView(iconView, iconLp)

        titleView = TextView(context)
        titleView.textSize = 16f
        titleView.setTextColor(QMUIResHelper.getAttrColor(context, R.attr.qmui_config_color_gray_1))
        addView(titleView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        foldView = AppCompatImageView(context)
        foldView.setImageResource(R.drawable.icon_cell_arrow)
        val foldLp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        foldLp.leftMargin = QMUIDisplayHelper.dp2px(context, 12)
        addView(foldView, foldLp)
    }

    fun render(section: Section<Header, Item>) {
        titleView.text = section.header.title
        foldView.rotation = if (section.isFold) 90f else 270f
    }
}