package com.example.util

data class BangladeshDistrict(
    val nameBn: String,
    val nameEn: String,
    val upazilas: List<String>,
)

data class BangladeshDivision(
    val nameBn: String,
    val nameEn: String,
    val districts: List<BangladeshDistrict>,
)

data class MouzaJlEntry(
    val mouza: String,
    val district: String,
    val upazila: String,
    val historicalDistricts: List<String> = emptyList(),
    val csJl: String = "",
    val saJl: String = "",
    val rsJl: String = "",
    val bsJl: String = "",
    val parganaHint: String = "",
    val sourceNote: String = "",
) {
    val jl: String
        get() = csJl.ifBlank { saJl.ifBlank { rsJl.ifBlank { bsJl } } }

    fun jlForSurvey(surveyType: com.example.model.SurveyType): String = when (surveyType) {
        com.example.model.SurveyType.CS -> csJl.ifBlank { saJl.ifBlank { rsJl.ifBlank { bsJl } } }
        com.example.model.SurveyType.SA -> saJl.ifBlank { csJl.ifBlank { rsJl.ifBlank { bsJl } } }
        com.example.model.SurveyType.RS -> rsJl.ifBlank { bsJl.ifBlank { saJl.ifBlank { csJl } } }
        com.example.model.SurveyType.BS,
        com.example.model.SurveyType.BRS,
        com.example.model.SurveyType.BDRS,
        com.example.model.SurveyType.CITY_SURVEY,
        com.example.model.SurveyType.CUSTOM -> bsJl.ifBlank { rsJl.ifBlank { saJl.ifBlank { csJl } } }
    }

    fun displayLabelForSurvey(surveyType: com.example.model.SurveyType): String {
        val code = jlForSurvey(surveyType)
        return if (code.isNotBlank()) "$code - $mouza" else mouza
    }

    val displayLabel: String
        get() = if (jl.isNotBlank()) "$jl - $mouza" else mouza

    val locationSubtitle: String
        get() = listOf(upazila, district).filter { it.isNotBlank() }.joinToString(", ")

    val surveyJlSummary: String
        get() = buildList {
            if (csJl.isNotBlank()) add("CS: $csJl")
            if (saJl.isNotBlank()) add("SA: $saJl")
            if (rsJl.isNotBlank()) add("RS: $rsJl")
            if (bsJl.isNotBlank()) add("BS: $bsJl")
        }.joinToString(" | ")
}

data class ParganaEntry(
    val name: String,
    val division: String,
    val districts: List<String>,
    val upazilas: List<String> = emptyList(),
    val mouzas: List<String> = emptyList(),
)

object BangladeshGeoData {

