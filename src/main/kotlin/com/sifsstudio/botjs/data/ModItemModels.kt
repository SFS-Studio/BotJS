package com.sifsstudio.botjs.data

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.item.Items
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.data.PackOutput
import net.minecraft.world.item.BlockItem

class ModItemModels(packOutput: PackOutput) : ModelProvider(packOutput, BotJS.ID) {
    override fun registerModels(blockModels: BlockModelGenerators, itemModels: ItemModelGenerators) {
        Items.REGISTRY.entries.forEach {
            val item = it.get()
            if (item is BlockItem) {
//                blockItem(it.id)
            } else {
                itemModels.generateFlatItem(it.get(), ModelTemplates.FLAT_ITEM)
            }
        }
    }
}