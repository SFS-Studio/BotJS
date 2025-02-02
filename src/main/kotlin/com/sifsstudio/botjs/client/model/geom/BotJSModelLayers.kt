package com.sifsstudio.botjs.client.model.geom

import com.sifsstudio.botjs.BotJS
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.resources.ResourceLocation

object BotJSModelLayers {
    val BOT = ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(BotJS.ID, "bot"), "main")
}