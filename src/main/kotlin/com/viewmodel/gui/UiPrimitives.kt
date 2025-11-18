package com.viewmodel.gui

import net.minecraft.client.gui.DrawContext
import kotlin.math.min
import kotlin.math.sqrt

object UiPrimitives {
    fun fillRoundedRect(context: DrawContext, x: Int, y: Int, w: Int, h: Int, radius: Int, color: Int) {
        val r = radius.coerceAtMost(min(w, h) / 2)
        if (r <= 0) {
            context.fill(x, y, x + w, y + h, color)
            return
        }

        // core rectangle
        context.fill(x + r, y, x + w - r, y + h, color)
        context.fill(x, y + r, x + r, y + h - r, color)
        context.fill(x + w - r, y + r, x + w, y + h - r, color)

        val r2 = r * r
        for (dy in 0 until r) {
            val dx = r - sqrt((r2 - dy * dy).toDouble()).toInt()
            val left = x + dx
            val right = x + w - dx
            val top = y + dy
            val bottom = y + h - dy - 1
            context.fill(left, top, right, top + 1, color)
            context.fill(left, bottom, right, bottom + 1, color)
        }
    }

    fun drawRoundedBorder(context: DrawContext, x: Int, y: Int, w: Int, h: Int, radius: Int, color: Int) {
        val r = radius.coerceAtMost(min(w, h) / 2)
        if (r <= 0) {
            context.fill(x, y, x + w, y + 1, color)
            context.fill(x, y + h - 1, x + w, y + h, color)
            context.fill(x, y, x + 1, y + h, color)
            context.fill(x + w - 1, y, x + w, y + h, color)
            return
        }

        context.fill(x + r, y, x + w - r, y + 1, color)
        context.fill(x + r, y + h - 1, x + w - r, y + h, color)
        context.fill(x, y + r, x + 1, y + h - r, color)
        context.fill(x + w - 1, y + r, x + w, y + h - r, color)

        val r2 = r * r
        for (dy in 0 until r) {
            val dx = r - sqrt((r2 - dy * dy).toDouble()).toInt()
            val left = x + dx
            val right = x + w - dx - 1
            val top = y + dy
            val bottom = y + h - dy - 1

            context.fill(left, top, left + 1, top + 1, color)
            context.fill(right, top, right + 1, top + 1, color)
            context.fill(left, bottom, left + 1, bottom + 1, color)
            context.fill(right, bottom, right + 1, bottom + 1, color)
        }
    }
}