    val divisions: List<BangladeshDivision> = listOf(
        BangladeshDivision(
            nameBn = "ঢাকা",
            nameEn = "Dhaka",
            districts = listOf(
                BangladeshDistrict("ঢাকা", "Dhaka", listOf("ধানমন্ডি", "গুলশান", "মিরপুর", "মোহাম্মদপুর", "উত্তরা", "মতিঝিল", "রমনা", "তেজগাঁও", "লালবাগ", "কোতোয়ালী", "বাড্ডা", "সাভার", "ধামরাই", "কেরানীগঞ্জ", "দোহার", "নবাবগঞ্জ")),
                BangladeshDistrict("গাজীপুর", "Gazipur", listOf("গাজীপুর সদর", "কালিয়াকৈর", "কালীগঞ্জ", "কাপাসিয়া", "শ্রীপুর", "টঙ্গী")),
                BangladeshDistrict("নারায়ণগঞ্জ", "Narayanganj", listOf("নারায়ণগঞ্জ সদর", "আড়াইহাজার", "বন্দর", "রূপগঞ্জ", "সোনারগাঁও")),
                BangladeshDistrict("নরসিংদী", "Narsingdi", listOf("নরসিংদী সদর", "বেলাবো", "মনোহরদী", "পলাশ", "রায়পুরা", "শিবপুর")),
                BangladeshDistrict("মুন্সিগঞ্জ", "Munshiganj", listOf("মুন্সিগঞ্জ সদর", "গজারিয়া", "লৌহজং", "সিরাজদিখান", "শ্রীনগর", "টঙ্গীবাড়ী")),
                BangladeshDistrict("মানিকগঞ্জ", "Manikganj", listOf("মানিকগঞ্জ সদর", "দৌলতপুর", "ঘিওর", "হরিরামপুর", "সাটুরিয়া", "শিবালয়", "সিংগাইর")),
                BangladeshDistrict("টাঙ্গাইল", "Tangail", listOf("টাঙ্গাইল সদর", "বাসাইল", "ভুয়াপুর", "দেলদুয়ার", "ধনবাড়ী", "ঘাটাইল", "গোপালপুর", "কালিহাতী", "মধুপুর", "মির্জাপুর", "নাগরপুর", "সখিপুর")),
                BangladeshDistrict("কিশোরগঞ্জ", "Kishoreganj", listOf("কিশোরগঞ্জ সদর", "অষ্টগ্রাম", "বাজিতপুর", "ভৈরব", "হোসেনপুর", "ইটনা", "করিমগঞ্জ", "কটিয়াদী", "কুলিয়ারচর", "মিঠামইন", "নিকলী", "পাকুন্দিয়া", "তাড়াইল")),
                BangladeshDistrict("ফরিদপুর", "Faridpur", listOf("ফরিদপুর সদর", "আলফাডাঙ্গা", "ভাঙ্গা", "বোয়ালমারী", "চরভদ্রাসন", "মধুখালী", "নগরকান্দা", "সদরপুর", "সালথা")),
                BangladeshDistrict("গোপালগঞ্জ", "Gopalganj", listOf("গোপালগঞ্জ সদর", "কাশিয়ানী", "কোটালীপাড়া", "মুকসুদপুর", "টুঙ্গিপাড়া")),
                BangladeshDistrict("মাদারীপুর", "Madaripur", listOf("মাদারীপুর সদর", "কালকিনি", "রাজৈর", "শিবচর", "ডাসার")),
                BangladeshDistrict("রাজবাড়ী", "Rajbari", listOf("রাজবাড়ী সদর", "বালিয়াকান্দি", "গোয়ালন্দ", "কালুখালী", "পাংশা")),
                BangladeshDistrict("শরীয়তপুর", "Shariatpur", listOf("শরীয়তপুর সদর", "ভেদরগঞ্জ", "ডামুড্যা", "গোসাইরহাট", "নড়িয়া", "জাজিরা")),
            ),
        ),
        BangladeshDivision(
            nameBn = "রংপুর",
            nameEn = "Rangpur",
            districts = listOf(
                BangladeshDistrict("রংপুর", "Rangpur", listOf("রংপুর সদর", "বদরগঞ্জ", "গঙ্গাচড়া", "কাউনিয়া", "মিঠাপুকুর", "পীরগাছা", "পীরগঞ্জ", "তারাগঞ্জ")),
                BangladeshDistrict("কুড়িগ্রাম", "Kurigram", listOf("কুড়িগ্রাম সদর", "ভুরুঙ্গামারী", "নাগেশ্বরী", "ফুলবাড়ী", "রাজারহাট", "উলিপুর", "চিলমারী", "রৌমারী", "চর রাজিবপুর")),
                BangladeshDistrict("লালমনিরহাট", "Lalmonirhat", listOf("লালমনিরহাট সদর", "আদিতমারী", "হাতীবান্ধা", "কালীগঞ্জ", "পাটগ্রাম")),
                BangladeshDistrict("গাইবান্ধা", "Gaibandha", listOf("গাইবান্ধা সদর", "ফুলছড়ি", "গোবিন্দগঞ্জ", "পলাশবাড়ী", "সাদুল্লাপুর", "সাঘাটা", "সুন্দরগঞ্জ")),
                BangladeshDistrict("নীলফামারী", "Nilphamari", listOf("নীলফামারী সদর", "ডিমলা", "ডোমার", "জলঢাকা", "কিশোরগঞ্জ", "সৈয়দপুর")),
                BangladeshDistrict("দিনাজপুর", "Dinajpur", listOf("দিনাজপুর সদর", "বিরামপুর", "বীরগঞ্জ", "বিড়াল", "বোচাগঞ্জ", "চিরিরবন্দর", "ফুলবাড়ী", "ঘোড়াঘাট", "হাকিমপুর", "কাহারোল", "খানসামা", "নবাবগঞ্জ", "পার্বতীপুর")),
                BangladeshDistrict("ঠাকুরগাঁও", "Thakurgaon", listOf("ঠাকুরগাঁও সদর", "বালিয়াডাঙ্গী", "হরিপুর", "পীরগঞ্জ", "রাণীশংকৈল")),
                BangladeshDistrict("পঞ্চগড়", "Panchagarh", listOf("পঞ্চগড় সদর", "আটোয়ারী", "বোদা", "দেবীগঞ্জ", "তেঁতুলিয়া")),
            ),
        ),
        BangladeshDivision(
            nameBn = "চট্টগ্রাম",
            nameEn = "Chattogram",
            districts = listOf(
                BangladeshDistrict("চট্টগ্রাম", "Chattogram", listOf("কোতোয়ালী", "পাঁচলাইশ", "ডবলমুরিং", "হালিশহর", "পতেঙ্গা", "আনোয়ারা", "বাঁশখালী", "বোয়ালখালী", "চন্দনাইশ", "ফটিকছড়ি", "হাটহাজারী", "কর্ণফুলী", "লোহাগাড়া", "মীরসরাই", "পটিয়া", "রাঙ্গুনিয়া", "রাউজান", "সন্দ্বীপ", "সাতকানিয়া", "সীতাকুণ্ড")),
                BangladeshDistrict("কক্সবাজার", "Cox's Bazar", listOf("কক্সবাজার সদর", "চকরিয়া", "ঈদগাঁও", "কুতুবদিয়া", "মহেশখালী", "পেকুয়া", "রামু", "টেকনাফ", "উখিয়া")),
                BangladeshDistrict("কুমিল্লা", "Cumilla", listOf("কুমিল্লা আদর্শ সদর", "সদর দক্ষিণ", "বরুড়া", "ব্রাহ্মণপাড়া", "বুড়িচং", "চান্দিনা", "চৌদ্দগ্রাম", "দাউদকান্দি", "দেবিদ্বার", "হোমনা", "লাকসাম", "লালমাই", "মেঘনা", "মনোহরগঞ্জ", "মুরাদনগর", "নাঙ্গলকোট", "তিতাস")),
                BangladeshDistrict("ব্রাহ্মণবাড়িয়া", "Brahmanbaria", listOf("ব্রাহ্মণবাড়িয়া সদর", "আখাউড়া", "আশুগঞ্জ", "বাঞ্ছারামপুর", "বিজয়নগর", "কসবা", "নবীনগর", "নাসিরনগর", "সরাইল")),
                BangladeshDistrict("চাঁদপুর", "Chandpur", listOf("চাঁদপুর সদর", "ফরিদগঞ্জ", "হাইমচর", "হাজীগঞ্জ", "কচুয়া", "মতলব উত্তর", "মতলব দক্ষিণ", "শাহরাস্তি")),
                BangladeshDistrict("নোয়াখালী", "Noakhali", listOf("নোয়াখালী সদর", "বেগমগঞ্জ", "চাটখিল", "কোম্পানীগঞ্জ", "হাতিয়া", "কবিরহাট", "সেনবাগ", "সোনাইমুড়ী", "সুবর্ণচর")),
                BangladeshDistrict("ফেনী", "Feni", listOf("ফেনী সদর", "ছাগলনাইয়া", "দাগনভূঞা", "ফুলগাজী", "পরশুরাম", "সোনাগাজী")),
                BangladeshDistrict("লক্ষ্মীপুর", "Lakshmipur", listOf("লক্ষ্মীপুর সদর", "কমলনগর", "রায়পুর", "রামগঞ্জ", "রামগতি")),
                BangladeshDistrict("রাঙ্গামাটি", "Rangamati", listOf("রাঙ্গামাটি সদর", "বাঘাইছড়ি", "বরকল", "বেলাইছড়ি", "জুরাছড়ি", "কাপ্তাই", "কাউখালী", "লংগদু", "নানিয়ারচর", "রাজস্থলী")),
                BangladeshDistrict("বান্দরবান", "Bandarban", listOf("বান্দরবান সদর", "আলীকদম", "লামা", "নাইক্ষ্যংছড়ি", "রোয়াংছড়ি", "রুমা", "থানচি")),
                BangladeshDistrict("খাগড়াছড়ি", "Khagrachhari", listOf("খাগড়াছড়ি সদর", "দীঘিনালা", "গুইমারা", "লক্ষ্মীছড়ি", "মহালছড়ি", "মানিকছড়ি", "মাটিরাঙ্গা", "পানছড়ি", "রামগড়")),
            ),
        ),
        BangladeshDivision(
            nameBn = "রাজশাহী",
            nameEn = "Rajshahi",
            districts = listOf(
                BangladeshDistrict("রাজশাহী", "Rajshahi", listOf("বোয়ালিয়া", "রাজপাড়া", "মতিহার", "শাহমখদুম", "বাঘা", "বাগমারা", "চারঘাট", "দুর্গাপুর", "গোদাগাড়ী", "মোহনপুর", "পবা", "পুঠিয়া", "তানোর")),
                BangladeshDistrict("বগুড়া", "Bogura", listOf("বগুড়া সদর", "আদমদীঘি", "ধুনট", "দুপচাঁচিয়া", "গাবতলী", "কাহালু", "নন্দীগ্রাম", "সারিয়াকান্দি", "শাজাহানপুর", "শেরপুর", "শিবগঞ্জ", "সোনাতলা")),
                BangladeshDistrict("পাবনা", "Pabna", listOf("পাবনা সদর", "আটঘরিয়া", "বেড়া", "ভাঙ্গুড়া", "চাটমোহর", "ফরিদপুর", "ঈশ্বরদী", "সাঁথিয়া", "সুজানগর")),
                BangladeshDistrict("সিরাজগঞ্জ", "Sirajganj", listOf("সিরাজগঞ্জ সদর", "বেলকুচি", "চৌহালী", "কামারখন্দ", "কাজীপুর", "রায়গঞ্জ", "শাহজাদপুর", "তাড়াশ", "উল্লাপাড়া")),
                BangladeshDistrict("নাটোর", "Natore", listOf("নাটোর সদর", "বাগাতিপাড়া", "বড়াইগ্রাম", "গুরুদাসপুর", "লালপুর", "নলডাঙ্গা", "সিংড়া")),
                BangladeshDistrict("নওগাঁ", "Naogaon", listOf("নওগাঁ সদর", "আত্রাই", "বদলগাছী", "ধামইরহাট", "মান্দা", "মহাদেবপুর", "নিয়ামতপুর", "পত্নীতলা", "পোরশা", "রাণীনগর", "সাপাহার")),
                BangladeshDistrict("চাঁপাইনবাবগঞ্জ", "Chapainawabganj", listOf("চাঁপাইনবাবগঞ্জ সদর", "ভোলাহাট", "গোমস্তাপুর", "নাচোল", "শিবগঞ্জ")),
                BangladeshDistrict("জয়পুরহাট", "Joypurhat", listOf("জয়পুরহাট সদর", "আক্কেলপুর", "কালাই", "ক্ষেতলাল", "পাঁচবিবি")),
            ),
        ),
        BangladeshDivision(
            nameBn = "খুলনা",
            nameEn = "Khulna",
            districts = listOf(
                BangladeshDistrict("খুলনা", "Khulna", listOf("খুলনা সদর", "সোনাডাঙ্গা", "খালিশপুর", "দৌলতপুর", "বটিয়াঘাটা", "দাকোপ", "দিঘলিয়া", "ডুমুরিয়া", "কয়রা", "পাইকগাছা", "ফুলতলা", "রূপসা", "তেরখাদা")),
                BangladeshDistrict("যশোর", "Jashore", listOf("যশোর সদর", "অভয়নগর", "বাঘারপাড়া", "চৌগাছা", "ঝিকরগাছা", "কেশবপুর", "মণিরামপুর", "শার্শা")),
                BangladeshDistrict("সাতক্ষীরা", "Satkhira", listOf("সাতক্ষীরা সদর", "আশাশুনি", "দেবহাটা", "কলারোয়া", "কালীগঞ্জ", "শ্যামনগর", "তালা")),
                BangladeshDistrict("বাগেরহাট", "Bagerhat", listOf("বাগেরহাট সদর", "চিতলমারী", "ফকিরহাট", "কচুয়া", "মোল্লাহাট", "মোংলা", "মোড়েলগঞ্জ", "রামপাল", "শরণখোলা")),
                BangladeshDistrict("কুষ্টিয়া", "Kushtia", listOf("কুষ্টিয়া সদর", "ভেড়ামারা", "দৌলতপুর", "খোকসা", "কুমারখালী", "মিরপুর")),
                BangladeshDistrict("ঝিনাইদহ", "Jhenaidah", listOf("ঝিনাইদহ সদর", "হরিণাকুণ্ডু", "কালীগঞ্জ", "কোটচাঁদপুর", "মহেশপুর", "শৈলকুপা")),
                BangladeshDistrict("মাগুরা", "Magura", listOf("মাগুরা সদর", "মহম্মদপুর", "শালিখা", "শ্রীপুর")),
                BangladeshDistrict("নড়াইল", "Narail", listOf("নড়াইল সদর", "কালিয়া", "লোহাগড়া")),
                BangladeshDistrict("চুয়াডাঙ্গা", "Chuadanga", listOf("চুয়াডাঙ্গা সদর", "আলমডাঙ্গা", "দামুড়হুদা", "জীবননগর")),
                BangladeshDistrict("মেহেরপুর", "Meherpur", listOf("মেহেরপুর সদর", "গাংনী", "মুজিবনগর")),
            ),
        ),
        BangladeshDivision(
            nameBn = "বরিশাল",
            nameEn = "Barishal",
            districts = listOf(
                BangladeshDistrict("বরিশাল", "Barishal", listOf("বরিশাল সদর", "আগৈলঝাড়া", "বাবুগঞ্জ", "বাকেরগঞ্জ", "বানারীপাড়া", "গৌরনদী", "হিজলা", "মেহেন্দিগঞ্জ", "মুলাদী", "উজিরপুর")),
                BangladeshDistrict("পটুয়াখালী", "Patuakhali", listOf("পটুয়াখালী সদর", "বাউফল", "দশমিনা", "দুমকি", "গলাচিপা", "কলাপাড়া", "মির্জাগঞ্জ", "রাঙ্গাবালী")),
                BangladeshDistrict("ভোলা", "Bhola", listOf("ভোলা সদর", "বোরহানউদ্দিন", "চরফ্যাশন", "দৌলতখান", "লালমোহন", "মনপুরা", "তজুমুদ্দিন")),
                BangladeshDistrict("পিরোজপুর", "Pirojpur", listOf("পিরোজপুর সদর", "ভান্ডারিয়া", "ইন্দুরকানী", "কাউখালী", "মঠবাড়িয়া", "নাজিরপুর", "নেছারাবাদ")),
                BangladeshDistrict("বরগুনা", "Barguna", listOf("বরগুনা সদর", "আমতলী", "বামনা", "বেতাগী", "পাথরঘাটা", "তালতলী")),
                BangladeshDistrict("ঝালকাঠি", "Jhalokathi", listOf("ঝালকাঠি সদর", "কাঁঠালিয়া", "নলছিটি", "রাজাপুর")),
            ),
        ),
        BangladeshDivision(
            nameBn = "সিলেট",
            nameEn = "Sylhet",
            districts = listOf(
                BangladeshDistrict("সিলেট", "Sylhet", listOf("সিলেট সদর", "বালাগঞ্জ", "বিয়ানীবাজার", "বিশ্বনাথ", "কোম্পানীগঞ্জ", "দক্ষিণ সুরমা", "ফেঞ্চুগঞ্জ", "গোলাপগঞ্জ", "গোয়াইনঘাট", "জৈন্তাপুর", "কানাইঘাট", "ওসমানীনগর", "জকিগঞ্জ")),
                BangladeshDistrict("মৌলভীবাজার", "Moulvibazar", listOf("মৌলভীবাজার সদর", "বড়লেখা", "জুড়ী", "কমলগঞ্জ", "কুলাউড়া", "রাজনগর", "শ্রীমঙ্গল")),
                BangladeshDistrict("হবিগঞ্জ", "Habiganj", listOf("হবিগঞ্জ সদর", "আজমিরীগঞ্জ", "বাহুবল", "বানিয়াচং", "চুনারুঘাট", "লাখাই", "মাধবপুর", "নবীগঞ্জ", "শায়েস্তাগঞ্জ")),
                BangladeshDistrict("সুনামগঞ্জ", "Sunamganj", listOf("সুনামগঞ্জ সদর", "বিশ্বম্ভরপুর", "ছাতক", "দক্ষিণ সুনামগঞ্জ", "দিরাই", "ধর্মপাশা", "দোয়ারাবাজার", "জগন্নাথপুর", "জামালগঞ্জ", "মধ্যনগর", "শাল্লা", "তাহিরপুর")),
            ),
        ),
        BangladeshDivision(
            nameBn = "ময়মনসিংহ",
            nameEn = "Mymensingh",
            districts = listOf(
                BangladeshDistrict("ময়মনসিংহ", "Mymensingh", listOf("ময়মনসিংহ সদর", "ভালুকা", "ধোবাউড়া", "ফুলবাড়ীয়া", "গফরগাঁও", "গৌরীপুর", "হালুয়াঘাট", "ঈশ্বরগঞ্জ", "মুক্তাগাছা", "নান্দাইল", "ফুলপুর", "তারাকান্দা", "ত্রিশাল")),
                BangladeshDistrict("জামালপুর", "Jamalpur", listOf("জামালপুর সদর", "বকশীগঞ্জ", "দেওয়ানগঞ্জ", "ইসলামপুর", "মাদারগঞ্জ", "মেলান্দহ", "সরিষাবাড়ী")),
                BangladeshDistrict("নেত্রকোণা", "Netrokona", listOf("নেত্রকোণা সদর", "আটপাড়া", "বারহাট্টা", "দুর্গাপুর", "কলমাকান্দা", "কেন্দুয়া", "খালিয়াজুরী", "মদন", "মোহনগঞ্জ", "পূর্বধলা")),
                BangladeshDistrict("শেরপুর", "Sherpur", listOf("শেরপুর সদর", "ঝিনাইগাতী", "নকলা", "নালিতাবাড়ী", "শ্রীবরদী")),
            ),
        ),
    )

