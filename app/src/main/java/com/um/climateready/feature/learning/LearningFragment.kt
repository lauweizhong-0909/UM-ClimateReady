package com.um.climateready.feature.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.databinding.FragmentLearningBinding

class LearningFragment : Fragment() {

    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataManager = AssetDataManager.getInstance(requireContext())
        val units = dataManager.learningUnits

        binding.chipAll.setOnClickListener {
            Toast.makeText(requireContext(), "Showing all ${units.size} micro-units", Toast.LENGTH_SHORT).show()
        }
        binding.chipRain.setOnClickListener {
            val count = units.count { it.category == "HEAVY_RAIN" }
            Toast.makeText(requireContext(), "Heavy Rain & Flooding: $count units", Toast.LENGTH_SHORT).show()
        }
        binding.chipHeat.setOnClickListener {
            val count = units.count { it.category == "EXTREME_HEAT" }
            Toast.makeText(requireContext(), "Extreme Heat: $count units", Toast.LENGTH_SHORT).show()
        }
        binding.chipHaze.setOnClickListener {
            val count = units.count { it.category == "HAZE_AIR_QUALITY" }
            Toast.makeText(requireContext(), "Haze & Air Quality: $count units", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

