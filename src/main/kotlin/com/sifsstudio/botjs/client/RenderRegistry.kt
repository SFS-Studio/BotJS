package com.sifsstudio.botjs.client

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.client.gui.screen.inventory.BotMountScreen
import com.sifsstudio.botjs.client.gui.screen.inventory.FirmwareProgrammerScreen
import com.sifsstudio.botjs.client.model.geom.BotJSModelLayers
import com.sifsstudio.botjs.client.renderer.entity.BotEntityRenderer
import com.sifsstudio.botjs.entity.Entities
import com.sifsstudio.botjs.inventory.MenuTypes
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

@Suppress("unused")
@EventBusSubscriber(modid = BotJS.ID, bus = EventBusSubscriber.Bus.MOD)
object RenderRegistry {
    @SubscribeEvent
    fun registerLayerDefinitions(event: EntityRenderersEvent.RegisterLayerDefinitions) {
        event.registerLayerDefinition(BotJSModelLayers.BOT) {
            LayerDefinition.create(
                HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f),
                64,
                64
            )
        }
    }

    @SubscribeEvent
    fun registerRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(Entities.BOT, ::BotEntityRenderer)
    }

    @SubscribeEvent
    fun registerMenuScreens(event: RegisterMenuScreensEvent) {
        event.register(MenuTypes.BOT_MENU, ::BotMountScreen)
        event.register(MenuTypes.FIRMWARE_PROGRAMMER, ::FirmwareProgrammerScreen)
    }
}