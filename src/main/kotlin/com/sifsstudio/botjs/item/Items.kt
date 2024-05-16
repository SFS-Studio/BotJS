package com.sifsstudio.botjs.item

import com.mojang.datafixers.util.Either
import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.block.Blocks
import com.sifsstudio.botjs.runtime.module.BioSensorModule
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderTooltipEvent
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

@EventBusSubscriber(modid = BotJS.ID, bus = EventBusSubscriber.Bus.GAME)
object Items {
    val REGISTRY: DeferredRegister<Item> = DeferredRegister.create(Registries.ITEM, BotJS.ID)

    val BIO_SENSOR_MODULE: BotModuleItem
            by REGISTRY.register("bio_sensor_module", Supplier {
                BotModuleItem.of(4u, "BS101", ::BioSensorModule)
            })

    val BASIC_MCU: McuItem
            by REGISTRY.register("basic_mcu", Supplier {
                McuItem.of(4u, 2u, "MC8F01", "Kickstart processor") {
                    when (it.toUInt()) {
                        0u -> PinFunction.COM(0u)
                        1u -> PinFunction.COM(1u)
                        2u -> PinFunction.GPIO
                        3u -> PinFunction.GPIO
                        else -> throw IllegalStateException()
                    }
                }
            })

    val FIRMWARE_PROGRAMMER: Item
            by REGISTRY.register("firmware_programmer", Supplier {
                BlockItem(Blocks.FIRMWARE_PROGRAMMER, Item.Properties())
            })

    val WRENCH: Item
            by REGISTRY.register("wrench", Supplier {
                Item(Item.Properties().stacksTo(1))
            })

    val LOG_DOWNLOADER: Item
            by REGISTRY.register("log_downloader", Supplier {
                Item(Item.Properties().stacksTo(1))
            })

    val SWITCH: Item
            by REGISTRY.register("switch", Supplier {
                Item(Item.Properties().stacksTo(1))
            })

    @SubscribeEvent
    fun renderTooltips(event: RenderTooltipEvent.GatherComponents) {
        val item = event.itemStack.item
        val stack = event.itemStack
        if (item is McuItem) {
            val script = stack.get(McuItem.SCRIPT_COMPONENT) ?: ""
            event.tooltipElements.add(
                Either.left(
                    FormattedText.of(
                        item.chipCode,
                        Style.EMPTY.withBold(true)
                    )
                )
            )
            event.tooltipElements.add(
                Either.left(
                    FormattedText.of(
                        "COMs: ${item.serials} | Pins: ${item.pins}",
                        Style.EMPTY
                    )
                )
            )
            event.tooltipElements.add(
                Either.left(
                    FormattedText.of(
                        "Script: ${script.length}",
                        Style.EMPTY.withItalic(true)
                    )
                )
            )
        }
    }
}