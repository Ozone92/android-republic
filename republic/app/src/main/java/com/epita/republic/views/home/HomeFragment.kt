package com.epita.republic.views.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.epita.republic.R
import kotlin.system.exitProcess


class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.home_quit_btn).setOnClickListener {
            exitProcess(0)
        }

        view.findViewById<Button>(R.id.home_play_btn).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_characterFragment)
        }
    }
}