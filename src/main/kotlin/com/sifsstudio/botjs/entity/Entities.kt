package com.sifsstudio.botjs.entity

import com.sifsstudio.botjs.BotJS
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object Entities {
    val REGISTRY: DeferredRegister.Entities = DeferredRegister.createEntities(BotJS.ID)

    val BOT: EntityType<BotEntity> by REGISTRY.registerEntityType("bot", ::BotEntity, MobCategory.MISC)
}