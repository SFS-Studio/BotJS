package com.sifsstudio.botjs.entity

import com.sifsstudio.botjs.BotJS
import net.minecraft.world.entity.Mob
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent

@Suppress("unused")
@EventBusSubscriber(modid = BotJS.ID, bus = EventBusSubscriber.Bus.MOD)
object EntityAttributeRegistry {
    @SubscribeEvent
    fun attributeCreation(event: EntityAttributeCreationEvent) {
        event.put(Entities.BOT, Mob.createMobAttributes().build())
    }
}