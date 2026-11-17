package com.example.model

import android.graphics.Bitmap
import com.example.util.LandUnit
import java.util.UUID

enum class SurveyType(
    val code: String,
    val titleBn: String,
    val titleEn: String,
    val eraLabelBn: String,
    val eraLabelEn: String,
    val subtitleBn: String,
    val subtitleEn: String,
    val isLandscapeNative: Boolean,
    val legalProvisionBn: String,
    val legalProvisionEn: String,
    val formRefBn: String,
) {
    CS(
        code = "CS",
        titleBn = "সি.এস. খতিয়ান",
        titleEn = "CS Khatian",
        eraLabelBn = "১৮৮৮–১৯৪০",
        eraLabelEn = "1888–1940",
        subtitleBn = "ক্যাডাস্ট্রাল সার্ভে (উপরিস্থ/অধীনস্থ স্বত্ব ও পরগনা ভিত্তিক)",
        subtitleEn = "Cadastral Survey (Superior/Subordinate Tenure & Pargana)",
        isLandscapeNative = false,
        legalProvisionBn = "বঙ্গীয় প্রজাস্বত্ব আইন, ১৮৮৫ (ধারা ১০১–১০৬)",
        legalProvisionEn = "Bengal Tenancy Act, 1885 (Sec. 101–106)",
        formRefBn = "ঐতিহ্যবাহী সি.এস. খতিয়ান ছক (খসড়া)",
    ),
    SA(
        code = "SA",
        titleBn = "এস.এ. খতিয়ান",
        titleEn = "SA Khatian",
        eraLabelBn = "১৯৫৬–১৯৬২",
        eraLabelEn = "1956–1962",
        subtitleBn = "স্টেট অ্যাকুইজিশন সার্ভে (জমিদারি উচ্ছেদ পরবর্তী রেকর্ড)",
        subtitleEn = "State Acquisition Survey (Post-Zamindari Record)",
        isLandscapeNative = false,
        legalProvisionBn = "রাষ্ট্রীয় অধিগ্রহণ ও প্রজাস্বত্ব আইন, ১৯৫০ (ধারা ৪৯–৫২)",
        legalProvisionEn = "State Acquisition & Tenancy Act, 1950 (Sec. 49–52)",
        formRefBn = "এস.এ. খতিয়ান নমুনা ছক (খসড়া)",
    ),
    RS(
        code = "RS",
        titleBn = "আর.এস. খতিয়ান",
        titleEn = "RS Khatian",
        eraLabelBn = "১৯৬৫–১৯৯৫",
        eraLabelEn = "1965–1995",
        subtitleBn = "রিভিশনাল সার্ভে (৯ কলাম বিশিষ্ট অনুভূমিক ছক)",
        subtitleEn = "Revisional Survey (9-Column Landscape Format)",
        isLandscapeNative = true,
        legalProvisionBn = "রাষ্ট্রীয় অধিগ্রহণ ও প্রজাস্বত্ব আইন, ১৯৫০ (ধারা ১৪৩–১৪৪)",
        legalProvisionEn = "State Acquisition & Tenancy Act, 1950 (Sec. 143–144)",
        formRefBn = "আর এস খতিয়ান নমুনা ছক (খসড়া)",
    ),
    BS(
        code = "BS",
        titleBn = "বি.এস. খতিয়ান",
        titleEn = "BS Khatian",
        eraLabelBn = "১৯৯০–বর্তমান",
        eraLabelEn = "1990–Present",
        subtitleBn = "বাংলাদেশ সার্ভে (আধুনিক কম্পিউটারাইজড অনুভূমিক ছক)",
        subtitleEn = "Bangladesh Survey (Modern Computerized Landscape Format)",
        isLandscapeNative = true,
        legalProvisionBn = "রাষ্ট্রীয় অধিগ্রহণ ও প্রজাস্বত্ব আইন, ১৯৫০ (ধারা ১৪৪)",
        legalProvisionEn = "State Acquisition & Tenancy Act, 1950 (Sec. 144)",
        formRefBn = "বি.এস. ডিজিটাল খতিয়ান নমুনা (খসড়া)",
    ),
    BRS(
        code = "BRS",
        titleBn = "বি.আর.এস. খতিয়ান",
        titleEn = "BRS Khatian",
        eraLabelBn = "সংশোধিত আধুনিক",
        eraLabelEn = "Modern Revisional",
        subtitleBn = "বাংলাদেশ রিভিশনাল সার্ভে (সাবেক আর.এস. রেফারেন্সসহ)",
        subtitleEn = "Bangladesh Revisional Survey (With Former RS Reference)",
        isLandscapeNative = true,
        legalProvisionBn = "ভূমি জরিপ ও রেকর্ড সংশোধন বিধিমালা অনুসারী",
        legalProvisionEn = "Bangladesh Revisional Survey Rules",
        formRefBn = "বি.আর.এস. খতিয়ান নমুনা ছক (খসড়া)",
    ),
    BDRS(
        code = "BDRS",
        titleBn = "বি.ডি.আর.এস. খতিয়ান",
        titleEn = "BDRS Khatian",
        eraLabelBn = "ডিজিটাল/দিয়ারা",
        eraLabelEn = "Digital/Diara",
        subtitleBn = "বাংলাদেশ ডিজিটাল/দিয়ারা রিভিশনাল সার্ভে (শিট ও সার্কেলসহ)",
        subtitleEn = "Bangladesh Digital/Diara Survey (With Sheet & Circle)",
        isLandscapeNative = true,
        legalProvisionBn = "ডিজিটাল ও দিয়ারা জরিপ রেকর্ড খসড়া কাঠামো",
        legalProvisionEn = "Digital & Diara Revisional Survey Structure",
        formRefBn = "বি.ডি.আর.এস. খতিয়ান নমুনা (খসড়া)",
    ),
    CITY_SURVEY(
        code = "CITY",
        titleBn = "City Survey",
        titleEn = "City Survey",
        eraLabelBn = "মহানগর জরিপ",
        eraLabelEn = "Metropolitan",
        subtitleBn = "মহানগর জরিপ খতিয়ান (ওয়ার্ড, রোড, হোল্ডিং ও শিট নম্বরসহ)",
        subtitleEn = "Metropolitan City Survey (Ward, Road, Holding & Sheet)",
        isLandscapeNative = true,
        legalProvisionBn = "মহানগর সিটি জরিপ খতিয়ান কাঠামো (ঢাকা/চট্টগ্রাম)",
        legalProvisionEn = "Metropolitan City Survey Khatian Format",
        formRefBn = "সিটি জরিপ খতিয়ান নমুনা (খসড়া)",
    ),
    CUSTOM(
        code = "CUSTOM",
        titleBn = "অন্যান্য খতিয়ান",
        titleEn = "Other / Custom",
        eraLabelBn = "Custom Template",
        eraLabelEn = "Custom Template",
        subtitleBn = "নমুনা ছবি দেখে নিজে কলাম ও ফিল্ড সাজিয়ে কাস্টম টেমপ্লেট তৈরি করুন",
        subtitleEn = "Build a Custom Template with reference scan & custom columns",
        isLandscapeNative = true,
        legalProvisionBn = "ব্যবহারকারীর নির্ধারিত Custom Template (সরকারি ফরম নয়)",
        legalProvisionEn = "User-Defined Custom Template (Not an Official Form)",
        formRefBn = "Custom Template — ব্যবহারকারীর তৈরি খসড়া",
    ),
}

