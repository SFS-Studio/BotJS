package com.sifsstudio.botjs.network

import com.sifsstudio.botjs.blockentity.FirmwareProgrammerEntity
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
            level.getBlockEntity(payload.flasherPos)?.let {
                if (it is FirmwareProgrammerEntity) {
                    it.script = payload.script
                    if (payload.flash) {
                        if (it.mcuIn.getStackInSlot(0) != ItemStack.EMPTY) {
                            if (it.mcuOut.getStackInSlot(0) == ItemStack.EMPTY) {
                                var compileError: Component? = null
                                withContext { ctx ->
                                    try {
                                        ctx.compileString(it.script, "bot_script", 0, null)
                                    } catch (parseError: ParserException) {
                                        compileError = Component.literal(parseError.message ?: "Compile Error")
                                    } catch (evalError: EvaluatorException) {
                                        compileError = Component.literal(evalError.message ?: "Compile Error")
                                    } catch (th: Throwable) {
                                        th.printStackTrace()
                                    }
                                }
                                if (compileError != null) {
                                    context.reply(FlashResult("menu.botjs.firmware_programmer.flash_result.compile_error"))
                                } else {
                                    val mcu = it.mcuIn.extractItem(0, 1, false)
                                    mcu.set(McuItem.SCRIPT_COMPONENT, it.script)
                                    it.mcuOut.setStackInSlot(0, mcu)
                                }
                            } else {
                                context.reply(FlashResult("menu.botjs.firmware_programmer.flash_result.output_occupied"))
                            }
                        } else {
                            context.reply(FlashResult("menu.botjs.firmware_programmer.flash_result.nothing_to_flash"))
                        }
                    }
                    it.setChanged()
                }
            }
        }.exceptionally {
            context.disconnect(Component.translatable("botjs.networking.failed", it.message))
            null
        }
    }
}