package com.sifsstudio.botjs.attachment

import com.mojang.serialization.Codec
import com.sifsstudio.botjs.BotJS
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object Attachments {
    val REGISTRY: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, BotJS.ID)

    val MCU_SCRIPT: AttachmentType<String> by REGISTRY.register("mcu_script", Supplier {
        AttachmentType.builder(Supplier { "" }).serialize(Codec.STRING).build()
    })
}