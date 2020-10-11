package ru.relabs.kurjercontroller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import ru.relabs.kurjercontroller.presentation.host.HostActivity
import ru.relabs.kurjercontroller.services.ReportService

class SplashActivity : AppCompatActivity() {
    private val supervisor = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + supervisor)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startService(Intent(this, ReportService::class.java))

        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                startActivity(HostActivity.getIntent(this@SplashActivity))
                finish()
            }
        }
    }


    override fun onBackPressed() {
        return
    }
}
