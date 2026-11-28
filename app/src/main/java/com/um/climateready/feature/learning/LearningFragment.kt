package com.um.climateready.feature.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.um.climateready.R
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.core.util.LearningDataHelper
import com.um.climateready.data.repository.LearningRepository
import com.um.climateready.databinding.FragmentLearningBinding

/**
 * Module 2 — Climate Micro-Learning fragment.
 *
 * Displays a progress header, category filter chips (persisted across rotation
 * via [LearningViewModel]), a list of 12 learning unit cards, empty/error states,
 * and deep-linking from Module 1 Assessment recommendations into [LessonDetailBottomSheet].
 */
class LearningFragment : Fragment() {

    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LearningViewModel by viewModels()
    private lateinit var repository: LearningRepository
    private lateinit var lessonAdapter: LessonAdapter
    private var suppressChipListener = false

    companion object {
        const val ARG_RECOMMENDED_UNIT_ID = "arg_recommended_unit_id"
        private const val TAG_LESSON_DETAIL = "lesson_detail"

        fun newInstance(recommendedUnitId: String? = null): LearningFragment {
            return LearningFragment().apply {
                if (!recommendedUnitId.isNullOrBlank()) {
                    arguments = Bundle().apply {
                        putString(ARG_RECOMMENDED_UNIT_ID, recommendedUnitId)
                    }
                }
            }
        }
    }

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
        setupEmptyStateAction()
        setupFragmentResultListener()

        syncChipWithCategory(viewModel.selectedCategory)
        refreshLearningUi()

        val deepLinkUnitId = arguments?.getString(ARG_RECOMMENDED_UNIT_ID)
        if (!deepLinkUnitId.isNullOrBlank()) {
            openRecommendedUnit(deepLinkUnitId)
            arguments?.remove(ARG_RECOMMENDED_UNIT_ID)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshLearningUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
            if (suppressChipListener) return@setOnCheckedStateChangeListener
            val category = when (checkedIds.firstOrNull()) {
                R.id.chip_rain -> LearningDataHelper.CATEGORY_HEAVY_RAIN
                R.id.chip_heat -> LearningDataHelper.CATEGORY_EXTREME_HEAT
                R.id.chip_haze -> LearningDataHelper.CATEGORY_HAZE
                else -> LearningDataHelper.CATEGORY_ALL
            }
            viewModel.setSelectedCategory(category)
            applyFilter()
        }
    }

    private fun setupEmptyStateAction() {
        binding.btnResetFilter.setOnClickListener {
            viewModel.setSelectedCategory(LearningDataHelper.CATEGORY_ALL)
            syncChipWithCategory(LearningDataHelper.CATEGORY_ALL)
            applyFilter()
        }
    }

    private fun setupFragmentResultListener() {
        childFragmentManager.setFragmentResultListener(
            LessonDetailBottomSheet.REQUEST_KEY_LESSON_UPDATED,
            viewLifecycleOwner
        ) { _, _ ->
            refreshLearningUi()
        }
    }

    private fun syncChipWithCategory(category: String) {
        val targetChipId = when (LearningDataHelper.normalizeCategory(category)) {
            LearningDataHelper.CATEGORY_HEAVY_RAIN -> R.id.chip_rain
            LearningDataHelper.CATEGORY_EXTREME_HEAT -> R.id.chip_heat
            LearningDataHelper.CATEGORY_HAZE -> R.id.chip_haze
            else -> R.id.chip_all
        }
        if (binding.chipGroupCategories.checkedChipId != targetChipId) {
            suppressChipListener = true
            binding.chipGroupCategories.check(targetChipId)
            suppressChipListener = false
        }
    }

    private fun refreshLearningUi() {
        if (_binding == null) return
        applyFilter()
        updateProgressHeader()
    }

    private fun applyFilter() {
        val filtered = repository.getFilteredUnits(viewModel.selectedCategory)
        lessonAdapter.updateData(filtered)

        if (filtered.isEmpty()) {
            binding.rvLearningUnits.visibility = View.GONE
            binding.layoutLearningEmpty.visibility = View.VISIBLE
            val hasAnyData = repository.getTotalUnitCount() > 0
            binding.tvLearningEmptyDesc.text = getString(
                if (hasAnyData) R.string.learning_empty_filter_desc else R.string.learning_empty_data_desc
            )
            binding.btnResetFilter.visibility = if (hasAnyData) View.VISIBLE else View.GONE
        } else {
            binding.rvLearningUnits.visibility = View.VISIBLE
            binding.layoutLearningEmpty.visibility = View.GONE
        }
    }

    private fun updateProgressHeader() {
        val completedCount = repository.getCompletedCount()
        val totalCount = repository.getTotalUnitCount().let { if (it > 0) it else 12 }
        binding.progressBarLearning.max = totalCount
        binding.progressBarLearning.progress = completedCount.coerceAtMost(totalCount)
        binding.tvProgressCount.text = getString(
            R.string.learning_progress_format,
            completedCount,
            totalCount
        )
    }

    /**
     * Deep-links directly to a recommended unit (e.g. from Module 1 Assessment results),
     * filtering to its category, scrolling it into view, and opening its detail sheet once.
     */
    fun openRecommendedUnit(unitId: String?, forceResetHistory: Boolean = false) {
        if (_binding == null) return
        if (forceResetHistory) {
            viewModel.resetDeepLinkHistory()
        }
        val consumedId = viewModel.consumeDeepLinkUnitId(unitId) ?: return
        val unit = repository.getUnitById(consumedId) ?: return

        viewModel.setSelectedCategory(unit.category)
        syncChipWithCategory(unit.category)
        refreshLearningUi()

        val pos = lessonAdapter.findPositionByUnitId(unit.id)
        if (pos >= 0) {
            binding.rvLearningUnits.scrollToPosition(pos)
        }
        openLessonDetail(unit)
    }

    private fun openLessonDetail(unit: LearningUnit) {
        if (childFragmentManager.findFragmentByTag(TAG_LESSON_DETAIL) != null) return
        val sheet = LessonDetailBottomSheet.newInstance(unit)
        sheet.show(childFragmentManager, TAG_LESSON_DETAIL)
    }
}
