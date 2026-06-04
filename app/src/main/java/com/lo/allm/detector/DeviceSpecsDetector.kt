package com.lo.allm.detector

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES20
import android.os.Build
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

class DeviceSpecsDetector(private val context: Context) {

    fun getCPUInfo(): String {
        val cores = Runtime.getRuntime().availableProcessors()
        val abi = Build.SUPPORTED_ABIS.joinToString(", ")
        val hardware = Build.HARDWARE ?: "Unknown"
        
        var cpuModel = "Unknown"
        try {
            val br = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                if (line!!.startsWith("Hardware")) {
                    cpuModel = line!!.split(":")[1].trim()
                    break
                }
            }
            br.close()
        } catch (e: IOException) {
            cpuModel = Build.MODEL ?: "Unknown"
        }

        return "CPU: $cpuModel | $cores cores | $abi"
    }

    fun getRAMInfo(): String {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        
        val totalRAM = memInfo.totalMem / (1024 * 1024)
        val availRAM = memInfo.availMem / (1024 * 1024)
        
        return "RAM: ${formatRAM(totalRAM)} total | ${formatRAM(availRAM)} available"
    }

    fun getGPUInfo(): String {
        return try {
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown GPU"
            val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown Vendor"
            
            "GPU: $renderer | Vendor: $vendor"
        } catch (e: Exception) {
            "GPU: Gak kedeteksi"
        }
    }

    fun getTotalRAMMB(): Long {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return memInfo.totalMem / (1024 * 1024)
    }

    fun hasGPU(): Boolean {
        return try {
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
            renderer != null && renderer != "Android Emulator OpenGL ES Translator"
        } catch (e: Exception) {
            false
        }
    }

    fun recommendModel(): String {
        val totalRAM = getTotalRAMMB()
        val hasGPU = hasGPU()

        return when {
            totalRAM >= 12000 && hasGPU -> "llama3"
            totalRAM >= 8000 -> if (hasGPU) "llama3" else "phi3-mini"
            totalRAM >= 6000 -> "phi3-mini"
            totalRAM >= 4000 -> "tinyllama"
            else -> "tinyllama"
        }
    }

    fun recommendConfig(): Map<String, Any> {
        val totalRAM = getTotalRAMMB()
        val cores = Runtime.getRuntime().availableProcessors()

        return when {
            totalRAM >= 12000 -> mapOf(
                "context_size" to 8192,
                "threads" to minOf(cores, 8),
                "gpu_layers" to "all"
            )
            totalRAM >= 8000 -> mapOf(
                "context_size" to 4096,
                "threads" to minOf(cores, 6),
                "gpu_layers" to "partial"
            )
            totalRAM >= 6000 -> mapOf(
                "context_size" to 4096,
                "threads" to minOf(cores, 4),
                "gpu_layers" to 0
            )
            else -> mapOf(
                "context_size" to 2048,
                "threads" to minOf(cores, 3),
                "gpu_layers" to 0
            )
        }
    }

    fun getFullSpecs(): String {
        return """
            ${getCPUInfo()}
            ${getRAMInfo()}
            ${getGPUInfo()}
            
            Device: ${Build.MANUFACTURER} ${Build.MODEL}
            Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            
            Rekomendasi Model: ${recommendModel()}
            Config: ${recommendConfig()}
        """.trimIndent()
    }

    private fun formatRAM(mb: Long): String {
        return if (mb >= 1024) {
            "%.1f GB".format(mb / 1024.0)
        } else {
            "$mb MB"
        }
    }
}
