package com.sifsstudio.botjs.data

import com.sifsstudio.botjs.BotJS
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

@Suppress("unused")
@EventBusSubscriber(modid = BotJS.ID, bus = EventBusSubscriber.Bus.MOD)
object ModData {
    @SubscribeEvent
    fun gatherData(event: GatherDataEvent.Client) {
        val gen = event.generator

        gen.addProvider(true, ::ModModels)
        gen.addProvider(true, ::ModLangEn)
    }
}