package com.draw.activity

import android.content.Intent
import android.os.Bundle
import com.draw.adapter.AnimationGuideAdapter
import com.draw.database.DataClient.listTypeAnimationGuide
import com.draw.databinding.ActivityChooseAnimationGuideBinding
import com.draw.ultis.Common.KEY_ANIM_GUIDE
import com.draw.ultis.Common.KEY_POSITION_ANIM_GUIDE

class ChooseAnimationGuideActivity : BaseActivity() {
    private val binding by lazy { ActivityChooseAnimationGuideBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val posTypeGuide = intent.getIntExtra(KEY_ANIM_GUIDE, -1)

        if (posTypeGuide == -1) {
            finish()
        }

        val listGuide = listTypeAnimationGuide()[posTypeGuide]

        binding.tvTitle.text = getText(listGuide.name)

        binding.rcv.adapter = AnimationGuideAdapter(this, listGuide.listAnimationGuide) {
            startActivity(
                Intent(this, PreviewGuideActivity::class.java).putExtra(
                    KEY_ANIM_GUIDE,
                    posTypeGuide
                ).putExtra(
                    KEY_POSITION_ANIM_GUIDE, it
                )
            )
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}