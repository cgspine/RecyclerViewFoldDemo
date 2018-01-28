package org.cgsdream.demo.view

import android.content.Context
import android.widget.TextView
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.util.QMUIResHelper
import org.cgsdream.demo.Item
import org.cgsdream.demo.R

/**
 * Created by cgspine on 2018/1/28.
 */

internal class SectionItemView(context: Context): TextView(context){

    init {
        textSize = 15f
        setTextColor(QMUIResHelper.getAttrColor(context, R.attr.qmui_config_color_gray_3))
        val paddingHor = QMUIDisplayHelper.dp2px(context, 16)
        val paddingVer = QMUIDisplayHelper.dp2px(context, 11)
        setPadding(paddingHor, paddingVer, paddingHor, paddingVer)
    }

    fun render(item: Item){
        text = item.content
    }
}