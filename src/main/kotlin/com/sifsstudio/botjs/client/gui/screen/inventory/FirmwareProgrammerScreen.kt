package com.sifsstudio.botjs.client.gui.screen.inventory

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.blockentity.FirmwareProgrammerBlockEntity
import com.sifsstudio.botjs.client.gui.screen.widget.ScriptEditBox
import com.sifsstudio.botjs.inventory.FirmwareProgrammerMenu
import com.sifsstudio.botjs.item.component.DataComponents
import com.sifsstudio.botjs.network.FlashMCU
import com.sifsstudio.botjs.network.SyncScript
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
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

    init {
        imageWidth = 512
        imageHeight = 316
        inventoryLabelX = 331
        inventoryLabelY = imageHeight - 94
    }

    companion object {
        private val TEXTURE = ResourceLocation.fromNamespaceAndPath(BotJS.ID, "textures/gui/firmware_programmer.png")
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

    override fun keyPressed(pKeyCode: Int, pScanCode: Int, pModifiers: Int): Boolean {
        if (pKeyCode == GLFW.GLFW_KEY_E) {
            if (scriptEdit.canTypeChar('e')) {
                return true
            }
        } else if (pKeyCode == GLFW.GLFW_KEY_S && pModifiers.or(GLFW.GLFW_MOD_CONTROL) == GLFW.GLFW_MOD_CONTROL) {
            syncScript()
            return true
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

        val be : FirmwareProgrammerBlockEntity? = menu.containerLevelAccess.evaluate { level, pos ->
            level.getBlockEntity(pos)
        }.orElse(null) as FirmwareProgrammerBlockEntity?

        be ?: return minecraft.setScreen(null)

        saveButton = addRenderableWidget(ImageButton(
            leftPos + 320, topPos + 119, 20, 20,
            WidgetSprites(
                ResourceLocation.fromNamespaceAndPath(BotJS.ID, "widget/save_button"),
                ResourceLocation.fromNamespaceAndPath(BotJS.ID, "widget/save_button_disabled"),
                ResourceLocation.fromNamespaceAndPath(BotJS.ID, "widget/save_button_highlighted")
            ),
            {
                syncScript()
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
        scriptEdit.value = be.script.getStackInSlot(0).get(DataComponents.SCRIPT) ?: ""

        flashButton = addRenderableWidget(ImageButton(
            leftPos + 391, topPos + 161, 11, 7,
            WidgetSprites(
                ResourceLocation.fromNamespaceAndPath(BotJS.ID, "widget/pcb_button"),
                ResourceLocation.fromNamespaceAndPath(BotJS.ID, "widget/pcb_button_disabled"),
                ResourceLocation.fromNamespaceAndPath(BotJS.ID, "widget/pcb_button_highlighted")
            ),
            {
                // save before flash
                syncScript()
                PacketDistributor.sendToServer(FlashMCU(menu.session, menu.pos))
                flashButton.isFocused = false
            },
        ))
    }

    override fun removed() {
        val minecraft = minecraft!!
        minecraft.window.guiScale =
            minecraft.window.calculateScale(minecraft.options.guiScale().get(), minecraft.isEnforceUnicode).toDouble()
        super.removed()
        // commented because one may discard changes by intentionally closing the interface
//        // ensure script synced
//        PacketDistributor.sendToServer(
//            SyncScript(menu.session, scriptEdit.value, menu.pos)
//        )
    }

    override fun containerTick() {
        super.containerTick()
        // two things to do: check synced and sync if not
        val minecraft = minecraft!!
        val be : FirmwareProgrammerBlockEntity? = menu.containerLevelAccess.evaluate { level, pos ->
            level.getBlockEntity(pos)
        }.orElse(null) as FirmwareProgrammerBlockEntity?
        be ?: return minecraft.setScreen(null)
        saveButton.active =
            be.script.getStackInSlot(0) != ItemStack.EMPTY
                    && be.script.getStackInSlot(0).get(DataComponents.SCRIPT) != scriptEdit.value
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
        renderTooltip(pGuiGraphics, pMouseX, pMouseY)
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {
        pGuiGraphics.blit(RenderType.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0f, 0.0f, imageWidth, imageHeight, imageWidth, imageHeight)
    }

    override fun renderLabels(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int) {
        // omit super call, no inventory tip
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false)
        val minecraft = minecraft!!
        val be : FirmwareProgrammerBlockEntity? = menu.containerLevelAccess.evaluate { level, pos ->
            level.getBlockEntity(pos)
        }.orElse(null) as FirmwareProgrammerBlockEntity?
        be ?: return minecraft.setScreen(null)
        val compileResult = be.compileResult
        pGuiGraphics.drawWordWrap(font, compileResult, 331, 17, 162, 0xFFFF0000.toInt())
    }

    private fun syncScript() {
        PacketDistributor.sendToServer(
            SyncScript(menu.session, scriptEdit.value, menu.pos)
        )
    }
}