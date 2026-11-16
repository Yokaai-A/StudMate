package com.example.studmate.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.studmate.data.repo.CatalogRepository
import com.example.studmate.ui.screens.AttendanceScannerScreen
import com.example.studmate.ui.screens.AuthScreen
import com.example.studmate.ui.screens.ClassBuilderScreen
import com.example.studmate.ui.screens.ClassDetailScreen
import com.example.studmate.ui.screens.ClassHubScreen
import com.example.studmate.ui.screens.CompareScreen
import com.example.studmate.ui.screens.DiscoverScreen
import com.example.studmate.ui.screens.MentorProfileScreen
import com.example.studmate.ui.screens.MyLearningScreen
import com.example.studmate.ui.screens.NotificationInboxScreen
import com.example.studmate.ui.screens.OnboardingScreen
import com.example.studmate.ui.screens.OrganizerStudioScreen
import com.example.studmate.ui.screens.ProfileScreen
import com.example.studmate.ui.screens.WalletScreen
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
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

enum class LearnerTab(val title: String) {
    BERANDA("Beranda"),
    COMPARE("Bandingkan"),
    KELASKU("Kelasku"),
    AKUN("Akun"),
}

enum class OrganizerTab(val title: String) {
    STUDIO("Studio"),
    WALLET("Dompet"),
    AKUN("Akun"),
}

