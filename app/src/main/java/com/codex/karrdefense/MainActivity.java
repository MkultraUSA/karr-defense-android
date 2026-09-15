package com.codex.karrdefense;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int REQ_PERMISSIONS = 42;
    private static final int REQ_REPORT_FOLDER = 43;
    private static final int MAX_LOG_LINES = 80;
    private static final int MAX_VISIBLE_TARGETS = 4;
    private static final long BLE_LOG_INTERVAL_MS = 15000;
    private static final long TARGET_RENDER_INTERVAL_MS = 1500;
    private static final int BLE_RSSI_DELTA = 12;
    private static final String PREFS = "field_security_inspector";
    private static final String PREF_REPORT_TREE_URI = "report_tree_uri";
    private static final String PREF_SPLASH_INDEX = "splash_index";
    // MANGA SCROLL THEME: fantasy-dusk night sky, sakura accents, inked panels.
    // Contrast rule: dark ink text (0xff14101f) on ALL bright fills; paper-white
    // text only on dark fills. Never bright-on-bright.
    private static final int COLOR_MACH_WHITE = 0xfffff6e9;   // warm paper white
    private static final int COLOR_CRIMSON = 0xffd81b4c;      // shonen/sakura red (dark enough for white text)
    private static final int COLOR_INDIGO = 0xff1b1035;       // fantasy dusk violet (dark bg)
    private static final int COLOR_CYAN = 0xff4dd8ff;         // spirit-sky blue
    private static final int COLOR_YELLOW = 0xffffc93c;       // manga highlight gold (dark text only)
    private static final int COLOR_PANEL      = 0xff241543;   // dusk panel (light text only)
    private static final int COLOR_STATUS     = 0xffe8dff2;   // pale wisteria body text
    private static final int COLOR_DIM        = 0xff9a8fb8;   // muted lavender for captions
    private static final int COLOR_WARM_BG    = 0xff221240;   // warm dusk violet for tool panels
    private static final int COLOR_SUB_PANEL  = 0xff2c1a52;   // lifted sub-panel
    private static final int COLOR_ACCENT_GLOW = 0xffff5c8a;  // sakura glow accent
    private static final int COLOR_RULE_LINE  = 0xff4a3566;   // violet dividers
    private static final int COLOR_SAKURA     = 0xffff7bac;   // sakura pink (dark text on fills)
    private static final int COLOR_INK        = 0xff14101f;   // manga ink (text on bright fills)
    private static final int COLOR_PAPER      = 0xfffff3dc;   // parchment paper
    private static final int COLOR_MANGA_GOLD = 0xffffd166;   // warm gold accent
    private static final int COLOR_SPIRIT     = 0xff7bf5d3;   // spirit mint (dark text on fills)
    private static final int COMPACT_BUTTON_HEIGHT = 42;

    // Narrow phone screens (Pixel 7 ~412dp) vs wide tablet (K12 ~800dp+).
    private boolean isNarrowScreen() {
        float w = getResources().getDisplayMetrics().widthPixels
                / getResources().getDisplayMetrics().density;
        return w < 700;
    }
    private static final int TOOL_PANEL_PADDING_TOP = 28;
    private static final int TOOL_PANEL_PADDING_SIDE = 24;

    // ── VEHICLE & KARR CLUE TABLES ───────────────────────────────────
    private static final String[][] VEHICLE_CLUES = new String[][] {
        {"karr","KARR/SWDS alarm clue"},{"swds","KARR/SWDS alarm clue"},
        {"southwest","Southwest Dealer Services clue"},{"acrisure","Acrisure/KARR clue"},
        {"ford","Ford clue"},{"lincoln","Lincoln clue"},{"chevrolet","Chevrolet clue"},
        {"chevy","Chevrolet clue"},{"gmc","GMC clue"},{"cadillac","Cadillac clue"},
        {"buick","Buick clue"},{"onstar","GM OnStar clue"},{"toyota","Toyota clue"},
        {"lexus","Lexus clue"},{"honda","Honda clue"},{"acura","Acura clue"},
        {"nissan","Nissan clue"},{"infiniti","Infiniti clue"},{"hyundai","Hyundai clue"},
        {"kia","Kia clue"},{"genesis","Genesis clue"},{"mazda","Mazda clue"},
        {"subaru","Subaru clue"},{"volkswagen","Volkswagen clue"},{" vw ","Volkswagen clue"},
        {"audi","Audi clue"},{"bmw","BMW clue"},{"mercedes","Mercedes clue"},
        {"mini","MINI clue"},{"tesla","Tesla clue"},{"rivian","Rivian clue"},
        {"jeep","Jeep clue"},{"dodge","Dodge clue"},{" ram ","Ram truck clue"},
        {"chrysler","Chrysler clue"},{"uconnect","Stellantis Uconnect clue"},
        {"volvo","Volvo clue"},{"polestar","Polestar clue"},{"porsche","Porsche clue"},
        {"jaguar","Jaguar clue"},{"land rover","Land Rover clue"},{"range rover","Range Rover clue"},
        {"sync","Ford SYNC clue"},{"mycar","Generic vehicle app/device clue"},
        {"carplay","CarPlay clue"},{"android auto","Android Auto clue"}
    };

    // ── FIELDS ────────────────────────────────────────────────────────
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<DetectorRule> rules = new ArrayList<>();
    private final List<String> eventLines = new ArrayList<>();
    private final Set<String> findingKeys = new LinkedHashSet<>();
    private final List<String> findingLines = new ArrayList<>();
    private final Map<String, ObservationState> observationStates = new HashMap<>();
    private final LinkedHashMap<String, Observation> latestTargets = new LinkedHashMap<>();

    // ── UI WIDGETS (main scan view) ───────────────────────────────────
    private FrameLayout frame;
    private View splashView;

    // -- TAKEOVER (kiosk-lite field mode) --
    private boolean takeoverActive;
    private int takeoverPriorDnd = -1;
    private TextToSpeech takeoverTts;
    private Thread.UncaughtExceptionHandler takeoverPrevHandler;
    private View targetDetailScreen;
    private View toolPanelOverlay;   // active tool panel (any of 10)
    private TextView status;
    private TextView sessionSummary;
    private TextView findings;
    private TextView events;
    private LinearLayout targetList;
    private TextView targetDetails;
    private Button sessionButton;
    private Button scanButton;
    private Button wifiButton;
    private Button reportButton;
    private Button toolsButton;      // opens tool palette
    private Button clearButton;
    private Button wardriveButton;

    // -- WARDRIVE STATE (passive drive-around discovery + GPS tagging) --
    private boolean wardriving;
    private boolean wardriveEverActive;
    private long wardriveStartedAt;
    private int wardriveFixes;
    private double wardriveLat = Double.NaN;
    private double wardriveLon = Double.NaN;
    private LocationManager wardriveLocMgr;
    private LocationListener wardriveListener;

    // ── BLE/WIFI STATE ────────────────────────────────────────────────
    private BluetoothLeScanner bleScanner;
    private boolean bleScanning;
    private WifiManager wifiManager;
    private File evidenceFile;
    private File reportFile;
    private File sdReportFile;
    private Uri reportTreeUri;
    private String sessionId = "";
    private long sessionStartedAt;
    private boolean sessionActive;
    private int bleSeen;
    private int bleLogged;
    private int wifiSeen;
    private int wifiLogged;
    private int findingCount;
    private long lastTargetRenderMs;
    private boolean targetRenderPending;

    // ── TARGET TOOL STATE ──────────────────────────────────────────────
    private boolean targetsFrozen = false;
    private final Map<String, TargetHistory> targetHistories = new HashMap<>();
    private final Map<String, TargetTags> targetTags = new HashMap<>();
    private final Map<String, TargetNotes> targetNotes = new HashMap<>();

    // -- VEHICLE AUDIT STATE (findings database + scan-results list) --
    private static final String[] AUDIT_CATEGORIES = new String[] {
            "Open Wi-Fi network",
            "WEP / legacy Wi-Fi security",
            "KARR/SWDS alarm module",
            "Unencrypted BLE telemetry",
            "Vehicle telematics / hotspot",
            "Delivery robot / autonomous vehicle",
            "Unknown vehicle device",
            "Other / needs follow-up"
    };
    private static final String[] AUDIT_SEVERITIES = new String[] {"low", "medium", "high"};
    /** While the scan-results screen is open, re-render discovery rows on this cadence. */
    private static final long AUDIT_REFRESH_INTERVAL_MS = 2500;
    // Full discovery inventory for the audit list (latestTargets stays capped for the HUD).
    private final LinkedHashMap<String, Observation> allTargets = new LinkedHashMap<>();
    private AuditDatabase auditDb;
    private View auditResultsScreen;
    private View auditDocumentScreen;
    private View auditFindingsScreen;
    private LinearLayout auditResultsList;
    private TextView auditResultsStatus;
    private LinearLayout auditCategoryRow;
    private LinearLayout auditSeverityRow;
    private EditText auditNotesInput;
    private Observation auditSelected;
    private String auditCategory = AUDIT_CATEGORIES[0];
    private String auditSeverity = "medium";

    // ── BLE CALLBACK ───────────────────────────────────────────────────
    private final ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            handleBleResult(result);
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) handleBleResult(result);
        }
        @Override
        public void onScanFailed(int errorCode) {
            addEvent("BLE scan failed: " + errorCode);
            setStatus("BLE scan failed: " + errorCode);
            bleScanning = false;
            if (scanButton != null) scanButton.setText("Start BLE");
        }
    };

    // ── WIFI RECEIVER ──────────────────────────────────────────────────
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleWifiResults();
        }
    };

    // ── ON CREATE ──────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rules.add(new KarrSwdsBleRule());
        rules.add(new OpenWifiRule());
        rules.add(new WepWifiRule());
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        File evidenceDir = new File(getExternalFilesDir(null), "evidence");
        evidenceFile = new File(evidenceDir, "field_security_evidence.jsonl");
        reportFile = new File(evidenceDir, "field_security_report.txt");
        sdReportFile = removableReportFile("field_security_report.txt");
        String savedTree = getPreferences(MODE_PRIVATE).getString(PREF_REPORT_TREE_URI, "");
        if (!TextUtils.isEmpty(savedTree)) reportTreeUri = Uri.parse(savedTree);
        auditDb = new AuditDatabase(getApplicationContext());
        buildUi();
        showSplash();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        appendEvidence("{\"type\":\"app_start\",\"time\":\"" + now() + "\",\"scope\":\"authorized defensive inspection only\",\"rules\":" + json(rulesSummary()) + "}");
        refreshPermissionState();
    }

    @Override
    public void onBackPressed() {
        if (splashView != null) {
            frame.removeView(splashView);
            splashView = null;
            return;
        }
        addEvent("Back pressed -- run kept alive in background. Swipe away to exit.");
        moveTaskToBack(true);
    }

    protected void onDestroy() {
        if (takeoverActive) speakLine("takeoff");
        releaseTakeover();
        stopBleScan();
        try { unregisterReceiver(wifiReceiver); } catch (Exception e) {}
        try { if (takeoverTts != null) takeoverTts.shutdown(); } catch (Exception e) {}
        super.onDestroy();
    }

    // ── BUILD UI ───────────────────────────────────────────────────────
    private void buildUi() {
        frame = new FrameLayout(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 18, 22, 18);
        root.setBackground(fantasySky());

        // Title block - manga scroll masthead (scaled down on narrow phones)
        boolean narrowTitle = isNarrowScreen();
        TextView title = text("\u2726 KARR MANGA FIELD SCROLL \u2726", narrowTitle ? 17 : 24, COLOR_PAPER);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTypeface(Typeface.SERIF, Typeface.BOLD_ITALIC);
        root.addView(title);

        TextView subtitle = text("AUTHORIZED DETECTION // EVIDENCE // CUSTOMER REPORTING", narrowTitle ? 10 : 13, COLOR_SAKURA);
        subtitle.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        subtitle.setPadding(0, 4, 0, 14);
        root.addView(subtitle);

        // Manga speed-stripe (sakura / ink / gold)
        root.addView(mangaStripe(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Session summary
        sessionSummary = text("", 15, COLOR_MACH_WHITE);
        sessionSummary.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        sessionSummary.setPadding(0, 8, 0, 6);
        sessionSummary.setBackground(panelDrawable(COLOR_PANEL, COLOR_CYAN, 2));
        root.addView(sessionSummary);

        // Status line
        status = text("", 15, COLOR_STATUS);
        status.setTypeface(Typeface.MONOSPACE);
        status.setPadding(0, 8, 0, 12);
        root.addView(status);

        // Primary vehicle-audit action: combined BLE + Wi-Fi discovery scan.
        Button startScanButton = button("Start Scan  (BLE + Wi-Fi vehicle discovery)");
        styleButton(startScanButton, COLOR_CYAN, 0xff101010, COLOR_MACH_WHITE);
        startScanButton.setOnClickListener(v -> startVehicleAuditScan());
        LinearLayout.LayoutParams startScanParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, COMPACT_BUTTON_HEIGHT);
        startScanParams.setMargins(0, 0, 0, 8);
        root.addView(startScanButton, startScanParams);

        // Top action buttons: Session / BLE / WiFi / Tools
        LinearLayout topButtons = new LinearLayout(this);
        topButtons.setOrientation(LinearLayout.HORIZONTAL);
        topButtons.setGravity(Gravity.CENTER_VERTICAL);

        sessionButton = button("Start Session");
        styleButton(sessionButton, COLOR_CRIMSON, COLOR_MACH_WHITE, COLOR_MACH_WHITE);
        sessionButton.setOnClickListener(v -> toggleSession());
        topButtons.addView(sessionButton, new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1));

        scanButton = button("Start BLE");
        styleButton(scanButton, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        scanButton.setOnClickListener(v -> toggleBleScan());
        LinearLayout.LayoutParams scanParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        scanParams.setMargins(10, 0, 0, 0);
        topButtons.addView(scanButton, scanParams);

        wifiButton = button("Scan Wi-Fi");
        styleButton(wifiButton, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        wifiButton.setOnClickListener(v -> startWifiScan());
        LinearLayout.LayoutParams wifiParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        wifiParams.setMargins(10, 0, 0, 0);
        topButtons.addView(wifiButton, wifiParams);

        toolsButton = button("TOOLS");
        styleButton(toolsButton, COLOR_YELLOW, 0xff101010, COLOR_MACH_WHITE);
        toolsButton.setOnClickListener(v -> showToolPalette());
        LinearLayout.LayoutParams toolsParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        toolsParams.setMargins(10, 0, 0, 0);
        topButtons.addView(toolsButton, toolsParams);

        wardriveButton = button("Wardrive");
        styleButton(wardriveButton, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        wardriveButton.setOnClickListener(v -> toggleWardrive());
        LinearLayout.LayoutParams wardriveParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        wardriveParams.setMargins(10, 0, 0, 0);
        topButtons.addView(wardriveButton, wardriveParams);

        if (isNarrowScreen()) {
            // Narrow phones: 3 + 2 rows so labels never squeeze/overlap.
            LinearLayout row2 = new LinearLayout(this);
            row2.setOrientation(LinearLayout.HORIZONTAL);
            row2.setGravity(Gravity.CENTER_VERTICAL);
            topButtons.removeView(toolsButton);
            topButtons.removeView(wardriveButton);
            toolsParams.setMargins(0, 0, 0, 0);
            row2.addView(toolsButton, toolsParams);
            row2.addView(wardriveButton, wardriveParams);
            float density = getResources().getDisplayMetrics().density;
            int roomyPx = (int) (52 * density + 0.5f);
            for (int i = 0; i < topButtons.getChildCount(); i++) {
                View c = topButtons.getChildAt(i);
                if (c != null && c.getLayoutParams() != null) c.getLayoutParams().height = roomyPx;
            }
            for (int i = 0; i < row2.getChildCount(); i++) {
                View c = row2.getChildAt(i);
                if (c != null && c.getLayoutParams() != null) c.getLayoutParams().height = roomyPx;
            }
            root.addView(topButtons);
            LinearLayout.LayoutParams row2P = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            row2P.setMargins(0, (int) (16 * getResources().getDisplayMetrics().density + 0.5f), 0, 0);
            root.addView(row2, row2P);
        } else {
            root.addView(topButtons);
        }

        // Bottom row: Report / Clear
        LinearLayout lowerButtons = new LinearLayout(this);
        lowerButtons.setOrientation(LinearLayout.HORIZONTAL);
        lowerButtons.setGravity(Gravity.CENTER_VERTICAL);

        reportButton = button("Save Report");
        styleButton(reportButton, COLOR_YELLOW, 0xff101010, COLOR_MACH_WHITE);
        reportButton.setOnClickListener(v -> saveReport("manual"));
        lowerButtons.addView(reportButton, new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1));

        clearButton = button("Clear View");
        styleButton(clearButton, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        clearButton.setOnClickListener(v -> clearCurrentView());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        clearParams.setMargins(10, 0, 0, 0);
        lowerButtons.addView(clearButton, clearParams);

        root.addView(lowerButtons);

        // Storage buttons
        LinearLayout storageButtons = new LinearLayout(this);
        storageButtons.setOrientation(LinearLayout.HORIZONTAL);
        storageButtons.setGravity(Gravity.CENTER_VERTICAL);

        Button chooseSdButton = button("SD Folder Override");
        styleButton(chooseSdButton, COLOR_PANEL, COLOR_CYAN, 0xff50556f);
        chooseSdButton.setOnClickListener(v -> chooseReportFolder());
        storageButtons.addView(chooseSdButton, new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1));

        Button forgetSdButton = button("Tablet Only");
        styleButton(forgetSdButton, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        forgetSdButton.setOnClickListener(v -> clearReportFolder());
        LinearLayout.LayoutParams forgetParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        forgetParams.setMargins(10, 0, 0, 0);
        storageButtons.addView(forgetSdButton, forgetParams);
        root.addView(storageButtons);

        // Vehicle audit: scan-results list and the documented-findings database.
        LinearLayout auditButtons = new LinearLayout(this);
        auditButtons.setOrientation(LinearLayout.HORIZONTAL);
        auditButtons.setGravity(Gravity.CENTER_VERTICAL);

        Button scanResultsButton = button("Scan Results");
        styleButton(scanResultsButton, COLOR_CYAN, 0xff101010, COLOR_MACH_WHITE);
        scanResultsButton.setOnClickListener(v -> showAuditScanResults());
        auditButtons.addView(scanResultsButton, new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1));

        Button findingsDbButton = button("Documented Findings");
        styleButton(findingsDbButton, COLOR_YELLOW, 0xff101010, COLOR_MACH_WHITE);
        findingsDbButton.setOnClickListener(v -> showAuditFindingsList());
        LinearLayout.LayoutParams findingsDbParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        findingsDbParams.setMargins(10, 0, 0, 0);
        auditButtons.addView(findingsDbButton, findingsDbParams);
        root.addView(auditButtons);

        // Findings section
        findings = section("Findings");
        root.addView(findings);

        // Target header + detail + list
        TextView targetHeader = text("Observed Targets", 14, COLOR_MACH_WHITE);
        targetHeader.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        targetHeader.setPadding(0, 12, 0, 4);
        root.addView(targetHeader);

        targetDetails = section("Target Detail");
        root.addView(targetDetails);

        targetList = new LinearLayout(this);
        targetList.setOrientation(LinearLayout.VERTICAL);
        targetList.setPadding(0, 0, 0, 4);
        root.addView(targetList);

        // Evidence column + assistant image
        LinearLayout workArea = new LinearLayout(this);
        workArea.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout evidenceColumn = new LinearLayout(this);
        evidenceColumn.setOrientation(LinearLayout.VERTICAL);

        events = section("Recent Evidence");
        ScrollView scroller = new ScrollView(this);
        scroller.addView(events);
        evidenceColumn.addView(scroller, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        workArea.addView(evidenceColumn, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 2));

        // ANIMAE assistant character in the work zone — visual anchor for the whole HUD.
        ImageView assistant = new ImageView(this);
        assistant.setImageResource(getResources().getIdentifier("racer_zero_assistant", "drawable", getPackageName()));
        assistant.setScaleType(ImageView.ScaleType.FIT_CENTER);
        assistant.setAlpha(0.72f);
        LinearLayout.LayoutParams assistantParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        assistantParams.setMargins(12, 16, 0, 0);
        workArea.addView(assistant, assistantParams);
        root.addView(workArea, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        frame.addView(root);
        setContentView(frame);
        renderTargets();
        updateSessionSummary();
    }

    // ── SPLASH ─────────────────────────────────────────────────────────
    private void showSplash() {
        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackground(fantasySky());
        ImageView image = new ImageView(this);
        int[] splashImages = new int[] {
            getResources().getIdentifier("racer_zero_splash", "drawable", getPackageName()),
            getResources().getIdentifier("racer_zero_splash_fantasy", "drawable", getPackageName())
        };
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        int splashIndex = prefs.getInt(PREF_SPLASH_INDEX, 0);
        int splashResource = splashImages[Math.abs(splashIndex) % splashImages.length];
        prefs.edit().putInt(PREF_SPLASH_INDEX, splashIndex + 1).apply();
        if (splashResource != 0) image.setImageResource(splashResource);
        else image.setVisibility(View.GONE);
        // Full-screen backdrop: art spans the full width pinned to the top,
        // gradient fills everything below. No crop, no postage stamp.
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams imgP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        imgP.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        overlay.addView(image, imgP);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(28, 22, 28, 22);
        card.setBackgroundColor(COLOR_WARM_BG);
        TextView hello = text("Hey! Let me take over for this run?", 18, COLOR_MACH_WHITE);
        hello.setPadding(0, 0, 0, 8);
        card.addView(hello);
        TextView sub = text("I will silence calls, keep the screen awake, and hold "
                + "foreground so scans survive. Everything restores when you exit.",
                13, COLOR_STATUS);
        sub.setPadding(0, 0, 0, 12);
        card.addView(sub);
        CheckBox takeBox = new CheckBox(this);
        takeBox.setText("Yes -- take over the phone for this run");
        takeBox.setTextColor(COLOR_MACH_WHITE);
        takeBox.setTextSize(14);
        card.addView(takeBox);
        Button go = button("Start");
        go.setOnClickListener(v -> {
            boolean want = takeBox.isChecked();
            frame.removeView(overlay);
            splashView = null;
            if (want) enableTakeover();
            speakLine(want ? "takeon" : "takeoff_skip");
        });
        LinearLayout.LayoutParams goP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, COMPACT_BUTTON_HEIGHT);
        goP.setMargins(0, 14, 0, 0);
        card.addView(go, goP);

        FrameLayout.LayoutParams cardP = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        float density = getResources().getDisplayMetrics().density;
        int m = (int) (24 * density);
        int bottom = (int) (200 * density);
        cardP.setMargins(m, m, m, bottom);
        cardP.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        overlay.addView(card, cardP);
        splashView = overlay;
        frame.addView(splashView);
        speakLine("consent");
    }

    // -- TAKEOVER engine: DND + wake lock flag + foreground pin + voice --
    private void enableTakeover() {
        takeoverActive = true;
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null && Build.VERSION.SDK_INT >= 23) {
                if (nm.isNotificationPolicyAccessGranted()) {
                    takeoverPriorDnd = nm.getCurrentInterruptionFilter();
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                } else {
                    addEvent("Takeover: DND access not granted -- calls stay on. "
                            + "Grant Do-Not-Disturb access manually for full silence; everything else is active.");
                }
            }
        } catch (Exception e) {
            addEvent("Takeover DND note: " + e.getMessage());
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            Intent fi = new Intent(this, FieldService.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(fi);
            else startService(fi);
        } catch (Exception e) {
            addEvent("Takeover foreground note: " + e.getMessage());
        }
        if (takeoverPrevHandler == null) {
            takeoverPrevHandler = Thread.getDefaultUncaughtExceptionHandler();
            final Thread.UncaughtExceptionHandler prev = takeoverPrevHandler;
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                releaseTakeover();
                if (prev != null) prev.uncaughtException(t, e);
            });
        }
        appendEvidence("{\"type\":\"takeover_start\",\"session_id\":" + json(sessionId)
                + ",\"time\":" + json(now()) + "}");
        addEvent("Takeover ON: calls silenced, screen awake, scans pinned foreground.");
        setStatus("TAKEOVER ACTIVE -- phone is a field instrument. Exit the app to restore.");
    }

    private void releaseTakeover() {
        if (!takeoverActive) return;
        takeoverActive = false;
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null && Build.VERSION.SDK_INT >= 23
                    && nm.isNotificationPolicyAccessGranted() && takeoverPriorDnd >= 0) {
                nm.setInterruptionFilter(takeoverPriorDnd);
            }
        } catch (Exception e) {
            android.util.Log.w("KARR_TAKEOVER", "dnd restore: " + e.getMessage());
        }
        try {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {
            android.util.Log.w("KARR_TAKEOVER", "wake restore: " + e.getMessage());
        }
        try {
            stopService(new Intent(this, FieldService.class));
        } catch (Exception e) {
            android.util.Log.w("KARR_TAKEOVER", "service restore: " + e.getMessage());
        }
        try {
            appendEvidence("{\"type\":\"takeover_stop\",\"session_id\":" + json(sessionId)
                    + ",\"time\":" + json(now()) + "}");
        } catch (Exception e) {
            android.util.Log.w("KARR_TAKEOVER", "evidence: " + e.getMessage());
        }
    }

    // Voice: plays res/raw ElevenLabs file when present, else system TTS.
    private void speakLine(String which) {
        String[] files = {"karr_consent", "karr_takeon", "karr_takeoff"};
        String pick = "karr_consent";
        if ("takeon".equals(which)) pick = "karr_takeon";
        else if ("takeoff".equals(which) || "takeoff_skip".equals(which)) pick = "karr_takeoff";
        if ("takeoff_skip".equals(which)) return;
        int resId = getResources().getIdentifier(pick, "raw", getPackageName());
        if (resId != 0) {
            try {
                MediaPlayer mp = MediaPlayer.create(this, resId);
                if (mp != null) {
                    mp.setOnCompletionListener(MediaPlayer::release);
                    mp.start();
                    return;
                }
            } catch (Exception e) {
                android.util.Log.w("KARR_VOICE", "raw playback: " + e.getMessage());
            }
        }
        String fallback = "consent".equals(which)
                ? "Hey! Let me take over for this run?"
                : ("takeon".equals(which)
                        ? "I have got the phone. Ride safe, I will log everything."
                        : "All yours again. Calls are back, settings restored.");
        try {
            if (takeoverTts == null) {
                takeoverTts = new TextToSpeech(this, status -> {
                    if (status == TextToSpeech.SUCCESS) takeoverTts.speak(fallback,
                            TextToSpeech.QUEUE_FLUSH, null, "karr_" + which);
                });
            } else {
                takeoverTts.speak(fallback, TextToSpeech.QUEUE_FLUSH, null, "karr_" + which);
            }
        } catch (Exception e) {
            android.util.Log.w("KARR_VOICE", "tts: " + e.getMessage());
        }
    }

    // ── TEXT / BUTTON FACTORIES ────────────────────────────────────────
    private TextView section(String label) {
        TextView view = text(label + "\nWaiting for scans.", 14, COLOR_STATUS);
        view.setTypeface(Typeface.MONOSPACE);
        view.setPadding(0, 18, 0, 0);
        return view;
    }

    private TextView text(String value, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setLineSpacing(4, 1.0f);
        return view;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        button.setTextSize(13);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(6, 2, 6, 2);
        return button;
    }

    private void styleButton(Button button, int fill, int textColor, int stroke) {
        button.setTextColor(textColor);
        button.setBackground(panelDrawable(fill, stroke, 3));
    }

    private GradientDrawable panelDrawable(int fill, int stroke, int width) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setStroke(width, stroke);
        drawable.setCornerRadius(12);
        return drawable;
    }

    // Fantasy night-sky gradient: dark dusk violet throughout so every label
    // (paper-white or sky-bright) stays readable. Code-drawn, no assets.
    private GradientDrawable fantasySky() {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] { 0xff2a1548, 0xff1b1035, 0xff33175c });
        d.setCornerRadius(0);
        return d;
    }

    // Sakura / ink / gold manga speed-stripe for section headers.
    private LinearLayout mangaStripe() {
        LinearLayout stripe = new LinearLayout(this);
        stripe.setOrientation(LinearLayout.VERTICAL);
        int[] bands = new int[] { COLOR_SAKURA, COLOR_INK, COLOR_MANGA_GOLD };
        int[] heights = new int[] { 5, 2, 3 };
        for (int i = 0; i < bands.length; i++) {
            View band = new View(this);
            band.setBackgroundColor(bands[i]);
            stripe.addView(band, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, heights[i]));
        }
        return stripe;
    }

    // ── PERMISSIONS ────────────────────────────────────────────────────
    private void refreshPermissionState() {
        if (!hasRequiredPermissions()) {
            setStatus("Permissions needed for BLE and Wi-Fi inspection.");
            requestPermissions(requiredPermissions(), REQ_PERMISSIONS);
        } else {
            setStatus("Ready. Start a session, then scan BLE and Wi-Fi.");
        }
    }

    // ── SD FOLDER ──────────────────────────────────────────────────────
    private void chooseReportFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        startActivityForResult(intent, REQ_REPORT_FOLDER);
    }

    private void clearReportFolder() {
        if (reportTreeUri != null) {
            try {
                getContentResolver().releasePersistableUriPermission(
                        reportTreeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (SecurityException ignored) {}
        }
        reportTreeUri = null;
        getPreferences(MODE_PRIVATE).edit().remove(PREF_REPORT_TREE_URI).apply();
        setStatus("Report destination reset to tablet storage only.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_REPORT_FOLDER) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                reportTreeUri = data.getData();
                int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(reportTreeUri, flags);
                getPreferences(MODE_PRIVATE).edit().putString(PREF_REPORT_TREE_URI, reportTreeUri.toString()).apply();
                setStatus("Report folder selected. Save Report will copy reports there.");
            } else {
                setStatus("No report folder selected. Reports stay on tablet storage.");
            }
        }
    }

    // ── SESSION ────────────────────────────────────────────────────────
    private void toggleSession() {
        if (sessionActive) endSession(); else startSession();
    }

    private void startSession() {
        sessionId = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        sessionStartedAt = System.currentTimeMillis();
        sessionActive = true;
        bleSeen = 0; bleLogged = 0;
        wifiSeen = 0; wifiLogged = 0;
        findingCount = 0;
        wardriving = false;
        wardriveEverActive = false;
        wardriveFixes = 0;
        wardriveLat = Double.NaN;
        wardriveLon = Double.NaN;
        if (wardriveButton != null) {
            wardriveButton.setText("Wardrive");
            styleButton(wardriveButton, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        }
        eventLines.clear();
        findingLines.clear();
        findingKeys.clear();
        observationStates.clear();
        latestTargets.clear();
        allTargets.clear();
        targetHistories.clear();
        targetTags.clear();
        targetNotes.clear();
        renderTargets();
        if (targetDetails != null)
            targetDetails.setText("Target Detail\nTap an observed BLE or Wi-Fi target.");

        File evidenceDir = new File(getExternalFilesDir(null), "evidence");
        evidenceFile = new File(evidenceDir, "field_security_" + sessionId + ".jsonl");
        reportFile = new File(evidenceDir, "field_report_" + sessionId + ".txt");
        sdReportFile = removableReportFile("field_report_" + sessionId + ".txt");

        sessionButton.setText("End Session");
        styleButton(sessionButton, COLOR_YELLOW, 0xff101010, COLOR_MACH_WHITE);
        findings.setText("Findings\nNo findings yet.");
        events.setText("Recent Evidence\nSession started.");
        appendEvidence("{\"type\":\"session_start\",\"session_id\":" + json(sessionId)
                + ",\"time\":" + json(now())
                + ",\"scope\":\"authorized defensive inspection only\"}");
        addEvent("Session started: " + sessionId);
        updateSessionSummary();
        setStatus("Session active. Evidence: " + evidenceFile.getAbsolutePath() + sdStatusSuffix());
    }

    private void endSession() {
        if (wardriving) stopWardrive();
        stopBleScan();
        appendEvidence("{\"type\":\"session_end\",\"session_id\":" + json(sessionId)
                + ",\"time\":" + json(now())
                + ",\"ble_seen\":" + bleSeen
                + ",\"wifi_seen\":" + wifiSeen
                + ",\"findings\":" + findingCount + "}");
        saveReport("session_end");
        if (takeoverActive) {
            setStatus("Send-off playing -- restoring your settings right after.");
            playTakeoffThen(() -> finishEndSessionCloseout(true));
        } else {
            finishEndSessionCloseout(false);
        }
    }

    private void finishEndSessionCloseout(boolean autoExit) {
        releaseTakeover();
        sessionActive = false;
        sessionButton.setText("Start Session");
        styleButton(sessionButton, COLOR_CRIMSON, COLOR_MACH_WHITE, COLOR_MACH_WHITE);
        updateSessionSummary();
        setStatus("Session ended. Report: " + reportFile.getAbsolutePath() + sdStatusSuffix());
        if (autoExit) {
            addEvent("Takeover run closed -- exiting to free the phone for gate app.");
            finish();
        }
    }

    // Exit send-off: play the takeoff clip FIRST, restore settings after it
    // finishes (or 8s timeout so a bad clip can never hang the exit).
    private void playTakeoffThen(Runnable next) {
        final boolean[] done = new boolean[] { false };
        final Handler h = new Handler(Looper.getMainLooper());
        final Runnable fire = () -> {
            if (!done[0]) { done[0] = true; next.run(); }
        };
        h.postDelayed(fire, 8000);
        try {
            int resId = getResources().getIdentifier("karr_takeoff", "raw", getPackageName());
            if (resId != 0) {
                MediaPlayer mp = MediaPlayer.create(this, resId);
                if (mp != null) {
                    mp.setOnCompletionListener(m -> { m.release(); h.post(fire); });
                    mp.start();
                    return;
                }
            }
        } catch (Exception e) {
            android.util.Log.w("KARR_VOICE", "takeoff clip: " + e.getMessage());
        }
        try {
            String fallback = "All yours again. Calls are back, settings restored.";
            if (takeoverTts == null) {
                takeoverTts = new TextToSpeech(this, status -> {
                    if (status == TextToSpeech.SUCCESS)
                        takeoverTts.speak(fallback, TextToSpeech.QUEUE_FLUSH, null, "karr_takeoff_exit");
                    h.postDelayed(fire, 5000);
                });
            } else {
                takeoverTts.speak(fallback, TextToSpeech.QUEUE_FLUSH, null, "karr_takeoff_exit");
                h.postDelayed(fire, 5000);
            }
        } catch (Exception e) {
            android.util.Log.w("KARR_VOICE", "takeoff tts: " + e.getMessage());
            h.post(fire);
        }
    }

    private void ensureSession() {
        if (!sessionActive) startSession();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        refreshPermissionState();
    }

    private boolean hasRequiredPermissions() {
        for (String p : requiredPermissions())
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) return false;
        return true;
    }

    private String[] requiredPermissions() {
        List<String> list = new ArrayList<>();
        list.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(Manifest.permission.BLUETOOTH_SCAN);
            list.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }
        return list.toArray(new String[0]);
    }

    // ── BLE SCAN ───────────────────────────────────────────────────────
    // -- WARDRIVE (drive-around passive discovery with GPS tagging) --
    // Passive only: reuses the BLE + Wi-Fi scan pipeline, adds GPS fixes.
    // No connections, no writes, no vehicle control of any kind.
    private void toggleWardrive() {
        if (wardriving) stopWardrive(); else startWardrive();
    }

    private void startWardrive() {
        if (!hasRequiredPermissions()) { refreshPermissionState(); return; }
        ensureSession();
        if (!bleScanning) startBleScan();
        startWifiScan();
        wardriving = true;
        wardriveEverActive = true;
        wardriveStartedAt = System.currentTimeMillis();
        wardriveFixes = 0;
        wardriveLat = Double.NaN;
        wardriveLon = Double.NaN;
        try {
            if (wardriveLocMgr == null)
                wardriveLocMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (wardriveListener == null) {
                wardriveListener = new LocationListener() {
                    @Override public void onLocationChanged(Location loc) {
                        wardriveLat = loc.getLatitude();
                        wardriveLon = loc.getLongitude();
                        wardriveFixes++;
                        appendEvidence("{\"type\":\"wardrive_fix\",\"session_id\":" + json(sessionId)
                                + ",\"time\":" + json(now())
                                + ",\"lat\":" + wardriveLat
                                + ",\"lon\":" + wardriveLon
                                + ",\"acc_m\":" + loc.getAccuracy()
                                + ",\"ble_seen\":" + bleSeen
                                + ",\"wifi_seen\":" + wifiSeen + "}");
                        updateWardriveStatus();
                    }
                    @Override public void onProviderEnabled(String p) {}
                    @Override public void onProviderDisabled(String p) {
                        addEvent("Wardrive GPS provider disabled: " + p);
                    }
                };
            }
            wardriveLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 10, wardriveListener, Looper.getMainLooper());
        } catch (SecurityException se) {
            addEvent("Wardrive GPS needs location permission.");
        } catch (Exception e) {
            addEvent("Wardrive GPS unavailable: " + e.getMessage());
        }
        wardriveButton.setText("Stop Wardrive");
        styleButton(wardriveButton, COLOR_CRIMSON, COLOR_MACH_WHITE, COLOR_MACH_WHITE);
        appendEvidence("{\"type\":\"wardrive_start\",\"session_id\":" + json(sessionId)
                + ",\"time\":" + json(now()) + "}");
        addEvent("Wardrive started: passive BLE + Wi-Fi with GPS tagging.");
        updateWardriveStatus();
    }

    private void stopWardrive() {
        wardriving = false;
        try {
            if (wardriveLocMgr != null && wardriveListener != null)
                wardriveLocMgr.removeUpdates(wardriveListener);
        } catch (Exception e) {
            addEvent("Wardrive GPS stop note: " + e.getMessage());
        }
        long mins = (System.currentTimeMillis() - wardriveStartedAt) / 60000;
        appendEvidence("{\"type\":\"wardrive_stop\",\"session_id\":" + json(sessionId)
                + ",\"time\":" + json(now())
                + ",\"minutes\":" + mins
                + ",\"gps_fixes\":" + wardriveFixes
                + ",\"ble_seen\":" + bleSeen
                + ",\"wifi_seen\":" + wifiSeen
                + ",\"findings\":" + findingCount + "}");
        addEvent("Wardrive stopped after ~" + mins + " min, " + wardriveFixes + " GPS fixes.");
        wardriveButton.setText("Wardrive");
        styleButton(wardriveButton, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        updateSessionSummary();
        setStatus("Wardrive stopped. Scans still running; End Session to close out.");
    }

    private void updateWardriveStatus() {
        if (!wardriving) return;
        String where = Double.isNaN(wardriveLat) ? "waiting for GPS fix"
                : String.format(Locale.US, "%.5f, %.5f (%d fixes)", wardriveLat, wardriveLon, wardriveFixes);
        setStatus("WARDRIVE ACTIVE -- " + where + " | BLE " + bleSeen + " WIFI " + wifiSeen + " FINDINGS " + findingCount);
    }

    // -- BLE SCAN (anchor restored) --
    private void toggleBleScan() {
        if (bleScanning) stopBleScan(); else startBleScan();
    }

    private void startBleScan() {
        if (!hasRequiredPermissions()) { refreshPermissionState(); return; }
        ensureSession();
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            setStatus("Bluetooth is off. Enable Bluetooth, then start BLE again.");
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            return;
        }
        bleScanner = adapter.getBluetoothLeScanner();
        if (bleScanner == null) {
            setStatus("BLE scanner unavailable on this tablet.");
            return;
        }
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        bleScanner.startScan(null, settings, bleCallback);
        bleScanning = true;
        scanButton.setText("Stop BLE");
        styleButton(scanButton, COLOR_CRIMSON, COLOR_MACH_WHITE, COLOR_MACH_WHITE);
        setStatus("BLE scan running. Passive advertisement collection only.");
        appendEvidence("{\"type\":\"scan_start\",\"time\":\"" + now() + "\",\"radio\":\"ble\",\"mode\":\"passive\"}");
    }

    private void stopBleScan() {
        if (bleScanner != null && bleScanning && hasRequiredPermissions()) {
            bleScanner.stopScan(bleCallback);
            appendEvidence("{\"type\":\"scan_stop\",\"time\":\"" + now() + "\",\"radio\":\"ble\"}");
        }
        bleScanning = false;
        if (scanButton != null) {
            scanButton.setText("Start BLE");
            styleButton(scanButton, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        }
    }

    // ── WIFI SCAN ──────────────────────────────────────────────────────
    private void startWifiScan() {
        if (!hasRequiredPermissions()) { refreshPermissionState(); return; }
        ensureSession();
        if (wifiManager == null) {
            setStatus("Wi-Fi manager unavailable on this tablet.");
            return;
        }
        boolean started = wifiManager.startScan();
        appendEvidence("{\"type\":\"scan_start\",\"time\":\"" + now() + "\",\"radio\":\"wifi\",\"started\":" + started + "}");
        setStatus(started ? "Wi-Fi scan requested."
                : "Wi-Fi scan request was throttled; showing latest cached results when available.");
        main.postDelayed(this::handleWifiResults, 2500);
    }

    // ── HANDLE RESULTS ─────────────────────────────────────────────────
    private void handleBleResult(ScanResult result) {
        BluetoothDevice device = result.getDevice();
        byte[] raw = result.getScanRecord() == null ? new byte[0]
                : result.getScanRecord().getBytes();
        String name = result.getScanRecord() == null ? ""
                : result.getScanRecord().getDeviceName();
        if (TextUtils.isEmpty(name) && hasPermissionSafe(Manifest.permission.BLUETOOTH_CONNECT))
            name = device.getName();
        BleObservation observation = new BleObservation(now(), safe(device.getAddress()),
                safe(name), result.getRssi(), raw);
        bleSeen++;
        rememberTarget(observation);
        if (shouldLogObservation(observation)) {
            bleLogged++;
            appendEvidence(observation.toJson(sessionId));
            addEvent("BLE " + shortAddress(observation.address) + " RSSI " + observation.rssi + label(observation.name));
        }
        evaluate(observation);
        recordRssiHistory(observation);
        updateSessionSummary();
    }

    private void handleWifiResults() {
        if (wifiManager == null || !hasRequiredPermissions()) return;
        List<android.net.wifi.ScanResult> results = wifiManager.getScanResults();
        appendEvidence("{\"type\":\"wifi_scan_results\",\"time\":\"" + now() + "\",\"count\":" + results.size() + "}");
        for (android.net.wifi.ScanResult result : results) {
            WifiObservation observation = new WifiObservation(now(), safe(result.SSID),
                    safe(result.BSSID), safe(result.capabilities), result.level);
            wifiSeen++;
            rememberTarget(observation);
            if (shouldLogObservation(observation)) {
                wifiLogged++;
                appendEvidence(observation.toJson(sessionId));
                addEvent("Wi-Fi " + displaySsid(observation.ssid) + " "
                        + shortAddress(observation.bssid) + " " + observation.capabilities);
            }
            evaluate(observation);
            recordRssiHistory(observation);
        }
        if (results.isEmpty()) addEvent("Wi-Fi scan returned no visible networks.");
        updateSessionSummary();
    }

    private void recordRssiHistory(Observation obs) {
        String key = obs.type() + "|" + obs.identity();
        TargetHistory history = targetHistories.get(key);
        if (history == null) {
            history = new TargetHistory();
            targetHistories.put(key, history);
        }
        history.add(now(), obs.rssi());
    }

    // ── TARGET MEMORY ──────────────────────────────────────────────────
    private void rememberTarget(Observation observation) {
        String key = observation.type() + "|" + observation.identity();
        latestTargets.put(key, observation);
        allTargets.put(key, observation);
        while (latestTargets.size() > MAX_VISIBLE_TARGETS) {
            String oldestKey = latestTargets.keySet().iterator().next();
            latestTargets.remove(oldestKey);
        }
        scheduleTargetRender();
    }

    private void scheduleTargetRender() {
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastTargetRenderMs >= TARGET_RENDER_INTERVAL_MS) {
            renderTargets();
            return;
        }
        if (!targetRenderPending) {
            targetRenderPending = true;
            main.postDelayed(this::renderTargets, TARGET_RENDER_INTERVAL_MS);
        }
    }

    // ── RENDER TARGET ROWS ─────────────────────────────────────────────
    private void renderTargets() {
        if (targetList == null) return;
        lastTargetRenderMs = System.currentTimeMillis();
        targetRenderPending = false;
        targetList.removeAllViews();
        if (latestTargets.isEmpty()) {
            TextView empty = targetRow("No observed targets yet.");
            empty.setEnabled(false);
            targetList.addView(empty);
            return;
        }
        List<Observation> observations = new ArrayList<>(latestTargets.values());
        for (int i = observations.size() - 1; i >= 0; i--) {
            final Observation observation = observations.get(i);
            String karr = karrClue(observation);
            String clue = vehicleClue(observation);
            boolean hasKarr = !TextUtils.isEmpty(karr);
            boolean hasVehicle = !TextUtils.isEmpty(clue);
            String prefix = hasKarr ? "KARR? " : (hasVehicle ? "VEHICLE? " : "");
            TextView row = targetRow(prefix + observation.summary(), hasKarr || hasVehicle);
            row.setOnClickListener(v -> showTargetDetails(observation));
            targetList.addView(row);
        }
    }

    private TextView targetRow(String value) {
        return targetRow(value, false);
    }

    private TextView targetRow(String value, boolean vehicleCandidate) {
        TextView row = text(value, 13, vehicleCandidate ? COLOR_YELLOW : COLOR_CYAN);
        row.setTypeface(Typeface.MONOSPACE);
        row.setPadding(8, 3, 8, 3);
        row.setBackground(panelDrawable(0xff101322,
                vehicleCandidate ? COLOR_YELLOW : 0xff283044, 1));
        return row;
    }

    // ── SHOW TARGET DETAIL ────────────────────────────────────────────
    private void showTargetDetails(Observation observation) {
        String detail = observation.detail();
        targetDetails.setText("Target Detail\nSelected " + observation.summary());
        showTargetDetailScreen(observation, detail);
        appendEvidence("{\"type\":\"target_selected\",\"session_id\":" + json(sessionId)
                + ",\"time\":" + json(now())
                + ",\"target_type\":" + json(observation.type())
                + ",\"target_id\":" + json(observation.identity()) + "}");
    }

    private void showTargetDetailScreen(Observation observation, String detail) {
        if (targetDetailScreen != null) frame.removeView(targetDetailScreen);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(26, 22, 26, 22);
        panel.setBackground(fantasySky());

        TextView title = text("TARGET DETAIL", 24, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        panel.addView(title);

        TextView summary = text(observation.summary(), 16, COLOR_CYAN);
        summary.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        summary.setPadding(0, 8, 0, 10);
        panel.addView(summary);

        String karr = karrClue(observation);
        String clue = vehicleClue(observation);
        TextView vehicle = text(!TextUtils.isEmpty(karr)
                ? "KARR/SWDS clue: " + karr
                : TextUtils.isEmpty(clue)
                    ? "Vehicle clue: none found in passive advertisement/beacon data."
                    : "Vehicle clue: " + clue,
                16, TextUtils.isEmpty(clue) ? COLOR_STATUS : COLOR_YELLOW);
        vehicle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        vehicle.setPadding(0, 0, 0, 10);
        panel.addView(vehicle);

        Button back = button("Back to Scan");
        styleButton(back, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        back.setOnClickListener(v -> {
            if (targetDetailScreen != null) {
                frame.removeView(targetDetailScreen);
                targetDetailScreen = null;
            }
        });
        panel.addView(back, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView body = text(detail + "\n\nMode: passive observation only. "
                + "No connection or vehicle-control action was performed.", 15, COLOR_STATUS);
        body.setTypeface(Typeface.MONOSPACE);
        body.setPadding(0, 14, 0, 0);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(body);
        panel.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        targetDetailScreen = panel;
        frame.addView(targetDetailScreen, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    // ── OBSERVATION LOGGING ────────────────────────────────────────────
    private boolean shouldLogObservation(Observation observation) {
        String key = observation.type() + "|" + observation.identity();
        long nowMs = System.currentTimeMillis();
        ObservationState state = observationStates.get(key);
        if (state == null) {
            observationStates.put(key, new ObservationState(nowMs, observation.rssi()));
            return true;
        }
        boolean stale = nowMs - state.lastLoggedMs >= BLE_LOG_INTERVAL_MS;
        boolean moved = Math.abs(observation.rssi() - state.lastRssi) >= BLE_RSSI_DELTA;
        if (stale || moved) {
            state.lastLoggedMs = nowMs;
            state.lastRssi = observation.rssi();
            return true;
        }
        return false;
    }

    // ── DETECTOR RULES ─────────────────────────────────────────────────
    private void evaluate(Observation observation) {
        for (DetectorRule rule : rules) {
            Finding finding = rule.evaluate(observation);
            if (finding != null) {
                String key = finding.ruleId + "|" + observation.identity();
                if (findingKeys.add(key)) {
                    findingCount++;
                    appendEvidence(finding.toJson(sessionId, observation));
                    findingLines.add(0, finding.title + "\n" + observation.summary()
                            + "\n" + finding.detail + "\nConfidence: " + finding.confidence
                            + "\nRule: " + finding.ruleId);
                    findings.setText("Findings\n" + TextUtils.join("\n\n", findingLines));
                    saveReport("finding");
                }
            }
        }
    }

    // ── EVENTS & STATUS ────────────────────────────────────────────────
    private void addEvent(String line) {
        eventLines.add(0, now() + "  " + line);
        while (eventLines.size() > MAX_LOG_LINES)
            eventLines.remove(eventLines.size() - 1);
        events.setText("Recent Evidence\n" + TextUtils.join("\n", eventLines));
    }

    private void setStatus(String line) {
        status.setText(line + "\nActive rules: " + rulesSummary());
    }

    private void updateSessionSummary() {
        if (sessionSummary == null) return;
        String state = sessionActive ? "SESSION " + sessionId : "NO ACTIVE SESSION";
        sessionSummary.setText(state
                + "\nBLE " + bleSeen + "/" + bleLogged
                + "   WIFI " + wifiSeen + "/" + wifiLogged
                + "   FINDINGS " + findingCount
                + (sdReportFile == null ? "   SD OFFLINE" : "   SD REPORT READY"));
    }

    private String rulesSummary() {
        List<String> names = new ArrayList<>();
        for (DetectorRule rule : rules) names.add(rule.id());
        names.add("passive_inventory");
        return TextUtils.join(", ", names);
    }

    // ── CLEAR ──────────────────────────────────────────────────────────
    private void clearCurrentView() {
        eventLines.clear();
        if (findings != null) {
            findings.setText(findingLines.isEmpty()
                    ? "Findings\nNo findings yet."
                    : "Findings\n" + TextUtils.join("\n\n", findingLines));
        }
        if (events != null) {
            events.setText("Recent Evidence\nView cleared.");
        }
    }

    // ── TOOL PALETTE (10 buttons, one per tool) ───────────────────────
    private void showToolPalette() {
        try {
            android.util.Log.d("KARR_TOOLS", "showToolPalette ENTER");
            if (toolPanelOverlay != null) frame.removeView(toolPanelOverlay);
            LinearLayout palette = new LinearLayout(this);
        palette.setOrientation(LinearLayout.VERTICAL);
        palette.setPadding(24, 18, 24, 18);
        palette.setBackground(fantasySky());

        TextView title = text("\u2726 FIELD TOOLS \u2014 SELECT A PANEL \u2726", 20, COLOR_PAPER);
        title.setTypeface(Typeface.SERIF, Typeface.BOLD_ITALIC);
        title.setPadding(0, 8, 0, 14);
        palette.addView(title);

        // ANIMAE accent bar
        View accentBar = new View(this);
        accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
        accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));
        palette.addView(accentBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));

        // Manga speed-stripe (sakura / ink / gold)
        palette.addView(mangaStripe(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // 10 tool buttons in a 2-column grid
        String[] toolNames = {
            "1. TARGET FREEZER",
            "2. KARR/SWDS RESEARCH",
            "3. BLE DEEP DETAIL",
            "4. WIFI DEEP DETAIL",
            "5. OUI/VENDOR LOOKUP",
            "6. SESSION TIMELINE",
            "7. TARGET TAGS",
            "8. EVIDENCE PACKET",
            "9. RESEARCH NOTES",
            "10. DISCLOSURE REPORT"
        };
        int[] toolColors = {
            COLOR_CRIMSON, COLOR_CYAN, COLOR_CYAN, COLOR_CYAN,
            COLOR_CYAN, COLOR_CYAN, COLOR_CYAN, COLOR_YELLOW,
            COLOR_YELLOW, COLOR_YELLOW
        };
        int[] toolTextColors = {
            COLOR_MACH_WHITE, 0xff101010, 0xff101010, 0xff101010,
            0xff101010, 0xff101010, 0xff101010, 0xff101010,
            0xff101010, 0xff101010
        };
        String[] toolShort = {
            "Freeze target list for inspection",
            "KARR/SWDS keyword & detection research",
            "Full BLE advertise decode: UUID, mfg, tx",
            "Full Wi-Fi decode: SSID, OUI, security",
            "Built-in OUI vendor database lookup",
            "RSSI timeline for every observed target",
            "Tag targets: KARR?, Vehicle?, Hotspot?...",
            "One-tap evidence packet export",
            "Quick notes per target",
            "Mercedes VDP disclosure template"
        };


        for (int i = 0; i < 10; i++) {
            Button b = button(toolNames[i]);
            b.setTextSize(12);
            b.setPadding(14, 7, 14, 7);
            b.setMinHeight(0);
            b.setMinimumHeight(0);
            styleButton(b, toolColors[i], toolTextColors[i],
                    toolColors[i] == COLOR_CRIMSON ? COLOR_MACH_WHITE :
                    (toolColors[i] == COLOR_YELLOW ? 0xff101010 : toolColors[i]));
            final int finalI = i;
            b.setOnClickListener(v -> openTool(finalI));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    COMPACT_BUTTON_HEIGHT);
            p.setMargins(0, 3, 0, 3);
            palette.addView(b, p);

            TextView desc = text(toolShort[i], 11, COLOR_DIM);
            desc.setPadding(14, 0, 14, 6);
            palette.addView(desc);
        }

        Button closePalette = button("Back to Scan");
        styleButton(closePalette, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        closePalette.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        palette.addView(closePalette, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                COMPACT_BUTTON_HEIGHT));

        palette.addView(animaeFooter());

        toolPanelOverlay = palette;
        frame.addView(toolPanelOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
            android.util.Log.d("KARR_TOOLS", "showToolPalette EXIT - palette added");
        } catch (Exception e) {
            android.util.Log.e("KARR_TOOLS", "showToolPalette FAILED", e);
            e.printStackTrace();
        }
    }

    // ── OPEN A SPECIFIC TOOL (index 0-9) ──────────────────────────────
    private void openTool(int index) {
        if (toolPanelOverlay != null) frame.removeView(toolPanelOverlay);
        toolPanelOverlay = buildToolPanel(index);
        frame.addView(toolPanelOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private View buildToolPanel(int index) {
        switch (index) {
            case 0: return buildTargetFreezerPanel();
            case 1: return buildKarrSwdsResearchPanel();
            case 2: return buildBleDeepDetailPanel();
            case 3: return buildWifiDeepDetailPanel();
            case 4: return buildOuiVendorPanel();
            case 5: return buildSessionTimelinePanel();
            case 6: return buildTargetTagsPanel();
            case 7: return buildEvidencePacketPanel();
            case 8: return buildResearchNotesPanel();
            case 9: return buildDisclosureReportPanel();
            default: return buildTargetFreezerPanel();
        }
    }

    // ── TOOL 1: TARGET FREEZER ─────────────────────────────────────────
    private View buildTargetFreezerPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
        root.setBackground(fantasySky());

        // ANIMAE-style title block with subtle accent glow

        TextView title = text("TARGET FREEZER", 22, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 10, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_SAKURA);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        TextView statusLine = text(targetsFrozen
                ? "TARGET LIST FROZEN — fast BLE traffic paused for inspection"
                : "TARGET LIST LIVE — BLE traffic updating normally",
                14, targetsFrozen ? COLOR_YELLOW : COLOR_CYAN);
        statusLine.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusLine.setPadding(0, 10, 0, 8);
        root.addView(statusLine);

        // Freeze toggle
        Button freezeBtn = button(targetsFrozen ? "Unfreeze Targets" : "Freeze Targets");
        styleButton(freezeBtn, targetsFrozen ? COLOR_YELLOW : COLOR_CRIMSON,
                targetsFrozen ? 0xff101010 : COLOR_MACH_WHITE,
                targetsFrozen ? COLOR_MACH_WHITE : COLOR_MACH_WHITE);
        freezeBtn.setOnClickListener(v -> {
            targetsFrozen = !targetsFrozen;
            if (targetsFrozen) {
                stopBleScan();
                setStatus("Targets frozen. BLE scan paused. Resume to continue.");
            } else {
                startBleScan();
                setStatus("Targets unfrozen. BLE scan resumed.");
            }
            frame.removeView(toolPanelOverlay);
            toolPanelOverlay = buildTargetFreezerPanel();
            frame.addView(toolPanelOverlay, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
        });
        root.addView(freezeBtn, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, COMPACT_BUTTON_HEIGHT));

        // Frozen target list
        TextView frozenHeader = text("Frozen Target Inventory", 14, COLOR_CYAN);
        frozenHeader.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        frozenHeader.setPadding(0, 14, 0, 4);
        root.addView(frozenHeader);

        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        // Subtle panel header before the list
        View listDivider = new View(this);
        listDivider.setBackgroundColor(COLOR_RULE_LINE);
        listDivider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
            // 3px separator inline
            list.addView(listDivider, new LinearLayout.LayoutParams(
                    0, 3));

        TextView listHeader = text("FROZEN INVENTORY", 12, COLOR_DIM);
        listHeader.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        listHeader.setPadding(8, 0, 8, 4);
        list.addView(listHeader);

        if (latestTargets.isEmpty()) {
            TextView empty = text("No targets observed yet in this session.", 13, COLOR_DIM);
            empty.setTypeface(Typeface.MONOSPACE);
            empty.setPadding(8, 4, 8, 4);
            list.addView(empty);
        } else {
            for (Observation obs : latestTargets.values()) {
                TextView row = text("• " + obs.summary(), 13,
                        karrClue(obs) != null && !karrClue(obs).isEmpty() ? COLOR_YELLOW : COLOR_CYAN);
                row.setTypeface(Typeface.MONOSPACE);
                row.setPadding(8, 3, 8, 3);
                row.setBackground(panelDrawable(0xff101322, 0xff283044, 1));
                list.addView(row);

                // Subtle divider between items
                if (obs != latestTargets.values().iterator().next()) {
                    View itemSep = new View(this);
                    itemSep.setBackgroundColor(0xff222536);
                    itemSep.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    list.addView(itemSep, new LinearLayout.LayoutParams(
                            0, 1));
                }
            }
        }

        // Back buttons with ANIMAE footer
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);

        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);

        root.addView(animaeFooter());
        return root;
    }

    // ── TOOL 2: KARR/SWDS RESEARCH PANEL ──────────────────────────────
    private View buildKarrSwdsResearchPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
        root.setBackground(fantasySky());

        // ANIMAE accent bar
        View accentBar = new View(this);
        accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
        accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));
        root.addView(accentBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));

        TextView title = text("KARR/SWDS RESEARCH PANEL", 20, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 10, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_CYAN);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        // Find current KARR candidate
        BleObservation karrObs = null;
        for (Observation obs : latestTargets.values()) {
            if (obs instanceof BleObservation) {
                BleObservation bo = (BleObservation) obs;
                if (!TextUtils.isEmpty(karrClue(bo))) {
                    karrObs = bo;
                    break;
                }
            }
        }

        ScrollView sv = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(10, 8, 10, 10);
        sv.addView(content);
        root.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        if (karrObs == null) {
            TextView none = text("No KARR/SWDS candidate in current target list.", 13, COLOR_DIM);
            none.setTypeface(Typeface.MONOSPACE);
            none.setPadding(8, 4, 8, 8);
            content.addView(none);

            TextView hint = text("Start a BLE scan and wait for a KARR/SWDS keyword "
                    + "(karr, swds, southwest, acrisure) to appear in the target list, "
                    + "then return to this panel.", 12, COLOR_DIM);
            hint.setTypeface(Typeface.MONOSPACE);
            hint.setPadding(8, 0, 8, 12);
            content.addView(hint);
        } else {
            // Research text from existing helper
            String research = RuleIdToResearchText.researchTextForRuleId(
                    "known_ble_karr_swds_keyword", karrObs, null);
            TextView body = text(research, 13, COLOR_STATUS);
            body.setTypeface(Typeface.MONOSPACE);
            body.setPadding(0, 6, 0, 6);
            content.addView(body);

            // Raw advertisement hex
            content.addView(new View(this));
            TextView rawLabel = text("RAW ADVERTISEMENT HEX", 13, COLOR_YELLOW);
            rawLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            rawLabel.setPadding(0, 8, 0, 2);
            content.addView(rawLabel);

            TextView rawHex = text(karrObs.rawHex(), 12, 0xffb8c4cc);
            rawHex.setTypeface(Typeface.MONOSPACE);
            rawHex.setPadding(4, 2, 4, 6);
            content.addView(rawHex);

            // Printable ASCII
            TextView asciiLabel = text("PRINTABLE ASCII", 13, COLOR_YELLOW);
            asciiLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            asciiLabel.setPadding(0, 4, 0, 2);
            content.addView(asciiLabel);

            TextView ascii = text(karrObs.printableAscii(), 12, 0xffb8c4cc);
            ascii.setTypeface(Typeface.MONOSPACE);
            ascii.setPadding(4, 2, 4, 6);
            content.addView(ascii);

            // RSSI history
            String key = karrObs.type() + "|" + karrObs.identity();
            TargetHistory hist = targetHistories.get(key);
            if (hist != null && hist.count() > 0) {
                TextView histLabel = text("RSSI TREND (" + hist.count() + " readings)", 13, COLOR_YELLOW);
                histLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                histLabel.setPadding(0, 8, 0, 2);
                content.addView(histLabel);

                TextView histText = text(hist.timelineString(), 12, COLOR_STATUS);
                histText.setTypeface(Typeface.MONOSPACE);
                histText.setPadding(4, 2, 4, 6);
                content.addView(histText);
            }
        }

        // Navigation
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);

        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);

        root.addView(animaeFooter());
        return root;
    }

    // ── TOOL 3: BLE DEEP DETAIL ────────────────────────────────────────
    private View buildBleDeepDetailPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
        root.setBackground(fantasySky());

        View accentBar = new View(this);
        accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
        accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));
        root.addView(accentBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));

        TextView title = text("BLE DEEP DETAIL", 22, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 10, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_CYAN);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        BleObservation selected = null;
        for (Observation obs : latestTargets.values()) {
            if (obs instanceof BleObservation) {
                selected = (BleObservation) obs;
                break;
            }
        }

        if (selected == null) {
            ScrollView sv = new ScrollView(this);
            LinearLayout c = new LinearLayout(this);
            c.setOrientation(LinearLayout.VERTICAL);
            c.setPadding(10, 8, 10, 10);
            sv.addView(c);
            root.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

            TextView empty = text("No BLE target in current view. "
                    + "Start a BLE scan and tap a BLE target to inspect.", 13, COLOR_DIM);
            empty.setTypeface(Typeface.MONOSPACE);
            empty.setPadding(8, 4, 8, 12);
            c.addView(empty);
        } else {
            ScrollView sv = new ScrollView(this);
            LinearLayout c = new LinearLayout(this);
            c.setOrientation(LinearLayout.VERTICAL);
            c.setPadding(10, 8, 10, 10);
            sv.addView(c);
            root.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

            BleDecoder dec = new BleDecoder(selected.raw);

            TextView summary = text(selected.summary(), 14, COLOR_CYAN);
            summary.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            summary.setPadding(0, 6, 0, 8);
            c.addView(summary);

            c.addView(sectionLabel("DECODED FIELDS"));
            c.addView(text(dec.summaryFields(), 12, COLOR_STATUS));

            c.addView(sectionLabel("SERVICE UUIDS (16-BIT)"));
            c.addView(text(dec.serviceUUIDs16String(), 12, COLOR_CYAN));

            c.addView(sectionLabel("SERVICE UUIDS (32-BIT)"));
            c.addView(text(dec.serviceUUIDs32String(), 12, COLOR_CYAN));

            c.addView(sectionLabel("MANUFACTURER DATA"));
            c.addView(text(dec.manufacturerIdString(), 12, COLOR_YELLOW));
            c.addView(text("Vendor data hex: " + dec.vendorSpecificDataHex(), 12, COLOR_STATUS));
            c.addView(text("Vendor data ASCII: " + dec.vendorSpecificDataAscii(), 12, COLOR_STATUS));

            c.addView(sectionLabel("SERVICE DATA"));
            c.addView(text(dec.serviceDataUuidsString(), 12, COLOR_STATUS));

            c.addView(sectionLabel("TX POWER"));
            c.addView(text(dec.txPowerValue() >= 0
                    ? dec.txPowerValue() + " dBm" : "not present", 12, COLOR_STATUS));

            c.addView(sectionLabel("RAW HEX (" + selected.raw.length + " bytes)"));
            TextView rawHex = text(selected.rawHex(), 11, 0xffb8c4cc);
            rawHex.setTypeface(Typeface.MONOSPACE);
            rawHex.setPadding(4, 2, 4, 4);
            c.addView(rawHex);

            c.addView(sectionLabel("PRINTABLE ASCII"));
            TextView ascii = text(selected.printableAscii(), 11, 0xffb8c4cc);
            ascii.setTypeface(Typeface.MONOSPACE);
            ascii.setPadding(4, 2, 4, 4);
            c.addView(ascii);
        }

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);
        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);

        root.addView(animaeFooter());
        return root;
    }

    // ── TOOL 4: WIFI DEEP DETAIL ───────────────────────────────────────
    private View buildWifiDeepDetailPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 14, 20, 14);
        root.setBackground(fantasySky());

        TextView title = text("WIFI DEEP DETAIL", 22, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 6, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_CYAN);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        WifiObservation selected = null;
        for (Observation obs : latestTargets.values()) {
            if (obs instanceof WifiObservation) {
                selected = (WifiObservation) obs;
                break;
            }
        }

        if (selected == null) {
            ScrollView sv = new ScrollView(this);
            LinearLayout c = new LinearLayout(this);
            c.setOrientation(LinearLayout.VERTICAL);
            c.setPadding(10, 8, 10, 10);
            sv.addView(c);
            root.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

            TextView empty = text("No Wi-Fi target in current view. "
                    + "Scan Wi-Fi and tap a Wi-Fi target to inspect.", 13, COLOR_DIM);
            empty.setTypeface(Typeface.MONOSPACE);
            empty.setPadding(8, 4, 8, 12);
            c.addView(empty);
        } else {
            ScrollView sv = new ScrollView(this);
            LinearLayout c = new LinearLayout(this);
            c.setOrientation(LinearLayout.VERTICAL);
            c.setPadding(10, 8, 10, 10);
            sv.addView(c);
            root.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

            // Decode using WifiManager's scan result directly if available
            // We'll use the WifiDecoder with a synthetic approach
            String ssid = selected.ssid;
            String bssid = selected.bssid;
            String caps = selected.capabilities;
            int rssi = selected.rssi;

            TextView summary = text(selected.summary(), 14, COLOR_CYAN);
            summary.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            summary.setPadding(0, 6, 0, 8);
            c.addView(summary);

            c.addView(sectionLabel("IDENTIFICATION"));
            c.addView(text("SSID: " + (TextUtils.isEmpty(ssid) ? "(hidden)" : ssid), 13, COLOR_STATUS));
            c.addView(text("BSSID: " + bssid, 13, COLOR_STATUS));
            c.addView(text("OUI/Vendor: " + OuiLookup.lookup(bssid), 13, COLOR_YELLOW));

            c.addView(sectionLabel("CHANNEL / FREQUENCY"));
            // Approximate from RSSI context — we don't have freq here, show what we know
            c.addView(text("RSSI: " + rssi + " dBm", 13, COLOR_STATUS));

            c.addView(sectionLabel("SECURITY DECODE"));
            String upper = caps.toUpperCase();
            c.addView(text("Raw capabilities: " + caps, 13, COLOR_STATUS));
            c.addView(text("Hidden SSID: " + (TextUtils.isEmpty(ssid) ? "YES" : "no"), 13,
                    TextUtils.isEmpty(ssid) ? COLOR_YELLOW : COLOR_CYAN));
            c.addView(text("Open network: " + (!upper.contains("WEP") && !upper.contains("WPA")
                    && !upper.contains("RSN") && !upper.contains("SAE") ? "YES" : "no"), 13,
                    (!upper.contains("WEP") && !upper.contains("WPA")
                            && !upper.contains("RSN") && !upper.contains("SAE")) ? COLOR_YELLOW : COLOR_CYAN));
            c.addView(text("WEP: " + (upper.contains("WEP") ? "YES — vulnerable" : "no"), 13,
                    upper.contains("WEP") ? COLOR_YELLOW : COLOR_CYAN));
            c.addView(text("WPA: " + (upper.contains("WPA") && !upper.contains("WPA2")
                    && !upper.contains("WPA3") ? "YES" : "no"), 13,
                    (upper.contains("WPA") && !upper.contains("WPA2")
                            && !upper.contains("WPA3")) ? COLOR_YELLOW : COLOR_CYAN));
            c.addView(text("WPA2/RSN: " + (upper.contains("WPA2") || upper.contains("RSN") ? "YES" : "no"), 13,
                    (upper.contains("WPA2") || upper.contains("RSN")) ? COLOR_CYAN : COLOR_CYAN));
            c.addView(text("WPA3/SAE: " + (upper.contains("WPA3") || upper.contains("SAE") ? "YES" : "no"), 13,
                    (upper.contains("WPA3") || upper.contains("SAE")) ? COLOR_CYAN : COLOR_CYAN));

            // RSSI history
            String key = selected.type() + "|" + selected.identity();
            TargetHistory hist = targetHistories.get(key);
            if (hist != null && hist.count() > 0) {
                c.addView(sectionLabel("RSSI TREND (" + hist.count() + " readings)"));
                c.addView(text(hist.timelineString(), 12, COLOR_STATUS));
                c.addView(text("Min: " + hist.minRssi() + "  Max: " + hist.maxRssi() + " dBm", 12, COLOR_YELLOW));
            }

            c.addView(sectionLabel("CAPABILITIES FLAGS"));
            c.addView(text(WifiDecoder.capabilitiesFlags(caps), 12, COLOR_STATUS));

            c.addView(sectionLabel("PASSIVE SCAN NOTE"));
            c.addView(text("Mode: passive Wi-Fi scan only. No association attempted.", 12, COLOR_DIM));
        }

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);
        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);
        return root;
    }

    // ── TOOL 5: OUI/VENDOR LOOKUP ──────────────────────────────────────
    private View buildOuiVendorPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
        root.setBackground(fantasySky());

        View accentBar = new View(this);
        accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
        accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));
        root.addView(accentBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));

        TextView title = text("OUI/VENDOR LOOKUP", 22, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 10, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_CYAN);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        // Search box
        EditText searchBox = new EditText(this);
        searchBox.setHint("Enter BSSID/MAC prefix (AA:BB:CC)");
        searchBox.setTextColor(COLOR_MACH_WHITE);
        searchBox.setHintTextColor(COLOR_DIM);
        searchBox.setTypeface(Typeface.MONOSPACE);
        searchBox.setTextSize(14);
        searchBox.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        searchBox.setLines(1);
        searchBox.setPadding(10, 6, 10, 6);
        searchBox.setBackground(panelDrawable(COLOR_PANEL, COLOR_CYAN, 2));
        root.addView(searchBox, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        Button lookupBtn = button("LOOKUP");
        styleButton(lookupBtn, COLOR_CYAN, COLOR_MACH_WHITE, COLOR_MACH_WHITE);
        lookupBtn.setOnClickListener(v -> {
            String query = searchBox.getText().toString().trim().toUpperCase();
            String result = OuiLookup.lookup(query);
            if (toolPanelOverlay != null) frame.removeView(toolPanelOverlay);
            LinearLayout resPanel = new LinearLayout(this);
            resPanel.setOrientation(LinearLayout.VERTICAL);
            resPanel.setPadding(20, 14, 20, 14);
            resPanel.setBackgroundColor(COLOR_INDIGO);

            TextView resTitle = text("OUI/VENDOR LOOKUP RESULT", 18, COLOR_MACH_WHITE);
            resTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            resTitle.setPadding(0, 6, 0, 4);
            resPanel.addView(resTitle);

            View resStripe = new View(this);
            resStripe.setBackgroundColor(COLOR_CYAN);
            resPanel.addView(resStripe, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 4));

            TextView queryLabel = text("Query: " + query, 13, COLOR_CYAN);
            queryLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            queryLabel.setPadding(0, 10, 0, 4);
            resPanel.addView(queryLabel);

            TextView resultText = text("Vendor: " + result, 14,
                    result.equals("Unknown") ? COLOR_YELLOW : COLOR_MACH_WHITE);
            resultText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            resultText.setPadding(0, 6, 0, 12);
            resPanel.addView(resultText);

            // Show all vendors in DB
            TextView dbHeader = text("BUILT-IN VENDOR DATABASE (" + OuiLookup.vendorCount() + " entries)",
                    13, COLOR_CYAN);
            dbHeader.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            dbHeader.setPadding(0, 12, 0, 4);
            resPanel.addView(dbHeader);

            ScrollView sv = new ScrollView(this);
            LinearLayout dbList = new LinearLayout(this);
            dbList.setOrientation(LinearLayout.VERTICAL);
            sv.addView(dbList);
            resPanel.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

            // Iterate OuiLookup's entries — we need to expose them
            // We'll show a curated subset
            String[][] sampleVendors = {
                {"00:1A:7D", "Apple, Inc."},
                {"3C:22:F2", "Apple, Inc."},
                {"A4:34:D9", "Apple, Inc."},
                {"FC:F8:AE", "Apple, Inc."},
                {"00:26:5A", "Tesla, Inc."},
                {"00:50:49", "NXP Semiconductors"},
                {"00:14:D1", "NXP Semiconductors"},
                {"00:60:37", "Microchip Technology"},
                {"00:1E:7E", "STMicroelectronics"},
                {"00:E0:4C", "Broadcom Corporation"},
                {"00:23:A2", "Broadcom Corporation"},
                {"00:1D:A1", "Broadcom Corporation"},
                {"00:18:39", "Marvell Technology Group"},
                {"00:24:0B", "Texas Instruments"},
                {"00:1E:68", "Renesas Electronics"},
                {"00:17:F2", "Sony Corporation"},
                {"00:18:28", "Samsung Electronics"},
                {"00:19:5B", "Realtek Semiconductor"},
                {"00:25:5D", "MediaTek Inc."},
                {"00:24:D6", "Atheros Communications"},
                {"00:19:B3", "Atheros Communications"},
                {"00:26:F2", "Atheros Communications"},
                {"00:40:9C", "Atheros Communications"},
                {"00:21:5A", "Ralink Technology (MediaTek)"},
                {"00:12:1C", "Cypress Semiconductor"},
                {"00:1B:21", "Infineon Technologies"},
                {"00:21:26", "Intel Corporation"},
                {"00:1F:44", "Qualcomm Atheros"},
                {"00:24:2D", "Foxconn"}
            };

            for (String[] entry : sampleVendors) {
                TextView entryLine = text(entry[0] + "  →  " + entry[1], 12,
                        COLOR_STATUS);
                entryLine.setTypeface(Typeface.MONOSPACE);
                entryLine.setPadding(6, 1, 6, 1);
                dbList.addView(entryLine);
            }

            LinearLayout nav = new LinearLayout(this);
            nav.setOrientation(LinearLayout.HORIZONTAL);
            nav.setPadding(0, 10, 0, 0);
            Button back = button("← Back to Tools");
            styleButton(back, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
            back.setOnClickListener(v1 -> showToolPalette());
            nav.addView(back, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            Button close = button("Close");
            styleButton(close, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
            close.setOnClickListener(v1 -> {
                if (toolPanelOverlay != null) {
                    frame.removeView(toolPanelOverlay);
                    toolPanelOverlay = null;
                }
            });
            nav.addView(close, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            resPanel.addView(nav);

            resPanel.addView(animaeFooter());

            toolPanelOverlay = resPanel;
            frame.addView(toolPanelOverlay, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
        });
        root.addView(lookupBtn, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                COMPACT_BUTTON_HEIGHT));

        TextView info = text("Enter the first 3 octets of a BSSID (AA:BB:CC) "
                + "to look up the registered IEEE vendor. "
                + "Database contains " + OuiLookup.vendorCount() + " curated entries.", 12, COLOR_DIM);
        info.setTypeface(Typeface.MONOSPACE);
        info.setPadding(0, 10, 0, 6);
        root.addView(info);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);
        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);
        return root;
    }

    // ── TOOL 6: SESSION TIMELINE ───────────────────────────────────────
    private View buildSessionTimelinePanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
        root.setBackground(fantasySky());

        View accentBar = new View(this);
        accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
        accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));
        root.addView(accentBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));

        TextView title = text("SESSION TIMELINE", 22, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 10, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_CYAN);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        TextView sessMeta = text(sessionActive
                ? "Session: " + sessionId + "  |  Started: " + new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date(sessionStartedAt))
                : "No active session", 13, COLOR_STATUS);
        sessMeta.setTypeface(Typeface.MONOSPACE);
        sessMeta.setPadding(0, 8, 0, 8);
        root.addView(sessMeta);

        ScrollView sv = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(10, 8, 10, 10);
        sv.addView(content);
        root.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        if (targetHistories.isEmpty()) {
            TextView empty = text("No target history yet. "
                    + "Start scanning to build a timeline.", 13, COLOR_DIM);
            empty.setTypeface(Typeface.MONOSPACE);
            empty.setPadding(8, 4, 8, 12);
            content.addView(empty);
        } else {
            int idx = 0;
            for (Map.Entry<String, TargetHistory> entry : targetHistories.entrySet()) {
                String key = entry.getKey();
                TargetHistory hist = entry.getValue();
                String[] parts = key.split("\\|");
                String type = parts.length > 0 ? parts[0] : "?";
                String id = parts.length > 1 ? parts[1] : "?";

                TextView obsHeader = text("◆ " + (type.equals("ble") ? "BLE" : "Wi-Fi")
                        + " target: " + id, 14, COLOR_YELLOW);
                obsHeader.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                obsHeader.setPadding(0, 10, 0, 2);
                content.addView(obsHeader);

                TextView timeline = text("Saw target " + id + " at "
                        + hist.timelineString(), 13, COLOR_STATUS);
                timeline.setTypeface(Typeface.MONOSPACE);
                timeline.setPadding(6, 2, 6, 4);
                content.addView(timeline);

                if (hist.count() > 0) {
                    TextView range = text("Range: " + hist.minRssi() + " to "
                            + hist.maxRssi() + " dBm  |  Readings: " + hist.count(), 12, COLOR_CYAN);
                    range.setTypeface(Typeface.MONOSPACE);
                    range.setPadding(6, 0, 6, 6);
                    content.addView(range);
                }

                if (idx < targetHistories.size() - 1) {
                    content.addView(new View(this));
                }
                idx++;
            }
        }

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);
        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);

        root.addView(animaeFooter());
        return root;
    }

    // ── TOOL 7: TARGET TAGS ────────────────────────────────────────────
    private View buildTargetTagsPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
        root.setBackground(fantasySky());

        View accentBar = new View(this);
        accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
        accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));
        root.addView(accentBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));

        TextView title = text("INTERESTING TARGET TAGS", 20, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 10, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_YELLOW);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        TextView info = text("Tag the currently observed target with suspect/follow-up flags. "
                + "Tags are session-scoped and appear in evidence packets.", 13, COLOR_DIM);
        info.setTypeface(Typeface.MONOSPACE);
        info.setPadding(0, 8, 0, 10);
        root.addView(info);

        // Pick a target — default to first BLE or Wi-Fi
        Observation target = null;
        for (Observation obs : latestTargets.values()) {
            target = obs;
            break;
        }

        if (target == null) {
            TextView none = text("No target available to tag. "
                    + "Start a scan first.", 13, COLOR_DIM);
            none.setTypeface(Typeface.MONOSPACE);
            none.setPadding(8, 4, 8, 12);
            root.addView(none);
        } else {
            String key = target.type() + "|" + target.identity();
            TargetTags existing = targetTags.get(key);
            final TargetTags tags = (existing != null) ? existing : new TargetTags();
            if (existing == null) {
                targetTags.put(key, tags);
            }

            // Target summary
            TextView targetLabel = text("Target: " + target.summary(), 14, COLOR_CYAN);
            targetLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            targetLabel.setPadding(0, 6, 0, 10);
            root.addView(targetLabel);

            // Tag buttons grid
            String[] tagNames = TargetTags.ALL_TAGS;
            int[] tagColors = {
                COLOR_YELLOW, COLOR_YELLOW, COLOR_CYAN, COLOR_CYAN,
                COLOR_CYAN, COLOR_CRIMSON, COLOR_CYAN
            };

            LinearLayout tagGrid = new LinearLayout(this);
            tagGrid.setOrientation(LinearLayout.VERTICAL);
            LinearLayout row1 = new LinearLayout(this);
            row1.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout row2 = new LinearLayout(this);
            row2.setOrientation(LinearLayout.HORIZONTAL);

        final String[] finalTagNames = tagNames;
        final int[] finalTagColors = tagColors;
        for (int i = 0; i < finalTagNames.length; i++) {
            final String tagName = finalTagNames[i];
            final int colorIdx = i;
            LinearLayout row = (i < 4) ? row1 : row2;
            boolean active = tags.has(tagName);
            Button tagBtn = button(tagName);
            tagBtn.setTextSize(13);
            tagBtn.setPadding(8, 5, 8, 5);
            tagBtn.setMinHeight(0);
            tagBtn.setMinimumHeight(0);
            styleButton(tagBtn,
                    active ? finalTagColors[colorIdx] : COLOR_PANEL,
                    active ? 0xff101010 : COLOR_MACH_WHITE,
                    active ? COLOR_MACH_WHITE : 0xff50556f);
            tagBtn.setText(active ? ("✓ " + tagName) : tagName);
            tagBtn.setOnClickListener(v -> {
                final TargetTags fTags = tags;
                final String tName = tagName;
                fTags.toggle(tName);
                    frame.removeView(toolPanelOverlay);
                    toolPanelOverlay = buildTargetTagsPanel();
                    frame.addView(toolPanelOverlay, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));
                });
                row.addView(tagBtn, new LinearLayout.LayoutParams(
                        0, COMPACT_BUTTON_HEIGHT, 1));
            }

            // Add margin between rows
            LinearLayout.LayoutParams row1Params = (LinearLayout.LayoutParams) row1.getLayoutParams();
            row1Params.setMargins(0, 0, 0, 4);
            row1.setLayoutParams(row1Params);

            root.addView(row1);
            root.addView(row2);

            // Current tags display
            if (!tags.isEmpty()) {
                TextView currentTags = text("Active tags: " + tags.tagsString(), 13, COLOR_YELLOW);
                currentTags.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                currentTags.setPadding(0, 12, 0, 4);
                root.addView(currentTags);
            }
        }

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);
        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);

        root.addView(animaeFooter());
        return root;
    }

    // ── TOOL 8: EVIDENCE PACKET EXPORT ────────────────────────────────
    private View buildEvidencePacketPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
        root.setBackground(fantasySky());

        View accentBar = new View(this);
        accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
        accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));
        root.addView(accentBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));

        TextView title = text("EVIDENCE PACKET EXPORT", 20, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 10, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_YELLOW);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        TextView info = text("One-tap export of selected target's raw data, decoded fields, "
                + "session metadata, tags, and notes. Packet is appended to evidence JSONL.", 13, COLOR_DIM);
        info.setTypeface(Typeface.MONOSPACE);
        info.setPadding(0, 8, 0, 10);
        root.addView(info);

        // Pick target
        Observation target = null;
        for (Observation obs : latestTargets.values()) {
            target = obs;
            break;
        }

        if (target == null) {
            TextView none = text("No target to export. Start a scan first.", 13, COLOR_DIM);
            none.setTypeface(Typeface.MONOSPACE);
            none.setPadding(8, 4, 8, 12);
            root.addView(none);
        } else {
            String key = target.type() + "|" + target.identity();
            TargetTags tags = targetTags.get(key);
            TargetTags finalTags = tags != null ? tags : new TargetTags();
            if (tags == null) {
                targetTags.put(key, finalTags);
            }
            TargetNotes notes = targetNotes.get(key);
            TargetNotes finalNotes = notes != null ? notes : new TargetNotes();
            if (notes == null) {
                targetNotes.put(key, finalNotes);
            }

            // Build evidence packet text
            final StringBuilder packet = new StringBuilder();
            packet.append("═════════════════════════════════════════════════════════\n");
            packet.append("  EVIDENCE PACKET\n");
            packet.append("  Session: ").append(sessionId).append("\n");
            packet.append("  Captured: ").append(Util.now()).append("\n");
            packet.append("═════════════════════════════════════════════════════════\n\n");
            packet.append("TARGET SUMMARY\n");
            packet.append("---------------\n");
            packet.append(target.summary()).append("\n\n");
            packet.append("RAW DATA\n");
            packet.append("--------\n");
            if (target instanceof BleObservation) {
                BleObservation bo = (BleObservation) target;
                packet.append("Type: BLE advertisement\n");
                packet.append("Address: ").append(bo.address).append("\n");
                packet.append("Name: ").append(bo.name).append("\n");
                packet.append("RSSI: ").append(bo.rssi).append(" dBm\n");
                packet.append("Advertisement hex: ").append(bo.rawHex()).append("\n\n");
                packet.append("Printable ASCII: ").append(bo.printableAscii()).append("\n\n");
            } else if (target instanceof WifiObservation) {
                WifiObservation wo = (WifiObservation) target;
                packet.append("Type: Wi-Fi beacon\n");
                packet.append("SSID: ").append(displaySsid(wo.ssid)).append("\n");
                packet.append("BSSID: ").append(wo.bssid).append("\n");
                packet.append("RSSI: ").append(wo.rssi).append(" dBm\n");
                packet.append("Capabilities: ").append(wo.capabilities).append("\n\n");
                packet.append("OUI/Vendor: ").append(OuiLookup.lookup(wo.bssid)).append("\n\n");
            }
            packet.append("DECODED FIELDS\n");
            packet.append("--------------\n");
            packet.append(target.detail()).append("\n\n");

            if (!tags.isEmpty()) {
                packet.append("TAGS\n");
                packet.append("----\n");
                for (String t : tags.all()) packet.append("  ").append(t).append("\n");
                packet.append("\n");
            }
            if (!notes.get().isEmpty()) {
                packet.append("RESEARCH NOTES\n");
                packet.append("--------------\n");
                packet.append(notes.get()).append("\n\n");
            }

            packet.append("SESSION / CAPTURE METADATA\n");
            packet.append("--------------------------\n");
            packet.append("Session ID: ").append(sessionId).append("\n");
            packet.append("Capture time: ").append(Util.now()).append("\n");
            packet.append("App: Field Security Inspector (com.codex.karrdefense)\n");
            packet.append("Scope: authorized defensive inspection only\n");
            packet.append("BLE GATT writes: none\n");
            packet.append("Vehicle-control commands: none\n");

            packet.append("\n\n─────────────────────────────────────────────────────────────\n");
            packet.append("  END OF EVIDENCE PACKET\n");
            packet.append("─────────────────────────────────────────────────────────────\n");

            // Display packet preview
            TextView packetLabel = text("EVIDENCE PACKET PREVIEW", 13, COLOR_YELLOW);
            packetLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            packetLabel.setPadding(0, 8, 0, 4);
            root.addView(packetLabel);

            TextView packetView = text(packet.toString(), 12, COLOR_STATUS);
            packetView.setTypeface(Typeface.MONOSPACE);
            packetView.setPadding(6, 4, 6, 8);
            root.addView(packetView);

            // Export button
            Button exportBtn = button("EXPORT EVIDENCE PACKET");
            styleButton(exportBtn, COLOR_YELLOW, 0xff101010, COLOR_MACH_WHITE);
            final Observation fTarget = target;
            final StringBuilder fPacket = packet;
            final String fSessionId = sessionId;
            exportBtn.setOnClickListener(v -> {
                appendEvidence("{\"type\":\"evidence_packet\",\"session_id\":" + json(fSessionId)
                        + ",\"time\":\"" + json(Util.now())
                        + ",\"target_type\":" + json(fTarget.type())
                        + ",\"target_id\":" + json(fTarget.identity())
                        + ",\"packet\":" + json(fPacket.toString()) + "}");
                addEvent("Evidence packet exported for " + fTarget.identity());
                setStatus("Evidence packet exported for " + fTarget.identity());

                // Show confirmation
                if (toolPanelOverlay != null) frame.removeView(toolPanelOverlay);
                LinearLayout confirm = new LinearLayout(this);
                confirm.setOrientation(LinearLayout.VERTICAL);
                confirm.setPadding(20, 14, 20, 14);
                confirm.setBackgroundColor(COLOR_INDIGO);

                TextView confTitle = text("EVIDENCE PACKET EXPORTED", 18, COLOR_MACH_WHITE);
                confTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                confTitle.setPadding(0, 6, 0, 4);
                confirm.addView(confTitle);

                View confStripe = new View(this);
                confStripe.setBackgroundColor(COLOR_YELLOW);
                confirm.addView(confStripe, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 4));

                TextView confMsg = text("Target " + fTarget.identity() + " packet appended "
                        + "to evidence JSONL.", 14, COLOR_STATUS);
                confMsg.setTypeface(Typeface.MONOSPACE);
                confMsg.setPadding(0, 12, 0, 12);
                confirm.addView(confMsg);

                Button backTools = button("← Back to Tools");
                styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
                backTools.setOnClickListener(v1 -> showToolPalette());
                confirm.addView(backTools, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        COMPACT_BUTTON_HEIGHT));

                Button backScan = button("Back to Scan");
                styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
                backScan.setOnClickListener(v1 -> {
                    if (toolPanelOverlay != null) {
                        frame.removeView(toolPanelOverlay);
                        toolPanelOverlay = null;
                    }
                });
                confirm.addView(backScan, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        COMPACT_BUTTON_HEIGHT));

                toolPanelOverlay = confirm;
                frame.addView(toolPanelOverlay, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
            });
            root.addView(exportBtn, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    COMPACT_BUTTON_HEIGHT));
        }

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);
        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);

        root.addView(animaeFooter());
        return root;
    }

    // ── TOOL 9: RESEARCH NOTES ─────────────────────────────────────────
    private View buildResearchNotesPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
        root.setBackground(fantasySky());

        View accentBar = new View(this);
        accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
        accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));
        root.addView(accentBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));

        TextView title = text("RESEARCH NOTES", 22, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 10, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_YELLOW);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        // Pick target
        Observation target = null;
        for (Observation obs : latestTargets.values()) {
            target = obs;
            break;
        }

        if (target == null) {
            TextView none = text("No target to annotate. Start a scan first.", 13, COLOR_DIM);
            none.setTypeface(Typeface.MONOSPACE);
            none.setPadding(8, 4, 8, 12);
            root.addView(none);
        } else {
            final Observation ft = target;
            String key = ft.type() + "|" + ft.identity();
            TargetNotes notes = targetNotes.get(key);
            if (notes == null) {
                notes = new TargetNotes();
                targetNotes.put(key, notes);
            }
            final TargetNotes fn = notes;
            TextView targetLabel = text("Target: " + ft.summary(), 14, COLOR_CYAN);
            targetLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            targetLabel.setPadding(0, 8, 0, 10);
            root.addView(targetLabel);

            // Existing notes display
            if (!fn.get().isEmpty()) {
                TextView existingLabel = text("EXISTING NOTES", 13, COLOR_CYAN);
                existingLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                existingLabel.setPadding(0, 6, 0, 2);
                root.addView(existingLabel);

                TextView existing = text(fn.get(), 13, COLOR_STATUS);
                existing.setTypeface(Typeface.MONOSPACE);
                existing.setPadding(6, 2, 6, 6);
                root.addView(existing);
            }

            // New notes input
            TextView newNoteLabel = text("NEW NOTE", 13, COLOR_CYAN);
            newNoteLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            newNoteLabel.setPadding(0, 10, 0, 4);
            root.addView(newNoteLabel);

            EditText noteInput = new EditText(this);
            noteInput.setHint("Type research note here...");
            noteInput.setTextColor(COLOR_MACH_WHITE);
            noteInput.setHintTextColor(COLOR_DIM);
            noteInput.setTypeface(Typeface.MONOSPACE);
            noteInput.setTextSize(14);
            noteInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_CLASS_TEXT);
            noteInput.setLines(4);
            noteInput.setMinHeight(0);
            noteInput.setMinimumHeight(0);
            noteInput.setPadding(10, 6, 10, 6);
            noteInput.setBackground(panelDrawable(COLOR_PANEL, COLOR_CYAN, 2));
            root.addView(noteInput, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            // Append vs Replace
            LinearLayout noteActions = new LinearLayout(this);
            noteActions.setOrientation(LinearLayout.HORIZONTAL);
            noteActions.setPadding(0, 6, 0, 0);

            Button appendBtn = button("Append");
            styleButton(appendBtn, COLOR_CYAN, COLOR_MACH_WHITE, COLOR_MACH_WHITE);
            final EditText fNoteInput = noteInput;
            appendBtn.setOnClickListener(v -> {
                String newText = fNoteInput.getText().toString().trim();
                if (!newText.isEmpty()) {
                    String existing = fn.get();
                    fn.set(existing.isEmpty() ? newText
                            : existing + "\n" + newText);
                    addEvent("Note appended for " + ft.identity());
                    clearCurrentView();
                    setStatus("Note appended for " + ft.identity());
                }
            });

            noteActions.addView(appendBtn, new LinearLayout.LayoutParams(
                    0, COMPACT_BUTTON_HEIGHT, 1));

            Button replaceBtn = button("Replace");
            styleButton(replaceBtn, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
            replaceBtn.setOnClickListener(v -> {
                String newText = fNoteInput.getText().toString().trim();
                if (!newText.isEmpty()) {
                    fn.set(newText);
                    addEvent("Note replaced for " + ft.identity());
                    clearCurrentView();
                    setStatus("Note replaced for " + ft.identity());
                }
            });
            noteActions.addView(replaceBtn, new LinearLayout.LayoutParams(
                    0, COMPACT_BUTTON_HEIGHT, 1));

            Button clearBtn = button("Clear");
            styleButton(clearBtn, COLOR_CRIMSON, COLOR_MACH_WHITE, COLOR_MACH_WHITE);
            clearBtn.setOnClickListener(v -> {
                fn.set("");
                addEvent("Notes cleared for " + ft.identity());
                setStatus("Notes cleared for " + ft.identity());
            });
            noteActions.addView(clearBtn, new LinearLayout.LayoutParams(
                    0, COMPACT_BUTTON_HEIGHT, 1));

            root.addView(noteActions);
        }

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);
        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);

        root.addView(animaeFooter());
        return root;
    }

    // ── TOOL 10: DISCLOSURE REPORT MODE ────────────────────────────────
    private View buildDisclosureReportPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
        root.setBackground(fantasySky());

        View accentBar = new View(this);
        accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
        accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));
        root.addView(accentBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3));

        TextView title = text("DISCLOSURE REPORT MODE", 20, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 10, 0, 4);
        root.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_YELLOW);
        root.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));

        TextView info = text("Mercedes-Benz VDP template. Fill in observed behavior, "
                + "reproduction steps, impact hypothesis, and attach evidence. "
                + "This is a DRAFT — not for public disclosure until vendor acknowledged.", 13, COLOR_DIM);
        info.setTypeface(Typeface.MONOSPACE);
        info.setPadding(0, 8, 0, 10);
        root.addView(info);

        // Pre-fill from current target
        Observation target = null;
        for (Observation obs : latestTargets.values()) {
            target = obs;
            break;
        }

        DisclosureReport.ReportFields fields = new DisclosureReport.ReportFields();
        fields.vendor = "Mercedes-Benz";
        fields.productIdentifier = target != null ? target.identity() : "";
        fields.affectedSystem = target != null
                ? "Pending — observed " + target.summary()
                : "Pending — no target selected";
        fields.observedBehavior = target != null
                ? "Pending — recorded " + target.summary()
                : "Pending — no observation recorded";
        fields.reproductionSteps = "Pending — capture methodology:\n"
                + "  1. Passive BLE/Wi-Fi scan with Field Security Inspector (K12 tablet)\n"
                + "  2. Recorded advertisement + scan metadata; no GATT writes\n"
                + "  3. Evidence packet archived from this session\n";
        fields.impactHypothesis = "Pending — assess against known vehicle cybersecurity concerns.\n"
                + "  Note: KARR/SWDS is a researched case. Do not assume impact\n"
                + "  without opposite-party verification or documented signature.\n";
        fields.evidence = target != null ? target.summary() + "\n\n" + target.detail() : "No target selected.";
        fields.dateDiscovered = Util.now();
        fields.status = "Draft";
        fields.reporterContact = "Field Security Inspector operator (pending owner authorization)";

        // Template preview
        TextView templateLabel = text("MERCEDES-BENZ VDP TEMPLATE (DRAFT)", 13, COLOR_YELLOW);
        templateLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        templateLabel.setPadding(0, 8, 0, 4);
        root.addView(templateLabel);

        String template = DisclosureReport.mercedesVdpTemplate(fields);
        ScrollView sv = new ScrollView(this);
        TextView templateView = text(template, 12, COLOR_STATUS);
        templateView.setTypeface(Typeface.MONOSPACE);
        templateView.setPadding(6, 4, 6, 4);
        sv.addView(templateView);
        root.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        // Edit fields
        TextView editLabel = text("EDIT FIELDS FOR VDP", 13, COLOR_CYAN);
        editLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        editLabel.setPadding(0, 12, 0, 6);
        root.addView(editLabel);

        // Affected system
        TextView affLabel = text("Affected system:", 12, COLOR_DIM);
        affLabel.setTypeface(Typeface.MONOSPACE);
        affLabel.setPadding(0, 4, 0, 2);
        root.addView(affLabel);

        EditText affInput = new EditText(this);
        affInput.setText(fields.affectedSystem);
        affInput.setTextColor(COLOR_MACH_WHITE);
        affInput.setHintTextColor(COLOR_DIM);
        affInput.setTypeface(Typeface.MONOSPACE);
        affInput.setTextSize(13);
        affInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_CLASS_TEXT);
        affInput.setLines(2);
        affInput.setMinHeight(0);
        affInput.setMinimumHeight(0);
        affInput.setPadding(8, 4, 8, 4);
        affInput.setBackground(panelDrawable(COLOR_PANEL, COLOR_CYAN, 1));
        root.addView(affInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Observed behavior
        TextView behLabel = text("Observed behavior:", 12, COLOR_DIM);
        behLabel.setTypeface(Typeface.MONOSPACE);
        behLabel.setPadding(0, 8, 0, 2);
        root.addView(behLabel);

        EditText behInput = new EditText(this);
        behInput.setText(fields.observedBehavior);
        behInput.setTextColor(COLOR_MACH_WHITE);
        behInput.setHintTextColor(COLOR_DIM);
        behInput.setTypeface(Typeface.MONOSPACE);
        behInput.setTextSize(13);
        behInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_CLASS_TEXT);
        behInput.setLines(3);
        behInput.setMinHeight(0);
        behInput.setMinimumHeight(0);
        behInput.setPadding(8, 4, 8, 4);
        behInput.setBackground(panelDrawable(COLOR_PANEL, COLOR_CYAN, 1));
        root.addView(behInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Reproduction steps
        TextView repLabel = text("Reproduction steps:", 12, COLOR_DIM);
        repLabel.setTypeface(Typeface.MONOSPACE);
        repLabel.setPadding(0, 8, 0, 2);
        root.addView(repLabel);

        EditText repInput = new EditText(this);
        repInput.setText(fields.reproductionSteps);
        repInput.setTextColor(COLOR_MACH_WHITE);
        repInput.setHintTextColor(COLOR_DIM);
        repInput.setTypeface(Typeface.MONOSPACE);
        repInput.setTextSize(13);
        repInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_CLASS_TEXT);
        repInput.setLines(4);
        repInput.setMinHeight(0);
        repInput.setMinimumHeight(0);
        repInput.setPadding(8, 4, 8, 4);
        repInput.setBackground(panelDrawable(COLOR_PANEL, COLOR_CYAN, 1));
        root.addView(repInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Impact hypothesis
        TextView impLabel = text("Impact hypothesis:", 12, COLOR_DIM);
        impLabel.setTypeface(Typeface.MONOSPACE);
        impLabel.setPadding(0, 8, 0, 2);
        root.addView(impLabel);

        EditText impInput = new EditText(this);
        impInput.setText(fields.impactHypothesis);
        impInput.setTextColor(COLOR_MACH_WHITE);
        impInput.setHintTextColor(COLOR_DIM);
        impInput.setTypeface(Typeface.MONOSPACE);
        impInput.setTextSize(13);
        impInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_CLASS_TEXT);
        impInput.setLines(4);
        impInput.setMinHeight(0);
        impInput.setMinimumHeight(0);
        impInput.setPadding(8, 4, 8, 4);
        impInput.setBackground(panelDrawable(COLOR_PANEL, COLOR_CYAN, 1));
        root.addView(impInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Generate Report button
        Button generateBtn = button("GENERATE REPORT FROM FIELDS");
        styleButton(generateBtn, COLOR_YELLOW, 0xff101010, COLOR_MACH_WHITE);
        generateBtn.setOnClickListener(v -> {
            fields.affectedSystem = affInput.getText().toString().trim();
            fields.observedBehavior = behInput.getText().toString().trim();
            fields.reproductionSteps = repInput.getText().toString().trim();
            fields.impactHypothesis = impInput.getText().toString().trim();

            String finalReport = DisclosureReport.mercedesVdpTemplate(fields);
            appendEvidence("{\"type\":\"vdp_draft\",\"session_id\":" + json(sessionId)
                    + ",\"time\":" + json(Util.now())
                    + ",\"vendor\":\"Mercedes-Benz\""
                    + ",\"product_identifier\":" + json(fields.productIdentifier)
                    + ",\"vdp_text\":" + json(finalReport) + "}");
            addEvent("VDP draft generated for " + fields.productIdentifier);

            if (toolPanelOverlay != null) frame.removeView(toolPanelOverlay);
            LinearLayout reportPanel = new LinearLayout(this);
            reportPanel.setOrientation(LinearLayout.VERTICAL);
            reportPanel.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP, TOOL_PANEL_PADDING_SIDE, 14);
            reportPanel.setBackgroundColor(COLOR_WARM_BG);

            View accentBar2 = new View(this);
            accentBar.setBackgroundColor(COLOR_ACCENT_GLOW);
            accentBar.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 3));
            reportPanel.addView(accentBar, new LinearLayout.LayoutParams(
                    0, 3));

            TextView repTitle = text("VDP DRAFT GENERATED", 18, COLOR_MACH_WHITE);
            repTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            repTitle.setPadding(0, 10, 0, 4);
            reportPanel.addView(repTitle);

            View repStripe = new View(this);
            repStripe.setBackgroundColor(COLOR_YELLOW);
            reportPanel.addView(repStripe, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 4));

            TextView repMsg = text("Draft appended to evidence JSONL for "
                    + fields.productIdentifier + ".", 14, COLOR_STATUS);
            repMsg.setTypeface(Typeface.MONOSPACE);
            repMsg.setPadding(0, 10, 0, 8);
            reportPanel.addView(repMsg);

            TextView repPreview = text(finalReport, 12, COLOR_STATUS);
            repPreview.setTypeface(Typeface.MONOSPACE);
            repPreview.setPadding(6, 4, 6, 6);
            reportPanel.addView(repPreview);

            LinearLayout nav = new LinearLayout(this);
            nav.setOrientation(LinearLayout.HORIZONTAL);
            nav.setPadding(0, 10, 0, 0);
            Button backTools = button("← Back to Tools");
            styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
            backTools.setOnClickListener(v1 -> showToolPalette());
            nav.addView(backTools, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            Button backScan = button("Back to Scan");
            styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
            backScan.setOnClickListener(v1 -> {
                if (toolPanelOverlay != null) {
                    frame.removeView(toolPanelOverlay);
                    toolPanelOverlay = null;
                }
            });
            nav.addView(backScan, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            reportPanel.addView(nav);

            reportPanel.addView(animaeFooter());

            toolPanelOverlay = reportPanel;
            frame.addView(toolPanelOverlay, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
        });
        root.addView(generateBtn, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                COMPACT_BUTTON_HEIGHT));

        // Export draft button
        Button exportDraftBtn = button("EXPORT DRAFT TO EVIDENCE");
        styleButton(exportDraftBtn, COLOR_YELLOW, 0xff101010, COLOR_MACH_WHITE);
        exportDraftBtn.setOnClickListener(v -> {
            fields.affectedSystem = affInput.getText().toString().trim();
            fields.observedBehavior = behInput.getText().toString().trim();
            fields.reproductionSteps = repInput.getText().toString().trim();
            fields.impactHypothesis = impInput.getText().toString().trim();

            String report = DisclosureReport.mercedesVdpTemplate(fields);
            appendEvidence("{\"type\":\"vdp_draft\",\"session_id\":" + json(sessionId)
                    + ",\"time\":" + json(Util.now())
                    + ",\"vendor\":\"Mercedes-Benz\""
                    + ",\"product_identifier\":" + json(fields.productIdentifier)
                    + ",\"vdp_text\":" + json(report) + "}");
            addEvent("VDP draft exported for " + fields.productIdentifier);
            setStatus("VDP draft exported to evidence JSONL");
        });
        root.addView(exportDraftBtn, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                COMPACT_BUTTON_HEIGHT));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);
        Button backTools = button("← Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> showToolPalette());
        nav.addView(backTools, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        backScan.setOnClickListener(v -> {
            if (toolPanelOverlay != null) {
                frame.removeView(toolPanelOverlay);
                toolPanelOverlay = null;
            }
        });
        nav.addView(backScan, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(nav);

        root.addView(animaeFooter());
        return root;
    }

    // ── HELPERS ────────────────────────────────────────────────────────
    private TextView sectionLabel(String label) {
        TextView v = text(label, 13, COLOR_CYAN);
        v.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        v.setPadding(0, 6, 0, 2);
        return v;
    }

    private void saveReport(String reason) {
        if (TextUtils.isEmpty(sessionId)) {
            setStatus("Start a session before saving a report.");
            return;
        }
        File parent = reportFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        StringBuilder report = new StringBuilder();
        report.append("Field Security Inspector Report\n");
        report.append("Session: ").append(sessionId).append("\n");
        report.append("Saved: ").append(now()).append("\n");
        report.append("Reason: ").append(reason).append("\n\n");
        report.append("Scope\n");
        report.append("Authorized defensive detection, patch-state documentation, "
                + "and customer reporting only.\n");
        report.append("No vehicle-control functions. No BLE writes. "
                + "Current scan mode is passive inventory.\n\n");
        report.append("Summary\n");
        report.append("BLE observations seen/logged: ").append(bleSeen)
                .append("/").append(bleLogged).append("\n");
        report.append("Wi-Fi observations seen/logged: ").append(wifiSeen)
                .append("/").append(wifiLogged).append("\n");
        report.append("Findings: ").append(findingCount).append("\n");
        if (wardriveEverActive) {
            report.append("Wardrive: ").append(wardriving ? "ACTIVE" : "done")
                    .append(", GPS fixes ").append(wardriveFixes);
            if (!Double.isNaN(wardriveLat)) {
                report.append(String.format(Locale.US, ", last fix %.5f, %.5f", wardriveLat, wardriveLon));
            }
            report.append("\n");
        }
        report.append("\n");
        report.append("Findings\n");
        if (findingLines.isEmpty()) {
            report.append("No known-rule findings recorded in this session.\n");
        } else {
            for (int i = findingLines.size() - 1; i >= 0; i--) {
                report.append("- ").append(findingLines.get(i)
                        .replace("\n", "\n  ")).append("\n\n");
            }
        }
        report.append("Evidence file\n");
        report.append(evidenceFile.getAbsolutePath()).append("\n");
        if (reportTreeUri != null) {
            report.append("External report folder\n");
            report.append(reportTreeUri.toString()).append("\n");
        }
        if (sdReportFile != null) {
            report.append("Removable SD app report\n");
            report.append(sdReportFile.getAbsolutePath()).append("\n");
        }
        String reportText = report.toString();
        boolean internalSaved = false;
        boolean sdSaved = false;
        String externalUri = "";
        try (FileWriter writer = new FileWriter(reportFile, false)) {
            writer.write(reportText);
            internalSaved = true;
        } catch (IOException e) {
            addEvent("Tablet report save failed: " + e.getMessage());
        }
        if (sdReportFile != null) {
            sdSaved = saveReportToFile(sdReportFile, reportText, "SD");
        }
        if (reportTreeUri != null) {
            externalUri = saveReportToTree(reportText);
        }
        if (internalSaved || sdSaved || !TextUtils.isEmpty(externalUri)) {
            appendEvidence("{\"type\":\"report_saved\",\"session_id\":" + json(sessionId)
                    + ",\"time\":" + json(now())
                    + ",\"reason\":" + json(reason)
                    + ",\"file\":" + json(reportFile.getAbsolutePath())
                    + ",\"sd_file\":" + json(sdSaved ? sdReportFile.getAbsolutePath() : "")
                    + ",\"external_uri\":" + json(externalUri) + "}");
            if (!TextUtils.isEmpty(externalUri)) {
                addEvent("Report saved to SD folder and tablet.");
            } else if (sdSaved) {
                addEvent("Report saved to SD card and tablet.");
            } else if (internalSaved) {
                addEvent("Report saved on tablet: " + reportFile.getName());
            }
        }
    }

    private boolean saveReportToFile(File target, String reportText, String label) {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (FileWriter writer = new FileWriter(target, false)) {
            writer.write(reportText);
            return true;
        } catch (IOException e) {
            addEvent(label + " report save failed: " + e.getMessage());
            return false;
        }
    }

    private String saveReportToTree(String reportText) {
        String fileName = "field_report_" + sessionId + ".txt";
        try {
            Uri documentUri = DocumentsContract.createDocument(
                    getContentResolver(), reportTreeUri, "text/plain", fileName);
            if (documentUri == null) {
                addEvent("SD report save failed: no document returned.");
                return "";
            }
            try (OutputStream stream = getContentResolver().openOutputStream(documentUri, "wt")) {
                if (stream == null) {
                    addEvent("SD report save failed: no output stream.");
                    return "";
                }
                stream.write(reportText.getBytes("UTF-8"));
                stream.flush();
            }
            return documentUri.toString();
        } catch (Exception e) {
            addEvent("SD report save failed: " + e.getMessage());
            return "";
        }
    }

    private void appendEvidence(String jsonLine) {
        File parent = evidenceFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (FileWriter writer = new FileWriter(evidenceFile, true)) {
            writer.write(jsonLine);
            writer.write("\n");
        } catch (IOException e) {
            addEvent("Evidence write failed: " + e.getMessage());
        }
    }

    private File removableReportFile(String name) {
        File dir = getRemovableEvidenceDir();
        if (dir == null) return null;
        return new File(dir, name);
    }

    private File getRemovableEvidenceDir() {
        File[] dirs = getExternalFilesDirs(null);
        if (dirs == null) return null;
        for (File dir : dirs) {
            if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                return new File(dir, "evidence");
            }
        }
        return null;
    }

    private String sdStatusSuffix() {
        return sdReportFile == null
                ? "\nNo SD card in this device -- report saved on phone storage only."
                : "\nSD report copy: " + sdReportFile.getAbsolutePath();
    }

    private boolean hasPermissionSafe(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(new Date());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String json(String value) {
        return "\"" + safe(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }

    private static String hex(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            builder.append(String.format(Locale.US, "%02x", b & 0xff));
        }
        return builder.toString();
    }

    private static String printableAscii(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            int value = b & 0xff;
            if (value >= 32 && value <= 126) {
                builder.append((char) value);
            } else {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    private static String truncate(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return safe(value);
        }
        return value.substring(0, maxChars) + "...";
    }

    private static String vehicleClue(Observation observation) {
        String text = " " + observation.searchableText().toLowerCase(Locale.US) + " ";
        List<String> matches = new ArrayList<>();
        for (String[] clue : VEHICLE_CLUES) {
            if (text.contains(clue[0]) && !matches.contains(clue[1])) {
                matches.add(clue[1]);
            }
        }
        if (matches.isEmpty()) return "";
        return TextUtils.join(", ", matches);
    }

    private static String karrClue(Observation observation) {
        String text = " " + observation.searchableText().toLowerCase(Locale.US) + " ";
        List<String> matches = new ArrayList<>();
        if (text.contains("karr")) matches.add("KARR keyword");
        if (text.contains("swds")) matches.add("SWDS keyword");
        if (text.contains("southwest") || text.contains("south west"))
            matches.add("Southwest Dealer Services keyword");
        if (text.contains("acrisure")) matches.add("Acrisure keyword");
        return TextUtils.join(", ", matches);
    }

    // ====================================================================
    // VEHICLE AUDIT: combined scan, discovery list, documentation, database
    // Slots used with showAuditOverlay(): 0 = scan results, 1 = document flow,
    // 2 = documented-findings list.
    // ====================================================================

    /** Main-screen "Start Scan": passive BLE + Wi-Fi discovery, then show results. */
    private void startVehicleAuditScan() {
        ensureSession();
        if (!bleScanning) startBleScan();
        startWifiScan();
        addEvent("Vehicle audit scan started (passive BLE + Wi-Fi discovery).");
        setStatus("Vehicle audit scan running. Passive discovery only; no connections made.");
        showAuditScanResults();
    }

    /** Shared framed panel used by every audit screen (matches the tool-panel style). */
    private LinearLayout auditPanel(String titleText) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(TOOL_PANEL_PADDING_SIDE, TOOL_PANEL_PADDING_TOP,
                TOOL_PANEL_PADDING_SIDE, 14);
        panel.setBackgroundColor(COLOR_WARM_BG);

        TextView title = text(titleText, 20, COLOR_MACH_WHITE);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 8, 0, 6);
        panel.addView(title);

        View stripe = new View(this);
        stripe.setBackgroundColor(COLOR_SAKURA);
        panel.addView(stripe, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4));
        return panel;
    }

    /** Standard back-navigation row for scan-result style screens. */
    private void addAuditNav(LinearLayout panel) {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);

        Button backTools = button("< Back to Tools");
        styleButton(backTools, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        backTools.setOnClickListener(v -> {
            removeAuditOverlays();
            showToolPalette();
        });
        nav.addView(backTools, new LinearLayout.LayoutParams(0, COMPACT_BUTTON_HEIGHT, 1));

        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        backParams.setMargins(10, 0, 0, 0);
        backScan.setOnClickListener(v -> removeAuditOverlays());
        nav.addView(backScan, backParams);
        panel.addView(nav);
    }

    /** Replaces any open audit overlay with the given one. */
    private void showAuditOverlay(View panel, int slot) {
        removeAuditOverlays();
        frame.addView(panel, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        if (slot == 0) auditResultsScreen = panel;
        else if (slot == 1) auditDocumentScreen = panel;
        else auditFindingsScreen = panel;
    }

    private void removeAuditOverlays() {
        main.removeCallbacks(auditResultsRefresh);
        if (auditResultsScreen != null) {
            frame.removeView(auditResultsScreen);
            auditResultsScreen = null;
        }
        if (auditDocumentScreen != null) {
            frame.removeView(auditDocumentScreen);
            auditDocumentScreen = null;
        }
        if (auditFindingsScreen != null) {
            frame.removeView(auditFindingsScreen);
            auditFindingsScreen = null;
        }
    }

    /** Human-readable device name for any observation type. */
    private String auditTargetName(Observation observation) {
        if (observation instanceof BleObservation) {
            String name = ((BleObservation) observation).name;
            return TextUtils.isEmpty(name) ? "(no name advertised)" : name;
        }
        if (observation instanceof WifiObservation) {
            return displaySsid(((WifiObservation) observation).ssid);
        }
        return observation.summary();
    }

    // -- SCAN RESULTS LIST -----------------------------------------------
    private void showAuditScanResults() {
        LinearLayout panel = auditPanel("VEHICLE AUDIT - SCAN RESULTS");

        auditResultsStatus = text(auditResultsStatusText(), 14,
                allTargets.isEmpty() ? COLOR_STATUS : COLOR_CYAN);
        auditResultsStatus.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        auditResultsStatus.setPadding(0, 10, 0, 8);
        panel.addView(auditResultsStatus);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button startScan = button(bleScanning ? "Scanning..." : "Start Scan");
        styleButton(startScan, bleScanning ? COLOR_YELLOW : COLOR_CYAN,
                0xff101010, COLOR_MACH_WHITE);
        startScan.setOnClickListener(v -> startVehicleAuditScan());
        actions.addView(startScan, new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1));

        Button refresh = button("Refresh List");
        styleButton(refresh, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        refreshParams.setMargins(10, 0, 0, 0);
        refresh.setOnClickListener(v -> showAuditScanResults());
        actions.addView(refresh, refreshParams);
        panel.addView(actions);

        TextView tableHeader = text("TYPE  CLASS  NAME                    MAC / ID            RSSI", 11, COLOR_DIM);
        tableHeader.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tableHeader.setPadding(8, 12, 8, 4);
        panel.addView(tableHeader);

        ScrollView scroll = new ScrollView(this);
        auditResultsList = new LinearLayout(this);
        auditResultsList.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(auditResultsList);
        panel.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        renderAuditResults();

        addAuditNav(panel);
        panel.addView(animaeFooter());
        showAuditOverlay(panel, 0);
        // Keep the discovery list live while the operator stays on this screen.
        main.removeCallbacks(auditResultsRefresh);
        main.postDelayed(auditResultsRefresh, AUDIT_REFRESH_INTERVAL_MS);
    }

    private String auditResultsStatusText() {
        return allTargets.isEmpty()
                ? "No devices discovered yet. Tap Start Scan to begin passive BLE + Wi-Fi discovery."
                : allTargets.size() + " device(s) discovered. Tap a row to inspect and document.";
    }

    /** Re-renders the scan-results rows every few seconds until the screen is closed. */
    private final Runnable auditResultsRefresh = new Runnable() {
        @Override
        public void run() {
            if (auditResultsScreen == null) return;
            renderAuditResults();
            main.postDelayed(this, AUDIT_REFRESH_INTERVAL_MS);
        }
    };

    private void renderAuditResults() {
        if (auditResultsList == null) return;
        if (auditResultsStatus != null) {
            auditResultsStatus.setText(auditResultsStatusText());
            auditResultsStatus.setTextColor(allTargets.isEmpty() ? COLOR_STATUS : COLOR_CYAN);
        }
        auditResultsList.removeAllViews();
        if (allTargets.isEmpty()) {
            TextView empty = text("No observed vehicles or devices yet.", 13, COLOR_DIM);
            empty.setTypeface(Typeface.MONOSPACE);
            empty.setPadding(8, 6, 8, 6);
            auditResultsList.addView(empty);
            return;
        }
        List<Observation> observations = new ArrayList<>(allTargets.values());
        for (int i = observations.size() - 1; i >= 0; i--) {
            auditResultsList.addView(auditResultRow(observations.get(i)));
        }
    }

    /** One discovered device: type, name, MAC/ID, RSSI + passive vehicle classification. */
    private TextView auditResultRow(Observation obs) {
        String vendor = OuiLookup.lookup(obs.identity());
        String category = VehicleClassifier.classify(obs.searchableText(), vendor);
        String karr = karrClue(obs);
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(obs.type().toUpperCase(Locale.US)).append("] ")
                .append(VehicleClassifier.badge(category)).append("  ")
                .append(auditTargetName(obs))
                .append("\nMAC/ID ").append(obs.identity())
                .append("   RSSI ").append(obs.rssi()).append(" dBm")
                .append("\n").append(VehicleClassifier.describe(category));
        if (!TextUtils.isEmpty(vendor) && !"Unknown".equals(vendor)) {
            sb.append(" | ").append(vendor);
        }
        if (!TextUtils.isEmpty(karr)) {
            sb.append("\n").append(karr);
        }
        boolean interesting = !TextUtils.isEmpty(karr)
                || !VehicleClassifier.UNKNOWN.equals(category);
        TextView row = text(sb.toString(), 12, interesting ? COLOR_YELLOW : COLOR_CYAN);
        row.setTypeface(Typeface.MONOSPACE);
        row.setPadding(8, 5, 8, 5);
        row.setBackground(panelDrawable(0xff101322, 0xff283044, 1));
        row.setOnClickListener(v -> showAuditDeviceDetail(obs));
        return row;
    }

    // -- DEVICE DETAIL ---------------------------------------------------
    private void showAuditDeviceDetail(Observation observation) {
        LinearLayout panel = auditPanel("DEVICE DETAIL");

        TextView summary = text(observation.summary(), 15, COLOR_CYAN);
        summary.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        summary.setPadding(0, 8, 0, 8);
        panel.addView(summary);

        String vendor = OuiLookup.lookup(observation.identity());
        String category = VehicleClassifier.classify(observation.searchableText(), vendor);
        TextView classification = text("Passive classification: "
                + VehicleClassifier.describe(category)
                + "\nOUI vendor: " + vendor, 14, COLOR_YELLOW);
        classification.setTypeface(Typeface.MONOSPACE);
        classification.setPadding(0, 0, 0, 10);
        panel.addView(classification);

        Button document = button("Document Finding");
        styleButton(document, COLOR_CRIMSON, COLOR_MACH_WHITE, COLOR_MACH_WHITE);
        document.setOnClickListener(v -> showAuditDocumentForm(observation));
        panel.addView(document, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, COMPACT_BUTTON_HEIGHT));

        Button back = button("Back to Scan Results");
        styleButton(back, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        back.setOnClickListener(v -> showAuditScanResults());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, COMPACT_BUTTON_HEIGHT);
        backParams.setMargins(0, 8, 0, 0);
        panel.addView(back, backParams);

        String clue = vehicleClue(observation);
        TextView body = text(observation.detail()
                + "\n\nVehicle clue: " + (TextUtils.isEmpty(clue) ? "none found" : clue)
                + "\n\nMode: passive observation only. No connection, pairing, or "
                + "vehicle-control action was performed.", 14, COLOR_STATUS);
        body.setTypeface(Typeface.MONOSPACE);
        body.setPadding(0, 14, 0, 0);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(body);
        panel.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        panel.addView(animaeFooter());
        showAuditOverlay(panel, 1);
    }

    // -- DOCUMENT FINDING FORM -------------------------------------------
    private void showAuditDocumentForm(Observation observation) {
        auditSelected = observation;
        LinearLayout panel = auditPanel("DOCUMENT FINDING");

        TextView target = text(auditTargetName(observation) + "\n" + observation.identity()
                + "   RSSI " + observation.rssi() + " dBm", 14, COLOR_CYAN);
        target.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        target.setPadding(0, 8, 0, 10);
        panel.addView(target);

        TextView catLabel = text("1. Issue category", 13, COLOR_STATUS);
        catLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        panel.addView(catLabel);
        auditCategoryRow = new LinearLayout(this);
        auditCategoryRow.setOrientation(LinearLayout.VERTICAL);
        panel.addView(auditCategoryRow);
        renderAuditCategoryButtons();

        TextView sevLabel = text("2. Severity", 13, COLOR_STATUS);
        sevLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        sevLabel.setPadding(0, 12, 0, 0);
        panel.addView(sevLabel);
        auditSeverityRow = new LinearLayout(this);
        auditSeverityRow.setOrientation(LinearLayout.HORIZONTAL);
        panel.addView(auditSeverityRow);
        renderAuditSeverityButtons();

        TextView notesLabel = text("3. Notes", 13, COLOR_STATUS);
        notesLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        notesLabel.setPadding(0, 12, 0, 4);
        panel.addView(notesLabel);

        auditNotesInput = new EditText(this);
        auditNotesInput.setHint("Observation, likely owner, follow-up needed...");
        auditNotesInput.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        auditNotesInput.setTextSize(13);
        auditNotesInput.setTextColor(COLOR_MACH_WHITE);
        auditNotesInput.setHintTextColor(COLOR_DIM);
        auditNotesInput.setBackground(panelDrawable(COLOR_PANEL, 0xff283044, 1));
        auditNotesInput.setPadding(10, 8, 10, 8);
        auditNotesInput.setMinLines(2);
        auditNotesInput.setMinHeight(120);
        auditNotesInput.setGravity(Gravity.TOP | Gravity.START);
        panel.addView(auditNotesInput, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        Button save = button("Save to Database");
        styleButton(save, COLOR_YELLOW, 0xff101010, COLOR_MACH_WHITE);
        save.setOnClickListener(v -> saveAuditFinding());
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, COMPACT_BUTTON_HEIGHT);
        saveParams.setMargins(0, 10, 0, 0);
        panel.addView(save, saveParams);

        Button cancel = button("Cancel");
        styleButton(cancel, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        cancel.setOnClickListener(v -> showAuditDeviceDetail(observation));
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, COMPACT_BUTTON_HEIGHT);
        cancelParams.setMargins(0, 8, 0, 0);
        panel.addView(cancel, cancelParams);

        panel.addView(animaeFooter());
        showAuditOverlay(panel, 1);
    }

    private void renderAuditCategoryButtons() {
        if (auditCategoryRow == null) return;
        auditCategoryRow.removeAllViews();
        for (int i = 0; i < AUDIT_CATEGORIES.length; i++) {
            final String category = AUDIT_CATEGORIES[i];
            boolean selected = category.equals(auditCategory);
            Button b = button(category);
            b.setTextSize(12);
            styleButton(b, selected ? COLOR_YELLOW : COLOR_PANEL,
                    selected ? 0xff101010 : COLOR_CYAN,
                    selected ? COLOR_MACH_WHITE : 0xff50556f);
            b.setOnClickListener(v -> {
                auditCategory = category;
                renderAuditCategoryButtons();
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, COMPACT_BUTTON_HEIGHT);
            p.setMargins(0, 2, 0, 2);
            auditCategoryRow.addView(b, p);
        }
    }

    private void renderAuditSeverityButtons() {
        if (auditSeverityRow == null) return;
        auditSeverityRow.removeAllViews();
        for (int i = 0; i < AUDIT_SEVERITIES.length; i++) {
            final String severity = AUDIT_SEVERITIES[i];
            boolean selected = severity.equals(auditSeverity);
            Button b = button(severity.toUpperCase(Locale.US));
            b.setTextSize(12);
            styleButton(b, selected ? COLOR_CRIMSON : COLOR_PANEL,
                    selected ? COLOR_MACH_WHITE : COLOR_CYAN,
                    selected ? COLOR_MACH_WHITE : 0xff50556f);
            b.setOnClickListener(v -> {
                auditSeverity = severity;
                renderAuditSeverityButtons();
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    0, COMPACT_BUTTON_HEIGHT, 1);
            p.setMargins(4, 0, 4, 0);
            auditSeverityRow.addView(b, p);
        }
    }

    /** Persists the documented finding to SQLite and to the session evidence log. */
    private void saveAuditFinding() {
        if (auditSelected == null) {
            setStatus("Select a scan result before documenting a finding.");
            return;
        }
        String notes = auditNotesInput == null ? ""
                : auditNotesInput.getText().toString().trim();
        AuditFinding finding = new AuditFinding(0, now(), sessionId,
                auditSelected.type(), auditSelected.identity(),
                auditTargetName(auditSelected), auditSelected.rssi(),
                auditCategory, auditSeverity, notes);
        long id;
        try {
            id = auditDb.insert(finding);
        } catch (RuntimeException e) {
            setStatus("Could not save finding to the local database: " + e.getMessage());
            return;
        }
        finding.id = id;
        appendEvidence(finding.toJson());
        addEvent("Documented finding #" + id + " [" + auditSeverity + "] " + auditCategory);
        setStatus("Finding #" + id + " saved to the local audit database.");
        auditSelected = null;
        showAuditFindingsList();
    }

    // -- DOCUMENTED FINDINGS LIST ----------------------------------------
    private void showAuditFindingsList() {
        LinearLayout panel = auditPanel("DOCUMENTED FINDINGS");

        List<AuditFinding> documented = auditDb.listAll();
        TextView count = text(documented.isEmpty()
                ? "No findings documented yet. Tap a scan result, then Document Finding."
                : documented.size() + " documented finding(s) in the local database.",
                14, documented.isEmpty() ? COLOR_STATUS : COLOR_CYAN);
        count.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        count.setPadding(0, 10, 0, 8);
        panel.addView(count);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        panel.addView(scroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        if (documented.isEmpty()) {
            TextView empty = text("Nothing documented yet.", 13, COLOR_DIM);
            empty.setTypeface(Typeface.MONOSPACE);
            empty.setPadding(8, 6, 8, 6);
            list.addView(empty);
        } else {
            for (AuditFinding finding : documented) {
                String targetType = TextUtils.isEmpty(finding.targetType)
                        ? "device" : finding.targetType.toUpperCase(Locale.US);
                String text1 = "#" + finding.id + "  " + finding.createdAt
                        + "\n" + finding.category + "  [" + finding.severity + "]"
                        + "\n" + targetType + " " + finding.targetName
                        + "  " + finding.targetId
                        + (TextUtils.isEmpty(finding.notes)
                            ? "" : "\nNotes: " + finding.notes);
                TextView row = text(text1, 12,
                        "high".equals(finding.severity) ? COLOR_YELLOW : COLOR_CYAN);
                row.setTypeface(Typeface.MONOSPACE);
                row.setPadding(8, 6, 8, 6);
                row.setBackground(panelDrawable(0xff101322, 0xff283044, 1));
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.setMargins(0, 2, 0, 2);
                list.addView(row, rowParams);
            }
        }

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 10, 0, 0);

        Button clear = button("Delete All Findings");
        styleButton(clear, COLOR_PANEL, COLOR_MACH_WHITE, 0xff50556f);
        clear.setOnClickListener(v -> {
            auditDb.deleteAll();
            setStatus("Local audit database cleared.");
            showAuditFindingsList();
        });
        nav.addView(clear, new LinearLayout.LayoutParams(0, COMPACT_BUTTON_HEIGHT, 1));

        Button backScan = button("Back to Scan");
        styleButton(backScan, COLOR_PANEL, COLOR_CYAN, COLOR_CYAN);
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                0, COMPACT_BUTTON_HEIGHT, 1);
        backParams.setMargins(10, 0, 0, 0);
        backScan.setOnClickListener(v -> removeAuditOverlays());
        nav.addView(backScan, backParams);
        panel.addView(nav);

        panel.addView(animaeFooter());
        showAuditOverlay(panel, 2);
    }

    // ── INNER CLASSES (same as before) ────────────────────────────────
    interface Observation {
        String type();
        String searchableText();
        String identity();
        int rssi();
        String summary();
        String detail();
    }

    static class BleObservation implements Observation {
        final String time;
        final String address;
        final String name;
        final int rssi;
        final byte[] raw;

        BleObservation(String time, String address, String name, int rssi, byte[] raw) {
            this.time = time;
            this.address = address;
            this.name = name;
            this.rssi = rssi;
            this.raw = raw;
        }

        @Override public String type() { return "ble"; }
        @Override public String searchableText() {
            return (name + " " + address + " "
                    + Util.printableAscii(raw) + " " + Util.hex(raw)).toLowerCase(Locale.US);
        }
        @Override public String identity() { return address; }
        @Override public int rssi() { return rssi; }
        @Override public String summary() {
            return "BLE " + shortAddress(address) + " RSSI " + rssi + label(name);
        }
        @Override public String detail() {
            return "Type: BLE advertisement"
                    + "\nName: " + (TextUtils.isEmpty(name) ? "(none advertised)" : name)
                    + "\nAddress: " + address
                    + "\nRSSI: " + rssi
                    + "\nVehicle/VIN: not advertised in passive BLE data"
                    + "Readable payload: " + truncate(Util.printableAscii(raw).trim(), 80)
                    + "\nAdvertisement hex: " + truncate(hex(raw), 96);
        }
        String toJson(String sessionId) {
            return "{\"type\":\"ble_observation\",\"session_id\":" + json(sessionId)
                    + ",\"time\":" + json(time)
                    + ",\"address\":" + json(address)
                    + ",\"name\":" + json(name)
                    + ",\"rssi\":" + rssi
                    + ",\"adv_hex\":" + json(hex(raw)) + "}";
        }
        String rawHex() { return hex(raw); }
        String printableAscii() { return Util.printableAscii(raw); }
    }

    static class WifiObservation implements Observation {
        final String time;
        final String ssid;
        final String bssid;
        final String capabilities;
        final int rssi;

        WifiObservation(String time, String ssid, String bssid, String capabilities, int rssi) {
            this.time = time;
            this.ssid = ssid;
            this.bssid = bssid;
            this.capabilities = capabilities;
            this.rssi = rssi;
        }

        @Override public String type() { return "wifi"; }
        @Override public String searchableText() {
            return (ssid + " " + bssid + " " + capabilities).toLowerCase(Locale.US);
        }
        @Override public String identity() { return bssid; }
        @Override public int rssi() { return rssi; }
        @Override public String summary() {
            return "Wi-Fi " + displaySsid(ssid) + " " + shortAddress(bssid) + " RSSI " + rssi;
        }
        @Override public String detail() {
            return "Type: Wi-Fi beacon"
                    + "\nSSID/AP name: " + displaySsid(ssid)
                    + "\nBSSID: " + bssid
                    + "\nRSSI: " + rssi
                    + "\nSecurity: " + capabilities
                    + "\nVehicle/VIN: not advertised in passive Wi-Fi data";
        }
        String toJson(String sessionId) {
            return "{\"type\":\"wifi_observation\",\"session_id\":" + json(sessionId)
                    + ",\"time\":" + json(time)
                    + ",\"ssid\":" + json(ssid)
                    + ",\"bssid\":" + json(bssid)
                    + ",\"capabilities\":" + json(capabilities)
                    + ",\"rssi\":" + rssi + "}";
        }
    }

    interface DetectorRule {
        String id();
        Finding evaluate(Observation observation);
    }

    static class KarrSwdsBleRule implements DetectorRule {
        private final String[] keywords = new String[]{
                "karr", "swds", "southwest", "south west", "acrisure"};

        @Override public String id() { return "known_ble_karr_swds_keyword"; }

        @Override public Finding evaluate(Observation observation) {
            if (!"ble".equals(observation.type())) return null;
            String text = observation.searchableText();
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    return new Finding(id(), "Possible KARR/SWDS module",
                            "BLE advertisement matched researched KARR/SWDS keyword '"
                                    + keyword + "'. Treat as a candidate until "
                                    + "owner-authorized verification confirms device identity "
                                    + "and patch state.", "medium");
                }
            }
            return null;
        }
    }

    static class OpenWifiRule implements DetectorRule {
        @Override public String id() { return "known_wifi_open_network"; }

        @Override public Finding evaluate(Observation observation) {
            if (!"wifi".equals(observation.type())
                    || !(observation instanceof WifiObservation)) return null;
            WifiObservation wifi = (WifiObservation) observation;
            String caps = wifi.capabilities.toUpperCase(Locale.US);
            boolean hasPrivacy = caps.contains("WEP") || caps.contains("WPA")
                    || caps.contains("RSN") || caps.contains("SAE")
                    || caps.contains("EAP");
            if (!hasPrivacy) {
                return new Finding(id(), "Open Wi-Fi network",
                        "Wi-Fi network '" + displaySsid(wifi.ssid)
                                + "' advertises no encryption. For an authorized vehicle "
                                + "or customer hotspot, recommend enabling WPA2/WPA3.", "high");
            }
            return null;
        }
    }

    static class WepWifiRule implements DetectorRule {
        @Override public String id() { return "known_wifi_wep_network"; }

        @Override public Finding evaluate(Observation observation) {
            if (!"wifi".equals(observation.type())
                    || !(observation instanceof WifiObservation)) return null;
            WifiObservation wifi = (WifiObservation) observation;
            if (wifi.capabilities.toUpperCase(Locale.US).contains("WEP")) {
                return new Finding(id(), "WEP Wi-Fi network",
                        "Wi-Fi network '" + displaySsid(wifi.ssid)
                                + "' advertises WEP, which is obsolete and easily broken. "
                                + "For an authorized vehicle or customer hotspot, recommend "
                                + "moving to WPA2/WPA3.", "high");
            }
            return null;
        }
    }

    private static String displaySsid(String ssid) {
        return TextUtils.isEmpty(ssid) ? "(hidden SSID)" : ssid;
    }

    private static String shortAddress(String value) {
        if (TextUtils.isEmpty(value) || value.length() <= 5) return value;
        return value.substring(value.length() - 5);
    }

    private static String label(String value) {
        return TextUtils.isEmpty(value) ? "" : " " + value;
    }

    /**
     * ANIMAE artist signature footer — consistent visual closure for every tool panel,
     * tying the UI back to the art direction of the splash/assistant PNGs.
     */
    private View animaeFooter() {
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setPadding(0, 14, 0, 6);
        footer.setBackgroundColor(COLOR_WARM_BG);

        // thin signature rule
        View sigRule = new View(this);
        sigRule.setBackgroundColor(COLOR_RULE_LINE);
        sigRule.setLayoutParams(new LinearLayout.LayoutParams(
                0, 1, 1));
        footer.addView(sigRule, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));

        TextView sig = text("RACER ZERO Field Console", 10, COLOR_DIM);
        sig.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        sig.setGravity(Gravity.CENTER);
        footer.addView(sig, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView artRef = text("Art: ANIMAE · RacerX HUD lineage", 9, COLOR_DIM);
        artRef.setTypeface(Typeface.MONOSPACE);
        artRef.setGravity(Gravity.CENTER);
        footer.addView(artRef, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        return footer;
    }

    static class ObservationState {
        long lastLoggedMs;
        int lastRssi;
        ObservationState(long lastLoggedMs, int lastRssi) {
            this.lastLoggedMs = lastLoggedMs;
            this.lastRssi = lastRssi;
        }
    }

    static class Finding {
        final String ruleId;
        final String title;
        final String detail;
        final String confidence;
        Finding(String ruleId, String title, String detail, String confidence) {
            this.ruleId = ruleId;
            this.title = title;
            this.detail = detail;
            this.confidence = confidence;
        }
        String toJson(String sessionId, Observation observation) {
            return "{\"type\":\"finding\",\"session_id\":" + json(sessionId)
                    + ",\"time\":" + json(now())
                    + ",\"rule_id\":" + json(ruleId)
                    + ",\"target_type\":" + json(observation.type())
                    + ",\"target_id\":" + json(observation.identity())
                    + ",\"target_summary\":" + json(observation.summary())
                    + ",\"title\":" + json(title)
                    + ",\"detail\":" + json(detail)
                    + ",\"confidence\":" + json(confidence) + "}";
        }
    }
}
