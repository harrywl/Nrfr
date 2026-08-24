package com.github.nrfr.manager

// Fork 变更说明：本文件基于 Ackites/Nrfr 修改，适配 Android 16 双 APK helper 写入链路。

import android.content.Context
import android.os.PersistableBundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.CarrierConfigManager as AndroidCarrierConfigManager
import com.github.nrfr.R
import com.github.nrfr.model.SimCardInfo
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import java.io.InputStream

object CarrierConfigManager {
    const val CONFIG_COUNTRY_CODE = "country_code"
    const val CONFIG_CARRIER_NAME = "carrier_name"

    private const val INSTRUMENTATION_CLASS = "com.github.nrfr.CarrierConfigInstrumentation"
    private const val INSTRUMENTATION_TARGET_PACKAGE = "com.github.nrfr.instrumentationtarget"
    private const val INSTRUMENTATION_RESULT_PREFIX = "INSTRUMENTATION_RESULT: "
    private val PHONE_ID_PATTERN = Regex("""^Phone Id = (\d+)""")

    fun getSimCards(context: Context): List<SimCardInfo> {
        val simCards = mutableListOf<SimCardInfo>()
        val subId1 = getSubIdForSlot(0)
        val subId2 = getSubIdForSlot(1)
        val configsByPhoneId = getCurrentConfigsByDumpsys(context)

        if (subId1 != null) {
            val config1 = configsByPhoneId[0] ?: getCurrentConfigByPublicApi(context, subId1)
            simCards.add(SimCardInfo(1, subId1, getCarrierNameBySubId(context, subId1), config1))
        }
        if (subId2 != null) {
            val config2 = configsByPhoneId[1] ?: getCurrentConfigByPublicApi(context, subId2)
            simCards.add(SimCardInfo(2, subId2, getCarrierNameBySubId(context, subId2), config2))
        }

        return simCards
    }

    private fun getSubIdForSlot(slotIndex: Int): Int? {
        return try {
            HiddenApiBypass.addHiddenApiExemptions("L")
            val getSubId = SubscriptionManager::class.java.getDeclaredMethod(
                "getSubId",
                Int::class.javaPrimitiveType
            )
            val subIds = getSubId.invoke(null, slotIndex) as? IntArray ?: return null
            subIds.firstOrNull { it != SubscriptionManager.INVALID_SUBSCRIPTION_ID }
        } catch (e: Exception) {
            null
        }
    }

