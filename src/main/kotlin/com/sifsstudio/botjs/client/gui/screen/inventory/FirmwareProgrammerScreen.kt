package com.sifsstudio.botjs.client.gui.screen.inventory

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.blockentity.FirmwareProgrammerBlockEntity
import com.sifsstudio.botjs.client.gui.screen.widget.ScriptEditBox
import com.sifsstudio.botjs.inventory.FirmwareProgrammerMenu
import com.sifsstudio.botjs.item.McuItem
import com.sifsstudio.botjs.network.FirmwareProgrammerAction
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.neoforge.network.PacketDistributor
import org.lwjgl.glfw.GLFW

@OnlyIn(Dist.CLIENT)
class FirmwareProgrammerScreen(
    pMenu: FirmwareProgrammerMenu,
    pPlayerInventory: Inventory, pTitle: Component
) : AbstractContainerScreen<FirmwareProgrammerMenu>(pMenu, pPlayerInventory, pTitle) {
    private lateinit var scriptEdit: ScriptEditBox
    private lateinit var flashButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    var flashResult: Component? = null

    init {
        imageWidth = 512
        imageHeight = 316
        inventoryLabelX = 331
        inventoryLabelY = imageHeight - 94
    }

    companion object {
        private val TEXTURE = ResourceLocation(BotJS.ID, "textures/gui/firmware_programmer.png")
    }

    private fun calculateGuiScale(pGuiScale: Int, pForceUnicode: Boolean): Int {
        var i = 1
        val window = minecraft!!.window
        while (i != pGuiScale && i < window.width
            && i < window.height && window.width / (i + 1) >= 640 && window.height / (i + 1) >= 480
        ) {
            i++
        }
        if (pForceUnicode && i % 2 != 0) {
            i++
        }
        return i
    }

    private fun findBlockEntity(): FirmwareProgrammerBlockEntity? {
        val mc = minecraft!!
        val be = mc.level!!.getBlockEntity(menu.flasherPos) as? FirmwareProgrammerBlockEntity
        return be
    }

    override fun keyPressed(pKeyCode: Int, pScanCode: Int, pModifiers: Int): Boolean {
        if (pKeyCode == GLFW.GLFW_KEY_E) {
            if (scriptEdit.charTyped('e', pModifiers)) {
                return true
            }
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers)
    }

    @Suppress("MoveLambdaOutsideParentheses")
    override fun init() {
        val minecraft = minecraft!!
        minecraft.window.guiScale =
            calculateGuiScale(minecraft.options.guiScale().get(), minecraft.isEnforceUnicode).toDouble()
        width = minecraft.window.guiScaledWidth
        height = minecraft.window.guiScaledHeight
        super.init()

        val be = findBlockEntity()
        if(be == null) {
            //TODO: Maybe I shouldn't close it right now
            minecraft.setScreen(null)
            return
        }
        be.outputHandler = { flashResult = it }

        saveButton = addRenderableWidget(ImageButton(
            leftPos + 320, topPos + 119, 20, 20,
            WidgetSprites(
                ResourceLocation(BotJS.ID, "widget/save_button"),
                ResourceLocation(BotJS.ID, "widget/save_button_disabled"),
                ResourceLocation(BotJS.ID, "widget/save_button_highlighted")
            ),
            {
                saveScript(false)
            },
        ))
        saveButton.active = false

        scriptEdit = addRenderableWidget(
            ScriptEditBox(
                font,
                leftPos + 11,
                topPos + 19,
                295,
                289,
                Component.translatable("menu.botjs.firmware_programmer.script_here"),
                Component.translatable("gui.abuseReport.comments")
            )
        ).apply { setValueListener { saveButton.active = true } }
        scriptEdit.value = menu.script

        cancelButton = addRenderableWidget(ImageButton(
            leftPos + 493, topPos + 5, 14, 14,
            WidgetSprites(
                ResourceLocation("widget/cross_button"),
                ResourceLocation("widget/cross_button_highlighted")
            ),
            {
                minecraft.player?.closeContainer()
            },
        ))

        flashButton =
            addRenderableWidget(Button.builder(
                Component.translatable("menu.botjs.firmware_programmer.flash"),
                {
                    saveScript(true)
                    flashButton.isFocused = false
                },
            ).pos(leftPos + 381, topPos + 175).size(35, 17).build())
    }

    override fun removed() {
        val minecraft = minecraft!!
        minecraft.window.guiScale =
            minecraft.window.calculateScale(minecraft.options.guiScale().get(), minecraft.isEnforceUnicode).toDouble()
        super.removed()
        findBlockEntity()?.outputHandler = null
    }

    override fun containerTick() {
        super.containerTick()
        flashResult?.let { res ->
            minecraft?.let {
                it.toasts.addToast(
                    SystemToast.multiline(
                        it,
                        SystemToast.SystemToastId(5000),
                        Component.translatable("toast.botjs.flash_failed"),
                        res
                    )
                )
            }
            this.flashResult = null
        }
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
        renderTooltip(pGuiGraphics, pMouseX, pMouseY)
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {
        pGuiGraphics.blit(TEXTURE, leftPos, topPos, 0.0f, 0.0f, imageWidth, imageHeight, imageWidth, imageHeight)
    }

    override fun renderLabels(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int) {
        super.renderLabels(pGuiGraphics, pMouseX, pMouseY)
        if (!menu.getSlot(0).hasItem()) {
            return
        }
        val flashResult = flashResult
        if (flashResult != null) {
            pGuiGraphics.drawWordWrap(font, flashResult, 331, 17, 162, 0xFFFF0000.toInt())
            return
        }
        (menu.getSlot(0).item.item as? McuItem)?.run {
            pGuiGraphics.drawWordWrap(
                font,
                FormattedText.of("MCU: $chipCode\nPIN: $pins\nCOM: $serials"),
                331,
                17,
                78,
                4210752
            )
            pGuiGraphics.drawWordWrap(
                font,
                FormattedText.of("DESCRIPTION: $description"),
                331 + 81,
                17,
                81,
                4210752
            )
        }
    }

    private fun saveScript(flash: Boolean) {
        PacketDistributor.sendToServer(FirmwareProgrammerAction(menu.flasherPos, scriptEdit.value, flash))
        saveButton.active = false
    }
}