enum class OwnerCategory(val labelBn: String, val labelEn: String) {
    MUL_MALIK("মূল মালিক", "Original Owner"),
    WARISH("ওয়ারিশ", "Legal Heir (Warish)"),
    KRETA("ক্রেতা", "Purchaser"),
    ONGSHIDAR("অংশীদার", "Co-sharer"),
    OTHERS("অন্যান্য", "Others"),
}

enum class AppLanguage(val labelBn: String, val labelEn: String) {
    BENGALI("বাংলা", "Bengali"),
    ENGLISH("ইংরেজি (English)", "English"),
}

enum class PageSizeOption(val labelBn: String, val labelEn: String, val widthPt: Int, val heightPt: Int) {
    NATIVE("জরিপের মূল সাইজ", "Survey Native Size", 595, 842),
    A4("A4 (আন্তর্জাতিক মান)", "A4 Standard", 595, 842),
    LEGAL("Legal (সরকারি লিগ্যাল)", "Legal Paper", 612, 1008),
}

enum class PdfQuality(val labelBn: String, val labelEn: String, val previewDpi: Int) {
    STANDARD("স্ট্যান্ডার্ড (১৫০ DPI)", "Standard (150 DPI)", 150),
    HIGH("হাই (৩০০ DPI)", "High (300 DPI)", 300),
    ULTRA_600("৬০০ DPI (আল্ট্রা এইচডি)", "600 DPI (Ultra HD)", 600),
}

