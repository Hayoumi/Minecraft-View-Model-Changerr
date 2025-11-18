package com.viewmodel.gui.components

import net.minecraft.client.gui.DrawContext
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

object GuiPrimitives {
    fun fillRoundedRect(context: DrawContext, x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
        val safeRadius = min(radius, min(width, height) / 2f).coerceAtLeast(0f)
        val left = x
        val right = x + width
        val top = y
        val bottom = y + height

        val innerLeft = left + safeRadius
        val innerRight = right - safeRadius
        val innerTop = top + safeRadius
        val innerBottom = bottom - safeRadius

        fill(context, innerLeft, top, innerRight, bottom, color)
        fill(context, left, innerTop, right, innerBottom, color)
        smoothCorners(context, innerLeft, innerTop, innerRight, innerBottom, safeRadius, color)
    }

    fun fillCapsule(context: DrawContext, x: Float, y: Float, width: Float, height: Float, color: Int) =
        fillRoundedRect(context, x, y, width, height, height / 2f, color)

    fun drawHairline(context: DrawContext, x: Float, y: Float, width: Float, color: Int) =
        fill(context, x, y, x + width, y + 1f, color)

    private fun smoothCorners(
        context: DrawContext,
        innerLeft: Float,
        innerTop: Float,
        innerRight: Float,
        innerBottom: Float,
        radius: Float,
        color: Int
    ) {
        if (radius <= 0f) return
        val steps = max(1, (radius * 3.5f).roundToInt())
        val centerLeft = innerLeft
        val centerRight = innerRight
        val centerTop = innerTop
        val centerBottom = innerBottom

        for (step in 0..steps) {
            val t = step / steps.toFloat()
            val angle = t * (PI / 2.0)
            val offsetX = (sin(angle) * radius).toFloat()
            val offsetY = (sin(PI / 2 - angle) * radius).toFloat()

            // top-left
            fillLine(context, centerLeft - offsetX, centerTop - offsetY, centerLeft, color)
            // top-right
            fillLine(context, centerRight, centerTop - offsetY, centerRight + offsetX, color)
            // bottom-right
            fillLine(context, centerRight, centerBottom + offsetY, centerRight + offsetX, color)
            // bottom-left
            fillLine(context, centerLeft - offsetX, centerBottom + offsetY, centerLeft, color)
        }
    }

    private fun fillLine(context: DrawContext, x1: Float, y: Float, x2: Float, color: Int) {
        val left = min(x1, x2).roundToInt()
        val right = max(x1, x2).roundToInt()
        val row = y.roundToInt()
        if (right > left) {
            context.fill(left, row, right, row + 1, color)
        }
    }

    private fun fill(context: DrawContext, left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        context.fill(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt(), color)
    }
}
