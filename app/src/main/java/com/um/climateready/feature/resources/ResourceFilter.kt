package com.um.climateready.feature.resources

import com.um.climateready.core.model.ClimateResource

object ResourceFilter {

    fun filter(
        resources: List<ClimateResource>,
        query: String,
        category: String?
    ): List<ClimateResource> {
        val q = query.trim().lowercase()

        return resources.filter { resource ->
            val matchesCategory =
                category == null || category == "ALL" || resource.category == category

            val matchesQuery =
                q.isEmpty() ||
                        resource.name.lowercase().contains(q) ||
                        resource.shortDescription.lowercase().contains(q) ||
                        resource.sourceOrganization.lowercase().contains(q)

            matchesCategory && matchesQuery
        }
    }
}
