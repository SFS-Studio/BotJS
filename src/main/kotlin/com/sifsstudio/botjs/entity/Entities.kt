package com.sifsstudio.botjs.entity

import com.sifsstudio.botjs.BotJS
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object Entities {
    val REGISTRY: DeferredRegister<EntityType<*>> = DeferredRegister.create(Registries.ENTITY_TYPE, BotJS.ID)

    val BOT: EntityType<BotEntity> by REGISTRY.register("bot", Supplier {
        EntityType.Builder.of(::BotEntity, MobCategory.MISC).build("bot")
    })

}