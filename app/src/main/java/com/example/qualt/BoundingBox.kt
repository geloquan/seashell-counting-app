package com.example.qualt

data class BoundingBox(
    val x1: Float,  // Left coordinate
    val y1: Float,  // Top coordinate
    val x2: Float,  // Right coordinate
    val y2: Float, // Bottom coordinate
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val cls: Int,
    val clsName: String,
    val confidence: Float
)
