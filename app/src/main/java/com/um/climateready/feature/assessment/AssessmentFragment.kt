package com.um.climateready.feature.assessment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.databinding.FragmentAssessmentBinding

class AssessmentFragment : Fragment() {

    private var _binding: FragmentAssessmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataManager = AssetDataManager.getInstance(requireContext())
        val preQuestions = dataManager.assessmentQuestions.filter { it.type == "PRE" }

        binding.btnLaunchQuiz.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Module 1: Loaded ${preQuestions.size} diagnostic pre-assessment questions.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
