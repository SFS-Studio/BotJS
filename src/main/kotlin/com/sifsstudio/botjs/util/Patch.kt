package com.sifsstudio.botjs.util

import net.minecraft.nbt.*
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import org.mozilla.javascript.Context

// ItemStack
infix fun ItemStack.isItem(item: Item) = `is`(item)
infix fun ItemStack.isItem(item: TagKey<Item>) = `is`(item)

// BlockState
infix fun BlockState.isBlock(block: Block) = `is`(block)

// CompoundTag
operator fun CompoundTag.set(key: String, value: Tag) = put(key, value)
operator fun CompoundTag.set(key: String, value: String) = putString(key, value)
operator fun CompoundTag.set(key: String, value: Boolean) = putBoolean(key, value)
operator fun CompoundTag.set(key: String, value: IntArray) = putIntArray(key, value)
operator fun CompoundTag.set(key: String, value: ByteArray) = putByteArray(key, value)

// String
fun String.asStringTag(): StringTag = StringTag.valueOf(this)

// Context
inline fun withContextCatching(block: (Context) -> Unit) =
    runCatching {
        Context.enter().use {
            block(it)
        }
    }

@Suppress("UNCHECKED_CAST")
fun <T> suppressNullCheck(): T = null as T
