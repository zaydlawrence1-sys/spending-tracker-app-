package com.example.spendingtrackerapp
//Student number: ST10545838 Name: Zayd Surname: Lawrence 
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpendingTrackerTheme {
                SpendingTrackerApp()
            }
        }
    }
}

// ----------------------------------------------------
// FOUR PARALLEL ARRAYS - SAMPLE DATA FOR ONE WEEK
// ----------------------------------------------------

val dates = arrayOf(
    "2026-10-07", "2026-10-08", "2026-10-09", "2026-10-10",
    "2026-10-11", "2026-10-12", "2026-10-13"
)

val dailyBudget = doubleArrayOf(500.0, 300.0, 400.0, 350.0, 450.0, 250.0, 500.0)

val dailySpent = doubleArrayOf(340.0, 220.0, 450.0, 280.0, 390.0, 180.0, 420.0)

val comments = arrayOf(
    "Grocery shopping", "Takeaways", "Petrol and groceries", "Lunch and shopping",
    "Household items", "Coffee and snacks", "Weekly groceries"
)

// Checked once instead of on every recomposition
val dataIsValid: Boolean =
    dates.size == dailyBudget.size &&
            dailyBudget.size == dailySpent.size &&
            dailySpent.size == comments.size

// ----------------------------------------------------
// HELPERS
// ----------------------------------------------------

fun money(amount: Double): String {
    val sign = if (amount < 0) "-" else ""
    return "${sign}R" + String.format(Locale.US, "%,.2f", abs(amount))
}

fun spentFraction(spent: Double, budget: Double): Float =
    if (budget <= 0.0) 0f else (spent / budget).toFloat().coerceIn(0f, 1f)

// ----------------------------------------------------
// THEME (light + dark, green "money" palette)
// ----------------------------------------------------

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B7F5C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBFEBD8),
    onPrimaryContainer = Color(0xFF00391F),
    secondaryContainer = Color(0xFFD6E8DE),
    background = Color(0xFFF6FAF7),
    surface = Color(0xFFF6FAF7),
    surfaceVariant = Color(0xFFFFFFFF),
    error = Color(0xFFC62828)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FD8AC),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF005236),
    onPrimaryContainer = Color(0xFFBFEBD8),
    secondaryContainer = Color(0xFF2B3E34),
    background = Color(0xFF101512),
    surface = Color(0xFF101512),
    surfaceVariant = Color(0xFF1B231F),
    error = Color(0xFFFF8A80)
)

@Composable
fun SpendingTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}

// ----------------------------------------------------
// MAIN APP - CONTROLS WHICH SCREEN IS DISPLAYED
// ----------------------------------------------------

enum class Screen { Splash, Main, View, Calculate }

@Composable
fun SpendingTrackerApp() {

    // rememberSaveable keeps the screen after rotation
    var currentScreen by rememberSaveable { mutableStateOf(Screen.Splash) }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.Splash) {
            delay(2000)
            currentScreen = Screen.Main
        }
    }

    // System back button goes to the main screen instead of closing the app
    BackHandler(enabled = currentScreen == Screen.View || currentScreen == Screen.Calculate) {
        currentScreen = Screen.Main
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Crossfade(targetState = currentScreen, label = "screens") { screen ->
            when (screen) {
                Screen.Splash -> SplashScreen()
                Screen.Main -> MainScreen(
                    onViewSpending = { currentScreen = Screen.View },
                    onCalculate = { currentScreen = Screen.Calculate }
                )
                Screen.View -> ViewScreen(onBack = { currentScreen = Screen.Main })
                Screen.Calculate -> CalculateScreen(onBack = { currentScreen = Screen.Main })
            }
        }
    }
}

// ----------------------------------------------------
// SPLASH SCREEN
// ----------------------------------------------------

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "💰", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Spending Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Track your daily spending",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ----------------------------------------------------
// MAIN SCREEN
// ----------------------------------------------------

@Composable
fun MainScreen(
    onViewSpending: () -> Unit,
    onCalculate: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(text = "💰", fontSize = 56.sp)

        Text(
            text = "Spending Tracker",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (dataIsValid) {
            WeekSummaryCard()
            Spacer(modifier = Modifier.height(32.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onViewSpending,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("View All Spending")
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilledTonalButton(
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Calculate Remaining Budget")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { (context as? Activity)?.finishAffinity() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Exit")
        }
    }
}

@Composable
fun WeekSummaryCard() {
    val totalBudget = dailyBudget.sum()
    val totalSpent = dailySpent.sum()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("This week", style = MaterialTheme.typography.labelLarge)
            Text(
                text = money(totalSpent),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "spent of ${money(totalBudget)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { spentFraction(totalSpent, totalBudget) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

// ----------------------------------------------------
// SHARED SCREEN LAYOUT (title + list + back button)
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreenScaffold(
    title: String,
    onBack: () -> Unit,
    content: LazyListScopeContent
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

typealias LazyListScopeContent = androidx.compose.foundation.lazy.LazyListScope.() -> Unit

@Composable
fun ErrorScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: Spending data is incomplete.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

// ----------------------------------------------------
// VIEW ALL SPENDING SCREEN
// ----------------------------------------------------

@Composable
fun ViewScreen(onBack: () -> Unit) {

    if (!dataIsValid) {
        ErrorScreen(onBack)
        return
    }

    ListScreenScaffold(title = "All Spending", onBack = onBack) {
        items(dates.indices.toList()) { i ->
            val overBudget = dailySpent[i] > dailyBudget[i]

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dates[i],
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = money(dailySpent[i]),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (overBudget) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = comments[i],
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { spentFraction(dailySpent[i], dailyBudget[i]) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (overBudget) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Daily budget: ${money(dailyBudget[i])}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// CALCULATE REMAINING BUDGET SCREEN
// ----------------------------------------------------

@Composable
fun CalculateScreen(onBack: () -> Unit) {

    if (!dataIsValid) {
        ErrorScreen(onBack)
        return
    }

    // Calculated up front - NOT inside the UI loop (that caused a side effect
    // during composition in the original code)
    val remaining = DoubleArray(dates.size) { i -> dailyBudget[i] - dailySpent[i] }
    val weeklyRemaining = remaining.sum()

    ListScreenScaffold(title = "Remaining Budget", onBack = onBack) {

        // Weekly total card at the top, where it is seen first
        item {
            val negative = weeklyRemaining < 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (negative) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total remaining for the week", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = money(weeklyRemaining),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(dates.indices.toList()) { i ->
            val overBudget = remaining[i] < 0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = dates[i],
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Budget: ${money(dailyBudget[i])}")
                        Text("Spent: ${money(dailySpent[i])}")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Remaining: ${money(remaining[i])}",
                        fontWeight = FontWeight.Bold,
                        color = if (overBudget) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )

                    // Warning if the user spends more than their budget
                    if (overBudget) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ You spent more than your daily budget!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