enum class PrintOrientationPref(val labelBn: String, val labelEn: String) {
    AUTO("স্বয়ংক্রিয় (জরিপ অনুযায়ী)", "Auto (By Survey)"),
    PORTRAIT("উল্লম্ব (Portrait)", "Portrait"),
    LANDSCAPE("অনুভূমিক (Landscape)", "Landscape"),
}

data class BasicInfo(
    val division: String = "",
    val district: String = "",
    val upazila: String = "",
    val thana: String = "",
    val unionName: String = "",
    val mouzaName: String = "",
    val jlNumber: String = "",
    val khatianNumber: String = "",
    val surveyName: String = "",
    val pageNumber: String = "১",
    val surveySpecificValues: Map<String, String> = emptyMap(),
)

data class OwnerEntry(
    val id: String = UUID.randomUUID().toString(),
    val category: OwnerCategory = OwnerCategory.MUL_MALIK,
    val name: String = "",
    val fatherOrHusbandName: String = "",
    val address: String = "",
    val ownershipDetails: String = "",
    val hissa: String = "",
    val landAmount: String = "",
    val landUnit: LandUnit = LandUnit.SHOTOK,
    val manualAreaOverride: Boolean = false,
)

data class DagEntry(
    val id: String = UUID.randomUUID().toString(),
    val dagNumber: String = "",
    val landClass: String = "",
    val isNonAgricultural: Boolean = false,
    val totalDagArea: String = "",
    val ownerShare: String = "",
    val entitledArea: String = "",
    val landUnit: LandUnit = LandUnit.SHOTOK,
    val manualCalculationDisabled: Boolean = false,
    val remarks: String = "",
)

data class ExtraSurveyInfo(
    val revenueAmount: String = "",
    val cessAmount: String = "",
    val boundaryNorth: String = "",
    val boundarySouth: String = "",
    val boundaryEast: String = "",
    val boundaryWest: String = "",
    val specialNotes: String = "",
    val referenceScanBitmap: Bitmap? = null,
    val referenceScanLabel: String = "",
)

data class CustomTemplateConfig(
    val customSurveyTitle: String = "কাস্টম খতিয়ান খসড়া (Custom Template)",
    val useLandscape: Boolean = true,
    val showSuperiorTenureColumn: Boolean = true,
    val showRevenueColumn: Boolean = true,
    val showFormerRefColumn: Boolean = false,
    val showLandClassColumn: Boolean = true,
    val showRemarksColumn: Boolean = true,
    val showBoundaryFooter: Boolean = false,
    val ownerColumnHeader: String = "মালিকের নাম, পিতা/স্বামী ও ঠিকানা",
    val hissaColumnHeader: String = "হিস্যা / অংশ",
    val dagColumnHeader: String = "দাগ নম্বর",
    val areaColumnHeader: String = "জমির পরিমাণ",
)

data class SessionSettings(
    val language: AppLanguage = AppLanguage.BENGALI,
    val useBengaliNumerals: Boolean = true,
    val defaultPageSize: PageSizeOption = PageSizeOption.NATIVE,
    val pdfQuality: PdfQuality = PdfQuality.ULTRA_600,
    val printOrientationPref: PrintOrientationPref = PrintOrientationPref.AUTO,
    val printInkSaverGrayscale: Boolean = true,
)

data class ValidationSummary(
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalWarnings: List<String> = emptyList(),
) {
    val hasBlockingErrors: Boolean get() = fieldErrors.isNotEmpty()
}

data class KhatianDraftState(
    val surveyType: SurveyType = SurveyType.RS,
    val basicInfo: BasicInfo = BasicInfo(surveyName = SurveyType.RS.titleBn),
    val owners: List<OwnerEntry> = emptyList(),
    val dags: List<DagEntry> = emptyList(),
    val extraInfo: ExtraSurveyInfo = ExtraSurveyInfo(),
    val customConfig: CustomTemplateConfig = CustomTemplateConfig(),
    val autoCalculateLand: Boolean = true,
    val displayUnit: LandUnit = LandUnit.SHOTOK,
    val validation: ValidationSummary = ValidationSummary(),
)
