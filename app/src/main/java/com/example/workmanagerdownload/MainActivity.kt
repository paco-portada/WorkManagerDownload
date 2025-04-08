package com.example.workmanagerdownload

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.workmanagerdownload.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityMainBinding
    lateinit var downloadWorkRequest: OneTimeWorkRequest
    // lateinit var downloadWorkRequest: PeriodicWorkRequest
    var start: Boolean = false

    companion object {
        const val WEB = "https://dam.org.es/ficheros/frases.html"
        const val FILE_NAME = "frases.html"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        */
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        binding.botonIniciar.setOnClickListener(this)
        binding.botonParar.setOnClickListener(this)

    }

    override fun onClick(v: View) {

        binding.salida.text = ""

        if (v == binding.botonIniciar) {
            startDownload(WEB, FILE_NAME)
            start = true
            binding.botonIniciar.isEnabled = false
        }
        if (v == binding.botonParar)
            if (start) {
                mostrarMensaje(downloadWorkRequest.id.toString() + " parado")
                Log.i("Parar", downloadWorkRequest.id.toString() + " parado")
                WorkManager.getInstance(this).cancelWorkById(downloadWorkRequest.id)
                start = false
                binding.botonIniciar.isEnabled = true
            }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun startDownload(webUrl: String, fileName: String) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputWorkData = workDataOf(
            DownloadWorker.KEY_WEB_URL to webUrl,
            DownloadWorker.KEY_FILE_NAME to fileName
        )

        downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputWorkData)
            .build()

        // downloadWorkRequest = PeriodicWorkRequestBuilder<DownloadWorker>(1, TimeUnit.HOURS).build()

        WorkManager.getInstance(this).enqueue(downloadWorkRequest)

        // You can observe the work's progress and output if needed:
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(downloadWorkRequest.id)
            .observe(this) {
                workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        val filePath = workInfo.outputData.getString(DownloadWorker.KEY_FILE_PATH)
                        // Do something with the downloaded file path
                        Log.d("Download", "File downloaded to: $filePath")
                        mostrarMensaje(filePath.toString())
                        binding.salida.text = filePath.toString()
                        binding.botonIniciar.isEnabled = true
                    }

                    WorkInfo.State.FAILED -> {
                        // Handle failure
                        val errorMessage = workInfo.outputData.getString(DownloadWorker.ERROR_MESSAGE)
                        // Do something with the error message
                        Log.e("Download", "Download failed: $errorMessage")
                        mostrarMensaje(errorMessage.toString())
                        binding.salida.text = errorMessage.toString()
                        binding.botonIniciar.isEnabled = true
                    }
                    // ... other states (RUNNING, ENQUEUED, etc.)
                    WorkInfo.State.ENQUEUED ->
                        mostrarMensaje("Enqueued")
                    WorkInfo.State.RUNNING ->
                        mostrarMensaje("Running")
                    WorkInfo.State.BLOCKED ->
                        mostrarMensaje("Blocked")
                    WorkInfo.State.CANCELLED ->
                        mostrarMensaje("Cancelled")
                    else ->
                        mostrarMensaje("Otro estado")
                }
            }
    }

    public override fun onDestroy() {
        super.onDestroy()
        //cancelar los trabajos con WorkManager
        // if (start)
            // WorkManager.getInstance().cancelAllWork()
            WorkManager.getInstance(this).cancelAllWork()
    }
}