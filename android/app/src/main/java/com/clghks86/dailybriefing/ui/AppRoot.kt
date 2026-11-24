package com.clghks86.dailybriefing.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.clghks86.dailybriefing.core.ServiceLocator
import com.clghks86.dailybriefing.domain.model.ThemeMode
import com.clghks86.dailybriefing.ui.home.HomeScreen
import com.clghks86.dailybriefing.ui.news.ArticleScreen
import com.clghks86.dailybriefing.ui.news.NewsScreen
import com.clghks86.dailybriefing.ui.settings.SettingsScreen
import com.clghks86.dailybriefing.ui.theme.DailyBriefingTheme
import com.clghks86.dailybriefing.ui.trend.TrendScreen

enum class AppTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "홈", Icons.Filled.Home),
    TREND("trend", "추이", Icons.Filled.ShowChart),
    NEWS("news", "뉴스", Icons.Filled.Article),
    SETTINGS("settings", "설정", Icons.Filled.Settings),
}

private const val ARTICLE_ROUTE = "article/{date}"

@Composable
fun AppRoot() {
    val themeMode by ServiceLocator.userPreferences.themeMode
        .collectAsStateWithLifecycle(initialValue = ThemeMode.DARK)

    DailyBriefingTheme(mode = themeMode) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val showBottomBar = AppTab.entries.any { it.route == currentRoute }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    if (currentRoute == tab.route) return@NavigationBarItem
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = AppTab.HOME.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding()),
            ) {
                composable(AppTab.HOME.route) {
                    HomeScreen(
                        onOpenArticle = { date -> navController.navigate("article/$date") },
                    )
                }
                composable(AppTab.TREND.route) { TrendScreen() }
                composable(AppTab.NEWS.route) {
                    NewsScreen(
                        onOpenArticle = { date -> navController.navigate("article/$date") },
                    )
                }
                composable(AppTab.SETTINGS.route) { SettingsScreen() }
                composable(ARTICLE_ROUTE) { entry ->
                    val date = entry.arguments?.getString("date").orEmpty()
                    ArticleScreen(
                        date = date,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
