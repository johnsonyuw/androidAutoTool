package com.quvideo.demo.automatetasks.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.quvideo.demo.automatetasks.database.ClickDatabaseHelper
import com.quvideo.demo.automatetasks.database.model.ClickModel
import com.quvideo.demo.automatetasks.utils.AccessabilityUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ClickRecorderService"

class ClickRecorderService : AccessibilityService() {

  private val databaseHelper = ClickDatabaseHelper(this)
  override fun onServiceConnected() {
    super.onServiceConnected()
    Log.d(TAG, "Service connected")

    // 获取 AccessibilityServiceInfo
    val info = serviceInfo

    // 应用配置
    info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY

    // 设置回去
    serviceInfo = info
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // return super.onStartCommand(intent, flags, startId)
    intent?.let {
      val commandType = it.getIntExtra("command_type", 0)
      when (commandType) {
        1 -> replayClicks()
        // 添加其他命令处理
      }
    }
    return START_STICKY
  }

  private var isAutoClick = false
  override fun onAccessibilityEvent(event: AccessibilityEvent) {
    Log.i(TAG, "Accessibility event: $event")
    if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
      // recordClick(event)
      if (event.source?.packageName == "com.quvideo.demo.automatetasks" && event.className == "android.widget.Button") {
        val nodeInfosByText =
          event.source?.parent?.findAccessibilityNodeInfosByText("支付宝刷单 打开刷单页")
        if (!isAutoClick && nodeInfosByText?.isNotEmpty() == true) {
          isAutoClick = true
          nodeInfosByText[0]?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
      }
    } else if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      Log.i(TAG, "event.source?.className: ${event.source?.className}")
      if (event.className == "com.quvideo.demo.automatetasks.AlipayAutoAct") {
        findWebViewInNode(event.source)
        isAutoClick = false
      }
    } else if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
      Log.i(TAG, "event.source?.className: ${event.source?.className}")
      if (event.packageName == "com.eg.android.AlipayGphone") {
        if (!isInputing && isAliPayEnterPasswordPage(event.source)) {
          val pws = listOf("9", "9", "9", "0", "0", "0")
          //每个 300ms 输入一个数字
          GlobalScope.launch {
            isInputing = true
            for (pw in pws) {
              findAliDoneButton(event.source, pw)
              delay(300)
            }
            isInputing = false
          }
        } else {
          if (event.source?.className == "android.widget.ScrollView" ||
            "android.widget.FrameLayout" == event.source?.className
          ) {
            findAliDoneButton(event.source, "完成")
          }
        }
      }
    }
  }

  private var isInputing = false
  private fun isAliPayEnterPasswordPage(nodeInfo: AccessibilityNodeInfo?): Boolean {
    if (nodeInfo != null) {
      val txtList = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
      var result = true
      for (txt in txtList) {
        if (!isTextInNode(nodeInfo, txt)) {
          result = false
          break
        }
      }
      return result
    }
    return false
  }

  private fun isTextInNode(nodeInfo: AccessibilityNodeInfo?, text: String): Boolean {
    if (nodeInfo != null) {
      val items = nodeInfo.findAccessibilityNodeInfosByText(text)
      if (items.isNotEmpty()) {
        return true
      }
      return isTextInNode(nodeInfo.parent, text) // 递归查找
    }
    return false
  }

  private fun findAliDoneButton(nodeInfo: AccessibilityNodeInfo?, text: String) {
    if (nodeInfo != null) {
      val items = nodeInfo.findAccessibilityNodeInfosByText(text)
      if (items.isNotEmpty()) {
        val size = items.size
        Log.i(TAG, "findAliDoneButton: $text has found ${items.size}")
        items[size - 1].performAction(AccessibilityNodeInfo.ACTION_CLICK)
      } else {
        findAliDoneButton(nodeInfo.parent, text) // 递归查找
      }
    }
  }

  private fun findWebViewInNode(nodeInfo: AccessibilityNodeInfo?) {
    Log.i(TAG, "findWebViewInNode")
    if (nodeInfo != null) {
      for (i in 0 until nodeInfo.childCount) {
        val child = nodeInfo.getChild(i)
        if ("android.webkit.WebView" == child?.className) {
          findEveryViewNode(child) // 递归查找
          break
        }
      }
    } else {
    }
  }

  private fun findEveryViewNode(node: AccessibilityNodeInfo?) {
    Log.i(TAG, "findEveryViewNode 11")
    if (node != null && node.childCount > 0) {
      for (i in 0 until node.childCount) {
        val child = node.getChild(i) ?: continue // 如果 child 为 null 则跳过

        val className = child.className.toString()
        if ("android.widget.Image" == className) {
          val isClickable = child.isClickable
          val isResIdNull = child.viewIdResourceName == "btn"
          Log.i(TAG, "isClickable: $isClickable, isResIdNull: $isResIdNull")
          // 检查按钮是否可点击且没有资源 ID
          if (isClickable && isResIdNull) {
            child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
          }
        }
        // 递归调用
        findEveryViewNode(child)
      }
    }
  }

  override fun onInterrupt() {
    Log.d(TAG, "Service interrupted")
  }

  private fun recordClick(event: AccessibilityEvent) {
    val bounds = Rect()
    event.source?.getBoundsInScreen(bounds)
    val click = ClickModel(System.currentTimeMillis(), bounds.centerX(), bounds.centerY())
    Log.i(TAG, "Recorded click: $click")
    databaseHelper.insertClick(click)
  }

  private fun replayClicks() {
    // 在这里调用你的重放逻辑
    val clickList = databaseHelper.getAllClicks()
    replayClicks(clickList)
  }

  private fun replayClicks(clicks: List<ClickModel>) {
    Log.i(TAG, "Replaying clicks: ${clicks.size}")
    GlobalScope.launch {
      var lastTime = 0L
      for (click in clicks) {
        AccessabilityUtils.tap(this@ClickRecorderService, click.x, click.y)
        val delayTime = click.timestamp - lastTime
        lastTime = click.timestamp
        delay(delayTime) // 添加延迟，模拟用户的点击操作间隔
      }
    }
  }
}