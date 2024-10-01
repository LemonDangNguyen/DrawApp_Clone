package com.draw.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.draw.R
import com.draw.database.AnimationViewModel
import com.draw.databinding.ActivityShareAnimationBinding
import com.draw.model.Animation
import com.draw.ultis.Common
import com.draw.ultis.ViewControl.gone
import com.draw.ultis.ViewControl.onSingleClick
import kotlinx.coroutines.launch
import java.io.File

class ShareAnimationActivity : BaseActivity() {
    private val binding by lazy { ActivityShareAnimationBinding.inflate(layoutInflater) }
    private var isCreated = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val link = intent.getStringExtra("LINK")

        val nameProject =
            intent.getStringExtra(Common.KEY_PROJECT_NAME) ?: getString(R.string.untitle)

        if (intent.getBooleanExtra(Common.KEY_FROM_MY_CREATORS, false)) {
            binding.btnHome.gone()
            binding.tvTitle.text = nameProject
        }

        val viewModel = AnimationViewModel(this)

        viewModel.getAllAnimations().observe(this) {
            for (animation in it) {
                if (animation.link == link) {
                    isCreated = true
                }
            }
        }

        Glide
            .with(this)
            .load(link)
            .into(binding.img)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnShare.onSingleClick(2000) {
            if (link != null) {
                shareFile(link)
            }
        }

        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()

        }

        binding.btnSave.setOnClickListener {
            if (link != null) {
                if (!isCreated) {
                    lifecycleScope.launch {
                        viewModel.insertAnimation(
                            Animation(
                                nameProject,
                                link,
                                intent.getBooleanExtra(Common.KEY_IS_GUIDE, true)
                            )
                        )
                        Toast.makeText(
                            this@ShareAnimationActivity,
                            getString(R.string.save) + "!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ShareAnimationActivity,
                        getString(R.string.is_save),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!intent.getBooleanExtra(Common.KEY_FROM_MY_CREATORS, false)) {
                    Common.back_next = true
                }
                finish()
            }
        })
    }

    private fun shareFile(filePath: String) {
        if (filePath.isBlank()) {
            return
        }
        val file = File(filePath)
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/gif"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, getString(R.string.share_animation))
        startActivity(chooser)
    }
}