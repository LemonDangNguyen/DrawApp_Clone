package com.draw.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.draw.R
import com.draw.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private val binding by lazy { FragmentSettingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnLanguage.setOnClickListener {

        }

        binding.btnShare.setOnClickListener {
            val appPackageName = requireContext().packageName
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


}