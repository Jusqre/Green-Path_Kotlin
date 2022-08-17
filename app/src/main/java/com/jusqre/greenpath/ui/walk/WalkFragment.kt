package com.jusqre.greenpath.ui.walk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jusqre.greenpath.databinding.WalkFragmentBinding

class WalkFragment : Fragment() {
    private lateinit var _binding: WalkFragmentBinding
    private val binding get() = _binding

    private lateinit var viewModel: WalkViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WalkFragmentBinding.inflate(inflater, container, false)

        binding.buttonMakeTrail.setOnClickListener {
            Toast.makeText(this.context,"산책로 생성 클릭됨",Toast.LENGTH_SHORT).show()
        }

        binding.buttonRecommend.setOnClickListener {
            Toast.makeText(this.context,"산책로 생성 클릭됨",Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }


}