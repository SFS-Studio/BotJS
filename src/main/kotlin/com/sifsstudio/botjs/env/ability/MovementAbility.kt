package com.sifsstudio.botjs.env.ability

import com.sifsstudio.botjs.env.task.TaskBase
import com.sifsstudio.botjs.env.task.TaskFuture
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

class MovementAbility : AbilityBase() {

    override val id = "movement"

    @Suppress("unused")
    fun moveTo(x: Double, z: Double): MoveResult { return env.pending(MovementTask(x, z)).joinOrThrow() }

    @Suppress("unused")
    fun moveToAsync(x: Double, z: Double): com.sifsstudio.botjs.env.api.FutureHandleImpl<MoveResult> { return com.sifsstudio.botjs.env.api.FutureHandleImpl(env.pending(MovementTask(x, z)))
    }

    @Suppress("unused")
    fun move(direction: Direction, distance: Double): MoveResult {
        check(distance >= 0)
        return if(distance > 1E-7) env.pending(DirectionalMovementTask(direction, distance)).join().getOrThrow()
                else successResult
    }

    @Suppress("unused")
    fun moveAsync(direction: Direction, distance: Double): com.sifsstudio.botjs.env.api.FutureHandleImpl<MoveResult> {
        check(distance >= 0)
        return com.sifsstudio.botjs.env.api.FutureHandleImpl(
            if(distance > 1E-7) env.pending(DirectionalMovementTask(direction, distance))
            else TaskFuture.successFuture(successResult)
        )
    }

    companion object {

        const val moveSpeed = 0.1
        const val moveSpeedSq = moveSpeed * moveSpeed
        val successResult = MoveResult(true)

        class MovementTask(private val endX: Double, private val endZ: Double): TaskBase<MoveResult>() {
            override fun tick() {
                val normal = Vec3(endX - env.entity.x, 0.0, endZ - env.entity.z).normalize()
                val distanceSq = env.entity.distanceToSqr(endX, env.entity.y, endZ)
                val movementSq = if (distanceSq >= moveSpeedSq) {
                    env.entity.deltaMovement = normal.scale(moveSpeed)
                    moveSpeedSq
                } else {
                    env.entity.deltaMovement = normal.scale(kotlin.math.sqrt(distanceSq))
                    distanceSq
                }
                if(distanceSq - movementSq < 1E-14) {
                    done(successResult)
                }
            }
        }

        class DirectionalMovementTask(direction: Direction, private var distance: Double): TaskBase<MoveResult>() {
            private val normal = Vec3(direction.stepX.toDouble(), direction.stepY.toDouble(), direction.stepZ.toDouble())
            override fun tick() {
                val movement = distance.coerceAtMost(moveSpeed)
                env.entity.deltaMovement = normal.scale(movement)
                distance -= movement
                if(distance < 1E-7) {
                    done(successResult)
                }
            }
        }

        data class MoveResult(val success: Boolean, val remaining: Double = 0.0)
    }
}
