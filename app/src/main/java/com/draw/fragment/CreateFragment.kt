package com.draw.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.draw.activity.ChooseAnimationGuideActivity
import com.draw.activity.CreateActivity
import com.draw.activity.HomeActivity
import com.draw.activity.PreviewGuideActivity
import com.draw.adapter.TypeAnimationAdapter
import com.draw.database.DataClient.listTypeAnimationGuide
import com.draw.databinding.FragmentCreateBinding
import com.draw.ultis.Common.KEY_ANIM_GUIDE
import com.draw.ultis.Common.KEY_POSITION_ANIM_GUIDE

class CreateFragment : Fragment() {
    private val binding by lazy { FragmentCreateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnFreestyle.setOnClickListener {
            startActivity(Intent(requireContext(), CreateActivity::class.java))
        }

        binding.rcv.adapter = TypeAnimationAdapter(requireContext(), listTypeAnimationGuide(), {
            startActivity(
                Intent(requireContext(), ChooseAnimationGuideActivity::class.java).putExtra(
                    KEY_ANIM_GUIDE, it
                )
            )
        }, { posType: Int, posGuide: Int ->
            startActivity(
                Intent(requireContext(), PreviewGuideActivity::class.java)
                    .putExtra(KEY_ANIM_GUIDE, posType)
                    .putExtra(KEY_POSITION_ANIM_GUIDE, posGuide)
            )
        })


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
}