package com.clghks86.dailybriefing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.clghks86.dailybriefing.ui.AppRoot
import com.clghks86.dailybriefing.work.WorkScheduler

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }

    override fun onResume() {
        super.onResume()
        // 09:35가 지난 뒤 앱을 열었다면 그 자리에서 한 번 따라잡기 체크.
        runCatching { WorkScheduler.runCatchUpCheck(this) }
    }
}
