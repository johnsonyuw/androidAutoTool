package com.quvideo.demo.automatetasks.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log

object AccessabilityUtils {
  private const val TAG = "AccessabilityUtils"
  fun tap(service: AccessibilityService, x: Int, y: Int) {
    Log.i(TAG, "tap++")
    val builder = GestureDescription.Builder()
    val path = Path().apply {
      moveTo(x.toFloat(), y.toFloat())
    }
    builder.addStroke(GestureDescription.StrokeDescription(path, 0L, 500L))
    val gesture = builder.build()

    service.dispatchGesture(gesture, object : GestureResultCallback() {
      override fun onCompleted(gestureDescription: GestureDescription?) {
        super.onCompleted(gestureDescription)
        Log.i(TAG, "onCompleted...")
      }

      override fun onCancelled(gestureDescription: GestureDescription?) {
        super.onCancelled(gestureDescription)
        Log.e(TAG, "onCancelled...")
      }
    }, null)
  }

  fun Context.isAccessibilitySettingsOn(clazz: Class<out AccessibilityService?>): Boolean {
    var accessibilityEnabled = false    // 判断设备的无障碍功能是否可用
    try {
      accessibilityEnabled = Settings.Secure.getInt(
        applicationContext.contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED
      ) == 1
    } catch (e: Settings.SettingNotFoundException) {
      e.printStackTrace()
    }
    val mStringColonSplitter = SimpleStringSplitter(':')
    if (accessibilityEnabled) {
      // 获取启用的无障碍服务
      val settingValue: String? = Settings.Secure.getString(
        applicationContext.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
      )
      if (settingValue != null) {
        // 遍历判断是否包含我们的服务
        mStringColonSplitter.setString(settingValue)
        while (mStringColonSplitter.hasNext()) {
          val accessibilityService = mStringColonSplitter.next()
          if (accessibilityService.equals(
              "${packageName}/${clazz.canonicalName}",
              ignoreCase = true
            )
          ) return true

        }
      }
    }
    return false
  }

}