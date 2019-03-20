package ru.relabs.kurjercontroller.ui.activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import ru.relabs.kurjercontroller.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideActionBar()
        setContentView(R.layout.activity_splash)

        //startService(Intent(this, ReportService::class.java))

        AsyncTask.execute {
            Thread.sleep(2000)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        return
    }
}
