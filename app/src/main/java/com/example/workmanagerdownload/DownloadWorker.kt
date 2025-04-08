package com.example.workmanagerdownload

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadWorker(appContext: Context, params: WorkerParameters) :  CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "DownloadWorker"
        const val KEY_WEB_URL = "web_url"
        const val KEY_FILE_NAME = "file_name"
        const val KEY_FILE_PATH = "file_path"
        const val ERROR_MESSAGE = "error_message"
    }

    override suspend fun doWork(): Result {
        val webUrl = inputData.getString(KEY_WEB_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()

        return try {
            Thread.sleep(5000)
            val route = withContext(Dispatchers.IO) {
                downloadFile(webUrl, fileName)
            }
            val outputData = workDataOf(KEY_FILE_PATH to route)
            Result.success(outputData)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file: ${e.message}", e)
            val outputData = workDataOf(ERROR_MESSAGE to "Download failed: ${e.message}")
            Result.failure(outputData)
        }
    }

    private fun downloadFile(url: String, fileName: String): String {

        val url = URL(url)
        val connection = url.openConnection()
        connection.connect()

        val inputStream = BufferedInputStream(connection.getInputStream())
        val file = File(applicationContext.getExternalFilesDir(null), fileName) // Or use internal storage: File(applicationContext.filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream.copyTo(outputStream)

        outputStream.close()
        inputStream.close()

        return file.absolutePath
    }
}
