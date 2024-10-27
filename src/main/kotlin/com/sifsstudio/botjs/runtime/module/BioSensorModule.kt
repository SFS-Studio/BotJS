package com.sifsstudio.botjs.runtime.module

import com.sifsstudio.botjs.entity.BotEntity
import com.sifsstudio.botjs.runtime.DUMMY_REGISTER
import com.sifsstudio.botjs.runtime.IntRegister
import com.sifsstudio.botjs.runtime.Register
import net.minecraft.world.phys.AABB
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.minus

class BioSensorModule : BotModule {
    companion object {
        const val SCAN_INTERVAL = 3
        const val SCAN_RANGE = 5.0
    }

    private val sensorCtrl = IntRegister(0, Register.RWFlag.Write)
    private val sensorSt = IntRegister(0, Register.RWFlag.Read)
    private val sensorRsltTyp: Array<Register> = Array(3) {
        IntRegister(-1, Register.RWFlag.Read)
    }
    private val sensorRsltPos: Array<Register> = Array(9) {
        IntRegister(0, Register.RWFlag.Read)
    }

    private var scanCountdown = -1

    override fun register(address: Int) =
        when (address) {
            0 -> sensorCtrl
            1 -> sensorSt
            in 2..4 -> sensorRsltTyp[address - 2]
            in 5..13 -> sensorRsltPos[address - 5]
            else -> DUMMY_REGISTER
        }

    override fun tick(bot: BotEntity) {
        if (scanCountdown > 0) {
            scanCountdown -= 1
        } else if (scanCountdown == 0) {
            sensorRsltPos.forEach {
                it.reset()
            }
            sensorRsltTyp.forEach {
                it.reset()
            }
            bot.level().getEntities(bot, AABB.ofSize(bot.position(), SCAN_RANGE, SCAN_RANGE, SCAN_RANGE)).take(3)
                .shuffled().forEachIndexed { index, entity ->
                    sensorRsltTyp[index].content = 1
                    val rotation = bot.rotationVector
                    val offset = (entity.position() - bot.position()).xRot(rotation.x).yRot(rotation.y)
                    sensorRsltPos[index * 3].content = offset.x.toFloat().toRawBits()
                    sensorRsltPos[index * 3 + 1].content = offset.y.toFloat().toRawBits()
                    sensorRsltPos[index * 3 + 2].content = offset.z.toFloat().toRawBits()
                }
            sensorSt.content = sensorSt.content and 0x1.inv()
        } else if (sensorCtrl.content and 1 == 1) {
            scanCountdown = SCAN_INTERVAL
            sensorSt.content = sensorSt.content or 1
        }
        sensorCtrl.content = sensorCtrl.content and 1.inv()
    }
}