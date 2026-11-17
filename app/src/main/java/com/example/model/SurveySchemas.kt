package com.example.model

data class SurveyExtraFieldDef(
    val key: String,
    val labelBn: String,
    val labelEn: String,
    val placeholderBn: String,
    val placeholderEn: String,
    val isMultiline: Boolean = false,
)

data class SurveySchema(
    val surveyType: SurveyType,
    val exclusiveBasicFields: List<SurveyExtraFieldDef>,
    val pdfColumnSummaryBn: List<String>,
    val commonLandClassesBn: List<String>,
)

object SurveySchemas {
    private val commonClasses = listOf(
        "নাল", "ভিটা", "বাস্তু", "বাগান", "পুকুর", "ডোবা", "দালান", "চালা", "চর", "পতিত", "বাণিজ্যিক"
    )

    fun schemaFor(type: SurveyType): SurveySchema = when (type) {
        SurveyType.CS -> SurveySchema(
            surveyType = SurveyType.CS,
            exclusiveBasicFields = listOf(
                SurveyExtraFieldDef("pargana", "পরগণা", "Pargana", "পরগণার নাম", "Pargana name"),
                SurveyExtraFieldDef("resa_no", "রেঃ সাঃ নং", "Re-Sa No.", "যেমন: ১২", "e.g., 12"),
                SurveyExtraFieldDef("touzi_no", "তৌজি নং", "Touzi No.", "যেমন: ১৪২", "e.g., 142"),
                SurveyExtraFieldDef("superior_khatian_occupant", "উপরিস্থ স্বত্বের দখলকার সংক্ষিপ্ত", "Superior Tenure Occupant", "যেমন: ২৭৫ চন্দ্রহরি বেপারী গং", "e.g., 275 Chandrahori Bepari Gang", isMultiline = true),
                SurveyExtraFieldDef("superior_mutual_share", "উপরিস্থ স্বত্বের পরস্পর অংশ", "Superior Mutual Share", "যেমন: ১১", "e.g., 11"),
                SurveyExtraFieldDef("own_dey_khajana", "অত্র স্বত্বের দেয় — খাজানা", "Own Payable — Khajana (Rent)", "খাজানার পরিমাণ", "Rent amount"),
                SurveyExtraFieldDef("own_dey_cess", "অত্র স্বত্বের দেয় — সেস্", "Own Payable — Cess", "সেস্ পরিমাণ", "Cess amount"),
                SurveyExtraFieldDef("upper_remarks", "উপরিস্থ স্বত্ব মন্তব্য", "Upper Table Remarks", "যেমন: সামিল খাজানা ২৭০ নং খতিয়ান, মোট খাজানা ১৪৬/৬", "e.g., Included Khajana 270", isMultiline = true),
                SurveyExtraFieldDef("dharamote_son_khajana", "ধারামতে ও কোন সন হইতে — খাজানা", "By Section & Year — Khajana", "খাজানা", "Khajana"),
                SurveyExtraFieldDef("dharamote_son_cess", "ধারামতে ও কোন সন হইতে — সেস্", "By Section & Year — Cess", "সেস্", "Cess"),
                SurveyExtraFieldDef("second_occupant_details", "২য় কলাম: অত্র স্বত্বের বিবরণ ও দখলকার", "2nd Col: Own Tenure & Occupant", "দ্বিতীয় কলামের বিবরণ (যদি থাকে)", "Optional 2nd column details", isMultiline = true),
                SurveyExtraFieldDef("second_occupant_share", "২য় কলাম: অংশ", "2nd Col: Share", "অংশ", "Share"),
                SurveyExtraFieldDef("tenure_class_rules", "অত্র স্বত্বের শ্রেণী এবং বিশেষ নিয়ম ও অনুসঙ্গ", "Tenure Class & Special Conditions", "যেমন: রায়ত / স্থিতিবান", "e.g., Raiyat / Sthitiban", isMultiline = true),
                SurveyExtraFieldDef("case_change_note", "ধারা মতে পরিবর্তন ও মোকদ্দমা নম্বর", "Section Change & Case No.", "পরিবর্তন ও মোকদ্দমা নম্বর", "Mutation / Case No.", isMultiline = true),
            ),
            pdfColumnSummaryBn = listOf(
                "উপরিস্থ স্বত্বের (দখলকার সংক্ষিপ্ত | পরস্পর অংশ)",
                "অত্র স্বত্বের দেয় (খাজানা | সেস্)",
                "মন্তব্য",
                "ধারামতে ও কোন সন হইতে (খাজানা | সেস্)",
                "অত্র স্বত্বের বিবরণ ও দখলকার | অংশ (২ কলাম) | শ্রেণী ও অনুসঙ্গ",
                "ধারা মতে পরিবর্তন ও মোকদ্দমা নম্বর",
            ),
            commonLandClassesBn = commonClasses,
        )

        SurveyType.SA -> SurveySchema(
            surveyType = SurveyType.SA,
            exclusiveBasicFields = listOf(
                SurveyExtraFieldDef("pargana", "পরগণা", "Pargana", "পরগণার নাম", "Pargana name"),
                SurveyExtraFieldDef("resa_no", "রেঃ সাঃ নং", "Re-Sa No.", "যেমন: ৪৫", "e.g., 45"),
                SurveyExtraFieldDef("touzi_no", "তৌজি নং", "Touzi No.", "যেমন: ৩১৯", "e.g., 319"),
                SurveyExtraFieldDef("superior_khatian_batta", "উপরিস্থ স্বত্বের খতিয়ান নং মায় বাটা", "Superior Khatian No. (May Batta)", "যেমন: ৮৯", "e.g., 89"),
                SurveyExtraFieldDef("superior_khatian_occupant", "উপরিস্থ স্বত্বের বিবরণ ও দখলকার [সংক্ষিপ্ত]", "Superior Details & Occupant [Brief]", "যেমন: সোনা ভান বেওয়া", "e.g., Sona Bhan Bewa", isMultiline = true),
                SurveyExtraFieldDef("superior_mutual_share", "উপরিস্থ স্বত্বের পরস্পর অংশ", "Superior Mutual Share", "যেমন: ১১", "e.g., 11"),
                SurveyExtraFieldDef("own_dey_khajana", "অত্র স্বত্বের দেয় — খাজানা", "Own Payable — Khajana", "যেমন: ৬।।০", "e.g., 6//0"),
                SurveyExtraFieldDef("own_dey_cess", "অত্র স্বত্বের দেয় — সেস", "Own Payable — Cess", "যেমন: ১/০", "e.g., 1/0"),
                SurveyExtraFieldDef("own_dey_edu_cess", "অত্র স্বত্বের দেয় — শিক্ষা সেস", "Own Payable — Education Cess", "যেমন: ১/৯", "e.g., 1/9"),
                SurveyExtraFieldDef("upper_remarks", "উপরিস্থ স্বত্ব মন্তব্য", "Upper Table Remarks", "মন্তব্য লিখুন", "Remarks if any", isMultiline = true),
                SurveyExtraFieldDef("dharamote_date_khajana", "ধারামতে ও কোন তারিখ হইতে — খাজানা", "By Section & Date — Khajana", "খাজানা", "Khajana"),
                SurveyExtraFieldDef("dharamote_date_cess", "ধারামতে ও কোন তারিখ হইতে — সেস্", "By Section & Date — Cess", "সেস্", "Cess"),
                SurveyExtraFieldDef("second_occupant_details", "২য় কলাম: অত্র স্বত্বের বিবরণ ও দখলকার", "2nd Col: Own Tenure & Occupant", "দ্বিতীয় কলামের বিবরণ (যদি থাকে)", "Optional 2nd column details", isMultiline = true),
                SurveyExtraFieldDef("second_occupant_share", "২য় কলাম: অংশ", "2nd Col: Share", "অংশ", "Share"),
                SurveyExtraFieldDef("tenure_class_rules", "অত্র স্বত্বের শ্রেণী (এবং বিশেষ নিয়ম ও অনুসঙ্গ)", "Tenure Class (Special Rules)", "যেমন: কোর্ফা", "e.g., Korfa", isMultiline = true),
                SurveyExtraFieldDef("case_change_note", "ধারামতে নোট বা পরিবর্তন [মায় মোকদ্দমা নং এবং সন]", "Section Note / Change [Case & Year]", "নোট বা মোকদ্দমা নং ও সন", "Case No. & Year", isMultiline = true),
            ),
            pdfColumnSummaryBn = listOf(
                "উপরিস্থ স্বত্বের (খতিয়ান নং মায় বাটা | বিবরণ ও দখলকার | পরস্পর অংশ)",
                "অত্র স্বত্বের দেয় (খাজানা | সেস | শিক্ষা সেস)",
                "মন্তব্য",
                "ধারামতে ও কোন তারিখ হইতে (খাজানা | সেস্)",
                "অত্র স্বত্বের বিবরণ ও দখলকার | অংশ (২ কলাম) | শ্রেণী (কোর্ফা ইত্যাদি)",
                "ধারামতে নোট বা পরিবর্তন [মায় মোকদ্দমা নং এবং সন]",
            ),
            commonLandClassesBn = commonClasses,
        )

        SurveyType.RS -> SurveySchema(
            surveyType = SurveyType.RS,
            exclusiveBasicFields = listOf(
                SurveyExtraFieldDef("resa_no", "রেঃ সাঃ নং", "Re-Sa No.", "যেমন: ১২৮", "e.g., 128"),
                SurveyExtraFieldDef("case_change_note", "ধারামতে নোট বা পরিবর্তন মায় মোকদ্দমা নং এবং সন", "Section Note / Change with Case & Year", "যদি থাকে লিখুন", "Optional legal note", isMultiline = true),
            ),
            pdfColumnSummaryBn = listOf(
                "১. মালিক, অকৃষি প্রজা বা ইজারাদারের নাম ও ঠিকানা",
                "২. অংশ",
                "৩. রাজস্ব",
                "৪. দাগ নং",
                "৫. জমির শ্রেণী [কৃষি ৫(ক) | অকৃষি ৫(খ)]",
                "৬. দাগের মোট পরিমাণ [একর ৬(ক) | শতাংশ ৬(খ)]",
                "৭. দাগের মধ্যে অত্র খতিয়ানের অংশ",
                "৮. অংশানুযায়ী জমির পরিমাণ [একর ৮(ক) | শতাংশ ৮(খ)]",
                "৯. দখল বিষয়ক বা অন্যান্য বিশেষ মন্তব্য",
            ),
            commonLandClassesBn = listOf("দলা", "বাড়ী", "নাল", "ভিটা", "বাগান", "পুকুর", "ডোবা", "দালান", "পতিত"),
        )

        SurveyType.BS -> SurveySchema(
            surveyType = SurveyType.BS,
            exclusiveBasicFields = listOf(
                SurveyExtraFieldDef("resa_no", "রে.সা. নম্বর", "Re-Sa No.", "যেমন: ৬১২", "e.g., 612"),
                SurveyExtraFieldDef("land_office", "ইউনিয়ন ভূমি অফিস", "Union Land Office", "সংশ্লিষ্ট ভূমি অফিস", "Concerned land office"),
                SurveyExtraFieldDef("survey_year", "জরিপের সন", "Survey Year", "যেমন: ২০১৪", "e.g., 2014"),
                SurveyExtraFieldDef("former_rs_ref", "সাবেক আর.এস. খতিয়ান (যদি থাকে)", "Former RS Ref (Optional)", "রেফারেন্স নং", "Reference No."),
            ),
            pdfColumnSummaryBn = listOf(
                "মালিকের নাম, পিতা/স্বামী ও ঠিকানা",
                "অংশ (হিস্যা)",
                "রাজস্ব (টাকা)",
                "দাগ নং",
                "জমির শ্রেণি",
                "দাগের মোট জমি (একর | শতাংশ)",
                "অংশানুযায়ী জমি (একর | শতাংশ)",
                "মন্তব্য ও মোট সারসংক্ষেপ",
            ),
            commonLandClassesBn = commonClasses,
        )

        SurveyType.BRS -> SurveySchema(
            surveyType = SurveyType.BRS,
            exclusiveBasicFields = listOf(
                SurveyExtraFieldDef("resa_no", "রে.সা. নং", "Re-Sa No.", "যেমন: ৪১২", "e.g., 412"),
                SurveyExtraFieldDef("prev_rs_khatian", "পূর্ববর্তী আর.এস. খতিয়ান নং", "Previous RS Khatian No.", "যেমন: ৮৮", "e.g., 88"),
                SurveyExtraFieldDef("prev_dag_ref", "পূর্ববর্তী দাগ নম্বর", "Previous Dag Reference", "যেমন: ৪০১, ৪০২", "e.g., 401, 402"),
                SurveyExtraFieldDef("revision_section", "সংশোধনী ধারা / আদেশ নং", "Revision Section / Order", "যেমন: ধারা ১৪৩/৩০", "e.g., Sec 143/30"),
            ),
            pdfColumnSummaryBn = listOf(
                "মালিকের বিবরণ ও ক্যাটাগরি",
                "হিস্যা (অংশ)",
                "রাজস্ব",
                "হাল দাগ (সাবেক রেফারেন্সসহ)",
                "জমির শ্রেণি",
                "দাগের মোট পরিমাণ",
                "অংশানুযায়ী প্রাপ্য জমি",
                "সংশোধনী মন্তব্য",
            ),
            commonLandClassesBn = commonClasses,
        )

        SurveyType.BDRS -> SurveySchema(
            surveyType = SurveyType.BDRS,
            exclusiveBasicFields = listOf(
                SurveyExtraFieldDef("diara_circle", "দিয়ারা / জরিপ অঞ্চল", "Diara / Survey Zone", "যেমন: ঢাকা দিয়ারা সেটেলমেন্ট", "e.g., Diara Settlement Zone"),
                SurveyExtraFieldDef("sheet_no", "মৌজা শিট নম্বর", "Mouza Sheet No.", "যেমন: শিট-২", "e.g., Sheet-2"),
                SurveyExtraFieldDef("diara_status", "দিয়ারা / চর অবস্থান বিবরণ", "Diara / Alluvion Notes", "নদী তীরবর্তী বা চর বিবরণ", "Alluvion / Diluvion details"),
            ),
            pdfColumnSummaryBn = listOf(
                "জরিপ অঞ্চল ও শিট নং",
                "মালিক/দখলদারের বিবরণ",
                "হিস্যা",
                "রাজস্ব",
                "দাগ ও শ্রেণি",
                "মোট ও প্রাপ্য জমি",
                "দিয়ারা ও দখল মন্তব্য",
            ),
            commonLandClassesBn = listOf("নাল", "চর", "বালুচর", "ভিটা", "পুকুর", "বাস্তু", "পতিত"),
        )

        SurveyType.CITY_SURVEY -> SurveySchema(
            surveyType = SurveyType.CITY_SURVEY,
            exclusiveBasicFields = listOf(
                SurveyExtraFieldDef("city_corp", "সিটি কর্পোরেশন / পৌরসভা", "City Corporation", "যেমন: ঢাকা উত্তর সিটি কর্পোরেশন", "e.g., Dhaka North City Corp."),
                SurveyExtraFieldDef("ward_no", "ওয়ার্ড নম্বর", "Ward No.", "যেমন: ওয়ার্ড-১৯", "e.g., Ward-19"),
                SurveyExtraFieldDef("road_mohalla", "রোড / মহল্লা / ব্লক", "Road / Mohalla / Block", "যেমন: রোড-৭, বনানী", "e.g., Road-7, Banani"),
                SurveyExtraFieldDef("holding_no", "হোল্ডিং / বাসা নম্বর", "Holding No.", "যেমন: ২৩/এ", "e.g., 23/A"),
                SurveyExtraFieldDef("sheet_no", "সিটি জরিপ শিট নং", "City Survey Sheet No.", "যেমন: শিট-৪ক", "e.g., Sheet-4A"),
            ),
            pdfColumnSummaryBn = listOf(
                "সিটি কর্পোরেশন, ওয়ার্ড ও হোল্ডিং",
                "মালিক/স্বত্বভোগীর বিবরণ",
                "হিস্যা",
                "ভূমি উন্নয়ন কর",
                "প্লট / দাগ নং ও শ্রেণি",
                "দাগের মোট ও প্রাপ্য জমি",
                "মন্তব্য",
            ),
            commonLandClassesBn = listOf("দালান", "ভিটা", "বাস্তু", "বাণিজ্যিক", "আবাসিক প্লট", "রাস্তা", "পুকুর", "খালি জমি"),
        )

        SurveyType.CUSTOM -> SurveySchema(
            surveyType = SurveyType.CUSTOM,
            exclusiveBasicFields = listOf(
                SurveyExtraFieldDef("custom_ref_code", "কাস্টম ফরম রেফারেন্স", "Custom Form Ref", "ব্যবহারকারীর নিজের রেফারেন্স", "User reference label"),
                SurveyExtraFieldDef("custom_office", "অফিস / এলাকা বিবরণ", "Office / Area Note", "ঐচ্ছিক এলাকা তথ্য", "Optional office note"),
            ),
            pdfColumnSummaryBn = listOf(
                "Custom Template (ব্যবহারকারীর নির্ধারিত কলামসমূহ)",
            ),
            commonLandClassesBn = commonClasses,
        )
    }
}
