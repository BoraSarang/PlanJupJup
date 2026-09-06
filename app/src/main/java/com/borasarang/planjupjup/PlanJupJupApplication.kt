package com.borasarang.planjupjup

import android.app.Application
import com.borasarang.planjupjup.data.db.PlanDatabase
import com.borasarang.planjupjup.data.preferences.PreferencesManager
import com.borasarang.planjupjup.data.repository.NotificationRepository
import com.borasarang.planjupjup.data.repository.NotificationService
import com.borasarang.planjupjup.data.repository.PlanRepository
import com.borasarang.planjupjup.data.repository.SourceRepository
import com.borasarang.planjupjup.data.repository.StatsRepository
import com.borasarang.planjupjup.data.seed.InitialDataSeeder
import com.borasarang.planjupjup.server.HttpServerService
import com.borasarang.planjupjup.util.DebugLogger
import com.borasarang.planjupjup.worker.CrawlScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlanJupJupApplication : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: PlanDatabase
        private set
    lateinit var planRepository: PlanRepository
        private set
    lateinit var sourceRepository: SourceRepository
        private set
    lateinit var statsRepository: StatsRepository
        private set
    lateinit var notificationRepository: NotificationRepository
        private set
    lateinit var notificationService: NotificationService
        private set
    lateinit var preferences: PreferencesManager
        private set
    lateinit var crawlScheduler: CrawlScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        DebugLogger.init()
        DebugLogger.i("앱", "알뜰요금줍줍 시작")

        database = try {
            PlanDatabase.getInstance(this)
        } catch (e: Exception) {
            // 마이그레이션 실패 등 DB 열기 불가 → 재생성 폴백 (앱 벽돌 방지)
            DebugLogger.e("앱", "E-AND-DB-0403", "DB 열기 실패, 재생성: ${e.message}", e)
            PlanDatabase.getInstanceFallback(this)
        }
        statsRepository = StatsRepository(database)
        planRepository = PlanRepository(database, statsRepository)
        sourceRepository = SourceRepository(database)
        notificationRepository = NotificationRepository(database)
        preferences = PreferencesManager.getInstance(this)
        notificationService = NotificationService(database, planRepository, sourceRepository, preferences)
        crawlScheduler = CrawlScheduler(this)

        appScope.launch(Dispatchers.IO) {
            InitialDataSeeder.seedIfEmpty(database)
            crawlScheduler.scheduleAll()
            crawlScheduler.scheduleDailySummary()
            val settings = preferences.getSettings()
            if (settings.autoStart) {
                DebugLogger.i("앱", "자동 시작 설정 켜짐 — 서버 시작")
                HttpServerService.start(this@PlanJupJupApplication)
            } else {
                DebugLogger.i("앱", "자동 시작 꺼짐 — 서버 미시작")
            }
        }
    }

    companion object {
        lateinit var instance: PlanJupJupApplication
            private set
    }
}
