package com.dowell.dowellmap.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.viewbinding.BuildConfig
import com.dowell.dowellmap.databinding.ActivitySplashScreenBinding
import com.dowell.dowellmap.startNewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){

            try {
                val packageInfo = applicationContext.packageManager.getPackageInfo(
                    packageName, 0
                )
                val version = "Version: " + packageInfo.versionName
                txtVersion.text = version
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                startNewActivity(MainActivity::class.java)
                try {
                    this@SplashActivity.finishAffinity()
                } catch (e: NullPointerException) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace()
                    }
                }
            }, 5000)


        }
    }
}