package com.sifsstudio.botjs.item

import com.mojang.datafixers.util.Either
import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.block.Blocks
import com.sifsstudio.botjs.item.component.DataComponents
import com.sifsstudio.botjs.runtime.module.BioSensorModule
import com.sifsstudio.botjs.util.isItem
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceKey
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
            by REGISTRY.registerItem("basic_mcu", { it ->
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
            by REGISTRY.registerSimpleBlockItem(
                "firmware_programmer",
                Blocks::FIRMWARE_PROGRAMMER,
                Properties()
            )

    val SCRIPT: Item
            by REGISTRY.register("script") { registryName ->
                Item(Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .component(DataComponents.SCRIPT, "")
                )
            }

    val WRENCH: Item
            by REGISTRY.registerItem("wrench", ::Item, Properties().stacksTo(1))

    val LOG_DOWNLOADER: Item
            by REGISTRY.registerItem("log_downloader", ::Item, Properties().stacksTo(1))

    val SWITCH: Item
            by REGISTRY.registerItem("switch", ::Item, Properties().stacksTo(1))

    @SubscribeEvent
    @Suppress("unused")
    fun renderTooltips(event: RenderTooltipEvent.GatherComponents) {
        val item = event.itemStack.item
        val stack = event.itemStack
        if (item is McuItem) {
            val firmware = stack.get(DataComponents.FIRMWARE)
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
                        "Firmware: ${firmware?.byteCode?.size ?: 0} bytes",
                        Style.EMPTY
                    )
                )
            )
        } else if (stack isItem SCRIPT) {
            val script = stack.get(DataComponents.SCRIPT) ?: ""
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