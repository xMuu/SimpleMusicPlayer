package dev.xmuu.smp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.MutableLiveData
import com.tencent.mmkv.MMKV
import dev.xmuu.smp.service.MusicService
import dev.xmuu.smp.service.MusicServiceConnection
import rxhttp.wrapper.annotation.DefaultDomain

@DefaultDomain
const val baseUrl = "https://api.163.xmuu.dev/"

class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        var musicController = MutableLiveData<MusicService.MusicController?>()
        val musicServiceConnection by lazy { MusicServiceConnection() }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        MMKV.initialize(this)
        startMusicService()
    }

    private fun startMusicService() {
        val intent = Intent(this, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, musicServiceConnection, BIND_AUTO_CREATE)
    }

}