@Composable
fun MainScreen(
    initialDeepLink: Uri? = null,
) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val discoverViewModel: DiscoverViewModel = koinViewModel()
    val compareViewModel: CompareViewModel = koinViewModel()
    val myLearningViewModel: MyLearningViewModel = koinViewModel()
    val organizerViewModel: OrganizerViewModel = koinViewModel()
    val walletViewModel: WalletViewModel = koinViewModel()
    val catalogRepo: CatalogRepository = koinInject()

    val currentUser by authViewModel.currentUser.collectAsState()
    val isOrganizerMode by profileViewModel.isOrganizerMode.collectAsState()
    val compareList by discoverViewModel.compareList.collectAsState()
    val unreadNotifCount by profileViewModel.unreadCount.collectAsState()

    var currentLearnerTab by remember { mutableStateOf(LearnerTab.BERANDA) }
    var currentOrganizerTab by remember { mutableStateOf(OrganizerTab.STUDIO) }

    // Update state compareViewModel saat compareList berubah
    LaunchedEffect(compareList) {
        compareViewModel.setComparedIds(compareList)
    }

    // Handle deep link
    LaunchedEffect(initialDeepLink) {
        if (initialDeepLink != null) {
            val scheme = initialDeepLink.scheme
            val host = initialDeepLink.host
            val path = initialDeepLink.pathSegments

            if (scheme == "studmate") {
                when (host) {
                    "class" -> path.firstOrNull()?.let { navController.navigate("class_detail/$it") }
                    "hub" -> path.firstOrNull()?.let { navController.navigate("class_hub/$it") }
                    "manage" -> path.firstOrNull()?.let { navController.navigate("attendance/$it") }
                    "ticket" -> path.firstOrNull()?.let { /* handled via scanner */ }
                }
            } else if (host == "studmate.app") {
                if (path.size >= 2 && path[0] == "class") {
                    navController.navigate("class_detail/${path[1]}")
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) "auth" else if (currentUser?.onboarded == false) "onboarding" else "home",
    ) {
        composable("auth") {
            AuthScreen(
                viewModel = authViewModel,
                onSuccess = {
                    if (currentUser?.onboarded == false) {
                        navController.navigate("onboarding") { popUpTo("auth") { inclusive = true } }
                    } else {
                        navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                    }
                },
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                viewModel = authViewModel,
                onFinished = {
                    navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                },
            )
        }

        composable("home") {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        if (!isOrganizerMode) {
                            // Bottom Nav Mode Belajar (Learner)
                            NavigationBarItem(
                                selected = currentLearnerTab == LearnerTab.BERANDA,
                                onClick = { currentLearnerTab = LearnerTab.BERANDA },
                                icon = { Icon(Icons.Default.Explore, contentDescription = "Beranda") },
                                label = { Text(LearnerTab.BERANDA.title) },
                            )
                            NavigationBarItem(
                                selected = currentLearnerTab == LearnerTab.COMPARE,
                                onClick = { currentLearnerTab = LearnerTab.COMPARE },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (compareList.isNotEmpty()) {
                                                Badge { Text(compareList.size.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.CompareArrows, contentDescription = "Bandingkan")
                                    }
                                },
                                label = { Text(LearnerTab.COMPARE.title) },
                            )
                            NavigationBarItem(
                                selected = currentLearnerTab == LearnerTab.KELASKU,
                                onClick = { currentLearnerTab = LearnerTab.KELASKU },
                                icon = { Icon(Icons.Default.School, contentDescription = "Kelasku") },
                                label = { Text(LearnerTab.KELASKU.title) },
                            )
                            NavigationBarItem(
                                selected = currentLearnerTab == LearnerTab.AKUN,
                                onClick = { currentLearnerTab = LearnerTab.AKUN },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotifCount > 0) {
                                                Badge { Text(unreadNotifCount.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = "Akun")
                                    }
                                },
                                label = { Text(LearnerTab.AKUN.title) },
                            )
                        } else {
                            // Bottom Nav Mode Mengajar (Organizer / Tutor Studio)
                            NavigationBarItem(
                                selected = currentOrganizerTab == OrganizerTab.STUDIO,
                                onClick = { currentOrganizerTab = OrganizerTab.STUDIO },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Studio") },
                                label = { Text(OrganizerTab.STUDIO.title) },
                            )
                            NavigationBarItem(
                                selected = currentOrganizerTab == OrganizerTab.WALLET,
                                onClick = { currentOrganizerTab = OrganizerTab.WALLET },
                                icon = { Icon(Icons.Default.Payments, contentDescription = "Dompet") },
                                label = { Text(OrganizerTab.WALLET.title) },
                            )
                            NavigationBarItem(
                                selected = currentOrganizerTab == OrganizerTab.AKUN,
                                onClick = { currentOrganizerTab = OrganizerTab.AKUN },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotifCount > 0) {
                                                Badge { Text(unreadNotifCount.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = "Akun")
                                    }
                                },
                                label = { Text(OrganizerTab.AKUN.title) },
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    if (!isOrganizerMode) {
                        when (currentLearnerTab) {
                            LearnerTab.BERANDA -> DiscoverScreen(
                                viewModel = discoverViewModel,
                                onClassClick = { navController.navigate("class_detail/$it") },
                                onMentorClick = { navController.navigate("mentor/$it") },
                                onNavigateCompare = { currentLearnerTab = LearnerTab.COMPARE },
                                onNavigateNotifications = { navController.navigate("notifications") },
                                unreadNotifCount = unreadNotifCount,
                            )
                            LearnerTab.COMPARE -> CompareScreen(
                                viewModel = compareViewModel,
                                onBack = { currentLearnerTab = LearnerTab.BERANDA },
                                onSelectClass = { navController.navigate("class_detail/$it") },
                            )
                            LearnerTab.KELASKU -> MyLearningScreen(
                                viewModel = myLearningViewModel,
                                onOpenHub = { navController.navigate("class_hub/$it") },
                                onExplore = { currentLearnerTab = LearnerTab.BERANDA },
                            )
                            LearnerTab.AKUN -> ProfileScreen(
                                profileViewModel = profileViewModel,
                                authViewModel = authViewModel,
                                onNavigateNotifications = { navController.navigate("notifications") },
                            )
                        }
                    } else {
                        when (currentOrganizerTab) {
                            OrganizerTab.STUDIO -> OrganizerStudioScreen(
                                viewModel = organizerViewModel,
                                onCreateClass = { navController.navigate("class_builder") },
                                onEditClass = { navController.navigate("class_builder?classId=$it") },
                                onScanAttendance = { navController.navigate("attendance/$it") },
                                onOpenWallet = { currentOrganizerTab = OrganizerTab.WALLET },
                                onSwitchToLearner = { profileViewModel.toggleOrganizerMode() },
                            )
                            OrganizerTab.WALLET -> WalletScreen(
                                viewModel = walletViewModel,
                                onBack = { currentOrganizerTab = OrganizerTab.STUDIO },
                            )
                            OrganizerTab.AKUN -> ProfileScreen(
                                profileViewModel = profileViewModel,
                                authViewModel = authViewModel,
                                onNavigateNotifications = { navController.navigate("notifications") },
                            )
                        }
                    }
                }
            }
        }

        composable(
            route = "class_detail/{classId}",
            arguments = listOf(navArgument("classId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "studmate://class/{classId}" },
                navDeepLink { uriPattern = "https://studmate.app/class/{classId}" },
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId").orEmpty()
            val vm: ClassDetailViewModel = koinViewModel(parameters = { parametersOf(classId) })
            ClassDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenHub = { navController.navigate("class_hub/$it") },
                onOpenMentor = { navController.navigate("mentor/$it") },
            )
        }

        composable(
            route = "class_hub/{classId}",
            arguments = listOf(navArgument("classId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "studmate://hub/{classId}" })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId").orEmpty()
            val vm: ClassHubViewModel = koinViewModel(parameters = { parametersOf(classId) })
            ClassHubScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "class_builder?classId={classId}",
            arguments = listOf(navArgument("classId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId")
            val vm: ClassBuilderViewModel = koinViewModel(parameters = { parametersOf(classId) })
            ClassBuilderScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = "attendance/{classId}",
            arguments = listOf(navArgument("classId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "studmate://manage/{classId}" })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId").orEmpty()
            AttendanceScannerScreen(
                classId = classId,
                viewModel = organizerViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable("wallet") {
            WalletScreen(
                viewModel = walletViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable("notifications") {
            NotificationInboxScreen(
                viewModel = profileViewModel,
                onBack = { navController.popBackStack() },
                onOpenClass = { classId ->
                    navController.navigate("class_hub/$classId")
                },
            )
        }

        composable(
            route = "mentor/{mentorId}",
            arguments = listOf(navArgument("mentorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mentorId = backStackEntry.arguments?.getString("mentorId").orEmpty()
            MentorProfileScreen(
                mentorId = mentorId,
                catalogRepo = catalogRepo,
                onBack = { navController.popBackStack() },
                onClassClick = { navController.navigate("class_detail/$it") },
            )
        }
    }
}
