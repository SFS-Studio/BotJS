package com.sifsstudio.botjs.data

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.block.Blocks
import com.sifsstudio.botjs.item.Items
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.data.PackOutput
import net.minecraft.world.item.BlockItem

class ModModels(packOutput: PackOutput) : ModelProvider(packOutput, BotJS.ID) {
    override fun registerModels(blockModels: BlockModelGenerators, itemModels: ItemModelGenerators) {
        blockModels.createTrivialCube(Blocks.FIRMWARE_PROGRAMMER)
        blockModels.createTrivialCube(Blocks.BOT_ASSEMBLER)
        val tools = listOf(
            Items.WRENCH,
            Items.SWITCH,
        )
        Items.REGISTRY.entries.forEach {
            val item = it.get()
            if (item is BlockItem) {
                return@forEach
            }
            if (tools.contains(item)) {
                itemModels.generateFlatItem(it.get(), ModelTemplates.FLAT_HANDHELD_ITEM)
            } else {
                itemModels.generateFlatItem(it.get(), ModelTemplates.FLAT_ITEM)
            }
        }
    }
}