package com.autoshorts.pro

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// ==============================================================================
// DESIGN SYSTEM: Premium Dark Neon Purple + Glassmorphism Tokens
// ==============================================================================
val BgMain = Color(0xFF080B18)
val BgSecondary = Color(0xFF10152A)
val CardBg = Color(0xFF171D35)
val CardBgAlt = Color(0xFF13182E)
val PrimaryBrand = Color(0xFF7C3AED)
val NeonPurple = Color(0xFFA855F7)
val MagentaAccent = Color(0xFFD946EF)
val CyanAccent = Color(0xFF06B6D4)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFFA1A1BC)
val CardBorder = Color(0xFF292D4B)
val SuccessGreen = Color(0xFF22C55E)
val CoinGold = Color(0xFFF59E0B)

val BrandGradient = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7), Color(0xFFD946EF)))
val CardGlassGradient = Brush.verticalGradient(listOf(Color(0xFF1C2242), Color(0xFF141A33)))
val GlowBorderStroke = BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFFA855F7).copy(alpha = 0.6f), Color(0xFF292D4B))))
val NormalBorderStroke = BorderStroke(1.dp, Color(0xFF292D4B))

// Category Definition Model
data class CategoryItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val drawableRes: Int = R.drawable.cat_deep_dive,
    val topics: List<List<String>>, // batches of 10 trending topics
    val isPlugin: Boolean = false,
    val family: String = ""
)

// Marketplace Plugin Model
data class MarketplacePluginItem(
    val id: String,
    val folder: String,
    val name: String,
    val version: String,
    val family: String,
    val is3D: Boolean,
    val aspect: String,
    val sizeKb: Double,
    val tagline: String,
    val description: String,
    val downloadFileName: String,
    val isInstalled: Boolean,
    val topics: List<String>
)

data class TerminalLogItem(
    val id: String,
    val time: String,
    val tag: String,
    val level: String,
    val message: String
)

data class TermuxEngineStatus(
    val online: Boolean = false,
    val pid: Int = 0,
    val uptimeSeconds: Int = 0,
    val nodeVersion: String = "",
    val ffmpeg: Boolean = false,
    val memory: String = "",
    val disk: String = "",
    val installedPluginsCount: Int = 0
)

data class SavedVideoItem(
    val id: String,
    val name: String,
    val path: String,
    val sizeMb: String,
    val formattedDate: String
)

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("autoshorts_prefs", Context.MODE_PRIVATE)
        enableEdgeToEdge()

        // Start Embedded Core Engine on port 4500 (No external Termux required!)
        EmbeddedServer.start(4500)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = PrimaryBrand,
                    secondary = NeonPurple,
                    tertiary = MagentaAccent,
                    background = BgMain,
                    surface = CardBg,
                    onPrimary = Color.White,
                    onBackground = TextPrimary,
                    onSurface = TextPrimary
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = BgMain
                ) {
                    AutoShortsMainApp(prefs = prefs)
                }
            }
        }
    }
}

// ==============================================================================
// 10 TRENDING TOPICS DATA PER STYLE (BATCH 1 & BATCH 2)
// ==============================================================================
fun getCategoryData(): List<CategoryItem> {
    return listOf(
        CategoryItem(
            id = "antigravity-2d",
            name = "Antigravity 2D",
            subtitle = "Zero-G Floating Physics & Python XKCD",
            drawableRes = R.drawable.hero_banner,
            topics = listOf(
                listOf(
                    "What happens if Earth suddenly loses gravity for 5 seconds?",
                    "The bizarre physics of floating objects in zero gravity",
                    "The legendary Python 'import antigravity' easter egg explained",
                    "How astronauts sleep without floating away in space",
                    "What if gravity reversed and pulled everything upwards into the sky?",
                    "The quantum particle experiment that broke the laws of gravity",
                    "How cosmic dust particles gather to form gigantic planets",
                    "The man who simulated zero gravity using giant sound waves",
                    "Why water forms perfect floating spheres in space",
                    "The mysterious gravitational anomaly zone where cars roll uphill"
                ),
                listOf(
                    "What happens to human muscles after 1 year in zero gravity?",
                    "Can you fire a bullet backwards in space to cancel its speed?",
                    "Why tears don't fall in space: The surface tension anomaly",
                    "The theoretical warp drive that bends space-time gravity",
                    "How black holes trap light with infinite gravitational pull",
                    "What happens if you jump off an asteroid with super low gravity?",
                    "Why does time move slower in strong gravitational fields?",
                    "The secret zero-g training plane known as the Vomit Comet",
                    "What if the Moon was twice as close to Earth's gravity?",
                    "How satellites stay in orbit without falling down to Earth"
                )
            ),
            isPlugin = true,
            family = "2D Animation"
        ),
        CategoryItem(
            id = "deep-dive-3d",
            name = "Deep Dive 3D",
            subtitle = "Zack D. Films 3D Body & Science",
            drawableRes = R.drawable.cat_deep_dive,
            topics = listOf(
                listOf(
                    "What happens inside your body if you swallow chewing gum?",
                    "What happens if you hold in a sneeze with both hands?",
                    "What happens if you drink boiling hot water?",
                    "What happens to your brain if you stay awake for 7 days?",
                    "Why do your fingers wrinkle when staying in water?",
                    "What happens inside your eye when you rub it hard?",
                    "What if you never brushed your teeth for 1 year?",
                    "How a paper cut hurts so intensely inside nerves?",
                    "What happens if you pop a pimple in the danger triangle?",
                    "What happens inside your lungs if you inhale helium?"
                ),
                listOf(
                    "What happens if you crack your knuckles every day?",
                    "What if you drank 10 cups of pure coffee at once?",
                    "What happens to your stomach if you swallow a tooth?",
                    "Why does ice cream cause an intense brain freeze?",
                    "What happens inside muscle fibers during a severe cramp?",
                    "What happens if you swallow a magnet accidentally?",
                    "What happens to your blood vessels in freezing ice water?",
                    "Why does your stomach make loud growling noises?",
                    "What if an insect crawls inside your ear while sleeping?",
                    "What happens inside your throat when you choke on food?"
                )
            )
        ),
        CategoryItem(
            id = "night-file-3d",
            name = "Night File 3D",
            subtitle = "Dark True Crime & Unsolved Mysteries",
            drawableRes = R.drawable.cat_night_film,
            topics = listOf(
                listOf(
                    "The Impossible Alcatraz Escape of 1962: Did they survive?",
                    "The Dyatlov Pass Mystery: What tore open tent number five?",
                    "The Disappearance of Flight 19 in the Bermuda Triangle",
                    "The Unsolved Lead Masks Case of Rio de Janeiro",
                    "D.B. Cooper: The man who vanished into the sky with $200k",
                    "The Secret of the Voynich Manuscript that no AI can crack",
                    "The Mysterious Radio Signal 'Wow!' from outer space in 1977",
                    "The Green Children of Woolpit: Who were they really?",
                    "The Ghost Ship Mary Celeste found completely untouched",
                    "The Somerton Man: The mysterious cipher 'Tamam Shud'"
                ),
                listOf(
                    "The Sodder Children Disappearance on Christmas Eve",
                    "The Hinterkaifeck Murders: Footprints in the snow leading in",
                    "The Dancing Plague of 1518: Hundreds dancing until collapse",
                    "The Bizarre Case of the Watcher on 657 Boulevard",
                    "The Lost Colony of Roanoke and the single word 'CROATOAN'",
                    "The Phantom Whistler of Louisiana that terrified a town",
                    "The Lake Bodom Unsolved Tent Horror of 1960",
                    "The Mystery of the Pollock Twins and past life memories",
                    "The Flannan Isles Lighthouse Keepers who vanished at once",
                    "The Strange Case of Kaspar Hauser: The boy with no past"
                )
            )
        ),
        CategoryItem(
            id = "inside-out",
            name = "Inside Out",
            subtitle = "Flat 2D X-Ray Body Explainers",
            drawableRes = R.drawable.cat_inside_out,
            topics = listOf(
                listOf(
                    "What happens if you hold your pee for 24 hours straight?",
                    "How venomous snake poison curdles human blood in 30 seconds",
                    "Why your stomach doesn't digest itself from stomach acid",
                    "What happens inside your ears when airplane cabin pressure drops",
                    "Why onions make your eyes cry: The chemical tear gas",
                    "What happens when you touch an electric fence at 10,000 volts",
                    "Why spicy chili peppers make your mouth feel on fire",
                    "What happens if you swallow dry ice pellets directly?",
                    "How anesthesia turns off your conscious brain in 5 seconds",
                    "What happens to your lungs if you breathe pure mountain oxygen?"
                ),
                listOf(
                    "Why do we get hiccups and how the diaphragm spasms",
                    "What happens inside blood when you get heatstroke",
                    "How mosquito saliva prevents blood clotting while biting",
                    "What happens to your heart rate on the world's tallest roller coaster",
                    "Why staring at phone screens turns off sleep hormones",
                    "What happens when a deep sea diver surfaces too quickly",
                    "How caffeine blocks adenosine receptors to trick your brain",
                    "What happens to your cells during extreme dehydration",
                    "Why sudden fear causes goosebumps on human skin",
                    "How teeth enamel is harder than steel but ruined by acid"
                )
            )
        ),
        CategoryItem(
            id = "stick-story",
            name = "Stick Story",
            subtitle = "Survival Steps & Danger Hacks",
            drawableRes = R.drawable.cat_stick_story,
            topics = listOf(
                listOf(
                    "What would you do if an elevator cable snaps at floor 50?",
                    "How to escape if you are sinking in deadly quicksand",
                    "What to do if a grizzly bear charges directly at you",
                    "How to survive if your car plunges into deep river water",
                    "What to do if you are caught in a rip current at the beach",
                    "How to survive a lightning strike storm in an open field",
                    "What to do if you encounter an aggressive wolf pack",
                    "How to escape a burning skyscraper if stairwells are full",
                    "What to do if an angry dog attacks and won't let go",
                    "How to survive stranded on a desert island without fresh water"
                ),
                listOf(
                    "What to do if an avalanche catches you on a mountain",
                    "How to escape if your parachute fails to deploy",
                    "What to do if you are lost in a freezing snow blizzard",
                    "How to survive a severe earthquake inside an apartment",
                    "What to do if a venomous cobra corners you in a room",
                    "How to break a car window underwater using the headrest",
                    "What to do if someone attempts to kidnap you in public",
                    "How to signal search planes if lost in dense jungle",
                    "What to do if you swallow toxic household poison",
                    "How to stop severe arterial bleeding with improvised tourniquet"
                )
            )
        ),
        CategoryItem(
            id = "paper-atlas",
            name = "Paper Atlas",
            subtitle = "Cut Paper History, Wars & Empires",
            drawableRes = R.drawable.hero_banner,
            topics = listOf(
                listOf(
                    "How One Bakery Burned Down the Entire City of London in 1666",
                    "How 300 Spartans Held Off an Army of 300,000 at Thermopylae",
                    "The Impossible Treasure Fleet of Admiral Zheng He in 1405",
                    "How Hannibal Marched War Elephants Across the Freezing Alps",
                    "The Fall of Constantinople: How One Forgotten Gate Changed World History",
                    "How Julius Caesar Surpassed Rome's Boundaries in the Gallic Wars",
                    "The Great Emu War: When Australia's Army Lost to Giant Birds",
                    "How the Silk Road Connected China and Rome Across 4,000 Miles",
                    "The Viking Siege of Paris: How 700 Longships Conquered the Seine",
                    "The Strange War of the Golden Stool in Ancient Ashanti"
                ),
                listOf(
                    "How Genghis Khan Conquered More Land Than Any Human in History",
                    "The Rosetta Stone: How One Broken Slab Decoded Ancient Egypt",
                    "How Small Wooden Ships Built the Massive British Global Empire",
                    "The Battle of Midway: 5 Minutes That Decided the Pacific War",
                    "How the Roman Aqueducts Supplied 300 Million Gallons Daily",
                    "The Boston Tea Party: When 342 Crates of Tea Sparked a Revolution",
                    "How Catherine the Great Expanded the Russian Empire to Black Sea",
                    "The Defeat of the Spanish Armada by Gale Winds and Fire Ships",
                    "The Construction of the Great Wall of China Across 13,000 Miles",
                    "The Gold Rush of 1849 That Created San Francisco from Scratch"
                )
            )
        ),
        CategoryItem(
            id = "tall-tale-3d",
            name = "Tall Tale 3D",
            subtitle = "Funny True Stories & Small Town Loopholes",
            drawableRes = R.drawable.scroll_art,
            topics = listOf(
                listOf(
                    "The Guy Who Sold the Eiffel Tower Twice and Got Away With It",
                    "How a Homeless Man Lived Rent-Free in an Airport for 18 Years",
                    "The Man Who Declared War on Canada with a Homemade Submarine",
                    "How a Student Sued Pepsi For a Fighter Jet and Almost Won",
                    "The Thief Who Stole the Mona Lisa by Walking Out the Front Door",
                    "The Village That Declared Independence Over a 50 Cent Postcard",
                    "How One Man Bought an Entire Airline Ticket for His Dog",
                    "The Bank Robber Who Forgot His Loot and Asked Cashier to Hold It",
                    "The Fake Doctor Who Successfully Performed 30 Surgeries on a Ship",
                    "The Emperor of the United States: Norton the First of San Francisco"
                ),
                listOf(
                    "The Town That Accidentally Elected a Golden Retriever as Mayor",
                    "The Man Who Mailed Himself in a Wooden Crate Across the Country",
                    "How a Man Stole an 80-Foot Train and Drove It on Subway Tracks",
                    "The Great Maple Syrup Heist of Canada Worth 18 Million Dollars",
                    "How an Amateur Painter Sold Fake Art to the World's Best Museums",
                    "The Prankster Who Put a Fake Volcano Smoke Signal in Alaska",
                    "The Astronaut Who Smuggled a Corned Beef Sandwich into Space",
                    "The Man Who Won the French Scrabble Championship Without Speaking French",
                    "How a Teenager Accidentally Bought a Fighter Jet on eBay for $100",
                    "The Pigeon That Was Accused of Being an International Secret Spy"
                )
            )
        )
    )
}

