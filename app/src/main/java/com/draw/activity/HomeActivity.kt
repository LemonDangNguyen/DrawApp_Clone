package com.draw.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.draw.R
import com.draw.databinding.ActivityHomeBinding
import com.draw.fragment.CreateFragment
import com.draw.fragment.MyCreationFragment
import com.draw.fragment.SettingFragment
import com.draw.ultis.ViewControl.actionAnimation

class HomeActivity : BaseActivity() {
    val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
        binding.vpHome.adapter = ViewPagerAdapter(this)
        binding.vpHome.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectBottomNavBar(position)
            }
        })

        binding.vpHome.currentItem = 0
        binding.vpHome.isUserInputEnabled = false


        binding.btnCreate.setOnClickListener {
            binding.vpHome.currentItem = 0
        }
        binding.btnMycreation.setOnClickListener {
            binding.vpHome.currentItem = 1
        }
        binding.btnSetting.setOnClickListener {
            binding.vpHome.currentItem = 2
        }
    }


    private fun selectBottomNavBar(position: Int) {
        binding.lnBottomBar.actionAnimation()
        when (position) {
            0 -> {
                binding.icCreate.setImageResource(R.drawable.ic_create_select)
                binding.tvCreate.setTextColor(Color.parseColor("#01C296"))
                binding.tvCreate.typeface = getFont(this, R.font.nunito_bold)

                binding.icMycreation.setImageResource(R.drawable.ic_mycreation)
                binding.tvMycreation.setTextColor(Color.parseColor("#667280"))
                binding.tvMycreation.typeface = getFont(this, R.font.nunito_medium)

                binding.icSetting.setImageResource(R.drawable.ic_setting)
                binding.tvSetting.setTextColor(Color.parseColor("#667280"))
                binding.tvSetting.typeface = getFont(this, R.font.nunito_medium)
            }

            1 -> {
                binding.icCreate.setImageResource(R.drawable.ic_create)
                binding.tvCreate.setTextColor(Color.parseColor("#667280"))
                binding.tvCreate.typeface = getFont(this, R.font.nunito_medium)

                binding.icMycreation.setImageResource(R.drawable.ic_mycreation_select)
                binding.tvMycreation.setTextColor(Color.parseColor("#01C296"))
                binding.tvMycreation.typeface = getFont(this, R.font.nunito_bold)

                binding.icSetting.setImageResource(R.drawable.ic_setting)
                binding.tvSetting.setTextColor(Color.parseColor("#667280"))
                binding.tvSetting.typeface = getFont(this, R.font.nunito_medium)
            }

            2 -> {
                binding.icCreate.setImageResource(R.drawable.ic_create)
                binding.tvCreate.setTextColor(Color.parseColor("#667280"))
                binding.tvCreate.typeface = getFont(this, R.font.nunito_medium)

                binding.icMycreation.setImageResource(R.drawable.ic_mycreation)
                binding.tvMycreation.setTextColor(Color.parseColor("#667280"))
                binding.tvMycreation.typeface = getFont(this, R.font.nunito_medium)

                binding.icSetting.setImageResource(R.drawable.ic_setting_select)
                binding.tvSetting.setTextColor(Color.parseColor("#01C296"))
                binding.tvSetting.typeface = getFont(this, R.font.nunito_bold)
            }
            else->{

            }
        }
    }

    inner class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CreateFragment()
                1 -> MyCreationFragment()
                2  -> SettingFragment()
                else -> CreateFragment()
            }
        }
    }
}