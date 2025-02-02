package com.sifsstudio.botjs.item

import com.mojang.datafixers.util.Either
import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.block.Blocks
import com.sifsstudio.botjs.runtime.module.BioSensorModule
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderTooltipEvent
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

@EventBusSubscriber(modid = BotJS.ID, bus = EventBusSubscriber.Bus.GAME)
object Items {
    val REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(BotJS.ID)

    val BIO_SENSOR_MODULE: BotModuleItem
            by REGISTRY.registerItem("bio_sensor_module", {
                BotModuleItem.of(it, 4u, "BS101", ::BioSensorModule)
            }, Properties().stacksTo(1))

    val BASIC_MCU: McuItem
            by REGISTRY.registerItem("basic_mcu", {
                McuItem.of(it, 4u, 2u, "MC8F01", "Kickstart processor") {
                    when (it.toUInt()) {
                        0u -> PinFunction.COM(0u)
                        1u -> PinFunction.COM(1u)
                        2u -> PinFunction.GPIO
                        3u -> PinFunction.GPIO
                        else -> throw IllegalStateException()
                    }
                }
            }, Properties().stacksTo(1))

    val FIRMWARE_PROGRAMMER: Item
            by REGISTRY.registerItem(
                "firmware_programmer",
                {
                    BlockItem(Blocks.FIRMWARE_PROGRAMMER, it)
                },
                Item.Properties()
            )

    val WRENCH: Item
            by REGISTRY.registerItem("wrench", ::Item, Properties().stacksTo(1))

    val LOG_DOWNLOADER: Item
            by REGISTRY.registerItem("log_downloader", ::Item, Properties().stacksTo(1))

    val SWITCH: Item
            by REGISTRY.registerItem("switch", ::Item, Properties().stacksTo(1))

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