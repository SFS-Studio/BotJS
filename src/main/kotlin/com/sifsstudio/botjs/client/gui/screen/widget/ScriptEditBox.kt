package com.sifsstudio.botjs.client.gui.screen.widget

import net.minecraft.Util
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractTextAreaWidget
import net.minecraft.client.gui.components.MultilineTextField
import net.minecraft.client.gui.components.Whence
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.util.StringUtil
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import org.lwjgl.glfw.GLFW
import kotlin.math.max

@OnlyIn(Dist.CLIENT)
class ScriptEditBox(
    private val font: Font,
    pX: Int,
    pY: Int,
    pWidth: Int,
    pHeight: Int,
    private val placeholder: Component,
    pMessage: Component
) : AbstractTextAreaWidget(pX, pY, pWidth, pHeight, pMessage) {
    private var textField: MultilineTextField = MultilineTextField(font, pWidth - totalInnerPadding())
    private var focusedTime = Util.getMillis()
    var value: String
        get() = textField.value()
        set(value) = textField.setValue(value)
    private var cachedLineCount = 0
    private var cachedValueListener: (String) -> Unit = { }
    private var lineNumberWidth = font.width("1") + 5
    private val displayableLineCount
        get() = (this.height - this.totalInnerPadding()).toDouble() / font.lineHeight.toDouble()

    companion object {
        private const val CURSOR_INSERT_WIDTH = 1
        private const val CURSOR_INSERT_COLOR = -3092272
        private const val CURSOR_APPEND_CHARACTER = "_"
        private const val TEXT_COLOR = -2039584
        private const val PLACEHOLDER_TEXT_COLOR = -857677600
        private const val CURSOR_BLINK_INTERVAL_MS = 300
    }

    init {
        textField.setCursorListener(::scrollToCursor)
    }

    private fun scrollToCursor() {
        var d0 = this.scrollAmount()
        val topLine = textField.getLineView((d0 / 9.0).toInt())
        if (textField.cursor() <= topLine.beginIndex()) {
            d0 = (textField.lineAtCursor * font.lineHeight).toDouble()
        } else {
            val bottomLine = textField.getLineView(((d0 + height.toDouble()) / 9.0).toInt() - 1)
            if (textField.cursor() > bottomLine.endIndex()) {
                d0 =
                    (textField.lineAtCursor * font.lineHeight - this.height + font.lineHeight + this.totalInnerPadding()).toDouble()
            }
        }
        this.setScrollAmount(d0)
    }

    fun setValueListener(listener: (String) -> Unit) {
        textField.setValueListener(listener)
        cachedValueListener = listener
    }

    override fun setFocused(pFocused: Boolean) {
        super.setFocused(pFocused)
        if (pFocused) {
            focusedTime = Util.getMillis()
        }
    }

    override fun updateWidgetNarration(pNarrationElementOutput: NarrationElementOutput) {
        pNarrationElementOutput.add(
            NarratedElementType.TITLE, Component.translatable(
                "gui.narrate.editBox",
                message,
                value
            )
        )
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (this.withinContentAreaPoint(pMouseX, pMouseY) && pButton == GLFW.GLFW_MOUSE_BUTTON_1) {
            textField.setSelecting(Screen.hasShiftDown())
            seekCursorScreen(pMouseX, pMouseY)
            return true
        } else {
            return super.mouseClicked(pMouseX, pMouseY, pButton)
        }
    }

    override fun mouseDragged(pMouseX: Double, pMouseY: Double, pButton: Int, pDragX: Double, pDragY: Double): Boolean {
        if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
            return true
        } else if (this.withinContentAreaPoint(pMouseX, pMouseY) && pButton == GLFW.GLFW_MOUSE_BUTTON_1) {
            textField.setSelecting(true)
            seekCursorScreen(pMouseX, pMouseY)
            textField.setSelecting(Screen.hasShiftDown())
            return true
        } else {
            return false
        }
    }

    override fun keyPressed(pKeyCode: Int, pScanCode: Int, pModifiers: Int): Boolean {
        val ret = textField.keyPressed(pKeyCode)
        if (textField.lineCount != cachedLineCount) {
            cachedLineCount = textField.lineCount
            lineNumberWidth = if (cachedLineCount == 0) {
                font.width("1") + 5
            } else {
                font.width(cachedLineCount.toString()) + 5
            }
            val oldTextField = textField
            textField = MultilineTextField(font, width - totalInnerPadding() - lineNumberWidth)
            textField.setValue(oldTextField.value())
            textField.setValueListener(cachedValueListener)
            textField.seekCursor(Whence.ABSOLUTE, oldTextField.cursor())
            textField.setCursorListener(::scrollToCursor)
        }
        return ret || pKeyCode == GLFW.GLFW_KEY_E
    }

    override fun charTyped(pCodePoint: Char, pModifiers: Int): Boolean {
        if (visible && isFocused && StringUtil.isAllowedChatCharacter(pCodePoint)) {
            textField.insertText(pCodePoint.toString())
            if (textField.lineCount != cachedLineCount) {
                cachedLineCount = textField.lineCount
                lineNumberWidth = if (cachedLineCount == 0) {
                    font.width("1") + 5
                } else {
                    font.width(cachedLineCount.toString()) + 5
                }
                val oldTextField = textField
                textField = MultilineTextField(font, width - totalInnerPadding() - lineNumberWidth)
                textField.setValue(oldTextField.value())
                textField.setValueListener(cachedValueListener)
                textField.seekCursor(Whence.ABSOLUTE, oldTextField.cursor())
                textField.setCursorListener(::scrollToCursor)
            }
            return true
        } else {
            return false
        }
    }

    override fun scrollbarVisible() = textField.lineCount > displayableLineCount

    override fun getInnerHeight() = font.lineHeight * textField.lineCount

    override fun scrollRate() = font.lineHeight.toDouble() / 2.0

    override fun renderBackground(pGuiGraphics: GuiGraphics) {}

    override fun renderContents(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        val s = textField.value()
        if (s.isEmpty() && !isFocused) {
            pGuiGraphics.drawWordWrap(
                font,
                placeholder,
                x + innerPadding(),
                y + innerPadding(),
                width - totalInnerPadding(),
                PLACEHOLDER_TEXT_COLOR
            )
        } else {
            val i = textField.cursor()
            val showCursor =
                isFocused && (Util.getMillis() - focusedTime) / CURSOR_BLINK_INTERVAL_MS % 2 == 0L
            val showLineCursor = i < s.length
            var j = 0
            var k = 0
            var l = y + this.innerPadding()
            var realLineNo = 1
            var lineNoUpdated = true

            for (stringView in textField.iterateLines()) {
                val inDisplayRange = this.withinContentAreaTopBottom(l, l + font.lineHeight)
                if (lineNoUpdated) {
                    pGuiGraphics.drawString(font, realLineNo.toString(), x + innerPadding(), l, TEXT_COLOR)
                    lineNoUpdated = false
                }
                if (stringView.endIndex < s.length && s[stringView.endIndex] == '\n') {
                    realLineNo++
                    lineNoUpdated = true
                }
                if (showCursor && showLineCursor && i >= stringView.beginIndex && i <= stringView.endIndex) {
                    if (inDisplayRange) {
                        j = (pGuiGraphics.drawString(
                            font, s.substring(stringView.beginIndex, i),
                            x + this.innerPadding() + lineNumberWidth, l, TEXT_COLOR
                        ) - CURSOR_INSERT_WIDTH)
                        pGuiGraphics.fill(
                            j,
                            l - 1,
                            j + CURSOR_INSERT_WIDTH,
                            l + 1 + font.lineHeight,
                            CURSOR_INSERT_COLOR
                        )
                        pGuiGraphics.drawString(
                            font,
                            s.substring(i, stringView.endIndex),
                            j,
                            l,
                            TEXT_COLOR
                        )
                    }
                } else {
                    if (inDisplayRange) {
                        j = (pGuiGraphics.drawString(
                            this.font,
                            s.substring(
                                stringView.beginIndex,
                                stringView.endIndex
                            ),
                            this.x + this.innerPadding() + lineNumberWidth,
                            l,
                            TEXT_COLOR
                        ) - CURSOR_INSERT_WIDTH)
                    }
                    k = l
                }
                l += font.lineHeight
            }
            if (showCursor && !showLineCursor && this.withinContentAreaTopBottom(k, k + font.lineHeight)) {
                pGuiGraphics.drawString(this.font, CURSOR_APPEND_CHARACTER, j, k, CURSOR_INSERT_COLOR)
            }
            pGuiGraphics.vLine(
                x + innerPadding() + lineNumberWidth - 2,
                y + innerPadding(), max(l, y + innerPadding() + font.lineHeight), TEXT_COLOR
            )

            if (textField.hasSelection()) {
                val selectedView = textField.selected
                val textX = x + innerPadding() + lineNumberWidth
                l = y + innerPadding()
                for (stringView in textField.iterateLines()) {
                    if (selectedView.beginIndex > stringView.endIndex) {
                        l += font.lineHeight
                    } else {
                        if (stringView.beginIndex > selectedView.endIndex) {
                            break
                        }
                        if (withinContentAreaTopBottom(l, l + font.lineHeight)) {
                            val selectStart = font
                                .width(
                                    s.substring(
                                        stringView.beginIndex,
                                        max(
                                            selectedView.beginIndex,
                                            stringView.beginIndex
                                        )
                                    )
                                )
                            val selectEnd =
                                if (selectedView.endIndex() > stringView.endIndex()) {
                                    width - this.innerPadding()
                                } else {
                                    font.width(
                                        s.substring(
                                            stringView.beginIndex(),
                                            selectedView.endIndex()
                                        )
                                    )
                                }

                            this.renderHighlight(
                                pGuiGraphics,
                                textX + selectStart,
                                l,
                                textX + selectEnd,
                                l + font.lineHeight
                            )
                        }
                        l += font.lineHeight
                    }
                }
            }
        }
    }

    private fun seekCursorScreen(pMouseX: Double, pMouseY: Double) {
        val d0 = pMouseX - x.toDouble() - innerPadding().toDouble() - lineNumberWidth
        val d1 = pMouseY - y.toDouble() - innerPadding().toDouble() + this.scrollAmount()
        textField.seekCursorToPoint(d0, d1)
    }

    private fun renderHighlight(pGuiGraphics: GuiGraphics, pMinX: Int, pMinY: Int, pMaxX: Int, pMaxY: Int) {
        pGuiGraphics.fill(RenderType.guiTextHighlight(), pMinX, pMinY, pMaxX, pMaxY, -16776961)
    }

    private fun withinContentAreaPoint(pMouseX: Double, pMouseY: Double)
        = pMouseX >= innerLeft && pMouseX <= x + width - innerPadding() && pMouseY >= innerTop && pMouseY <= y + height - innerPadding()
}