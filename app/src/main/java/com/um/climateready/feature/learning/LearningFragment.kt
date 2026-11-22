package com.um.climateready.feature.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.um.climateready.R
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.data.repository.LearningRepository
import com.um.climateready.databinding.FragmentLearningBinding

/**
 * Module 2 — Climate Micro-Learning fragment.
 *
 * Displays a progress header and a category-filtered list of 12 learning unit cards.
 * Tapping a card opens [LessonDetailBottomSheet] which shows the full lesson content,
 * CCNA-style knowledge check, and "Mark as Completed" toggle.
 *
 * Progress is persisted via SharedPreferences in [AssetDataManager] and reflected
 * immediately on the [LinearProgressIndicator] and unit card checkmarks.
 */
class LearningFragment : Fragment() {

    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: LearningRepository
    private lateinit var lessonAdapter: LessonAdapter

    /** Currently selected category filter. "ALL" = show all 12 units. */
    private var selectedCategory: String = "ALL"

    // ---------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------

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

        repository = LearningRepository(requireContext())

        setupRecyclerView()
        setupChipFilter()
        applyFilter()           // initial load — shows all 12 units
        updateProgressHeader()
    }

    /**
     * Refresh list and progress on return from back stack or when the
     * bottom sheet is dismissed (completion state may have changed).
     */
    override fun onResume() {
        super.onResume()
        applyFilter()
        updateProgressHeader()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------------

    private fun setupRecyclerView() {
        lessonAdapter = LessonAdapter(emptyList()) { unit ->
            openLessonDetail(unit)
        }
        binding.rvLearningUnits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lessonAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupChipFilter() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedCategory = when (checkedIds.firstOrNull()) {
                R.id.chip_rain -> "HEAVY_RAIN"
                R.id.chip_heat -> "EXTREME_HEAT"
                R.id.chip_haze -> "HAZE_AIR_QUALITY"
                else           -> "ALL"
            }
            applyFilter()
        }
    }

    // ---------------------------------------------------------------------------
    // Data & UI update helpers
    // ---------------------------------------------------------------------------

    /** Applies the current category filter and refreshes the RecyclerView. */
    private fun applyFilter() {
        val filtered = repository.getFilteredUnits(selectedCategory)
        lessonAdapter.updateData(filtered)
    }

    /**
     * Updates the progress bar and counter label above the chip filter.
     * Shows "X / 12 completed" with the ProgressBar filled proportionally.
     */
    private fun updateProgressHeader() {
        val completedCount = repository.getCompletedCount()
        val totalCount = 12
        binding.progressBarLearning.progress = completedCount
        binding.tvProgressCount.text = "$completedCount / $totalCount completed"
    }

    // ---------------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------------

    /**
     * Opens the full lesson detail as a [LessonDetailBottomSheet].
     * Registers a dismiss callback so the list and progress header refresh
     * immediately when the user closes the sheet.
     */
    private fun openLessonDetail(unit: LearningUnit) {
        val sheet = LessonDetailBottomSheet.newInstance(unit)
        sheet.onDismissCallback = {
            // Completion state may have changed — refresh everything
            applyFilter()
            updateProgressHeader()
        }
        sheet.show(childFragmentManager, "lesson_detail")
    }
}
