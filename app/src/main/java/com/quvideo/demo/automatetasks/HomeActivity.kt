package com.quvideo.demo.automatetasks

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.quvideo.demo.automatetasks.adapter.ButtonAdapter
import com.quvideo.demo.automatetasks.adapter.OnItemClickListener
import com.quvideo.demo.automatetasks.databinding.ActivityHomeBinding
import com.quvideo.demo.automatetasks.services.ClickRecorderService
import com.quvideo.demo.automatetasks.utils.AccessabilityUtils.isAccessibilitySettingsOn

class HomeActivity : AppCompatActivity() {
  private lateinit var binding: ActivityHomeBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    // enableEdgeToEdge()

    setContentView(binding.root)

    val list = listOf(
      ButtonItems(1, "支付宝刷单 开始录制"),
      ButtonItems(2, "支付宝刷单 播放录制"),
      ButtonItems(3, "支付宝刷单 打开刷单页")
    )
    //在 binding的recycleview中显示 list ;纵向排列
    binding.recycleview.adapter = ButtonAdapter(list, object : OnItemClickListener {
      override fun onItemClick(item: ButtonItems) {
        when (item.id) {
          1 -> {
            startRecording()
          }

          2 -> {
            val serviceIntent = Intent(applicationContext, ClickRecorderService::class.java)
            serviceIntent.putExtra("command_type", 1)
            // startService(serviceIntent)
          }

          3 -> {
            Log.i("ClickRecorderService", "startActivity AlipayAutoAct")
            startActivity(AlipayAutoAct.newIntent(this@HomeActivity))
          }
        }
      }
    })
    val layoutManager = LinearLayoutManager(this)
    layoutManager.orientation = LinearLayoutManager.VERTICAL
    binding.recycleview.layoutManager = layoutManager

    //每次打开我们的APP都调用下这个方法判断无障碍服务是否打开，没有弹窗或者给提示，引导用户去 无障碍设置页设置下
    if(!isAccessibilitySettingsOn(ClickRecorderService::class.java)){
      // 引导用户去设置无障碍服务
      startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
  }

  private fun startRecording() {
    // startForegroundService(Intent(this, ClickRecorderService::class.java))
    startService(Intent(this, ClickRecorderService::class.java))
  }

  private fun stopRecording() {
    stopService(Intent(this, ClickRecorderService::class.java))
  }

  private fun updateRecyclerView() {
    // val clicks = databaseHelper.getAllClicks()
    // recyclerView.adapter = ClickAdapter(clicks)
  }
}

//实现一个 composable 函数；显示一个 功能按钮列表，使用简洁明快的按钮样式

data class ButtonItems(val id: Int, val name: String)
