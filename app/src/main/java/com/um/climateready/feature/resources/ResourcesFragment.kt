package com.um.climateready.feature.resources

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.um.climateready.R
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.core.model.ClimateResource
import com.um.climateready.databinding.FragmentResourcesBinding

class ResourcesFragment : Fragment() {

    private var _binding: FragmentResourcesBinding? = null
    private val binding get() = _binding!!

    private lateinit var resourceAdapter: ResourceAdapter
    private var allResources: List<ClimateResource> = emptyList()
    private var currentQuery: String = ""
    private var selectedCategory: String = "ALL"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allResources = AssetDataManager.getInstance(requireContext()).climateResources

        resourceAdapter = ResourceAdapter(allResources)
        binding.rvClimateResources.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClimateResources.adapter = resourceAdapter

        binding.etSearchResources.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString() ?: ""
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedCategory = when (checkedIds.firstOrNull()) {
                R.id.chip_rain -> "HEAVY_RAIN"
                R.id.chip_heat -> "EXTREME_HEAT"
                R.id.chip_haze -> "HAZE_AIR_QUALITY"
                R.id.chip_climate -> "CLIMATE_CHANGE"
                else -> "ALL"
            }
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filtered = ResourceFilter.filter(allResources, currentQuery, selectedCategory)
        resourceAdapter.updateData(filtered)
        binding.tvEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvClimateResources.visibility = if (filtered.isEmpty()) View.INVISIBLE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
