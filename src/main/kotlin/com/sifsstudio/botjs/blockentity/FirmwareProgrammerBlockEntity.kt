package com.sifsstudio.botjs.blockentity

import com.sifsstudio.botjs.item.McuItem
import com.sifsstudio.botjs.runtime.threading.tryCompile
import com.sifsstudio.botjs.util.set
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler
import org.mozilla.javascript.EvaluatorException
import org.openjdk.nashorn.internal.runtime.ParserException

class FirmwareProgrammerBlockEntity(pPos: BlockPos, pBlockState: BlockState) :
    BaseBlockEntity(BlockEntities.FIRMWARE_PROGRAMMER, pPos, pBlockState) {

    val mcuIn = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack) = stack.item is McuItem
    }
    val mcuOut = ItemStackHandler(1)
    var script = ""

    private var output: Component = Component.empty()
    private var flashInProgress = false

    //This is only useful on client side, and it's never useful on server side
    //You should not be referencing it on server side
    var outputHandler: ((Component) -> Unit)? = null

    override fun saveAdditional(pTag: CompoundTag, pRegistries: HolderLookup.Provider) {
        super.saveAdditional(pTag, pRegistries)
        pTag.putString("script", script)
        pTag.put("mcuIn", mcuIn.serializeNBT(pRegistries))
        pTag.put("mcuOut", mcuOut.serializeNBT(pRegistries))
    }

    override fun loadAdditional(pTag: CompoundTag, pRegistries: HolderLookup.Provider) {
        super.loadAdditional(pTag, pRegistries)
        script = pTag.getString("script")
        mcuIn.deserializeNBT(pRegistries, pTag.getCompound("mcuIn"))
        mcuOut.deserializeNBT(pRegistries, pTag.getCompound("mcuOut"))
    }

    override val sync = true

    override fun getUpdateTag(pRegistries: HolderLookup.Provider) = CompoundTag().apply {
        this["script"] = script
        this["output"] = Component.Serializer.toJson(output, pRegistries)
    }

    override fun handleUpdateTag(tag: CompoundTag, lookupProvider: HolderLookup.Provider) {
        script = tag.getString("script")
        //TODO: "Unexpected error" content
        output = Component.Serializer.fromJson(tag.getString("output"), lookupProvider)
            ?: Component.literal("[Unexpected error]")
        outputHandler?.invoke(output)
    }

    fun flash() {
        output = when {
            mcuIn.getStackInSlot(0) == ItemStack.EMPTY ->
                Component.translatable("menu.botjs.firmware_programmer.flash_result.nothing_to_flash")

            mcuOut.getStackInSlot(0) != ItemStack.EMPTY ->
                Component.translatable("menu.botjs.firmware_programmer.flash_result.output_occupied")

            flashInProgress ->
                Component.translatable("menu.botjs.firmware_programmer.flash_result.already_in_progress")

            else -> {
                flashInProgress = true
                tryCompile(script) {
                    onFailure {
                        val errMsg = when (it) {
                            is ParserException -> Component.literal(it.message ?: "[Unknown Compile Error]")
                            is EvaluatorException -> Component.literal(it.message ?: "[Unknown Compile Error]")
                            else -> Component.literal("Unexpected error: $it")
                        }
                        output = Component.translatable("menu.botjs.firmware_programmer.flash_result.compile_error", errMsg)
                        setChangedAndSync()
                    }
                    onSuccess {
                        val mcu = mcuIn.extractItem(0, 1, false)
                        mcu.set(McuItem.SCRIPT_COMPONENT, script)
                        mcuOut.setStackInSlot(0, mcu)
                        output = Component.translatable("menu.botjs.firmware_programmer.flash_result.success")
                        setChangedAndSync()
                    }
                }
                Component.translatable("menu.botjs.firmware_programmer.flash_result.flashing")
            }
        }
        setChangedAndSync()
    }

}