    val allDivisions: List<String> get() = divisions.map { it.nameBn }

    fun allDistrictNames(): List<String> = divisions.flatMap { d -> d.districts.map { it.nameBn } }

    fun districtsForDivision(divisionName: String): List<String> {
        val clean = divisionName.trim()
        if (clean.isEmpty()) return allDistrictNames()
        val matched = divisions.firstOrNull {
            it.nameBn.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true)
        }
        return matched?.districts?.map { it.nameBn } ?: allDistrictNames()
    }

    fun allUpazilaNames(): List<String> =
        divisions.flatMap { div -> div.districts.flatMap { it.upazilas } }.distinct()

    fun upazilasForDistrict(districtName: String, divisionName: String = ""): List<String> {
        val cleanDist = districtName.trim()
        if (cleanDist.isNotEmpty()) {
            val matchedDist = divisions.flatMap { it.districts }.firstOrNull {
                it.nameBn.equals(cleanDist, ignoreCase = true) || it.nameEn.equals(cleanDist, ignoreCase = true)
            }
            if (matchedDist != null) return matchedDist.upazilas
        }
        val cleanDiv = divisionName.trim()
        if (cleanDiv.isNotEmpty()) {
            val matchedDiv = divisions.firstOrNull {
                it.nameBn.equals(cleanDiv, ignoreCase = true) || it.nameEn.equals(cleanDiv, ignoreCase = true)
            }
            if (matchedDiv != null) return matchedDiv.districts.flatMap { it.upazilas }.distinct()
        }
        return allUpazilaNames()
    }

    fun findDivisionForDistrict(districtName: String): String? {
        val clean = districtName.trim()
        return divisions.firstOrNull { div ->
            div.districts.any {
                it.nameBn.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true)
            }
        }?.nameBn
    }

    fun findLocationForUpazila(upazilaName: String): Pair<String, String>? {
        val clean = upazilaName.trim()
        divisions.forEach { div ->
            div.districts.forEach { dist ->
                if (dist.upazilas.any { it.equals(clean, ignoreCase = true) }) {
                    return div.nameBn to dist.nameBn
                }
            }
        }
        return null
    }

    /**
     * শুধুমাত্র যাচাইকৃত মৌজা ও জরিপ-ভিত্তিক (CS / SA / RS) জে.এল. নং তালিকা।
     * অনুমাননির্ভর বা ভুল জে.এল. নম্বর এখানে রাখা হয়নি — সারাদেশে ~৬০,০০০+ মৌজায় জরিপভেদে জে.এল. নম্বর আলাদা হয়।
     */
    val mouzaJlList: List<MouzaJlEntry> = listOf(
        MouzaJlEntry(
            mouza = "সতিপুরী",
            district = "কুড়িগ্রাম",
            upazila = "ভুরুঙ্গামারী",
            historicalDistricts = listOf("রংপুর"),
            csJl = "৭০",
            parganaHint = "সতিপুরী পরগণা",
            sourceNote = "সি.এস. খতিয়ান নথি",
        ),
        MouzaJlEntry(
            mouza = "দক্ষিণ ছাট গোপালপুর",
            district = "কুড়িগ্রাম",
            upazila = "ভুরুঙ্গামারী",
            historicalDistricts = listOf("রংপুর"),
            rsJl = "২০",
            parganaHint = "বাহারবন্দ পরগণা",
            sourceNote = "আর.এস. খতিয়ান নথি",
        ),
        MouzaJlEntry(
            mouza = "বাহের কেদার",
            district = "কুড়িগ্রাম",
            upazila = "নাগেশ্বরী",
            historicalDistricts = listOf("রংপুর"),
            saJl = "৩২",
            rsJl = "৪৫৭",
            parganaHint = "ভেতরবন্দ পরগণা",
            sourceNote = "এস.এ. নথি ও কুড়িগ্রাম জেলা পোর্টাল",
        ),
        MouzaJlEntry(
            mouza = "পশ্চিম নাগেশ্বরী",
            district = "কুড়িগ্রাম",
            upazila = "নাগেশ্বরী",
            historicalDistricts = listOf("রংপুর"),
            csJl = "২৮",
            saJl = "২২",
            rsJl = "৪২১",
            parganaHint = "ভেতরবন্দ পরগণা",
            sourceNote = "kurigram.gov.bd",
        ),
        MouzaJlEntry(
            mouza = "পূর্ব নাগেশ্বরী",
            district = "কুড়িগ্রাম",
            upazila = "নাগেশ্বরী",
            historicalDistricts = listOf("রংপুর"),
            csJl = "২৯",
            saJl = "২৩",
            rsJl = "৪২২",
            parganaHint = "ভেতরবন্দ পরগণা",
            sourceNote = "kurigram.gov.bd",
        ),
        MouzaJlEntry(
            mouza = "পূর্ব পয়রাডাঙ্গা",
            district = "কুড়িগ্রাম",
            upazila = "নাগেশ্বরী",
            historicalDistricts = listOf("রংপুর"),
            csJl = "৩০",
            saJl = "৪১",
            rsJl = "৪২৩",
            parganaHint = "ভেতরবন্দ পরগণা",
            sourceNote = "kurigram.gov.bd",
        ),
        MouzaJlEntry(
            mouza = "পশ্চিম পয়রাডাঙ্গা",
            district = "কুড়িগ্রাম",
            upazila = "নাগেশ্বরী",
            historicalDistricts = listOf("রংপুর"),
            csJl = "৩১",
            saJl = "১৯",
            rsJl = "৪২৪",
            parganaHint = "ভেতরবন্দ পরগণা",
            sourceNote = "kurigram.gov.bd",
        ),
    )

    fun allMouzas(): List<MouzaJlEntry> = mouzaJlList

    fun mouzasForLocation(districtName: String, upazilaName: String = ""): List<MouzaJlEntry> {
        val cleanDist = districtName.trim()
        val cleanUpz = upazilaName.trim()
        if (cleanDist.isEmpty() && cleanUpz.isEmpty()) return mouzaJlList

        val districtMatches = if (cleanDist.isEmpty()) {
            mouzaJlList
        } else {
            mouzaJlList.filter {
                it.district.equals(cleanDist, ignoreCase = true) ||
                    it.historicalDistricts.any { h -> h.equals(cleanDist, ignoreCase = true) }
            }
        }

        if (cleanUpz.isNotEmpty()) {
            val exactUpz = districtMatches.filter { it.upazila.equals(cleanUpz, ignoreCase = true) }
            if (exactUpz.isNotEmpty()) return exactUpz
            val nationwideUpz = mouzaJlList.filter { it.upazila.equals(cleanUpz, ignoreCase = true) }
            if (nationwideUpz.isNotEmpty()) return nationwideUpz
            // নির্দিষ্ট উপজেলায় কোনো যাচাইকৃত মৌজা না থাকলে অন্য উপজেলার ভুল মৌজা না দেখিয়ে খালি ফেরত দেয়
            return emptyList()
        }

        return districtMatches
    }

    /**
     * "৭০ - সতিপুরী" অথবা "70-সতিপুরী" অথবা শুধু মৌজা নাম ভেঙে Pair(jlNumber, mouzaName) ফেরত দেয়।
     */
    fun parseMouzaJlDisplay(raw: String): Pair<String, String> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return "" to ""
        val parts = trimmed.split(Regex("\\s*[-–—]\\s*"), limit = 2)
        if (parts.size == 2) {
            val p0 = parts[0].trim()
            val p1 = parts[1].trim()
            val p0IsNumeric = p0.any { it.isDigit() || it in '০'..'৯' }
            val p1IsNumeric = p1.any { it.isDigit() || it in '০'..'৯' }
            return when {
                p0IsNumeric && !p1IsNumeric -> p0 to p1
                p1IsNumeric && !p0IsNumeric -> p1 to p0
                else -> p0 to p1
            }
        }
        return "" to trimmed
    }

    /**
     * বাংলাদেশ ও বাংলার ঐতিহাসিক পরগণা তালিকা (বিভাগ, জেলা, উপজেলা ও মৌজা ভিত্তিক)।
     */
    val parganas: List<ParganaEntry> = listOf(
        // রংপুর বিভাগ (কুড়িগ্রাম ও বৃহত্তর রংপুর)
        ParganaEntry(
            name = "বাহারবন্দ পরগণা",
            division = "রংপুর",
            districts = listOf("কুড়িগ্রাম", "রংপুর", "গাইবান্ধা"),
            upazilas = listOf("ভুরুঙ্গামারী", "কুড়িগ্রাম সদর", "উলিপুর", "চিলমারী", "চর রাজিবপুর", "সুন্দরগঞ্জ"),
            mouzas = listOf("দক্ষিণ ছাট গোপালপুর", "উত্তর ছাট গোপালপুর", "ভুরুঙ্গামারী", "জয়মনিরহাট", "পাইকেরছড়া", "বলদিয়া", "চর ভুরুঙ্গামারী", "কুড়িগ্রাম", "কাঁঠালবাড়ী"),
        ),
        ParganaEntry(
            name = "ভেতরবন্দ পরগণা",
            division = "রংপুর",
            districts = listOf("কুড়িগ্রাম", "রংপুর", "গাইবান্ধা"),
            upazilas = listOf("নাগেশ্বরী", "ভুরুঙ্গামারী", "কুড়িগ্রাম সদর", "গাইবান্ধা সদর"),
            mouzas = listOf("বাহের কেদার", "কেদার", "কচাকাটা", "ভিতরবন্দ", "রায়গঞ্জ", "নেওয়াশী", "হাসনাবাদ", "বামনডাঙ্গা", "আন্ধারীঝাড়", "তিলাই", "সোনাহাট"),
        ),
        ParganaEntry(
            name = "সতিপুরী পরগণা",
            division = "রংপুর",
            districts = listOf("কুড়িগ্রাম", "রংপুর"),
            upazilas = listOf("ভুরুঙ্গামারী", "নাগেশ্বরী"),
            mouzas = listOf("সতিপুরী", "পাথরডুবি", "শিলখুড়ি"),
        ),
        ParganaEntry(
            name = "পাঙ্গা পরগণা",
            division = "রংপুর",
            districts = listOf("কুড়িগ্রাম", "রংপুর", "লালমনিরহাট"),
            upazilas = listOf("রাজারহাট", "ফুলবাড়ী", "ভুরুঙ্গামারী", "কুড়িগ্রাম সদর", "কাউনিয়া"),
            mouzas = listOf("ফুলবাড়ী", "রাজারহাট"),
        ),
        ParganaEntry(
            name = "কাঁকিনা পরগণা",
            division = "রংপুর",
            districts = listOf("লালমনিরহাট", "রংপুর", "কুড়িগ্রাম"),
            upazilas = listOf("কালীগঞ্জ", "আদিতমারী", "লালমনিরহাট সদর", "রৌমারী"),
            mouzas = listOf("কাঁকিনা", "আদিতমারী"),
        ),
        ParganaEntry(
            name = "তুষভাণ্ডার পরগণা",
            division = "রংপুর",
            districts = listOf("লালমনিরহাট", "রংপুর"),
            upazilas = listOf("কালীগঞ্জ", "হাতীবান্ধা"),
            mouzas = listOf("তুষভাণ্ডার"),
        ),
        ParganaEntry(
            name = "কাজিরহাট পরগণা",
            division = "রংপুর",
            districts = listOf("রংপুর", "নীলফামারী", "কুড়িগ্রাম"),
            upazilas = listOf("রংপুর সদর", "বদরগঞ্জ", "তারাগঞ্জ", "নীলফামারী সদর"),
            mouzas = listOf("মাহিগঞ্জ", "বদরগঞ্জ"),
        ),
        ParganaEntry(
            name = "মন্থনা পরগণা",
            division = "রংপুর",
            districts = listOf("রংপুর", "লালমনিরহাট"),
            upazilas = listOf("গঙ্গাচড়া", "রংপুর সদর"),
            mouzas = listOf("মন্থনা", "তামপাট"),
        ),
        ParganaEntry(
            name = "পীরগাছা পরগণা",
            division = "রংপুর",
            districts = listOf("রংপুর"),
            upazilas = listOf("পীরগাছা", "কাউনিয়া"),
            mouzas = listOf("পীরগাছা"),
        ),
        ParganaEntry(
            name = "টেপা পরগণা",
            division = "রংপুর",
            districts = listOf("রংপুর", "লালমনিরহাট"),
            upazilas = listOf("কাউনিয়া", "পীরগাছা"),
        ),
        ParganaEntry(
            name = "ফতেহপুর পরগণা",
            division = "রংপুর",
            districts = listOf("রংপুর", "গাইবান্ধা", "নীলফামারী"),
            upazilas = listOf("মিঠাপুকুর", "গোবিন্দগঞ্জ", "সৈয়দপুর"),
        ),
        ParganaEntry(
            name = "বাঁশবাড়ী পরগণা",
            division = "রংপুর",
            districts = listOf("রংপুর", "গাইবান্ধা"),
            upazilas = listOf("পীরগঞ্জ", "পলাশবাড়ী"),
        ),
        ParganaEntry(
            name = "পাটগ্রাম পরগণা",
            division = "রংপুর",
            districts = listOf("লালমনিরহাট", "রংপুর"),
            upazilas = listOf("পাটগ্রাম", "হাতীবান্ধা"),
        ),
        ParganaEntry(
            name = "বোদা পরগণা",
            division = "রংপুর",
            districts = listOf("পঞ্চগড়", "ঠাকুরগাঁও", "নীলফামারী", "দিনাজপুর"),
            upazilas = listOf("বোদা", "দেবীগঞ্জ", "পঞ্চগড় সদর", "ডোমার"),
        ),
        ParganaEntry(
            name = "দিনাজপুর পরগণা",
            division = "রংপুর",
            districts = listOf("দিনাজপুর"),
            upazilas = listOf("দিনাজপুর সদর", "বিরল", "চিরিরবন্দর"),
        ),
        ParganaEntry(
            name = "ঘোড়াঘাট পরগণা",
            division = "রংপুর",
            districts = listOf("দিনাজপুর", "গাইবান্ধা"),
            upazilas = listOf("ঘোড়াঘাট", "হাকিমপুর", "গোবিন্দগঞ্জ"),
        ),
        ParganaEntry(
            name = "আপাইল পরগণা",
            division = "রংপুর",
            districts = listOf("দিনাজপুর", "জয়পুরহাট"),
            upazilas = listOf("বিরামপুর", "নবাবগঞ্জ", "পাঁচবিবি"),
        ),
        ParganaEntry(
            name = "সালবাড়ী পরগণা",
            division = "রংপুর",
            districts = listOf("ঠাকুরগাঁও", "দিনাজপুর"),
            upazilas = listOf("ঠাকুরগাঁও সদর", "বালিয়াডাঙ্গী", "পীরগঞ্জ"),
        ),

        // ঢাকা ও ময়মনসিংহ বিভাগ
        ParganaEntry(
            name = "ভাওয়াল পরগণা",
            division = "ঢাকা",
            districts = listOf("গাজীপুর", "ঢাকা", "নরসিংদী"),
            upazilas = listOf("গাজীপুর সদর", "শ্রীপুর", "কালীগঞ্জ", "কাপাসিয়া", "টঙ্গী", "উত্তরা", "গুলশান", "ধানমন্ডি", "মিরপুর", "পলাশ"),
            mouzas = listOf("চান্দনা", "টঙ্গী", "ধানমন্ডি", "সেনপাড়া পর্বতা"),
        ),
        ParganaEntry(
            name = "বিক্রমপুর পরগণা",
            division = "ঢাকা",
            districts = listOf("মুন্সিগঞ্জ", "ঢাকা", "শরীয়তপুর"),
            upazilas = listOf("মুন্সিগঞ্জ সদর", "শ্রীনগর", "লৌহজং", "টঙ্গীবাড়ী", "সিরাজদিখান", "কেরানীগঞ্জ", "নওয়াবগঞ্জ"),
        ),
        ParganaEntry(
            name = "সোনারগাঁও পরগণা",
            division = "ঢাকা",
            districts = listOf("নারায়ণগঞ্জ", "ঢাকা", "কুমিল্লা"),
            upazilas = listOf("সোনারগাঁও", "বন্দর", "নারায়ণগঞ্জ সদর", "আড়াইহাজার"),
        ),
        ParganaEntry(
            name = "মাহেশ্বরদী পরগণা",
            division = "ঢাকা",
            districts = listOf("নরসিংদী", "নারায়ণগঞ্জ", "ঢাকা"),
            upazilas = listOf("নরসিংদী সদর", "শিবপুর", "মনোহরদী", "রূপগঞ্জ", "ধামরাই"),
        ),
        ParganaEntry(
            name = "নূরুল্লাপুর পরগণা",
            division = "ঢাকা",
            districts = listOf("ঢাকা", "মানিকগঞ্জ"),
            upazilas = listOf("সাভার", "মানিকগঞ্জ সদর", "সিংগাইর"),
        ),
        ParganaEntry(
            name = "কাঁসিমপুর পরগণা",
            division = "ঢাকা",
            districts = listOf("গাজীপুর", "ঢাকা"),
            upazilas = listOf("কালিয়াকৈর", "সাভার"),
        ),
        ParganaEntry(
            name = "ইদিলপুর পরগণা",
            division = "ঢাকা",
            districts = listOf("শরীয়তপুর", "মাদারীপুর", "ফরিদপুর"),
            upazilas = listOf("শরীয়তপুর সদর", "গোসাইরহাট", "মাদারীপুর সদর", "কালকিনি"),
        ),
        ParganaEntry(
            name = "জালালপুর পরগণা",
            division = "ঢাকা",
            districts = listOf("ফরিদপুর", "ঢাকা", "রাজবাড়ী"),
            upazilas = listOf("ফরিদপুর সদর", "দোহার", "চরভদ্রাসন", "সদরপুর"),
        ),
        ParganaEntry(
            name = "ফতেহজঙ্গপুর পরগণা",
            division = "ঢাকা",
            districts = listOf("ফরিদপুর", "গোপালগঞ্জ"),
            upazilas = listOf("ভাঙ্গা", "মুকসুদপুর", "নগরকান্দা"),
        ),
        ParganaEntry(
            name = "তেলিয়াহাটি পরগণা",
            division = "ঢাকা",
            districts = listOf("গোপালগঞ্জ", "ফরিদপুর"),
            upazilas = listOf("টুঙ্গিপাড়া", "গোপালগঞ্জ সদর", "কোটালীপাড়া"),
        ),
        ParganaEntry(
            name = "আটিয়া পরগণা",
            division = "ঢাকা",
            districts = listOf("টাঙ্গাইল", "ময়মনসিংহ"),
            upazilas = listOf("দেলদুয়ার", "বাসাইল", "সখিপুর", "মির্জাপুর", "টাঙ্গাইল সদর"),
        ),
        ParganaEntry(
            name = "কাগমারী পরগণা",
            division = "ঢাকা",
            districts = listOf("টাঙ্গাইল", "ময়মনসিংহ"),
            upazilas = listOf("টাঙ্গাইল সদর", "কালিহাতী", "নাগরপুর", "গোপালপুর"),
        ),
        ParganaEntry(
            name = "পুখুরিয়া পরগণা",
            division = "ঢাকা",
            districts = listOf("টাঙ্গাইল", "জামালপুর", "ময়মনসিংহ"),
            upazilas = listOf("মধুপুর", "ধনবাড়ী", "ঘাটাইল"),
        ),
        ParganaEntry(
            name = "নাসিরুজিয়াল পরগণা",
            division = "ঢাকা",
            districts = listOf("কিশোরগঞ্জ", "ময়মনসিংহ"),
            upazilas = listOf("কিশোরগঞ্জ সদর", "তাড়াইল", "করিমগঞ্জ", "নিকলী"),
        ),
        ParganaEntry(
            name = "আলাফসিংহ পরগণা",
            division = "ময়মনসিংহ",
            districts = listOf("ময়মনসিংহ"),
            upazilas = listOf("ময়মনসিংহ সদর", "ত্রিশাল", "ভালুকা", "গফরগাঁও"),
        ),
        ParganaEntry(
            name = "মোমেনশাহী পরগণা",
            division = "ময়মনসিংহ",
            districts = listOf("ময়মনসিংহ"),
            upazilas = listOf("মুক্তাগাছা", "ফুলবাড়ীয়া", "ময়মনসিংহ সদর"),
        ),
        ParganaEntry(
            name = "জফরশাহী পরগণা",
            division = "ময়মনসিংহ",
            districts = listOf("জামালপুর", "কিশোরগঞ্জ", "ময়মনসিংহ"),
            upazilas = listOf("জামালপুর সদর", "সরিষাবাড়ী", "বাজিতপুর"),
        ),
        ParganaEntry(
            name = "সুসং পরগণা",
            division = "ময়মনসিংহ",
            districts = listOf("নেত্রকোণা", "ময়মনসিংহ"),
            upazilas = listOf("দুর্গাপুর", "কলমাকান্দা"),
        ),
        ParganaEntry(
            name = "খালিয়াজুরী পরগণা",
            division = "ময়মনসিংহ",
            districts = listOf("নেত্রকোণা", "ময়মনসিংহ"),
            upazilas = listOf("খালিয়াজুরী", "মদন", "মোহনগঞ্জ"),
        ),
        ParganaEntry(
            name = "শেরপুর পরগণা",
            division = "ময়মনসিংহ",
            districts = listOf("শেরপুর", "ময়মনসিংহ"),
            upazilas = listOf("শেরপুর সদর", "নালিতাবাড়ী", "শ্রীবরদী"),
        ),

        // চট্টগ্রাম বিভাগ
        ParganaEntry(
            name = "চাকলা রোশনাবাদ",
            division = "চট্টগ্রাম",
            districts = listOf("কুমিল্লা", "ফেনী", "ব্রাহ্মণবাড়িয়া"),
            upazilas = listOf("কুমিল্লা আদর্শ সদর", "সদর দক্ষিণ", "চৌদ্দগ্রাম", "বুড়িচং", "কসবা"),
        ),
        ParganaEntry(
            name = "সরাইল পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("ব্রাহ্মণবাড়িয়া", "কুমিল্লা"),
            upazilas = listOf("সরাইল", "ব্রাহ্মণবাড়িয়া সদর", "আশুগঞ্জ", "নাসিরনগর"),
        ),
        ParganaEntry(
            name = "নূরনগর পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("কুমিল্লা", "ব্রাহ্মণবাড়িয়া"),
            upazilas = listOf("মুরাদনগর", "দেবিদ্বার", "নবীনগর"),
        ),
        ParganaEntry(
            name = "মেহার পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("কুমিল্লা", "চাঁদপুর"),
            upazilas = listOf("লাকসাম", "শাহরাস্তি", "বরুড়া"),
        ),
        ParganaEntry(
            name = "গঙ্গামণ্ডল পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("চাঁদপুর", "কুমিল্লা"),
            upazilas = listOf("চাঁদপুর সদর", "হাজীগঞ্জ", "ফরিদগঞ্জ"),
        ),
        ParganaEntry(
            name = "ভুলুয়া পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("নোয়াখালী", "লক্ষ্মীপুর"),
            upazilas = listOf("নোয়াখালী সদর", "বেগমগঞ্জ", "লক্ষ্মীপুর সদর", "রামগঞ্জ", "রায়পুর"),
        ),
        ParganaEntry(
            name = "আমিরাবাদ পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("ফেনী", "নোয়াখালী"),
            upazilas = listOf("ফেনী সদর", "দাগনভূঞা", "সোনাগাজী"),
        ),
        ParganaEntry(
            name = "নিজামপুর পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("চট্টগ্রাম"),
            upazilas = listOf("পাঁচলাইশ", "সীতাকুণ্ড", "মীরসরাই", "হাটহাজারী"),
        ),
        ParganaEntry(
            name = "চক্রশালা পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("চট্টগ্রাম"),
            upazilas = listOf("পটিয়া", "আনোয়ারা", "বোয়ালখালী", "চন্দনাইশ"),
        ),
        ParganaEntry(
            name = "সন্দ্বীপ পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("চট্টগ্রাম"),
            upazilas = listOf("সন্দ্বীপ"),
        ),
        ParganaEntry(
            name = "রামু পরগণা",
            division = "চট্টগ্রাম",
            districts = listOf("কক্সবাজার", "চট্টগ্রাম"),
            upazilas = listOf("রামু", "কক্সবাজার সদর", "চকরিয়া", "উখিয়া"),
        ),

        // রাজশাহী বিভাগ
        ParganaEntry(
            name = "লস্করপুর (রাজশাহী) পরগণা",
            division = "রাজশাহী",
            districts = listOf("রাজশাহী", "নাটোর"),
            upazilas = listOf("বোয়ালিয়া", "পবা", "চারঘাট", "বাঘা"),
        ),
        ParganaEntry(
            name = "পুঠিয়া পরগণা",
            division = "রাজশাহী",
            districts = listOf("রাজশাহী"),
            upazilas = listOf("পুঠিয়া", "দুর্গাপুর"),
        ),
        ParganaEntry(
            name = "তাহিরপুর পরগণা",
            division = "রাজশাহী",
            districts = listOf("রাজশাহী"),
            upazilas = listOf("বাগমারা", "মোহনপুর"),
        ),
        ParganaEntry(
            name = "দিঘাপতিয়া পরগণা",
            division = "রাজশাহী",
            districts = listOf("নাটোর", "রাজশাহী"),
            upazilas = listOf("নাটোর সদর", "সিংড়া", "গুরুদাসপুর"),
        ),
        ParganaEntry(
            name = "ভাতুরিয়া পরগণা",
            division = "রাজশাহী",
            districts = listOf("নওগাঁ", "নাটোর", "রাজশাহী"),
            upazilas = listOf("আত্রাই", "রাণীনগর", "নওগাঁ সদর"),
        ),
        ParganaEntry(
            name = "শিলবরষ পরগণা",
            division = "রাজশাহী",
            districts = listOf("বগুড়া"),
            upazilas = listOf("বগুড়া সদর", "শিবগঞ্জ", "কাহালু"),
        ),
        ParganaEntry(
            name = "প্রতাপবাজু পরগণা",
            division = "রাজশাহী",
            districts = listOf("বগুড়া", "পাবনা"),
            upazilas = listOf("শেরপুর", "ধুনট", "নন্দীগ্রাম"),
        ),
        ParganaEntry(
            name = "বড়বাজু পরগণা",
            division = "রাজশাহী",
            districts = listOf("পাবনা", "সিরাজগঞ্জ"),
            upazilas = listOf("পাবনা সদর", "সাঁথিয়া", "বেড়া"),
        ),
        ParganaEntry(
            name = "ইউসুফশাহী পরগণা",
            division = "রাজশাহী",
            districts = listOf("সিরাজগঞ্জ", "পাবনা"),
            upazilas = listOf("সিরাজগঞ্জ সদর", "শাহজাদপুর", "উল্লাপাড়া", "বেলকুচি"),
        ),
        ParganaEntry(
            name = "নাজিরপুর (পাবনা) পরগণা",
            division = "রাজশাহী",
            districts = listOf("পাবনা"),
            upazilas = listOf("ঈশ্বরদী", "চাটমোহর", "আটঘরিয়া"),
        ),
        ParganaEntry(
            name = "বারবকপুর পরগণা",
            division = "রাজশাহী",
            districts = listOf("চাঁপাইনবাবগঞ্জ", "নওগাঁ"),
            upazilas = listOf("শিবগঞ্জ", "চাঁপাইনবাবগঞ্জ সদর", "গোমস্তাপুর"),
        ),
        ParganaEntry(
            name = "খট্টা পরগণা",
            division = "রাজশাহী",
            districts = listOf("জয়পুরহাট", "নওগাঁ", "বগুড়া"),
            upazilas = listOf("জয়পুরহাট সদর", "আক্কেলপুর", "বদলগাছী"),
        ),

        // খুলনা বিভাগ
        ParganaEntry(
            name = "সৈয়দপুর পরগণা",
            division = "খুলনা",
            districts = listOf("খুলনা", "যশোর"),
            upazilas = listOf("খুলনা সদর", "সোনাডাঙ্গা", "ফুলতলা", "অভয়নগর"),
        ),
        ParganaEntry(
            name = "খলিফাতাবাদ পরগণা",
            division = "খুলনা",
            districts = listOf("বাগেরহাট", "খুলনা"),
            upazilas = listOf("বাগেরহাট সদর", "ফকিরহাট", "রামপাল", "মোরেলগঞ্জ"),
        ),
        ParganaEntry(
            name = "হগলা পরগণা",
            division = "খুলনা",
            districts = listOf("খুলনা"),
            upazilas = listOf("ডুমুরিয়া", "বটিয়াঘাটা", "পাইকগাছা"),
        ),
        ParganaEntry(
            name = "চাঁচড়া পরগণা",
            division = "খুলনা",
            districts = listOf("যশোর"),
            upazilas = listOf("যশোর সদর", "ঝিকরগাছা", "মণিরামপুর", "কেশবপুর"),
        ),
        ParganaEntry(
            name = "রায়মঙ্গল পরগণা",
            division = "খুলনা",
            districts = listOf("সাতক্ষীরা", "খুলনা"),
            upazilas = listOf("সাতক্ষীরা সদর", "শ্যামনগর", "কালীগঞ্জ", "আশাশুনি"),
        ),
        ParganaEntry(
            name = "নলদী পরগণা",
            division = "খুলনা",
            districts = listOf("মাগুরা", "নড়াইল", "যশোর"),
            upazilas = listOf("মহম্মদপুর", "নড়াইল সদর", "লোহাগড়া"),
        ),
        ParganaEntry(
            name = "ভূষণা পরগণা",
            division = "খুলনা",
            districts = listOf("মাগুরা", "ফরিদপুর", "রাজবাড়ী"),
            upazilas = listOf("মাগুরা সদর", "মহম্মদপুর", "বালিয়াকান্দি", "বোয়ালমারী"),
        ),
        ParganaEntry(
            name = "মামুদশাহী পরগণা",
            division = "খুলনা",
            districts = listOf("ঝিনাইদহ", "মাগুরা"),
            upazilas = listOf("ঝিনাইদহ সদর", "শৈলকুপা", "শ্রীপুর"),
        ),
        ParganaEntry(
            name = "ইসলামপুর পরগণা",
            division = "খুলনা",
            districts = listOf("কুষ্টিয়া", "মেহেরপুর"),
            upazilas = listOf("কুমারখালী", "কুষ্টিয়া সদর", "মেহেরপুর সদর"),
        ),
        ParganaEntry(
            name = "হাবেলী পরগণা",
            division = "খুলনা",
            districts = listOf("চুয়াডাঙ্গা", "কুষ্টিয়া"),
            upazilas = listOf("চুয়াডাঙ্গা সদর", "আলমডাঙ্গা", "দৌলতপুর"),
        ),

        // বরিশাল বিভাগ
        ParganaEntry(
            name = "চন্দ্রদ্বীপ পরগণা",
            division = "বরিশাল",
            districts = listOf("বরিশাল", "ঝালকাঠি", "পটুয়াখালী"),
            upazilas = listOf("বরিশাল সদর", "বাবুগঞ্জ", "বাকেরগঞ্জ", "উজিরপুর", "গৌরনদী"),
        ),
        ParganaEntry(
            name = "সেলিমাবাদ পরগণা",
            division = "বরিশাল",
            districts = listOf("পিরোজপুর", "ঝালকাঠি", "বরিশাল"),
            upazilas = listOf("পিরোজপুর সদর", "ঝালকাঠি সদর", "বানারীপাড়া", "নেছারাবাদ", "ভান্ডারিয়া"),
        ),
        ParganaEntry(
            name = "বুজুর্গউমেদপুর পরগণা",
            division = "বরিশাল",
            districts = listOf("পটুয়াখালী", "বরিশাল"),
            upazilas = listOf("বাউফল", "পটুয়াখালী সদর", "বাকেরগঞ্জ"),
        ),
        ParganaEntry(
            name = "উত্তর শাহবাজপুর পরগণা",
            division = "বরিশাল",
            districts = listOf("ভোলা"),
            upazilas = listOf("ভোলা সদর", "দৌলতখান", "বোরহানউদ্দিন"),
        ),
        ParganaEntry(
            name = "দক্ষিণ শাহবাজপুর পরগণা",
            division = "বরিশাল",
            districts = listOf("ভোলা"),
            upazilas = listOf("লালমোহন", "চরফ্যাশন", "তজুমুদ্দিন", "মনপুরা"),
        ),
        ParganaEntry(
            name = "আওরঙ্গপুর পরগণা",
            division = "বরিশাল",
            districts = listOf("বরগুনা", "পটুয়াখালী"),
            upazilas = listOf("বরগুনা সদর", "আমতলী", "পাথরঘাটা"),
        ),

        // সিলেট বিভাগ
        ParganaEntry(
            name = "জৈন্তা পরগণা",
            division = "সিলেট",
            districts = listOf("সিলেট"),
            upazilas = listOf("জৈন্তাপুর", "কানাইঘাট", "গোয়াইনঘাট"),
        ),
        ParganaEntry(
            name = "ঢাকাদক্ষিণ পরগণা",
            division = "সিলেট",
            districts = listOf("সিলেট"),
            upazilas = listOf("গোলাপগঞ্জ", "বিয়ানীবাজার"),
        ),
        ParganaEntry(
            name = "চাপঘাট পরগণা",
            division = "সিলেট",
            districts = listOf("সিলেট"),
            upazilas = listOf("সিলেট সদর", "বিশ্বনাথ", "জকিগঞ্জ"),
        ),
        ParganaEntry(
            name = "লাউড় পরগণা",
            division = "সিলেট",
            districts = listOf("সুনামগঞ্জ", "সিলেট"),
            upazilas = listOf("তাহিরপুর", "সুনামগঞ্জ সদর", "বিশ্বম্ভরপুর"),
        ),
        ParganaEntry(
            name = "চৌয়াল্লিশ পরগণা",
            division = "সিলেট",
            districts = listOf("সুনামগঞ্জ", "সিলেট"),
            upazilas = listOf("ছাতক", "দোয়ারাবাজার", "জগন্নাথপুর"),
        ),
        ParganaEntry(
            name = "বানিয়াচং পরগণা",
            division = "সিলেট",
            districts = listOf("হবিগঞ্জ", "সিলেট"),
            upazilas = listOf("বানিয়াচং", "আজমিরীগঞ্জ", "নবীগঞ্জ"),
        ),
        ParganaEntry(
            name = "লস্করপুর (হবিগঞ্জ) পরগণা",
            division = "সিলেট",
            districts = listOf("হবিগঞ্জ", "সিলেট"),
            upazilas = listOf("হবিগঞ্জ সদর", "চুনারুঘাট", "শায়েস্তাগঞ্জ", "বাহুবল"),
        ),
        ParganaEntry(
            name = "ইটা পরগণা",
            division = "সিলেট",
            districts = listOf("মৌলভীবাজার", "সিলেট"),
            upazilas = listOf("রাজনগর", "মৌলভীবাজার সদর", "কুলাউড়া"),
        ),
        ParganaEntry(
            name = "সাতগাঁও পরগণা",
            division = "সিলেট",
            districts = listOf("মৌলভীবাজার", "সিলেট"),
            upazilas = listOf("শ্রীমঙ্গল", "কমলগঞ্জ"),
        ),
    )

    fun allParganaNames(): List<String> = parganas.map { it.name }.distinct()

    /**
     * উপজেলা ও মৌজা এবং জেলা/বিভাগ অনুযায়ী প্রাসঙ্গিক পরগণাগুলোর তালিকা।
     * মিল পাওয়া পরগণাগুলো অগ্রাধিকার অনুযায়ী উপরে থাকবে।
     */
    fun parganasForLocation(
        divisionName: String = "",
        districtName: String = "",
        upazilaName: String = "",
        mouzaName: String = "",
    ): List<String> {
        val cleanDiv = divisionName.trim()
        val cleanDist = districtName.trim()
        val cleanUpz = upazilaName.trim()
        val cleanMouza = mouzaName.trim()

        if (cleanDiv.isEmpty() && cleanDist.isEmpty() && cleanUpz.isEmpty() && cleanMouza.isEmpty()) {
            return allParganaNames()
        }

        val scored = parganas.mapNotNull { p ->
            var score = 0
            if (cleanMouza.isNotEmpty() && p.mouzas.any { it.equals(cleanMouza, ignoreCase = true) }) {
                score += 100
            }
            if (cleanUpz.isNotEmpty() && p.upazilas.any { it.equals(cleanUpz, ignoreCase = true) }) {
                score += 40
            }
            if (cleanDist.isNotEmpty() && p.districts.any { it.equals(cleanDist, ignoreCase = true) }) {
                score += 15
            }
            if (cleanDiv.isNotEmpty() && p.division.equals(cleanDiv, ignoreCase = true)) {
                score += 3
            }
            if (score > 0) p.name to score else null
        }

        if (scored.isEmpty()) return allParganaNames()
        return scored.sortedByDescending { it.second }.map { it.first }.distinct()
    }
}
