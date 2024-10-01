package com.draw.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.draw.database.DataClient
import com.draw.database.DataClient.listTypeAnimationGuide
import com.draw.databinding.ActivityPreviewGuideBinding
import com.draw.ultis.Common

class PreviewGuideActivity : BaseActivity() {
    private val binding by lazy { ActivityPreviewGuideBinding.inflate(layoutInflater) }
    private lateinit var handle: Handler
    private lateinit var runnable: Runnable
    private var i = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val posTypeGuide = intent.getIntExtra(Common.KEY_ANIM_GUIDE, -1)
        val positionAnimGuide = intent.getIntExtra(Common.KEY_POSITION_ANIM_GUIDE, -1)

        if (posTypeGuide == -1 || positionAnimGuide == -1) {
            finish()
        }

        val animationGuide = listTypeAnimationGuide()[posTypeGuide].listAnimationGuide[positionAnimGuide]

        handle = Handler(Looper.getMainLooper())

        runnable = Runnable {
            if (i >= animationGuide.listFrame.size) {
                i = 0
            }

            binding.imgPreview.setImageResource(animationGuide.listFrame[i])

            i++
            handle.postDelayed(runnable, 1000L / animationGuide.animationSpeed)
        }

        handle.postDelayed(runnable, 0)

        handle.postDelayed({
            startActivity(
                Intent(this, DrawActivity::class.java)
                    .putExtra(Common.KEY_ANIM_GUIDE, posTypeGuide)
                    .putExtra(Common.KEY_POSITION_ANIM_GUIDE, positionAnimGuide)
            )
            finish()
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handle.removeCallbacksAndMessages(null)
    }
}