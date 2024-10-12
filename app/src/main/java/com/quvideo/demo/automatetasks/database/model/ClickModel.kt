package com.quvideo.demo.automatetasks.database.model

data class ClickModel(val timestamp: Long, val x: Int, val y: Int) {
  override fun toString(): String {
    return "ClickModel(timestamp=$timestamp, x=$x, y=$y)"
  }
}