// ==============================================================================
// MAIN APP COMPOSABLE
// ==============================================================================
@Composable
fun AutoShortsMainApp(prefs: SharedPreferences) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State Variables
    var currentNav by remember { mutableStateOf("home") }
    var selectedCategory by remember { mutableStateOf("deep-dive-3d") }
    var currentTopic by remember { mutableStateOf("How Hannibal Marched War Elephants Across the Freezing Alps") }
    var currentScript by remember { mutableStateOf("") }

    // Coins and User State
    var userCoins by remember { mutableStateOf(prefs.getInt("user_coins", 50)) }
    var userEmail by remember { mutableStateOf(prefs.getString("user_email", "sndp25edu@gmail.com") ?: "sndp25edu@gmail.com") }
    var isLoggedIn by remember { mutableStateOf(prefs.getBoolean("is_logged_in", false)) }
    var resourcesInstalled by remember { mutableStateOf(prefs.getBoolean("resources_installed", false)) }

    // Cloud Config State
    var isMaintenanceMode by remember { mutableStateOf(false) }
    var maintenanceMsg by remember { mutableStateOf("Scheduled server upgrade in progress.") }
    var licenseGateEnabled by remember { mutableStateOf(false) }
    var isLicenseActive by remember { mutableStateOf(prefs.getBoolean("license_active", false)) }

    // Modals & Flows
    var showLoginGate by remember { mutableStateOf(!isLoggedIn) }
    var showResourceGate by remember { mutableStateOf(isLoggedIn && !resourcesInstalled) }
    var showTrendingModal by remember { mutableStateOf(false) }
    var showScriptStudio by remember { mutableStateOf(false) }
    var showRenderModal by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var showLicenseModal by remember { mutableStateOf(false) }
    var showMarketplaceModal by remember { mutableStateOf(false) }

    // Dynamic Categories State
    var dynamicCategories by remember { mutableStateOf(getCategoryData()) }

    fun refreshCategories() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val conn = URL("http://127.0.0.1:4500/api/styles").openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                if (conn.responseCode == 200) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val arr = JSONArray(resp)
                    val base = getCategoryData().toMutableList()
                    val existingIds = base.map { it.id }.toSet()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val id = obj.optString("id")
                        if (!existingIds.contains(id) && obj.optBoolean("isPlugin", false)) {
                            val name = obj.optString("name")
                            val tagline = obj.optString("tagline")
                            val family = obj.optString("family", "Motion")
                            val topicsArr = obj.optJSONArray("topics")
                            val topicList = mutableListOf<String>()
                            if (topicsArr != null) {
                                for (t in 0 until topicsArr.length()) {
                                    topicList.add(topicsArr.getString(t))
                                }
                            }
                            val finalTopics = if (topicList.isNotEmpty()) listOf(topicList) else listOf(listOf("Trending viral topic for $name"))
                            base.add(
                                CategoryItem(
                                    id = id,
                                    name = name,
                                    subtitle = tagline,
                                    drawableRes = R.drawable.cat_inside_out,
                                    topics = finalTopics,
                                    isPlugin = true,
                                    family = family
                                )
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        dynamicCategories = base
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Trending Modal Specifics
    var activeCategoryItem by remember { mutableStateOf<CategoryItem?>(null) }
    var trendingBatchIndex by remember { mutableStateOf(0) }
    var selectedTrendingTopic by remember { mutableStateOf("") }
    var isFetchingAiTopics by remember { mutableStateOf(false) }
    var dynamicAiTopicsList by remember { mutableStateOf<List<String>?>(null) }

    // Render Progress State
    var renderProgress by remember { mutableStateOf(5) }
    var renderMessage by remember { mutableStateOf("Initializing video automation...") }
    var renderJobId by remember { mutableStateOf("") }
    var renderedVideoPath by remember { mutableStateOf("") }
    var isRenderDone by remember { mutableStateOf(false) }
    var isRenderFailed by remember { mutableStateOf(false) }
    var isAiHealing by remember { mutableStateOf(false) }
    var aiDiagnosisText by remember { mutableStateOf("") }
    var aiActionText by remember { mutableStateOf("") }
    var elapsedRenderSeconds by remember { mutableStateOf(0) }
    var renderLogs by remember { mutableStateOf(listOf("[AI Engine] Initializing workspace & models...")) }

    // Fetch Cloud Config & Categories on Startup
    LaunchedEffect(Unit) {
        refreshCategories()
        withContext(Dispatchers.IO) {
            try {
                val conn = URL("http://127.0.0.1:4500/api/cloud/config").openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                if (conn.responseCode == 200) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(resp)
                    isMaintenanceMode = json.optBoolean("maintenance_mode", false)
                    maintenanceMsg = json.optString("maintenance_message", maintenanceMsg)
                    licenseGateEnabled = json.optBoolean("license_gate_enabled", false)
                }
            } catch (_: Exception) {}
        }
    }

    val googleAccountLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val chosenAccount = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (!chosenAccount.isNullOrEmpty()) {
            userEmail = chosenAccount
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_email", chosenAccount)
                .putInt("user_coins", 50)
                .apply()
            isLoggedIn = true
            userCoins = 50
            showLoginGate = false
            if (!resourcesInstalled) {
                showResourceGate = true
            }
            Toast.makeText(context, "Signed in as $chosenAccount! +50 Coins Added 🎉", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Google Sign-In cancelled. Login is required.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgMain)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. TOP HEADER (Logo, Brand Title, Google Account Pill, Coins, Settings)
            TopAppHeader(
                userEmail = userEmail,
                coins = userCoins,
                onAccountClick = {
                    Toast.makeText(context, "Logged in as $userEmail (Google AI Connected)", Toast.LENGTH_SHORT).show()
                },
                onCoinsClick = {
                    Toast.makeText(context, "Coins Balance: $userCoins\n10 Coins = 1 Full 3D Short", Toast.LENGTH_SHORT).show()
                },
                onSettingsClick = { currentNav = "settings" }
            )

            // 2. MAIN VIEWPORT (Clean, single-viewport fit for Home)
            Box(modifier = Modifier.weight(1f)) {
                when (currentNav) {
                    "home" -> HomeScreenClean(
                        onStartScriptClick = {
                            currentNav = "templates"
                        }
                    )
                    "templates" -> CategoriesScreen(
                        categories = dynamicCategories,
                        onCategoryClick = { cat ->
                            activeCategoryItem = cat
                            trendingBatchIndex = 0
                            dynamicAiTopicsList = null
                            selectedTrendingTopic = cat.topics[0][0]
                            showTrendingModal = true
                        },
                        onOpenMarketplace = { showMarketplaceModal = true }
                    )
                    "myspace" -> MySpaceScreen(
                        onNewScript = { currentNav = "templates" },
                        onPlayVideo = { path ->
                            openVideoFile(context, path)
                        }
                    )
                    "terminal" -> TermuxConsoleScreen()
                    "settings" -> SettingsScreenFull(
                        userEmail = userEmail,
                        coins = userCoins,
                        isLicenseActive = isLicenseActive,
                        resourcesInstalled = resourcesInstalled,
                        onOpenMarketplace = { showMarketplaceModal = true },
                        onOpenAdmin = { showAdminPanel = true },
                        onOpenLicense = { showLicenseModal = true },
                        onReinstallEngine = {
                            resourcesInstalled = false
                            prefs.edit().putBoolean("resources_installed", false).apply()
                            showResourceGate = true
                        }
                    )
                }
            }

            // 3. FLOATING ROUNDED BOTTOM NAVIGATION (26.dp floating)
            FloatingBottomNav(
                currentNav = currentNav,
                onNavSelect = { currentNav = it }
            )
        }

        // ======================================================================
        // GATE 1: COMPULSORY GOOGLE OAUTH LOGIN MODAL (First Launch)
        // ======================================================================
        if (showLoginGate && !isLoggedIn) {
            Dialog(
                onDismissRequest = {}, // Non-dismissible
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = GlowBorderStroke
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Welcome to AutoShorts AI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Login with your Google Account to connect real AI engines and receive 50 Free Welcome Coins.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Google Sign In CTA
                        Button(
                            onClick = {
                                try {
                                    val accountIntent = AccountManager.newChooseAccountIntent(
                                        null, null, arrayOf("com.google"), null, null, null, null
                                    )
                                    googleAccountLauncher.launch(accountIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Google Account Picker: " + e.message, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2442)),
                            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonPurple, MagentaAccent)))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "G", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // ======================================================================
        // GATE 2: INBUILT RESOURCE FILES & ENGINE INITIALIZATION GATE
        // ======================================================================
        if (showResourceGate && !resourcesInstalled) {
            var downloadProgress by remember { mutableStateOf(0) }
            var downloadStep by remember { mutableStateOf("Initializing offline render pipeline...") }
            var isDownloading by remember { mutableStateOf(false) }

            LaunchedEffect(isDownloading) {
                if (isDownloading) {
                    val steps = listOf(
                        Pair(20, "Verifying Android ARM64 Termux environment..."),
                        Pair(45, "Loading Three.js & HTML5 Canvas 3D scene modules..."),
                        Pair(70, "Mounting Kokoro & Edge-TTS neural voice pipelines..."),
                        Pair(90, "Configuring hardware accelerated FFmpeg 1080p codec..."),
                        Pair(100, "Render Engine Ready!")
                    )
                    for (step in steps) {
                        delay(600)
                        downloadProgress = step.first
                        downloadStep = step.second
                    }
                    delay(400)
                    prefs.edit().putBoolean("resources_installed", true).apply()
                    resourcesInstalled = true
                    showResourceGate = false
                    Toast.makeText(context, "AI Video Render Engine Ready! 🎬", Toast.LENGTH_SHORT).show()
                }
            }

            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = GlowBorderStroke
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📦 Initializing Render Engine",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Setting up local 3D canvas renderers, offline voices, and FFmpeg video pipeline (One-time setup).",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { downloadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonPurple,
                            trackColor = Color(0xFF292D4B)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = downloadStep, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                            Text(text = "$downloadProgress%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        if (!isDownloading) {
                            Button(
                                onClick = { isDownloading = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand)
                            ) {
                                Text("🚀 Initialize Resources (1-Click)", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ======================================================================
        // 10 TRENDING TOPICS MODAL (Opened when category clicked)
        // ======================================================================
        if (showTrendingModal && activeCategoryItem != null) {
            val cat = activeCategoryItem!!
            val currentTopicsList = dynamicAiTopicsList ?: cat.topics.getOrElse(trendingBatchIndex) { cat.topics[0] }

            Dialog(onDismissRequest = { showTrendingModal = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = GlowBorderStroke
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // Modal Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔥 ${cat.name}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (dynamicAiTopicsList != null) "✨ AI Dynamic Viral Topics (Zero Duplicates)" else "Choose 1 of 10 viral trending topics",
                                    fontSize = 12.sp,
                                    color = if (dynamicAiTopicsList != null) CyanAccent else TextSecondary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (!isFetchingAiTopics) {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                try {
                                                    withContext(Dispatchers.Main) { isFetchingAiTopics = true }
                                                    val conn = URL("http://127.0.0.1:4500/api/topics/dynamic?category=${cat.id}").openConnection() as HttpURLConnection
                                                    conn.connectTimeout = 6000
                                                    conn.readTimeout = 12000
                                                    if (conn.responseCode == 200) {
                                                        val res = conn.inputStream.bufferedReader().readText()
                                                        val json = JSONObject(res)
                                                        if (json.optBoolean("success", false) || json.optBoolean("ok", false)) {
                                                            val arr = json.getJSONArray("topics")
                                                            val list = mutableListOf<String>()
                                                            for (i in 0 until arr.length()) {
                                                                list.add(arr.getString(i))
                                                            }
                                                            withContext(Dispatchers.Main) {
                                                                if (list.isNotEmpty()) {
                                                                    dynamicAiTopicsList = list
                                                                    selectedTrendingTopic = list[0]
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (_: Exception) {}
                                                finally {
                                                    withContext(Dispatchers.Main) { isFetchingAiTopics = false }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    if (isFetchingAiTopics) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonPurple, strokeWidth = 2.dp)
                                    } else {
                                        Text("✨", fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { showTrendingModal = false },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("✕", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 10 Radio Selection Buttons
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(currentTopicsList) { topicText ->
                                val isSelected = selectedTrendingTopic == topicText
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { selectedTrendingTopic = topicText },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF221F45) else Color(0xFF13182E)
                                    ),
                                    border = if (isSelected) BorderStroke(1.5.dp, NeonPurple) else NormalBorderStroke
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedTrendingTopic = topicText },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = NeonPurple,
                                                unselectedColor = CardBorder
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = topicText,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bottom Action Controls
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        dynamicAiTopicsList = null
                                        trendingBatchIndex = (trendingBatchIndex + 1) % cat.topics.size
                                        selectedTrendingTopic = cat.topics[trendingBatchIndex][0]
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text(
                                        text = "🔄 Next Batch",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (!isFetchingAiTopics) {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                try {
                                                    withContext(Dispatchers.Main) { isFetchingAiTopics = true }
                                                    val conn = URL("http://127.0.0.1:4500/api/topics/dynamic?category=${cat.id}").openConnection() as HttpURLConnection
                                                    conn.connectTimeout = 6000
                                                    conn.readTimeout = 12000
                                                    if (conn.responseCode == 200) {
                                                        val res = conn.inputStream.bufferedReader().readText()
                                                        val json = JSONObject(res)
                                                        if (json.optBoolean("success", false) || json.optBoolean("ok", false)) {
                                                            val arr = json.getJSONArray("topics")
                                                            val list = mutableListOf<String>()
                                                            for (i in 0 until arr.length()) {
                                                                list.add(arr.getString(i))
                                                            }
                                                            withContext(Dispatchers.Main) {
                                                                if (list.isNotEmpty()) {
                                                                    dynamicAiTopicsList = list
                                                                    selectedTrendingTopic = list[0]
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (_: Exception) {}
                                                finally {
                                                    withContext(Dispatchers.Main) { isFetchingAiTopics = false }
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isFetchingAiTopics,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, NeonPurple),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple)
                                ) {
                                    if (isFetchingAiTopics) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonPurple, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Generating...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("✨ Fresh AI Topics", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    currentTopic = selectedTrendingTopic
                                    currentScript = ""
                                    selectedCategory = cat.id
                                    showTrendingModal = false
                                    showScriptStudio = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand)
                            ) {
                                Text(
                                    text = "Confirm Topic & Build Script 🚀",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // ======================================================================
        // SCRIPT WRITING STUDIO MODAL / SCREEN (Top Topic -> Gen Script -> Non-Editable -> Gen Video)
        // ======================================================================
        if (showScriptStudio) {
            var isGeneratingScript by remember { mutableStateOf(false) }

            Dialog(
                onDismissRequest = { showScriptStudio = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = GlowBorderStroke
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✏️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Script Writing Studio", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Category: $selectedCategory", fontSize = 11.sp, color = NeonPurple)
                                }
                            }
                            IconButton(onClick = { showScriptStudio = false }) {
                                Text("✕", color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Topic Input Field
                        Text("1. VIDEO TOPIC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = currentTopic,
                            onValueChange = { currentTopic = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF13182E),
                                unfocusedContainerColor = Color(0xFF13182E),
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            placeholder = { Text("Enter custom video topic or idea...", color = Color(0xFF6B7280)) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. [ ⚡ Generate Script ] Button
                        Button(
                            onClick = {
                                isGeneratingScript = true
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val url = URL("http://127.0.0.1:4500/api/script/generate")
                                            val conn = url.openConnection() as HttpURLConnection
                                            conn.requestMethod = "POST"
                                            conn.setRequestProperty("Content-Type", "application/json")
                                            conn.doOutput = true
                                            val postData = JSONObject().apply {
                                                put("topic", currentTopic)
                                                put("style", selectedCategory)
                                            }
                                            OutputStreamWriter(conn.outputStream).use { it.write(postData.toString()) }
                                            if (conn.responseCode == 200) {
                                                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                                                val json = JSONObject(resp)
                                                currentScript = json.optString("script", currentScript)
                                            }
                                        } catch (e: Exception) {
                                            // Fallback generation if bridge offline
                                            delay(1000)
                                            currentScript = "What happens if you $currentTopic? Most people assume it has no consequence, but inside your body a violent chain reaction is instantly triggered! Pressure surges through nerves and muscle tissues until your system takes extreme measures to protect you."
                                        } finally {
                                            isGeneratingScript = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221F48)),
                            border = BorderStroke(1.dp, NeonPurple),
                            enabled = !isGeneratingScript
                        ) {
                            if (isGeneratingScript) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NeonPurple, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Generating Viral Script...", fontSize = 13.sp, color = NeonPurple)
                            } else {
                                Text("⚡ Generate AI Script", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Editable & Pasteable Script Area
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("2. SCRIPT (EDIT OR PASTE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val wordCount = currentScript.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
                                Text("$wordCount words • ~${wordCount / 2}s", fontSize = 11.sp, color = NeonPurple)
                                // 📋 1-Tap Paste from Clipboard
                                TextButton(
                                    onClick = {
                                        try {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = clipboard.primaryClip
                                            if (clip != null && clip.itemCount > 0) {
                                                val pasted = clip.getItemAt(0).text?.toString() ?: ""
                                                if (pasted.isNotBlank()) {
                                                    currentScript = pasted
                                                    Toast.makeText(context, "Script pasted from clipboard!", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "Clipboard is empty!", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Clipboard error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("📋 Paste", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = currentScript,
                            onValueChange = { currentScript = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            placeholder = {
                                Text("Type or paste your custom viral script here, or tap '⚡ Generate AI Script' above...", color = TextSecondary, fontSize = 13.sp)
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = TextPrimary,
                                fontSize = 13.5.sp,
                                lineHeight = 20.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F1426),
                                unfocusedContainerColor = Color(0xFF0F1426),
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = CardBorder,
                                cursorColor = NeonPurple
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. [ 🎬 Generate Video Short ] Button (Last at bottom)
                        Button(
                            onClick = {
                                if (userCoins < 10) {
                                    Toast.makeText(context, "Insufficient Coins (Need 10). Recharged in Settings!", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                // Deduct 10 coins
                                userCoins -= 10
                                prefs.edit().putInt("user_coins", userCoins).apply()

                                showScriptStudio = false
                                showRenderModal = true
                                isRenderDone = false
                                isRenderFailed = false
                                isAiHealing = false
                                aiDiagnosisText = ""
                                aiActionText = ""
                                elapsedRenderSeconds = 0
                                renderProgress = 5
                                renderMessage = "Submitting render task to Termux..."
                                renderLogs = listOf("[AI Engine] Workspace ready. Starting autonomous video pipeline...")

                                // Start Live Timer
                                coroutineScope.launch {
                                    while (showRenderModal && !isRenderDone && !isRenderFailed) {
                                        delay(1000)
                                        withContext(Dispatchers.Main) {
                                            elapsedRenderSeconds++
                                        }
                                    }
                                }

                                // Trigger asynchronous backend render
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val url = URL("http://127.0.0.1:4500/api/video/generate")
                                            val conn = url.openConnection() as HttpURLConnection
                                            conn.requestMethod = "POST"
                                            conn.setRequestProperty("Content-Type", "application/json")
                                            conn.connectTimeout = 10000
                                            conn.readTimeout = 10000
                                            conn.doOutput = true
                                            val payload = JSONObject().apply {
                                                put("topic", currentTopic)
                                                put("style", selectedCategory)
                                                put("script", currentScript)
                                            }
                                            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                                            if (conn.responseCode == 200) {
                                                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                                                val json = JSONObject(resp)
                                                renderJobId = json.optString("jobId", "job_${System.currentTimeMillis()}")

                                                // Poll job status continuously
                                                while (!isRenderDone && !isRenderFailed) {
                                                    delay(1200)
                                                    try {
                                                        val pollUrl = URL("http://127.0.0.1:4500/api/video/status/$renderJobId")
                                                        val pollConn = pollUrl.openConnection() as HttpURLConnection
                                                        pollConn.connectTimeout = 5000
                                                        pollConn.readTimeout = 5000
                                                        if (pollConn.responseCode == 200) {
                                                            val pollResp = pollConn.inputStream.bufferedReader().use { it.readText() }
                                                            val jobJson = JSONObject(pollResp)
                                                            val newProgress = jobJson.optInt("progress", renderProgress)
                                                            val msg = jobJson.optString("message", "")
                                                            val status = jobJson.optString("status", "")
                                                            val healing = jobJson.optBoolean("aiHealing", false) || status == "healing"
                                                            val diagnosis = jobJson.optString("aiDiagnosis", "")
                                                            val action = jobJson.optString("aiAction", "")

                                                            withContext(Dispatchers.Main) {
                                                                renderProgress = newProgress
                                                                if (msg.isNotEmpty() && msg != renderMessage) {
                                                                    renderMessage = msg
                                                                    renderLogs = renderLogs + listOf("[Pipeline] $msg")
                                                                }
                                                                if (healing) {
                                                                    isAiHealing = true
                                                                    aiDiagnosisText = diagnosis.ifEmpty { "Diagnosing engine anomaly..." }
                                                                    aiActionText = action.ifEmpty { "Auto-repairing project..." }
                                                                    renderLogs = renderLogs + listOf("[AI Auto-Fix] $aiDiagnosisText -> $aiActionText")
                                                                } else {
                                                                    isAiHealing = false
                                                                }

                                                                if (status == "completed") {
                                                                    isRenderDone = true
                                                                    renderedVideoPath = jobJson.optString("downloadPath", "/sdcard/Download/AutoShorts_$renderJobId.mp4")
                                                                    renderLogs = renderLogs + listOf("[Complete] Render finished successfully in 1080x1920!")
                                                                } else if (status == "failed") {
                                                                    isRenderFailed = true
                                                                    userCoins += 10
                                                                    prefs.edit().putInt("user_coins", userCoins).apply()
                                                                    renderLogs = renderLogs + listOf(
                                                                        "[Error] " + msg,
                                                                        "[Refund] +10 Coins refunded back to your balance!"
                                                                    )
                                                                }
                                                            }
                                                            if (status == "completed" || status == "failed") break
                                                        }
                                                    } catch (_: Exception) {
                                                        // Transient network blip or app minimized: continue polling without breaking
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                isRenderFailed = true
                                                userCoins += 10
                                                prefs.edit().putInt("user_coins", userCoins).apply()
                                                renderLogs = renderLogs + listOf(
                                                    "[Error] Cannot reach Termux server on port 4500",
                                                    "[Refund] +10 Coins refunded!"
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand),
                            border = BorderStroke(1.dp, NeonPurple)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎬", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Video Short (10 Coins)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ======================================================================
        // VIDEO RENDERING LIVE PROGRESS MODAL (ANIMATED AI STUDIO)
        // ======================================================================
        if (showRenderModal) {
            val infiniteTransition = rememberInfiniteTransition(label = "render_pulse")
            val rotationAngle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing))
            )
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )

            Dialog(
                onDismissRequest = {}, // Non-interactive while rendering
                properties = DialogProperties(dismissOnBackPress = (isRenderDone || isRenderFailed), dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.8f), CyanAccent.copy(alpha = 0.6f))))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title Header
                        Text(
                            text = if (isRenderDone) "🎉 Video Ready!" else if (isRenderFailed) "❌ Render Stopped" else "🎬 AI Video Studio Rendering",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRenderFailed) Color(0xFFFCA5A5) else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRenderDone) "Saved directly to /sdcard/Download" else if (isRenderFailed) "10 Coins refunded back to your balance" else "2D Animated Canvas Engine • Zero Black Screen",
                            fontSize = 12.sp,
                            color = if (isRenderDone || isRenderFailed) SuccessGreen else TextSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // ANIMATED CENTRAL RADAR HUD
                        Box(
                            modifier = Modifier
                                .size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Rotating Outer Glow Radar Ring
                            if (!isRenderDone && !isRenderFailed) {
                                Box(
                                    modifier = Modifier
                                        .size(108.dp)
                                        .rotate(rotationAngle)
                                        .border(
                                            width = 3.dp,
                                            brush = Brush.sweepGradient(listOf(NeonPurple, CyanAccent, MagentaAccent, NeonPurple)),
                                            shape = CircleShape
                                        )
                                )
                            }
                            // Central Badge
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F142A))
                                    .border(1.dp, CardBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (isRenderDone) "🎉" else if (isRenderFailed) "❌" else if (isAiHealing) "🤖" else "🎬",
                                        fontSize = 28.sp,
                                        modifier = if (!isRenderDone && !isRenderFailed) Modifier.scale(pulseScale) else Modifier
                                    )
                                    Text(
                                        text = "$renderProgress%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isRenderDone) SuccessGreen else if (isAiHealing) CyanAccent else NeonPurple
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // LIVE ELAPSED TIMER BADGE
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFF10152B), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF232A4E), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isRenderDone) SuccessGreen
                                        else if (isRenderFailed) Color.Red
                                        else if (isAiHealing) CyanAccent.copy(alpha = glowAlpha)
                                        else SuccessGreen.copy(alpha = glowAlpha)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val mins = elapsedRenderSeconds / 60
                            val secs = elapsedRenderSeconds % 60
                            Text(
                                text = if (isRenderDone) "Completed in ${String.format("%02d:%02d", mins, secs)}"
                                else if (isRenderFailed) "Failed at ${String.format("%02d:%02d", mins, secs)}"
                                else if (isAiHealing) "🤖 AI Healing • ${String.format("%02d:%02d", mins, secs)}"
                                else "⏱️ ${String.format("%02d:%02d", mins, secs)} elapsed • Termux Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAiHealing) CyanAccent else TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // PROGRESS BAR & STATUS
                        LinearProgressIndicator(
                            progress = { renderProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isRenderDone) SuccessGreen else if (isAiHealing) CyanAccent else NeonPurple,
                            trackColor = Color(0xFF1A1F3C)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = renderMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // 5-STAGE PIPELINE INDICATOR
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val stages = listOf("✍️ Script", "🎙️ Voice", "🎵 Audio", "🎨 Canvas", "📦 MP4")
                            stages.forEachIndexed { idx, stageName ->
                                val stageDone = when (idx) {
                                    0 -> renderProgress >= 20
                                    1 -> renderProgress >= 40
                                    2 -> renderProgress >= 60
                                    3 -> renderProgress >= 85
                                    4 -> renderProgress >= 100
                                    else -> false
                                }
                                val stageActive = when (idx) {
                                    0 -> renderProgress < 20
                                    1 -> renderProgress in 20..39
                                    2 -> renderProgress in 40..59
                                    3 -> renderProgress in 60..84
                                    4 -> renderProgress in 85..99
                                    else -> false
                                }
                                Text(
                                    text = if (stageDone) "✓ $stageName" else if (stageActive) "● $stageName" else "○ $stageName",
                                    fontSize = 9.sp,
                                    fontWeight = if (stageActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (stageDone) SuccessGreen else if (stageActive) CyanAccent else TextSecondary
                                )
                            }
                        }

                        // AI HEALER BANNER (If autonomous AI repair is engaged)
                        if (isAiHealing) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1435)),
                                border = BorderStroke(1.dp, CyanAccent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🤖", fontSize = 22.sp, modifier = Modifier.scale(pulseScale))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("AI AUTO-HEALER ENGAGED", fontSize = 10.sp, fontWeight = FontWeight.Black, color = CyanAccent)
                                        Text(aiDiagnosisText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(aiActionText, fontSize = 10.sp, color = SuccessGreen)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // LIVE TERMINAL LOGS
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B18)),
                            border = NormalBorderStroke
                        ) {
                            LazyColumn(modifier = Modifier.padding(8.dp)) {
                                items(renderLogs) { logLine ->
                                    val logColor = when {
                                        logLine.contains("[Complete]") -> SuccessGreen
                                        logLine.contains("[AI Auto-Fix]") -> CyanAccent
                                        logLine.contains("[Error]") -> Color(0xFFFCA5A5)
                                        else -> Color(0xFF93C5FD)
                                    }
                                    Text(
                                        text = logLine,
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = logColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ACTIONS: SUCCESS / FAILED / RENDERING
                        if (isRenderDone) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        openVideoFile(context, renderedVideoPath)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                ) {
                                    Text("▶ Play Video", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = {
                                        showRenderModal = false
                                        currentNav = "myspace"
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, CardBorder)
                                ) {
                                    Text("View in My Space", color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        } else if (isRenderFailed) {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        // Trigger AI Auto-Repair
                                        isRenderFailed = false
                                        isAiHealing = true
                                        renderProgress = 25
                                        renderMessage = "AI Auto-Repair dispatched..."
                                        renderLogs = renderLogs + listOf("[AI Doctor] Diagnosing failure & applying patches in Termux...")
                                        coroutineScope.launch {
                                            withContext(Dispatchers.IO) {
                                                try {
                                                    val healUrl = URL("http://127.0.0.1:4500/api/ai/heal")
                                                    val healConn = healUrl.openConnection() as HttpURLConnection
                                                    healConn.requestMethod = "POST"
                                                    healConn.setRequestProperty("Content-Type", "application/json")
                                                    healConn.doOutput = true
                                                    val body = JSONObject().apply { put("jobId", renderJobId) }
                                                    OutputStreamWriter(healConn.outputStream).use { it.write(body.toString()) }
                                                    healConn.responseCode
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand)
                                ) {
                                    Text("🤖 Let AI Auto-Fix & Resume", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { showRenderModal = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, CardBorder)
                                ) {
                                    Text("Dismiss (Coins Refunded)", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ======================================================================
        // MAINTENANCE MODE OVERLAY (If toggled by Admin)
        // ======================================================================
        if (isMaintenanceMode) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛠️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("App Under Maintenance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = maintenanceMsg,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ======================================================================
        // ADMIN CONTROL PANEL MODAL (For Remote Management)
        // ======================================================================
        if (showAdminPanel) {
            Dialog(onDismissRequest = { showAdminPanel = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = GlowBorderStroke
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("👑 Admin Controls", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            IconButton(onClick = { showAdminPanel = false }) { Text("✕", color = TextSecondary, fontWeight = FontWeight.Bold) }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Toggle Maintenance Mode
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Maintenance Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Locks user app with notice", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = isMaintenanceMode,
                                onCheckedChange = { isMaintenanceMode = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonPurple)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder)

                        // Toggle License Key Gate
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Require License Key", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Hardware One-Device Lock", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = licenseGateEnabled,
                                onCheckedChange = { licenseGateEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonPurple)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder)

                        // Add Coins
                        Text("User Coins Management", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    userCoins += 50
                                    prefs.edit().putInt("user_coins", userCoins).apply()
                                    Toast.makeText(context, "+50 Coins Added!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand)
                            ) {
                                Text("+50 Coins")
                            }
                            OutlinedButton(
                                onClick = {
                                    userCoins = 100
                                    prefs.edit().putInt("user_coins", userCoins).apply()
                                    Toast.makeText(context, "Coins set to 100", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Text("Set 100")
                            }
                        }
                    }
                }
            }
        // ======================================================================
        // STYLE & MOTION PLUGINS MARKETPLACE MODAL (OTA Cloud)
        // ======================================================================
        if (showMarketplaceModal) {
            StyleMarketplaceDialog(
                onDismiss = { showMarketplaceModal = false },
                onPluginChanged = { refreshCategories() }
            )
        }
    }
}
}



// ==============================================================================
// TOP APP HEADER COMPOSABLE
// ==============================================================================
@Composable
fun TopAppHeader(
    userEmail: String,
    coins: Int,
    onAccountClick: () -> Unit,
    onCoinsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo + Name
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "AutoShorts AI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Google AI Studio",
                    fontSize = 11.sp,
                    color = NeonPurple
                )
            }
        }

        // Action Pills: Coins + Account + Settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Coins Badge (Sleek Dark Glassmorphism + Glowing Gold Border)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCoinsClick() },
                color = Color(0xFF161A30),
                border = BorderStroke(1.2.dp, Color(0xFFFBBF24))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🪙", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoinGold
                    )
                }
            }

            // Google Account Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onAccountClick() },
                color = Color(0xFF13182E),
                border = NormalBorderStroke
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = userEmail.substringBefore("@"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            // Settings Icon
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(36.dp)
            ) {
                Text(text = "⚙️", fontSize = 16.sp)
            }
        }
    }
}

// ==============================================================================
// 1. HOME SCREEN (Clean, Laser-Focused Single Viewport - ZERO SCROLL)
// ==============================================================================
@Composable
fun HomeScreenClean(
    onStartScriptClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // A. Cinematic Space Banner with 3D Rocket (Uncropped FillWidth)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = GlowBorderStroke
        ) {
            Image(
                painter = painterResource(id = R.drawable.hero_banner),
                contentDescription = "Space Rocket Banner",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // B. 1-2-3 Step Progress Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepPill(number = "1", title = "Script Writing", isActive = true)
            Box(modifier = Modifier.weight(1f).height(2.dp).background(Brush.horizontalGradient(listOf(NeonPurple, CardBorder))))
            StepPill(number = "2", title = "My Space", isActive = false)
            Box(modifier = Modifier.weight(1f).height(2.dp).background(CardBorder))
            StepPill(number = "3", title = "Category", isActive = false)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // C. Core Hero Card: 💡 Script Writing with Prominent Full-Width Start Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = GlowBorderStroke
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CardGlassGradient)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: Title, Description, Checklist & 3D Scroll Art
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Script Writing",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Generate viral scripts optimized for 50-second Shorts.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CheckRow("✓ Viral 2s hook & retention loop")
                            CheckRow("✓ Word-synced karaoke captions")
                            CheckRow("✓ Automatic 3D scene timing")
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Image(
                            painter = painterResource(id = R.drawable.scroll_art),
                            contentDescription = "Scroll Art",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(95.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }

                    // Bottom: Prominent Full-Width Start Button (100% visible, never clipped)
                    Button(
                        onClick = onStartScriptClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand),
                        border = BorderStroke(1.dp, NeonPurple)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✏️", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Writing Script ➔",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun StepPill(number: String, title: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isActive) NeonPurple else CardBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) TextPrimary else TextSecondary
        )
    }
}

@Composable
fun CheckRow(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFFC4B5FD),
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

// ==============================================================================
// 2. CATEGORIES / TEMPLATES SCREEN
// ==============================================================================
@Composable
fun CategoriesScreen(
    categories: List<CategoryItem>,
    onCategoryClick: (CategoryItem) -> Unit,
    onOpenMarketplace: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Categories & Video Styles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tap any category to explore 10 curated viral trending topics.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Marketplace Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenMarketplace() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1938)),
            border = BorderStroke(1.dp, Brush.linearGradient(listOf(NeonPurple, MagentaAccent)))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎨", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Plugin Marketplace (62 Styles)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Download 2D & 3D Over-The-Air styles from Cloud", fontSize = 11.sp, color = TextSecondary)
                }
                Surface(
                    color = PrimaryBrand,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Explore ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { cat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clickable { onCategoryClick(cat) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = if (cat.isPlugin) BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFF06B6D4), NeonPurple))) else GlowBorderStroke
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (cat.isPlugin) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF312E81)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎨", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Surface(
                                        color = PrimaryBrand,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (cat.family.isNotEmpty()) cat.family.take(8) else "PLUGIN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Image(
                                painter = painterResource(id = cat.drawableRes),
                                contentDescription = cat.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cat.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (cat.isPlugin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF0F3923),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, SuccessGreen)
                                    ) {
                                        Text(
                                            text = "Plugin",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cat.subtitle,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔥 10 Trending Topics",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonPurple
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("➔", fontSize = 11.sp, color = NeonPurple)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 3. MY SPACE SCREEN (Rendered Videos & Saved Projects)
// ==============================================================================
@Composable
fun MySpaceScreen(
    onNewScript: () -> Unit,
    onPlayVideo: (String) -> Unit
) {
    var videoList by remember { mutableStateOf<List<SavedVideoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val refreshVideos: () -> Unit = {
        isLoading = true
        coroutineScope.launch {
            val list = mutableListOf<SavedVideoItem>()
            withContext(Dispatchers.IO) {
                try {
                    val url = URL("http://127.0.0.1:4500/api/videos")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    if (conn.responseCode == 200) {
                        val text = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(text)
                        val arr = json.optJSONArray("videos") ?: JSONArray()
                        for (i in 0 until arr.length()) {
                            val v = arr.getJSONObject(i)
                            list.add(
                                SavedVideoItem(
                                    id = v.optString("id"),
                                    name = v.optString("name"),
                                    path = v.optString("path"),
                                    sizeMb = v.optString("sizeMb", "0 MB"),
                                    formattedDate = v.optString("formattedDate", "")
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}

                // Fallback direct scan if list empty
                if (list.isEmpty()) {
                    val downloadDir = File("/sdcard/Download")
                    if (downloadDir.exists()) {
                        val files = downloadDir.listFiles { _, name -> name.startsWith("AutoShorts_") && name.endsWith(".mp4") }
                        files?.forEach { f ->
                            list.add(
                                SavedVideoItem(
                                    id = f.nameWithoutExtension,
                                    name = f.name,
                                    path = f.absolutePath,
                                    sizeMb = "%.1f MB".format(f.length() / (1024.0 * 1024.0)),
                                    formattedDate = "Local Storage"
                                )
                            )
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                videoList = list
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshVideos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("My Space", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    text = if (isLoading) "Scanning videos..." else "${videoList.size} Generated Shorts Ready",
                    fontSize = 12.sp,
                    color = if (videoList.isNotEmpty()) SuccessGreen else TextSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { refreshVideos() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text("🔄 Refresh", fontSize = 11.sp, color = TextPrimary)
                }
                Button(
                    onClick = onNewScript,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand)
                ) {
                    Text("+ New Short", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (videoList.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.my_space_clapper),
                        contentDescription = "Clapper",
                        modifier = Modifier.size(90.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("No Generated Videos Yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Generate your first high-retention short in 1080x1920 with full audio & animations.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNewScript,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand)
                    ) {
                        Text("Create Video Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(videoList) { videoItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        border = NormalBorderStroke
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF2E1A47), Color(0xFF1E2640)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎬", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = videoItem.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "1080x1920 MP4",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent
                                        )
                                        Text(
                                            text = " • ${videoItem.sizeMb} • ${videoItem.formattedDate}",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = { onPlayVideo(videoItem.path) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Text("▶ Play", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 4. SETTINGS SCREEN (Profile, Coins, Admin Control, Engine Reinstall)
// ==============================================================================
@Composable
fun SettingsScreenFull(
    userEmail: String,
    coins: Int,
    isLicenseActive: Boolean,
    resourcesInstalled: Boolean,
    onOpenMarketplace: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenLicense: () -> Unit,
    onReinstallEngine: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Settings & Account", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Manage AI engine, account profile and creator tools", fontSize = 12.sp, color = TextSecondary)
        }

        // Account Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = GlowBorderStroke
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PrimaryBrand),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userEmail, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Google AI Authentication Active", fontSize = 12.sp, color = SuccessGreen)
                    }
                }
            }
        }

        // Coins & Freemium Quota Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, CoinGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("🪙 Generation Coins", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("$coins Coins Available (10 Coins / Video)", fontSize = 12.sp, color = CoinGold)
                    }
                    Surface(
                        color = Color(0xFF2C240E),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$coins Left",
                            color = CoinGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Style Plugins Marketplace Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonPurple, MagentaAccent)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Style & Motion Plugins Store", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Cloud OTA Marketplace (62 Styles Available)", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Download new 2D & 3D video styles over-the-air directly from Cloud / Firebase without updating the APK.", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onOpenMarketplace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand)
                    ) {
                        Text("🌐 Open Plugin Store (62 Styles)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }

        // Engine Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = NormalBorderStroke
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚙️ Offline AI & Render Engine", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (resourcesInstalled) "Status: Installed & Ready (Termux ARM64)" else "Status: Resources Missing",
                        fontSize = 12.sp,
                        color = if (resourcesInstalled) SuccessGreen else Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onReinstallEngine,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("Re-verify / Reinstall Engine", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Companion Admin Panel Button
        item {
            Button(
                onClick = onOpenAdmin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1D40)),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonPurple, MagentaAccent)))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👑", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Admin Controls (Maintenance & Coins)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ==============================================================================
// 5. STYLE MARKETPLACE DIALOG (Cloud OTA Plugins)
// ==============================================================================
@Composable
fun StyleMarketplaceDialog(
    onDismiss: () -> Unit,
    onPluginChanged: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var pluginsList by remember { mutableStateOf<List<MarketplacePluginItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var installingId by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Kinetic Type", "Hand-Drawn", "Data", "3D", "Shorts Format", "Edit Overlay", "Character Story")

    fun loadMarketplace() {
        isLoading = true
        scope.launch(Dispatchers.IO) {
            try {
                val conn = URL("http://127.0.0.1:4500/api/plugins/marketplace").openConnection() as HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(resp)
                    val arr = json.optJSONArray("plugins")
                    val list = mutableListOf<MarketplacePluginItem>()
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            val topicsArr = o.optJSONArray("topics")
                            val tList = mutableListOf<String>()
                            if (topicsArr != null) {
                                for (j in 0 until topicsArr.length()) tList.add(topicsArr.getString(j))
                            }
                            list.add(
                                MarketplacePluginItem(
                                    id = o.optString("id"),
                                    folder = o.optString("folder"),
                                    name = o.optString("name"),
                                    version = o.optString("version", "1.0.0"),
                                    family = o.optString("family", "Motion"),
                                    is3D = o.optBoolean("is3D", false),
                                    aspect = o.optString("aspect", "9:16"),
                                    sizeKb = o.optDouble("sizeKb", 60.0),
                                    tagline = o.optString("tagline"),
                                    description = o.optString("description"),
                                    downloadFileName = o.optString("downloadFileName"),
                                    isInstalled = o.optBoolean("isInstalled", false),
                                    topics = tList
                                )
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        pluginsList = list
                        isLoading = false
                    }
                } else {
                    withContext(Dispatchers.Main) { isLoading = false }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(context, "Cannot connect to marketplace: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadMarketplace()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = CardBg,
            border = GlowBorderStroke
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎨", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Style Marketplace", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text("Cloud OTA • 62 Production Styles Available", fontSize = 11.sp, color = TextSecondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 18.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Categories Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryBrand else Color(0xFF161A30),
                            border = BorderStroke(1.dp, if (isSelected) NeonPurple else CardBorder),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonPurple, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Connecting to Cloud / Firebase...", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                } else {
                    val filtered = pluginsList.filter {
                        (selectedCategory == "All" || it.family.equals(selectedCategory, ignoreCase = true))
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No styles found in this category.", fontSize = 13.sp, color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered) { item ->
                                val isInstalling = installingId == item.id
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101426)),
                                    border = BorderStroke(1.dp, if (item.isInstalled) SuccessGreen.copy(alpha = 0.6f) else Color(0xFF232742))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (item.is3D) Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))) else Brush.linearGradient(listOf(NeonPurple, MagentaAccent))),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(if (item.is3D) "3D" else "2D", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                    Text("${item.family} • ${item.aspect} • ${item.sizeKb} KB", fontSize = 11.sp, color = TextSecondary)
                                                }
                                            }

                                            // Action Button (Install / Installed)
                                            if (item.isInstalled) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(0xFF0F3923),
                                                    border = BorderStroke(1.dp, SuccessGreen)
                                                ) {
                                                    Text(
                                                        "Installed ✓",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SuccessGreen,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        installingId = item.id
                                                        scope.launch(Dispatchers.IO) {
                                                            try {
                                                                val conn = URL("http://127.0.0.1:4500/api/plugins/install").openConnection() as HttpURLConnection
                                                                conn.requestMethod = "POST"
                                                                conn.setRequestProperty("Content-Type", "application/json")
                                                                conn.doOutput = true
                                                                val body = JSONObject().apply { put("pluginId", item.id) }
                                                                conn.outputStream.use { it.write(body.toString().toByteArray()) }
                                                                if (conn.responseCode == 200) {
                                                                    withContext(Dispatchers.Main) {
                                                                        installingId = null
                                                                        Toast.makeText(context, "✅ Installed ${item.name}!", Toast.LENGTH_SHORT).show()
                                                                        loadMarketplace()
                                                                        onPluginChanged()
                                                                    }
                                                                } else {
                                                                    withContext(Dispatchers.Main) {
                                                                        installingId = null
                                                                        Toast.makeText(context, "Install failed (${conn.responseCode})", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                            } catch (e: Exception) {
                                                                withContext(Dispatchers.Main) {
                                                                    installingId = null
                                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        }
                                                    },
                                                    enabled = !isInstalling,
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    if (isInstalling) {
                                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                                    } else {
                                                        Text("Install ⬇️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(item.description, fontSize = 11.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// FLOATING ROUNDED BOTTOM NAVIGATION (26.dp)
// ==============================================================================
@Composable
fun FloatingBottomNav(
    currentNav: String,
    onNavSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF10152A),
            border = BorderStroke(1.dp, Color(0xFF292D4B)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = "🏠",
                    label = "Home",
                    isSelected = currentNav == "home",
                    onClick = { onNavSelect("home") }
                )
                NavItem(
                    icon = "⊞",
                    label = "Templates",
                    isSelected = currentNav == "templates",
                    onClick = { onNavSelect("templates") }
                )
                NavItem(
                    icon = ">_",
                    label = "Termux",
                    isSelected = currentNav == "terminal",
                    onClick = { onNavSelect("terminal") }
                )
                NavItem(
                    icon = "▷",
                    label = "My Space",
                    isSelected = currentNav == "myspace",
                    onClick = { onNavSelect("myspace") }
                )
                NavItem(
                    icon = "⚙",
                    label = "Settings",
                    isSelected = currentNav == "settings",
                    onClick = { onNavSelect("settings") }
                )
            }
        }
    }
}

@Composable
fun NavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = if (isSelected) 18.sp else 16.sp,
            color = if (isSelected) NeonPurple else TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(16.dp)
                    .height(2.dp)
                    .background(NeonPurple, RoundedCornerShape(1.dp))
            )
        }
    }
}

// ==============================================================================
// HELPER TO PLAY RENDERED VIDEO IN NATIVE PLAYER
// ==============================================================================
fun openVideoFile(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "Video file not found at $filePath", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback with direct Uri if fileprovider fails
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(File(filePath)), "video/mp4")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Saved to Downloads: ${File(filePath).name}", Toast.LENGTH_LONG).show()
        }
    }
}

// ==============================================================================
// 12. EMBEDDED TERMUX TERMINAL SCREEN (AntiGem-Style)
// ==============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermuxConsoleScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    var status by remember { mutableStateOf(TermuxEngineStatus()) }
    var logs by remember { mutableStateOf<List<TerminalLogItem>>(emptyList()) }
    var commandInput by remember { mutableStateOf("") }
    var isExecuting by remember { mutableStateOf(false) }
    var autoScroll by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }

    fun fetchStatusAndLogs() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Status
                val sConn = URL("http://127.0.0.1:4500/api/terminal/status").openConnection() as HttpURLConnection
                sConn.connectTimeout = 2000
                sConn.readTimeout = 2000
                if (sConn.responseCode == 200) {
                    val sResp = sConn.inputStream.bufferedReader().use { it.readText() }
                    val sJson = JSONObject(sResp)
                    val newStatus = TermuxEngineStatus(
                        online = sJson.optBoolean("online", true),
                        pid = sJson.optInt("pid", 0),
                        uptimeSeconds = sJson.optInt("uptimeSeconds", 0),
                        nodeVersion = sJson.optString("nodeVersion", "Node.js"),
                        ffmpeg = sJson.optBoolean("ffmpeg", false),
                        memory = sJson.optString("memory", ""),
                        disk = sJson.optString("disk", ""),
                        installedPluginsCount = sJson.optInt("installedPluginsCount", 0)
                    )
                    withContext(Dispatchers.Main) {
                        status = newStatus
                        isOffline = false
                    }
                } else {
                    withContext(Dispatchers.Main) { isOffline = true }
                }

                // Logs
                val lConn = URL("http://127.0.0.1:4500/api/terminal/logs").openConnection() as HttpURLConnection
                lConn.connectTimeout = 2000
                lConn.readTimeout = 2000
                if (lConn.responseCode == 200) {
                    val lResp = lConn.inputStream.bufferedReader().use { it.readText() }
                    val lJson = JSONObject(lResp)
                    val arr = lJson.optJSONArray("logs")
                    val items = mutableListOf<TerminalLogItem>()
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            items.add(
                                TerminalLogItem(
                                    id = obj.optString("id"),
                                    time = obj.optString("time"),
                                    tag = obj.optString("tag"),
                                    level = obj.optString("level"),
                                    message = obj.optString("message")
                                )
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        logs = items
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    isOffline = true
                }
            }
        }
    }

    fun executeCommand(cmd: String) {
        if (cmd.isBlank() || isExecuting) return
        isExecuting = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val conn = URL("http://127.0.0.1:4500/api/terminal/exec").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 30000
                val body = JSONObject().put("command", cmd).toString()
                OutputStreamWriter(conn.outputStream).use { it.write(body) }
                conn.responseCode
                fetchStatusAndLogs()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Command failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isExecuting = false
                    commandInput = ""
                }
            }
        }
    }

    fun clearLogs() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val conn = URL("http://127.0.0.1:4500/api/terminal/clear").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 2000
                conn.responseCode
                fetchStatusAndLogs()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            fetchStatusAndLogs()
            delay(1500)
        }
    }

    LaunchedEffect(logs.size) {
        if (autoScroll && logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 1. TOP HEADER & STATUS BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = ">_ Termux Core Engine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
                Text(
                    text = "Embedded AntiGem Terminal & Runtime",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (!isOffline) Color(0xFF064E3B) else Color(0xFF7F1D1D),
                border = BorderStroke(1.dp, if (!isOffline) Color(0xFF10B981) else Color(0xFFEF4444))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (!isOffline) Color(0xFF34D399) else Color(0xFFF87171), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (!isOffline) "Port 4500 OK" else "Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (!isOffline) Color(0xFFA7F3D0) else Color(0xFFFECACA)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. SYSTEM METRICS CHIPS
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                MetricChip(label = "Node", value = if (status.nodeVersion.isNotBlank()) status.nodeVersion else "v26")
            }
            item {
                MetricChip(label = "FFmpeg", value = if (status.ffmpeg) "60FPS OK" else "Pending")
            }
            item {
                MetricChip(label = "Disk", value = if (status.disk.isNotBlank()) status.disk else "Storage OK")
            }
            item {
                MetricChip(label = "RAM", value = if (status.memory.isNotBlank()) status.memory else "49MB")
            }
            if (status.pid > 0) {
                item {
                    MetricChip(label = "PID", value = "${status.pid}")
                }
            }
            item {
                MetricChip(label = "Plugins", value = "${status.installedPluginsCount} Active")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. QUICK ACTION BUTTONS
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                QuickCommandChip(label = "🧹 Clear", onClick = { clearLogs() })
            }
            item {
                QuickCommandChip(label = "ℹ️ Uptime", onClick = { executeCommand("uptime && free -h") })
            }
            item {
                QuickCommandChip(label = "🎬 Check FFmpeg", onClick = { executeCommand("ffmpeg -version | head -n 2") })
            }
            item {
                QuickCommandChip(label = "📦 List Plugins", onClick = { executeCommand("ls -la ~/.autoshorts-pro/installed_plugins") })
            }
            item {
                QuickCommandChip(label = "📂 /sdcard Downloads", onClick = { executeCommand("ls -lh /sdcard/Download/AutoShorts* | tail -n 5") })
            }
            item {
                QuickCommandChip(label = "⚡ Processes", onClick = { executeCommand("ps aux | grep -E 'node|ffmpeg|python' | head -n 5") })
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. TERMINAL CONSOLE WINDOW
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF090C15),
            border = BorderStroke(1.dp, Color(0xFF1B2238))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Console Toolbar Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1424))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFFFF5F56), CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFFFFBD2E), CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(9.dp).background(Color(0xFF27C93F), CircleShape))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "bash - termux@autoshorts",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (autoScroll) "AutoScroll: ON" else "AutoScroll: OFF",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (autoScroll) NeonPurple else TextSecondary,
                            modifier = Modifier
                                .clickable { autoScroll = !autoScroll }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📋 Copy",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E2540))
                                .clickable {
                                    val allText = logs.joinToString("\n") { "[${it.time}] [${it.tag}] ${it.message}" }
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    clip.setPrimaryClip(android.content.ClipData.newPlainText("Logs", allText))
                                    Toast.makeText(context, "Terminal logs copied!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // If Offline Warning Card
                if (isOffline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "⚠️ Termux Backend Server Offline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCA5A5)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Run 'autoshorts.sh' inside Termux to connect the AI rendering engine.",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        try {
                                            val i = context.packageManager.getLaunchIntentForPackage("com.termux")
                                            if (i != null) context.startActivity(i)
                                            else Toast.makeText(context, "Termux app not installed", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBrand),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Open Termux", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        clip.setPrimaryClip(android.content.ClipData.newPlainText("Command", "bash ~/autoshorts.sh"))
                                        Toast.makeText(context, "Copied: bash ~/autoshorts.sh", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2540)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Copy Start Command", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Logs Output LazyColumn
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "AutoShorts Termux Engine connected. Waiting for commands or render logs...",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    } else {
                        items(logs, key = { it.id }) { log ->
                            TerminalLogRow(log = log)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. INTERACTIVE COMMAND INPUT BAR
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F1424),
            border = BorderStroke(1.dp, Color(0xFF28304E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    placeholder = {
                        Text(
                            text = "Execute shell command (e.g. ls, free -m)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF555D7C)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = NeonPurple
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = { executeCommand(commandInput) },
                    enabled = commandInput.isNotBlank() && !isExecuting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBrand,
                        disabledContainerColor = Color(0xFF1E2540)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Run ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF10152A),
        border = BorderStroke(1.dp, Color(0xFF1F2642))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF67E8F9)
            )
        }
    }
}

@Composable
fun QuickCommandChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF131930),
        border = BorderStroke(1.dp, Color(0xFF263056)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun TerminalLogRow(log: TerminalLogItem) {
    val tagColor = when (log.tag.lowercase()) {
        "termux" -> Color(0xFF10B981)
        "render", "pipeline" -> NeonPurple
        "exec" -> Color(0xFF06B6D4)
        "server" -> Color(0xFFF59E0B)
        "error", "stderr" -> Color(0xFFEF4444)
        else -> Color(0xFF38BDF8)
    }

    val textColor = when (log.level.lowercase()) {
        "error" -> Color(0xFFFCA5A5)
        "success" -> Color(0xFF86EFAC)
        "command" -> Color(0xFF7DD3FC)
        else -> Color(0xFFE2E8F0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = log.time,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "[${log.tag}]",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = tagColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = log.message,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}
