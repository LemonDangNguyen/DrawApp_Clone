package com.draw.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.draw.activity.ShareAnimationActivity
import com.draw.adapter.MyCreationsAdapter
import com.draw.database.AnimationViewModel
import com.draw.databinding.FragmentMyCreationBinding
import com.draw.ultis.Common
import com.draw.ultis.ViewControl.gone
import com.draw.ultis.ViewControl.visible

class MyCreationFragment : Fragment() {
    private val binding by lazy { FragmentMyCreationBinding.inflate(layoutInflater) }
    private lateinit var adapter: MyCreationsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = AnimationViewModel(requireContext())

        adapter = MyCreationsAdapter(requireContext(), mutableListOf()) { item ->
            startActivity(
                Intent(requireContext(), ShareAnimationActivity::class.java)
                    .putExtra("LINK", item.link)
                    .putExtra(Common.KEY_PROJECT_NAME, item.name)
                    .putExtra(Common.KEY_FROM_MY_CREATORS, true)
            )
        }
        binding.rcv.adapter = adapter

        viewModel.getAllAnimations().observe(this) { listAnimation ->
            adapter.setList(listAnimation.toMutableList())
            if (listAnimation.isEmpty()) {
                binding.lnEmpty.visible()
                binding.rcv.gone()
            } else {
                binding.lnEmpty.gone()
                binding.rcv.visible()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


}