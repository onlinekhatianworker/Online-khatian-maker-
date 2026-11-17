package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.SessionSettings
import com.example.model.SurveyType
import com.example.ui.components.DraftDisclaimerBanner
import com.example.ui.components.PrivacyRamBadge
import com.example.util.AppStrings

@Composable
fun HomeScreen(
    settings: SessionSettings,
    onSelectSurvey: (SurveyType) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val strings = AppStrings.get(settings.language)
    val isBn = settings.language == AppLanguage.BENGALI

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
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
                    .widthIn(max = 960.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Hero Portal Header Card
                item {
                    HeroPortalCard(
                        title = strings.appTitle,
                        subtitle = strings.appSubtitleBn,
                        privacyLabel = strings.privacyChip,
                        onOpenSettings = onOpenSettings,
                    )
                }

                // Mandatory Digital Draft Disclaimer
                item {
                    DraftDisclaimerBanner(text = strings.draftDisclaimerFull)
                }

                // Section Title
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = strings.selectSurveyHeading,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = strings.selectSurveySubheading,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // 8 Khatian Survey Cards (Adaptive Columns)
                item {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val columns = when {
                            maxWidth >= 780.dp -> 4
                            maxWidth >= 520.dp -> 3
                            else -> 2
                        }
                        val rows = SurveyType.entries.chunked(columns)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            rows.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    rowItems.forEach { survey ->
                                        SurveyCategoryCard(
                                            survey = survey,
                                            isBengali = isBn,
                                            onClick = { onSelectSurvey(survey) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    repeat(columns - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Links: Usage Guide, Privacy Policy, About App, Settings
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = strings.homeQuickLinksTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            QuickActionTile(
                                title = strings.usageGuide,
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                onClick = onOpenGuide,
                                testTag = "home_guide_button",
                                modifier = Modifier.weight(1f),
                            )
                            QuickActionTile(
                                title = strings.privacyPolicy,
                                icon = Icons.Outlined.PrivacyTip,
                                onClick = onOpenPrivacy,
                                testTag = "home_privacy_button",
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            QuickActionTile(
                                title = strings.aboutApp,
                                icon = Icons.Outlined.Info,
                                onClick = onOpenAbout,
                                testTag = "home_about_button",
                                modifier = Modifier.weight(1f),
                            )
                            QuickActionTile(
                                title = strings.settings,
                                icon = Icons.Outlined.Settings,
                                onClick = onOpenSettings,
                                testTag = "home_settings_button",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroPortalCard(
    title: String,
    subtitle: String,
    privacyLabel: String,
    onOpenSettings: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_portal_banner),
                contentDescription = "Online Khatian Maker portal banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC064E3B),
                                Color(0xEB043D2E),
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_khatian_logo),
                            contentDescription = "Online Khatian Maker Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_settings_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                        )
                    }
                }
                PrivacyRamBadge(label = privacyLabel)
            }
        }
    }
}

@Composable
private fun SurveyCategoryCard(
    survey: SurveyType,
    isBengali: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = surveyIcon(survey)
    val accentColor = surveyAccentColor(survey)
    val title = if (isBengali) survey.titleBn else survey.titleEn
    val era = if (isBengali) survey.eraLabelBn else survey.eraLabelEn
    val subtitle = if (isBengali) survey.subtitleBn else survey.subtitleEn
    val orientLabel = when {
        survey == SurveyType.CUSTOM -> "Custom"
        survey.isLandscapeNative -> "Landscape"
        else -> "Portrait"
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .height(172.dp)
            .testTag("survey_card_${survey.code.lowercase()}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.13f),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(22.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = orientLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = era,
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun surveyIcon(survey: SurveyType): ImageVector = when (survey) {
    SurveyType.CS -> Icons.Outlined.Gavel
    SurveyType.SA -> Icons.Outlined.Description
    SurveyType.RS -> Icons.Outlined.Verified
    SurveyType.BS -> Icons.Outlined.Architecture
    SurveyType.BRS -> Icons.Outlined.Map
    SurveyType.BDRS -> Icons.Outlined.Map
    SurveyType.CITY_SURVEY -> Icons.Outlined.LocationCity
    SurveyType.CUSTOM -> Icons.Outlined.Tune
}

private fun surveyAccentColor(survey: SurveyType): Color = when (survey) {
    SurveyType.CS -> Color(0xFF92400E)
    SurveyType.SA -> Color(0xFF1E3A5F)
    SurveyType.RS -> Color(0xFF0B6E4F)
    SurveyType.BS -> Color(0xFF047857)
    SurveyType.BRS -> Color(0xFF0F766E)
    SurveyType.BDRS -> Color(0xFF1D4ED8)
    SurveyType.CITY_SURVEY -> Color(0xFF6D28D9)
    SurveyType.CUSTOM -> Color(0xFFB45309)
}
