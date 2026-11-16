package com.um.climateready.feature.explorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.um.climateready.databinding.FragmentExplorerBinding

class ExplorerFragment : Fragment() {

    private var _binding: FragmentExplorerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExplorerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipScenario15.setOnClickListener {
            binding.tvScenarioSummary.text = "At +1.5°C above pre-industrial levels:\n• Tropical extreme heat frequency increases by ~2.8x\n• Extreme heavy precipitation events intensify by ~1.5x\n• Coral reefs decline by 70–90%\n• Substantial adaptation needed for Southeast Asian urban centers (IPCC AR6)."
        }

        binding.chipScenario20.setOnClickListener {
            binding.tvScenarioSummary.text = "At +2.0°C above pre-industrial levels:\n• Extreme heatwaves become 5.6x more frequent\n• Urban heat island intensity increases significantly across tropical cities\n• Over 99% of coral reefs irreversibly lost\n• Sea-level rise threatens coastal lowlands and urban drainage (IPCC AR6)."
        }

        binding.chipScenario30.setOnClickListener {
            binding.tvScenarioSummary.text = "At +3.0°C above pre-industrial levels:\n• Unprecedented heat stress exceeds physiological human survivability thresholds outdoors\n• Severe compound events: intense monsoon deluge alternating with prolonged severe droughts\n• Catastrophic impacts on regional agriculture and urban infrastructure (IPCC AR6)."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
