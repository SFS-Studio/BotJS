package com.sifsstudio.botjs.network

import com.sifsstudio.botjs.blockentity.FirmwareProgrammerBlockEntity
import com.sifsstudio.botjs.item.McuItem
import com.sifsstudio.botjs.util.withContext
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.mozilla.javascript.EvaluatorException
import org.openjdk.nashorn.internal.runtime.ParserException

object ServerPayloadHandlers {
    fun handleFirmwareProgrammerAction(payload: FirmwareProgrammerAction, context: IPayloadContext) {
        context.enqueueWork {
            val level = context.player().level()
            (level.getBlockEntity(payload.flasherPos) as? FirmwareProgrammerBlockEntity) ?.let { be ->
                be.script = payload.script
                if (payload.flash) {
                    if (be.mcuIn.getStackInSlot(0) != ItemStack.EMPTY) {
                        if (be.mcuOut.getStackInSlot(0) == ItemStack.EMPTY) {
                            // Compile the script in advance to check compile errors.
                            withContext { ctx ->
                                ctx.compileString(be.script, "bot_script", 0, null)
                            }.fold (
                                onFailure =  {
                                    //TODO: Show error message to client
                                    @Suppress("UNUSED_VARIABLE")
                                    val errMsg = when (it) {
                                        is ParserException -> Component.literal(it.message ?: "Compile Error")
                                        is EvaluatorException -> Component.literal(it.message ?: "Compile Error")
                                        else -> Component.literal("Unexpected error: $it")
                                    }
                                    context.reply(FlashResult("menu.botjs.firmware_programmer.flash_result.compile_error"))
                                },
                                onSuccess = {
                                    val mcu = be.mcuIn.extractItem(0, 1, false)
                                    mcu.set(McuItem.SCRIPT_COMPONENT, be.script)
                                    be.mcuOut.setStackInSlot(0, mcu)
                                },
                            )
                        } else {
                            context.reply(FlashResult("menu.botjs.firmware_programmer.flash_result.output_occupied"))
                        }
                    } else {
                        context.reply(FlashResult("menu.botjs.firmware_programmer.flash_result.nothing_to_flash"))
                    }
                }
                be.setChanged()
            }
        }.exceptionally {
            context.disconnect(Component.translatable("botjs.networking.failed", it.message))
            null
        }
    }
}