package com.sifsstudio.botjs

import com.sifsstudio.botjs.attachment.Attachments
import com.sifsstudio.botjs.block.Blocks
import com.sifsstudio.botjs.blockentity.BlockEntities
import com.sifsstudio.botjs.entity.Entities
import com.sifsstudio.botjs.inventory.MenuTypes
import com.sifsstudio.botjs.item.Items
import com.sifsstudio.botjs.runtime.BotRuntime
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier

@Mod(BotJS.ID)
object BotJS {
    const val ID = "botjs"
    private val CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID)
    private val BOTJS_CREATIVE_TAB = CREATIVE_TABS.register("botjs", Supplier {
        CreativeModeTab.builder()
            .title(Component.translatable("item_group.$ID"))
            .icon { ItemStack(Items.WRENCH) }
            .displayItems { _, output ->
                Items.REGISTRY.entries.forEach { item ->
                    output.accept(item.get())
                }
//                Blocks.REGISTRY.entries.forEach { block ->
//                    output.accept(block.get())
//                }
            }
            .build()
    })

    init {
        CREATIVE_TABS.register(MOD_BUS)
        Items.REGISTRY.register(MOD_BUS)
        Blocks.REGISTRY.register(MOD_BUS)
        Entities.REGISTRY.register(MOD_BUS)
        BlockEntities.REGISTRY.register(MOD_BUS)
        MenuTypes.REGISTRY.register(MOD_BUS)
        Attachments.REGISTRY.register(MOD_BUS)
        FORGE_BUS.addListener(BotRuntime.Companion::onServerStarting)
    }
}