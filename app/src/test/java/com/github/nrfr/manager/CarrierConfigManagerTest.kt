package com.github.nrfr.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CarrierConfigManagerTest {
    @Test
    fun parseDumpsysCarrierConfig_readsOverridesForEachPhone() {
        val output = """
            Phone Id = 0
            mOverrideConfigs:
              sim_country_iso_override_string = jp
              carrier_name_override_bool = true
              carrier_name_string = NTT docomo
            mPersistentOverrideConfigs:
            Phone Id = 1
            mOverrideConfigs: null
        """.trimIndent()

        val result = CarrierConfigManager.parseDumpsysCarrierConfig(output)

        assertEquals(
            mapOf(
                CarrierConfigManager.CONFIG_COUNTRY_CODE to "jp",
                CarrierConfigManager.CONFIG_CARRIER_NAME to "NTT docomo"
            ),
            result[0]
        )
        assertEquals(emptyMap<String, String>(), result[1])
    }

    @Test
    fun parseDumpsysCarrierConfig_ignoresCarrierNameWithoutOverrideFlag() {
        val output = """
            Phone Id = 0
            mOverrideConfigs:
              carrier_name_override_bool = false
              carrier_name_string = Ignored carrier
            mPersistentOverrideConfigs:
        """.trimIndent()

        val result = CarrierConfigManager.parseDumpsysCarrierConfig(output)

        assertFalse(result.getValue(0).containsKey(CarrierConfigManager.CONFIG_CARRIER_NAME))
    }

    @Test
    fun parseInstrumentationResult_readsValuesAndFinalCode() {
        val output = """
            INSTRUMENTATION_STATUS: class=com.github.nrfr.CarrierConfigInstrumentation
            INSTRUMENTATION_RESULT: result=ok
            INSTRUMENTATION_RESULT: carrierName=AT&T=Test
            INSTRUMENTATION_CODE: -1
        """.trimIndent()

        val result = CarrierConfigManager.parseInstrumentationResult(output)

        assertEquals("ok", result.values["result"])
        assertEquals("AT&T=Test", result.values["carrierName"])
        assertEquals(-1, result.code)
    }

    @Test
    fun shellQuote_handlesWhitespaceApostrophesAndEmptyValues() {
        assertEquals("'hello world'", CarrierConfigManager.shellQuote("hello world"))
        assertEquals("'O'\\''Brien'", CarrierConfigManager.shellQuote("O'Brien"))
        assertEquals("''", CarrierConfigManager.shellQuote(""))
    }
}
