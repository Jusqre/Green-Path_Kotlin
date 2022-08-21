package com.jusqre.greenpath.ui.walk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jusqre.greenpath.databinding.FragmentWalkBinding
import com.jusqre.greenpath.ui.MainActivity

class WalkFragment : Fragment() {
    private lateinit var _binding: FragmentWalkBinding
    private val binding get() = _binding

    private lateinit var walkViewModel: WalkViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val map = (requireActivity() as MainActivity).map.value
        walkViewModel = ViewModelProvider(this)[WalkViewModel::class.java]
        _binding = FragmentWalkBinding.inflate(inflater, container, false)
        _binding.lifecycleOwner = viewLifecycleOwner

        binding.buttonRecommend.setOnClickListener {
            map?.let {
                walkViewModel.startLookUpTrail(it, this.requireContext())
            }
        }

        binding.buttonMakeTrail.setOnClickListener {
            Toast.makeText(this.context, "산책로 생성 클릭됨", Toast.LENGTH_SHORT).show()
            map?.let {
                walkViewModel.startMakeTrail(it, this.requireContext())
            }
        }

        return binding.root
    }

}