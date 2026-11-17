package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.SessionSettings
import com.example.ui.components.DraftDisclaimerBanner
import com.example.ui.components.SectionCard
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageGuideScreen(
    settings: SessionSettings,
    onBack: () -> Unit,
) {
    val isBn = settings.language == AppLanguage.BENGALI
    val strings = AppStrings.get(settings.language)

    InfoScaffold(
        title = strings.usageGuide,
        onBack = onBack,
    ) {
        item { DraftDisclaimerBanner(text = strings.draftDisclaimerFull) }
        item {
            SectionCard(title = if (isBn) "ধাপ ১: সঠিক জরিপ নির্বাচন" else "Step 1: Choose Survey Type") {
                Text(
                    text = if (isBn) {
                        "হোমপেজ থেকে CS, SA, RS, BS, BRS, BDRS, City Survey অথবা অন্যান্য খতিয়ান (Custom Template) নির্বাচন করুন। প্রতিটি জরিপের ফরম ও PDF টেমপ্লেট সম্পূর্ণ আলাদা।"
                    } else {
                        "Pick CS, SA, RS, BS, BRS, BDRS, City Survey, or Other/Custom Template from the Home screen. Every survey loads its own isolated fields and dedicated PDF template."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            SectionCard(title = if (isBn) "ধাপ ২: মৌলিক, মালিক ও দাগের তথ্য" else "Step 2: Enter Basic, Owner & Dag Data") {
                Text(
                    text = if (isBn) {
                        "১. মৌলিক ট্যাবে বিভাগ, জেলা, মৌজা, জে.এল. নং ও খতিয়ান নং এবং জরিপ-ভিত্তিক বিশেষ ঘর পূরণ করুন।\n২. মালিক ট্যাবে 'Add Owner' চাপিয়ে মূল মালিক, ওয়ারিশ, ক্রেতা বা অংশীদার যুক্ত করুন।\n৩. দাগ ও জমি ট্যাবে শতক, একর, কাঠা, বিঘা বা হেক্টর এককে জমির পরিমাণ ও হিস্যা দিন; স্বয়ংক্রিয় গণনা চাইলে চালু রাখুন অথবা বন্ধ করে ম্যানুয়ালি লিখুন।"
                    } else {
                        "1. Fill administrative jurisdiction & survey-exclusive fields in the Basic tab.\n2. Use 'Add Owner' to add multiple owners with categories, Hissa, and land share.\n3. Use 'Add Dag' with built-in Shotok/Acre/Katha/Bigha/Hectare unit conversion and optional manual calculation override."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            SectionCard(title = if (isBn) "ধাপ ৩: প্রিভিউ, ডাউনলোড ও প্রিন্ট" else "Step 3: Preview, Download PDF & Print") {
                Text(
                    text = if (isBn) {
                        "Generate PDF বা Preview বাটনে ক্লিক করে Zoom In/Out এবং পৃষ্ঠাগুলো যাচাই করুন। এরপর Download PDF দিয়ে আপনার পছন্দের ফোল্ডারে সেভ করুন অথবা Direct Print-এর মাধ্যমে সরাসরি প্রিন্ট করুন। অ্যাপটি বন্ধ করলে কোনো তথ্য সংরক্ষিত থাকে না।"
                    } else {
                        "Tap Preview or Generate PDF to inspect pages with Zoom & pagination controls. Export via Download PDF using the Android File Picker or print immediately via Direct Print. Closing the app erases all temporary inputs."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    settings: SessionSettings,
    onBack: () -> Unit,
) {
    val isBn = settings.language == AppLanguage.BENGALI
    val strings = AppStrings.get(settings.language)

    InfoScaffold(
        title = strings.privacyPolicy,
        onBack = onBack,
    ) {
        item {
            SectionCard(
                title = if (isBn) "শূন্য-সংরক্ষণ গোপনীয়তা নীতি (Zero-Persistence)" else "Zero-Persistence Privacy Policy",
            ) {
                Text(
                    text = if (isBn) {
                        "Online Khatian Maker সম্পূর্ণ অফলাইন এবং গোপনীয়তা-কেন্দ্রিক অ্যাপ:\n\n" +
                            "• কোনো অ্যাকাউন্ট বা লগইন প্রয়োজন নেই।\n" +
                            "• কোনো Firebase, Cloud Server, বা Analytics ব্যবহার করা হয় না।\n" +
                            "• কোনো স্থানীয় ডাটাবেস, Auto-Save বা History সংরক্ষণ করা হয় না।\n" +
                            "• আপনার প্রবেশ করা সকল তথ্য শুধুমাত্র অ্যাপ চলাকালীন অস্থায়ী RAM মেমোরিতে থাকে।\n" +
                            "• 'Clear All' চাপলে অথবা অ্যাপ বন্ধ/ক্র্যাশ হলে সকল তথ্য সম্পূর্ণভাবে মুছে যায়।\n" +
                            "• PDF ফাইল শুধুমাত্র আপনার নির্দেশে আপনার নির্বাচিত ফোল্ডারে সংরক্ষিত হয়; অ্যাপ কোনো অতিরিক্ত কপি রাখে না।"
                    } else {
                        "Online Khatian Maker is a 100% offline, privacy-first utility:\n\n" +
                            "• No accounts, no login, and no personal tracking.\n" +
                            "• No Firebase, cloud databases, or analytics servers.\n" +
                            "• No local database, no Auto-Save, and no History.\n" +
                            "• All entered form inputs and picked reference images stay strictly in volatile RAM during your active session.\n" +
                            "• Tapping 'Clear All' or closing/terminating the app erases all data permanently with zero recovery.\n" +
                            "• PDFs are written only to the exact file location you choose via Android's File Picker."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    settings: SessionSettings,
    onBack: () -> Unit,
) {
    val isBn = settings.language == AppLanguage.BENGALI
    val strings = AppStrings.get(settings.language)

    InfoScaffold(
        title = strings.aboutApp,
        onBack = onBack,
    ) {
        item { DraftDisclaimerBanner(text = strings.draftDisclaimerFull) }
        item {
            SectionCard(
                title = "Online Khatian Maker (অনলাইন খতিয়ান প্রস্তুত)",
                subtitle = if (isBn) "সংস্করণ ১.০ • সম্পূর্ণ অফলাইন সংস্করণ" else "Version 1.0 • 100% Offline Edition",
            ) {
                Text(
                    text = if (isBn) {
                        "বাংলাদেশের ঐতিহাসিক ও আধুনিক ভূমি জরিপ (CS, SA, RS, BS, BRS, BDRS, City Survey এবং Custom Template)-এর আদলে ডিজিটাল খসড়া খতিয়ান প্রস্তুত করার জন্য এই অ্যাপটি তৈরি করা হয়েছে।\n\nগুরুত্বপূর্ণ ঘোষণা: এই অ্যাপের মাধ্যমে তৈরি কোনো PDF সরকারি রেকর্ড, মূল খতিয়ান বা প্রত্যয়িত নকল নয়। এটি শুধুমাত্র ব্যবহারকারীর তৈরি ডিজিটাল খসড়া।"
                    } else {
                        "Built to help draft structured digital land survey Khatian layouts (CS, SA, RS, BS, BRS, BDRS, City Survey, and Custom Templates) completely offline with native Bengali typography and Android Print integration.\n\nImportant Notice: Documents generated by this app are user-created digital drafts and do NOT constitute official government records or certified copies."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoScaffold(
    title: String,
    onBack: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content,
            )
        }
    }
}
