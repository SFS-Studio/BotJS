package com.sifsstudio.botjs.util

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import org.mozilla.javascript.Context

fun <T> T.use(block: T.() -> Unit) = this.run {
    block()
}

// ItemStack
infix fun ItemStack.isItem(item: Item) = `is`(item)
infix fun ItemStack.isItem(item: TagKey<Item>) = `is`(item)

// BlockState
infix fun BlockState.isBlock(block: Block) = `is`(block)

// CompoundTag
fun CompoundTag.getList(key: String, type: Byte): ListTag = this.getList(key, type.toInt())
operator fun CompoundTag.set(key: String, value: Tag) = put(key, value)
operator fun CompoundTag.set(key: String, value: String) = putString(key, value)
operator fun CompoundTag.set(key: String, value: ByteArray) = putByteArray(key, value)

// String
fun String.asStringTag(): StringTag = StringTag.valueOf(this)

// Context
fun withContext(block: (Context) -> Unit) {
    val ctx = Context.enter()
    block(ctx)
    Context.exit()
}

@Suppress("UNCHECKED_CAST")
fun <T> nonnull(): T = null as T
