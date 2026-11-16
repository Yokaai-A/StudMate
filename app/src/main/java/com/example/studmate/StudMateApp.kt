package com.example.studmate

import android.app.Application
import androidx.room.Room
import com.example.studmate.data.SeedData
import com.example.studmate.data.local.AppDatabase
import com.example.studmate.data.prefs.SessionStore
import com.example.studmate.data.repo.AuthRepository
import com.example.studmate.data.repo.CatalogRepository
import com.example.studmate.data.repo.LearningRepository
import com.example.studmate.data.repo.Notifier
import com.example.studmate.data.repo.OrganizerRepository
import com.example.studmate.notif.NotificationHelper
import com.example.studmate.notif.ReminderScheduler
import com.example.studmate.ui.viewmodel.AuthViewModel
import com.example.studmate.ui.viewmodel.ClassBuilderViewModel
import com.example.studmate.ui.viewmodel.ClassDetailViewModel
import com.example.studmate.ui.viewmodel.ClassHubViewModel
import com.example.studmate.ui.viewmodel.CompareViewModel
import com.example.studmate.ui.viewmodel.DiscoverViewModel
import com.example.studmate.ui.viewmodel.MyLearningViewModel
import com.example.studmate.ui.viewmodel.OrganizerViewModel
import com.example.studmate.ui.viewmodel.ProfileViewModel
import com.example.studmate.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "studmate.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().classDao() }
    single { get<AppDatabase>().enrollmentDao() }
    single { get<AppDatabase>().communityDao() }
    single { get<AppDatabase>().walletDao() }

    single { SessionStore(androidContext()) }
    single { Notifier(androidContext(), get(), get()) }
    single { ReminderScheduler(androidContext()) }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single { AuthRepository(get(), get(), get()) }
    single { CatalogRepository(get(), get(), get(), get(), get()) }
    single { LearningRepository(get(), get(), get(), get()) }
    single { OrganizerRepository(get(), get(), get()) }

    viewModel { AuthViewModel(get(), get()) }
    viewModel { DiscoverViewModel(get(), get(), get()) }
    viewModel { CompareViewModel(get()) }
    viewModel { MyLearningViewModel(get(), get()) }
    viewModel { (classId: String) -> ClassDetailViewModel(classId, get(), get(), get()) }
    viewModel { (classId: String) -> ClassHubViewModel(classId, get(), get(), get(), get()) }
    viewModel { OrganizerViewModel(get(), get(), get()) }
    viewModel { (classId: String?) -> ClassBuilderViewModel(classId, get(), get(), get()) }
    viewModel { WalletViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
}

class StudMateApp : Application() {
    private val db: AppDatabase by inject()
    private val session: SessionStore by inject()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@StudMateApp)
            modules(appModule)
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            SeedData.populateIfEmpty(db)
            if (session.currentUserIdNow() == null) {
                session.setCurrentUser(SeedData.DEMO_LEARNER_ID)
            }
        }
    }
}
