package org.cgsdream.demo

/**
 * Created by cgspine on 2018/1/26.
 */

data class Header(val title: String) : Cloneable<Header> {
    override fun clone(): Header {
        return Header(title)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Header) {
            return false
        }

        return other.title == title
    }
}

data class Item(val content: String) : Cloneable<Item> {
    override fun clone(): Item {
        return Item(content)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Item) {
            return false
        }

        return other.content == content
    }
}

