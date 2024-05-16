package com.sifsstudio.botjs.inventory

import com.sifsstudio.botjs.BotJS
import net.minecraft.core.registries.Registries
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.inventory.MenuType
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object MenuTypes {
    val REGISTRY: DeferredRegister<MenuType<*>> = DeferredRegister.create(Registries.MENU, BotJS.ID)

    val BOT_MENU: MenuType<BotMountMenu> by REGISTRY.register("bot_mount_menu", Supplier {
        MenuType(::BotMountMenu, FeatureFlagSet.of())
    })
    val FIRMWARE_PROGRAMMER: MenuType<FirmwareProgrammerMenu> by REGISTRY.register(
        "firmware_programmer_menu",
        Supplier {
            IMenuTypeExtension.create(::FirmwareProgrammerMenu)
        })
}