    private fun getCurrentConfigByPublicApi(context: Context, subId: Int): Map<String, String> {
        return try {
            val manager = context.getSystemService(AndroidCarrierConfigManager::class.java)
                ?: return emptyMap()
            val config = manager.getConfigForSubId(subId) ?: return emptyMap()
            val result = mutableMapOf<String, String>()

            // 获取当前覆盖的 SIM 国家码。
            config.getString(AndroidCarrierConfigManager.KEY_SIM_COUNTRY_ISO_OVERRIDE_STRING)?.let {
                result[CONFIG_COUNTRY_CODE] = it
            }

            // 获取当前覆盖的运营商名称。
            if (config.getBoolean(AndroidCarrierConfigManager.KEY_CARRIER_NAME_OVERRIDE_BOOL, false)) {
                config.getString(AndroidCarrierConfigManager.KEY_CARRIER_NAME_STRING)?.let {
                    result[CONFIG_CARRIER_NAME] = it
                }
            }

            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun getCurrentConfigsByDumpsys(context: Context): Map<Int, Map<String, String>> {
        return try {
            parseDumpsysCarrierConfig(runShellCommand(context, "dumpsys carrier_config"))
        } catch (e: Exception) {
            emptyMap()
        }
    }

    internal fun parseDumpsysCarrierConfig(output: String): Map<Int, Map<String, String>> {
        val result = mutableMapOf<Int, Map<String, String>>()
        var currentPhoneId: Int? = null
        var readingOverrideConfig = false
        val rawValues = mutableMapOf<String, String>()

        fun flushCurrentConfig() {
            val phoneId = currentPhoneId ?: return
            val config = mutableMapOf<String, String>()
            rawValues["sim_country_iso_override_string"]?.takeIf { it.isNotBlank() }?.let {
                config[CONFIG_COUNTRY_CODE] = it
            }
            if (rawValues["carrier_name_override_bool"] == "true") {
                rawValues["carrier_name_string"]?.takeIf { it.isNotBlank() }?.let {
                    config[CONFIG_CARRIER_NAME] = it
                }
            }
            result[phoneId] = config
            rawValues.clear()
        }

        output.lineSequence().forEach { line ->
            PHONE_ID_PATTERN.find(line)?.let { match ->
                flushCurrentConfig()
                currentPhoneId = match.groupValues[1].toIntOrNull()
                readingOverrideConfig = false
                return@forEach
            }

            val trimmed = line.trim()
            if (trimmed.startsWith("mOverrideConfigs")) {
                readingOverrideConfig = !trimmed.endsWith("null")
                if (!readingOverrideConfig) {
                    rawValues.clear()
                }
                return@forEach
            }

            if (!readingOverrideConfig) {
                return@forEach
            }

            if (trimmed.startsWith("m") && trimmed.endsWith(":")) {
                readingOverrideConfig = false
                return@forEach
            }

            val separatorIndex = trimmed.indexOf(" = ")
            if (separatorIndex > 0) {
                rawValues[trimmed.substring(0, separatorIndex)] =
                    trimmed.substring(separatorIndex + 3)
            }
        }

        flushCurrentConfig()
        return result
    }

    private fun getCarrierNameBySubId(context: Context, subId: Int): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return ""

        return try {
            telephonyManager.createForSubscriptionId(subId).networkOperatorName
        } catch (e: Exception) {
            // 如果分订阅查询失败，回退到默认 TelephonyManager，避免 SIM 列表空名称。
            telephonyManager.networkOperatorName
        }
    }

    fun setCarrierConfig(
        context: Context,
        subId: Int,
        countryCode: String?,
        carrierName: String? = null
    ): CarrierConfigOperationResult {
        val bundle = PersistableBundle()

        // 设置 SIM 国家码覆盖值，系统要求使用小写 ISO 3166-1 alpha-2 格式。
        if (!countryCode.isNullOrEmpty() && countryCode.length == 2) {
            bundle.putString(
                AndroidCarrierConfigManager.KEY_SIM_COUNTRY_ISO_OVERRIDE_STRING,
                countryCode.lowercase()
            )
        }

        // 设置运营商名称覆盖值。
        if (!carrierName.isNullOrEmpty()) {
            bundle.putBoolean(AndroidCarrierConfigManager.KEY_CARRIER_NAME_OVERRIDE_BOOL, true)
            bundle.putString(AndroidCarrierConfigManager.KEY_CARRIER_NAME_STRING, carrierName)
        }

        overrideCarrierConfig(context, subId, bundle, persistent = false)
        return CarrierConfigOperationResult()
    }

    fun resetCarrierConfig(context: Context, subId: Int): CarrierConfigOperationResult {
        val warnings = mutableListOf<String>()

        overrideCarrierConfig(context, subId, null, persistent = false)

        runCatching {
            overrideCarrierConfig(context, subId, null, persistent = true)
        }.onFailure { error ->
            warnings.add(formatPersistentResetWarning(context, error))
        }

        return CarrierConfigOperationResult(warnings)
    }

    private fun overrideCarrierConfig(
        context: Context,
        subId: Int,
        bundle: PersistableBundle?,
        persistent: Boolean
    ) {
        val arguments = mutableListOf(
            "subId" to subId.toString(),
            "persistent" to persistent.toString()
        )

        if (bundle == null) {
            arguments.add("reset" to "true")
        } else {
            bundle.getString(AndroidCarrierConfigManager.KEY_SIM_COUNTRY_ISO_OVERRIDE_STRING)?.let {
                arguments.add("countryCode" to it)
            }
            bundle.getString(AndroidCarrierConfigManager.KEY_CARRIER_NAME_STRING)?.let {
                arguments.add("carrierName" to it)
            }
        }

        runInstrumentation(context, arguments, relaunchApp = true)
    }

    private fun runInstrumentation(
        context: Context,
        arguments: List<Pair<String, String>>,
        relaunchApp: Boolean
    ): InstrumentationResult {
        ensureInstrumentationTargetInstalled(context)

        val instrumentCommand = mutableListOf("am", "instrument", "-w")
        arguments.forEach { (key, value) ->
            instrumentCommand.addAll(listOf("-e", key, value))
        }
        instrumentCommand.add("${context.packageName}/$INSTRUMENTATION_CLASS")

        // 写入时 instrumentation 可能影响前台状态，命令结束后拉起主应用，保证用户仍停留在操作界面。
        val script = buildString {
            append(instrumentCommand.joinToString(" ") { shellQuote(it) })
            if (relaunchApp) {
                append("; status=\$?; ")
                append("monkey -p ")
                append(shellQuote(context.packageName))
                append(" 1 >/dev/null 2>&1; exit \$status")
            }
        }
        val process = newShizukuProcess(arrayOf("sh", "-c", script))
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val stdoutThread = readAsync(process.inputStream, stdout)
        val stderrThread = readAsync(process.errorStream, stderr)
        val exitCode = process.waitFor()
        stdoutThread.join()
        stderrThread.join()

        val output = stdout.toString()
        val error = stderr.toString()
        val result = parseInstrumentationResult(output)
        if (
            exitCode != 0 ||
            result.code == 0 ||
            result.values["result"] == "failed" ||
            result.values.containsKey("error")
        ) {
            throw IllegalStateException(
                listOf(output, error)
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
                    .ifBlank {
                        context.getString(
                            R.string.carrier_config_override_failed_with_exit_code,
                            exitCode
                        )
                    }
            )
        }

        return result
    }

    private fun ensureInstrumentationTargetInstalled(context: Context) {
        val output = try {
            runShellCommand(context, "pm path ${shellQuote(INSTRUMENTATION_TARGET_PACKAGE)}")
        } catch (_: IllegalStateException) {
            ""
        }
        val installed = output.lineSequence().any { it.startsWith("package:") }

        if (!installed) {
            throw IllegalStateException(context.getString(R.string.nrfr_helper_missing))
        }
    }

    private fun formatPersistentResetWarning(context: Context, error: Throwable): String {
        val message = error.message.orEmpty()
        return if (
            message.contains("persistent=true", ignoreCase = true) ||
            message.contains("only can be invoked by system app", ignoreCase = true)
        ) {
            context.getString(R.string.persistent_override_reset_not_allowed)
        } else {
            val firstLine = message.lineSequence().firstOrNull().orEmpty().take(120)
            if (firstLine.isBlank()) {
                context.getString(R.string.persistent_override_reset_failed)
            } else {
                context.getString(R.string.persistent_override_reset_failed_with_details, firstLine)
            }
        }
    }

    private fun runShellCommand(context: Context, command: String): String {
        val process = newShizukuProcess(arrayOf("sh", "-c", command))
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val stdoutThread = readAsync(process.inputStream, stdout)
        val stderrThread = readAsync(process.errorStream, stderr)
        val exitCode = process.waitFor()
        stdoutThread.join()
        stderrThread.join()

        if (exitCode != 0) {
            throw IllegalStateException(
                stderr.toString().ifBlank {
                    context.getString(R.string.shell_command_failed_with_exit_code, exitCode)
                }
            )
        }

        return stdout.toString()
    }

    internal fun parseInstrumentationResult(output: String): InstrumentationResult {
        val values = mutableMapOf<String, String>()
        var code: Int? = null

        output.lineSequence().forEach { line ->
            when {
                line.startsWith(INSTRUMENTATION_RESULT_PREFIX) -> {
                    val payload = line.removePrefix(INSTRUMENTATION_RESULT_PREFIX)
                    val separatorIndex = payload.indexOf('=')
                    if (separatorIndex > 0) {
                        values[payload.substring(0, separatorIndex)] =
                            payload.substring(separatorIndex + 1)
                    }
                }

                line.startsWith("INSTRUMENTATION_CODE: ") -> {
                    code = line.removePrefix("INSTRUMENTATION_CODE: ").trim().toIntOrNull()
                }
            }
        }

        return InstrumentationResult(values, code)
    }

    internal fun shellQuote(value: String): String {
        return "'" + value.replace("'", "'\\''") + "'"
    }

    private fun newShizukuProcess(command: Array<String>): Process {
        val newProcess = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        newProcess.isAccessible = true
        return newProcess.invoke(null, command, null, null) as Process
    }

    private fun readAsync(inputStream: InputStream, output: StringBuilder): Thread {
        return Thread {
            inputStream.bufferedReader().use { reader ->
                output.append(reader.readText())
            }
        }.apply { start() }
    }

    internal data class InstrumentationResult(
        val values: Map<String, String>,
        val code: Int?
    )

    data class CarrierConfigOperationResult(
        val warnings: List<String> = emptyList()
    )
}
