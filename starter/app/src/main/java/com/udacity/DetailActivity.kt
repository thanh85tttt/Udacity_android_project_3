package com.udacity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.Constants.STATUS
import com.udacity.Constants.SUCCESS_DOWNLOAD
import com.udacity.Constants.TITLE
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

  private lateinit var detailBinding: ActivityDetailBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    detailBinding = ActivityDetailBinding.inflate(layoutInflater)
    setContentView(detailBinding.root)
    setSupportActionBar(detailBinding.toolbar)

    settingDetailValue()
  }

  private fun settingDetailValue() {
    intent.extras?.let { bundle ->
      val title = bundle.getString(TITLE, "")
      val status = bundle.getString(STATUS, "")

      with(detailBinding.detailContent) {
        filenameValue.text = title
        statusValue.text = status
        statusValue.setTextColor(
          if (status == SUCCESS_DOWNLOAD) getColor(R.color.colorPrimaryDark) else Color.RED
        )

        doneButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
      }
    }
  }
}
