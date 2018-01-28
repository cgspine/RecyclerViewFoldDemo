package org.cgsdream.demo

/**
 * Created by cgspine on 2018/1/26.
 */

data class Section<H: Cloneable<H>, T: Cloneable<T>>(
        val header: H,
        val list: MutableList<T>,
        var hasBefore: Boolean,
        var hasAfter: Boolean,
        var isFold: Boolean,
        var isLocked: Boolean): Cloneable<Section<H, T>>{

    var isLoadBeforeError: Boolean = false
    var isLoadAfterError: Boolean = false

    fun count() = list.size

    fun cloneStatusTo(other: Section<H, T>){
        other.hasBefore = hasBefore
        other.hasAfter = hasAfter
        other.isFold = isFold
        other.isLocked = isLocked
        other.isLoadAfterError = isLoadAfterError
        other.isLoadBeforeError = isLoadBeforeError
    }

    override fun clone(): Section<H, T> {
        val newList = ArrayList<T>()
        list.forEach{ it: T ->
            newList.add(it.clone())
        }
        val section =  Section(header.clone(), newList, hasBefore, hasAfter, isFold, isLocked)
        section.isLoadBeforeError = isLoadBeforeError
        section.isLoadAfterError = isLoadAfterError
        return section
    }
}