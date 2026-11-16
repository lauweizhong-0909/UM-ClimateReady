package com.um.climateready

import com.um.climateready.core.model.ClimateResource
import com.um.climateready.feature.resources.ResourceFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceFilterTest {

    private fun resource(id: String, name: String, category: String, desc: String, org: String) =
        ClimateResource(id, name, category, desc, org, "https://example.com", "123", "2026-11-01", "VERIFIED")

    private val sample = listOf(
        resource("1", "UM Security BKU", "HEAVY_RAIN", "Help during floods", "Universiti Malaya"),
        resource("2", "Student Health Clinic", "EXTREME_HEAT", "Heat exhaustion care", "PKU UM"),
        resource("3", "APIMS Portal", "HAZE_AIR_QUALITY", "Air quality index", "Department of Environment")
    )

    @Test
    fun emptyQueryAndAllCategory_returnsAll() {
        assertEquals(3, ResourceFilter.filter(sample, "", "ALL").size)
    }

    @Test
    fun categoryFilter_returnsOnlyThatCategory() {
        val result = ResourceFilter.filter(sample, "", "HEAVY_RAIN")
        assertEquals(1, result.size)
        assertEquals("UM Security BKU", result[0].name)
    }

    @Test
    fun query_matchesNameDescriptionAndOrg_caseInsensitive() {
        assertEquals(1, ResourceFilter.filter(sample, "CLINIC", "ALL").size)
        assertEquals(1, ResourceFilter.filter(sample, "floods", "ALL").size)
        assertEquals(1, ResourceFilter.filter(sample, "PKU", "ALL").size)
    }

    @Test
    fun queryAndCategory_mustBothMatch() {
        // "clinic" exists, but not under HEAVY_RAIN -> AND logic gives empty
        assertTrue(ResourceFilter.filter(sample, "clinic", "HEAVY_RAIN").isEmpty())
        assertEquals(1, ResourceFilter.filter(sample, "clinic", "EXTREME_HEAT").size)
    }

    @Test
    fun noMatch_returnsEmpty() {
        assertTrue(ResourceFilter.filter(sample, "zzzz", "ALL").isEmpty())
    }
}
