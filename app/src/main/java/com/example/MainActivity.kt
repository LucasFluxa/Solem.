package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.ui.updater.UpdateChecker
import com.example.BuildConfig
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.data.api.NetworkModule
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.UsmDataRepositoryImpl
import com.example.ui.components.SolemBottomBar
import com.example.ui.components.SolemTab
import com.example.ui.configuracion.ConfiguracionScreen
import com.example.ui.horario.HorarioScreen
import com.example.ui.horario.HorarioViewModel
import com.example.ui.malla.MallaScreen
import com.example.ui.malla.MallaViewModel
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.profesores.ProfesoresScreen
import com.example.ui.profesores.ProfesoresViewModel
import com.example.ui.theme.SolemAccentCyan
import com.example.ui.theme.SolemBackground
import com.example.ui.theme.SolemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val apiService = NetworkModule.apiService
        val moshi = NetworkModule.moshi
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        val repository = UsmDataRepositoryImpl(apiService, database, moshi, userPreferencesRepository, applicationContext)

        // Programar sincronización periódica en segundo plano cada 12 horas
        com.example.data.worker.DataSyncWorker.schedulePeriodicSync(applicationContext)

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (!repository.isDataAvailable()) {
                repository.syncAllData(false)
            }
        }

        setContent {
            SolemTheme {
                val preferencesState by userPreferencesRepository.userPreferencesFlow
                    .collectAsState(initial = null)
                var splashCompleted by remember { mutableStateOf(false) }

                AnimatedContent(
                    targetState = !splashCompleted || preferencesState == null,
                    label = "AppTransition"
                ) { isSplash ->
                    if (isSplash) {
                        com.example.ui.splash.SplashScreen(
                            onAnimationFinished = { splashCompleted = true }
                        )
                    } else {
                        val prefs = preferencesState!!
                        if (!prefs.isOnboardingCompleted) {
                            OnboardingScreen(
                                repository = repository,
                                userPreferencesRepository = userPreferencesRepository,
                                onFinished = { /* StateFlow updates automatically */ }
                            )
                        } else {
                            SolemApp(
                                repository = repository,
                                userPreferencesRepository = userPreferencesRepository,
                                userPreferences = prefs
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SolemApp(
    repository: UsmDataRepositoryImpl,
    userPreferencesRepository: UserPreferencesRepository,
    userPreferences: UserPreferences
) {
    // Al abrir la app lo primero que se ve es el Horario Propio
    var currentTab by remember { mutableStateOf(SolemTab.HORARIO) }

    val mallaViewModel: MallaViewModel = viewModel(
        factory = MallaViewModel.provideFactory(repository, userPreferencesRepository, NetworkModule.moshi)
    )
    val profesoresViewModel: ProfesoresViewModel = viewModel(
        factory = ProfesoresViewModel.provideFactory(repository, userPreferencesRepository)
    )
    val horarioViewModel: HorarioViewModel = viewModel(
        factory = HorarioViewModel.provideFactory(repository, userPreferencesRepository)
    )

    Scaffold(
        containerColor = SolemBackground,
        bottomBar = {
            SolemBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SolemBackground)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            UpdateChecker(currentVersion = BuildConfig.VERSION_NAME)

            when (currentTab) {
                SolemTab.HORARIO -> HorarioScreen(
                    viewModel = horarioViewModel,
                    onOpenSettings = { currentTab = SolemTab.CONFIGURACION },
                    onNavigateToRamos = { currentTab = SolemTab.MALLA }
                )
                SolemTab.MALLA -> MallaScreen(
                    viewModel = mallaViewModel,
                    onOpenSettings = { currentTab = SolemTab.CONFIGURACION }
                )
                SolemTab.PROFESORES -> ProfesoresScreen(
                    viewModel = profesoresViewModel,
                    onOpenSettings = { currentTab = SolemTab.CONFIGURACION }
                )
                SolemTab.CONFIGURACION -> ConfiguracionScreen(
                    userPreferencesRepository = userPreferencesRepository,
                    repository = repository
                )
            }
        }
    }
}
