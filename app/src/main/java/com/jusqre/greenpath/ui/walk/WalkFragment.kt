package com.jusqre.greenpath.ui.walk

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jusqre.greenpath.R

class WalkFragment : Fragment() {

    companion object {
        fun newInstance() = WalkFragment()
    }

    private lateinit var viewModel: WalkViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.walk_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(WalkViewModel::class.java)
        // TODO: Use the ViewModel
    }

}