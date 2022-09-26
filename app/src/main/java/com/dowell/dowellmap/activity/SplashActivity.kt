package com.dowell.dowellmap.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    @Inject
    lateinit var userpref: UserPref

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

            userpref.isFirstLaunch().asLiveData().observe(this@SplashActivity, Observer
            { hasloggedin->

                if(hasloggedin!=null){

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (hasloggedin) {
                            startNewActivity(MainActivity::class.java)
                        } else {
                            lifecycleScope.launch {
                                //userpref.setFirstLaunch(false)
                                startNewActivity(FeatureActivity::class.java)
                            }
                        }
                        try {
                            this@SplashActivity.finishAffinity()
                        } catch (e: NullPointerException) {
                            if (BuildConfig.DEBUG) {
                                e.printStackTrace()
                            }
                        }
                    }, 5000)


                }


            })

        }
    }
}