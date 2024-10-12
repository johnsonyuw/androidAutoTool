package com.quvideo.demo.automatetasks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import com.quvideo.demo.automatetasks.databinding.ActivityAlipayAutoBinding

class AlipayAutoAct : AppCompatActivity() {

  private lateinit var binding: ActivityAlipayAutoBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityAlipayAutoBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.webview.settings.apply {
      javaScriptEnabled = true
      domStorageEnabled = true
      cacheMode = WebSettings.LOAD_DEFAULT
      // setAppCacheEnabled(true)
    }
    binding.webview.loadUrl("https://h5.eeyeful.cn/mstoreh5/payzfb.html")
  }

  companion object {
    fun newIntent(context: Context?): Intent {
      val intent = Intent(context, AlipayAutoAct::class.java)
      return intent
    }
  }
}