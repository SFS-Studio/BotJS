package com.sifsstudio.botjs.data

import com.sifsstudio.botjs.BotJS
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent

@Suppress("unused")
@EventBusSubscriber(modid = BotJS.ID, bus = EventBusSubscriber.Bus.MOD)
object ModData {
    @SubscribeEvent
    fun gatherData(event: GatherDataEvent) {
        val gen = event.generator

        gen.addProvider<ModItemModels>(event.includeClient()) {
            ModItemModels(it, event.existingFileHelper)
        }
        gen.addProvider(event.includeClient(), ::ModLangEn)
    }
}