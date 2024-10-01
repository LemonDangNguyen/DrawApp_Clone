
package com.draw.activity

import android.content.Intent
import android.os.Bundle
import com.draw.R
import com.draw.databinding.ActivitySettingBinding

class SettingActivity : BaseActivity() {
    private val binding by lazy { ActivitySettingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLanguage.setOnClickListener {

        }

        binding.btnShare.setOnClickListener {
            val appPackageName = packageName
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out this cool app: https://play.google.com/store/apps/details?id=$appPackageName"
                )
                type = "text/plain"
            }
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    getText(R.string.share_to_your_friends)
                )
            )
        }

        binding.btnRate.setOnClickListener {

        }

        binding.btnPolicy.setOnClickListener {

        }

    }



}