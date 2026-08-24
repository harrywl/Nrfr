package com.github.nrfr.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetDataTest {
    @Test
    fun countryCodes_areUniqueUppercaseIsoCodes() {
        val countryCodes = CountryPresets.countries.map { it.code }

        assertEquals(countryCodes.size, countryCodes.toSet().size)
        assertTrue(countryCodes.all { it.matches(Regex("[A-Z]{2}")) })
    }

    @Test
    fun carrierRegions_referenceKnownCountries() {
        val countryCodes = CountryPresets.countries.map { it.code }.toSet()
        val carrierRegions = PresetCarriers.presets
            .map { it.region }
            .filter { it.isNotEmpty() }

        assertTrue(carrierRegions.all { it in countryCodes })
    }

    @Test
    fun carrierPresets_haveExactlyOneCustomOption() {
        val customOptions = PresetCarriers.presets.filter { it.region.isEmpty() }

        assertEquals(1, customOptions.size)
        assertTrue(customOptions.single().displayName.isEmpty())
    }
}
