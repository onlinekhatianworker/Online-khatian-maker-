package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BasicInfo
import com.example.model.DagEntry
import com.example.model.KhatianDraftState
import com.example.model.OwnerCategory
import com.example.model.OwnerEntry
import com.example.model.SessionSettings
import com.example.model.SurveySchemas
import com.example.model.SurveyType
import com.example.pdf.KhatianPdfEngine
import com.example.util.BengaliNumberUtils
import com.example.util.LandCalculationEngine
import com.example.util.LandUnit
import com.example.viewmodel.KhatianViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KhatianEngineTest {

    @Test
    fun `bengali and english numeral conversion and hissa parsing`() {
        assertEquals("123.45", BengaliNumberUtils.toEnglishDigits("১২৩.৪৫"))
        assertEquals("০.২৫", BengaliNumberUtils.toBengaliDigits("0.25"))
        assertEquals(0.25, LandCalculationEngine.parseHissaFraction("১/৪")!!, 0.0001)
        assertEquals(0.25, LandCalculationEngine.parseHissaFraction("৪ আনা")!!, 0.0001)
        assertEquals(0.5, LandCalculationEngine.parseHissaFraction("50%")!!, 0.0001)
        assertEquals(33.0, LandCalculationEngine.toShotok(1.0, LandUnit.BIGHA), 0.001)
        assertEquals(1.0, LandCalculationEngine.convert(100.0, LandUnit.SHOTOK, LandUnit.ACRE), 0.001)
    }

    @Test
    fun `all eight surveys have isolated non-mixed schemas`() {
        val csKeys = SurveySchemas.schemaFor(SurveyType.CS).exclusiveBasicFields.map { it.key }
        val saKeys = SurveySchemas.schemaFor(SurveyType.SA).exclusiveBasicFields.map { it.key }
        val cityKeys = SurveySchemas.schemaFor(SurveyType.CITY_SURVEY).exclusiveBasicFields.map { it.key }
        assertTrue(csKeys.contains("superior_khatian_occupant"))
        assertTrue(saKeys.contains("own_dey_edu_cess"))
        assertTrue(cityKeys.contains("holding_no"))
        assertNotEquals(csKeys, cityKeys)
    }

    @Test
    fun `bangladesh geo data includes 8 divisions 64 districts and cascading upazilas`() {
        assertEquals(8, com.example.util.BangladeshGeoData.allDivisions.size)
        assertEquals(64, com.example.util.BangladeshGeoData.allDistrictNames().size)
        val kurigramUpazilas = com.example.util.BangladeshGeoData.upazilasForDistrict("কুড়িগ্রাম")
        assertTrue(kurigramUpazilas.contains("ভুরুঙ্গামারী"))
        assertTrue(kurigramUpazilas.contains("নাগেশ্বরী"))
        assertEquals("রংপুর", com.example.util.BangladeshGeoData.findDivisionForDistrict("কুড়িগ্রাম"))
    }

    @Test
    fun `district mouza jl lookup and upazila mouza pargana lookup work accurately`() {
        val bhurungamariMouzas = com.example.util.BangladeshGeoData.mouzasForLocation("কুড়িগ্রাম", "ভুরুঙ্গামারী")
        assertTrue(bhurungamariMouzas.any { it.displayLabelForSurvey(SurveyType.CS) == "৭০ - সতিপুরী" })
        assertTrue(bhurungamariMouzas.any { it.displayLabelForSurvey(SurveyType.RS) == "২০ - দক্ষিণ ছাট গোপালপুর" })

        val nageshwariMouzas = com.example.util.BangladeshGeoData.mouzasForLocation("কুড়িগ্রাম", "নাগেশ্বরী")
        val westNageshwari = nageshwariMouzas.first { it.mouza == "পশ্চিম নাগেশ্বরী" }
        assertEquals("২৮", westNageshwari.jlForSurvey(SurveyType.CS))
        assertEquals("২২", westNageshwari.jlForSurvey(SurveyType.SA))
        assertEquals("৪২১", westNageshwari.jlForSurvey(SurveyType.RS))

        val baherKedar = nageshwariMouzas.first { it.mouza == "বাহের কেদার" }
        assertEquals("৩২", baherKedar.jlForSurvey(SurveyType.SA))
        assertEquals("৪৫৭", baherKedar.jlForSurvey(SurveyType.RS))

        val unbundledUpazila = com.example.util.BangladeshGeoData.mouzasForLocation("ঢাকা", "সাভার")
        assertTrue("Unbundled upazila must not return guessed or other-district mouzas", unbundledUpazila.isEmpty())

        assertEquals("৭০" to "সতিপুরী", com.example.util.BangladeshGeoData.parseMouzaJlDisplay("৭০ - সতিপুরী"))
        assertEquals("70" to "সতিপুরী", com.example.util.BangladeshGeoData.parseMouzaJlDisplay("70-সতিপুরী"))

        val parganas = com.example.util.BangladeshGeoData.parganasForLocation(
            divisionName = "রংপুর",
            districtName = "কুড়িগ্রাম",
            upazilaName = "ভুরুঙ্গামারী",
            mouzaName = "সতিপুরী",
        )
        assertEquals("সতিপুরী পরগণা", parganas.first())
        assertTrue(parganas.contains("বাহারবন্দ পরগণা"))
        assertTrue(parganas.contains("ভেতরবন্দ পরগণা"))

        val allParganas = com.example.util.BangladeshGeoData.allParganaNames()
        assertTrue(allParganas.size >= 40)
        assertTrue(allParganas.contains("ভাওয়াল পরগণা"))
        assertTrue(allParganas.contains("চন্দ্রদ্বীপ পরগণা"))
        assertTrue(allParganas.contains("জৈন্তা পরগণা"))
    }

    @Test
    fun `pdf engine generates valid multi-page pdf for all eight surveys`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = KhatianPdfEngine(context)
        val settings = SessionSettings()

        val manyOwners = (1..18).map { i ->
            OwnerEntry(
                category = OwnerCategory.MUL_MALIK,
                name = "মোঃ আব্দুর রহমান $i",
                fatherOrHusbandName = "মৃত করিম উদ্দিন",
                address = "গ্রাম: রামপুর, ডাকঘর: সদর, জেলা: ঢাকা",
                hissa = "০.০৫",
                landAmount = "৫.০০",
            )
        }
        val manyDags = (1..18).map { i ->
            DagEntry(
                dagNumber = "${100 + i}",
                landClass = "নাল",
                totalDagArea = "৫০.০০",
                ownerShare = "০.৫০",
                entitledArea = "২৫.০০",
                remarks = "দখল সঠিক আছে",
            )
        }

        val kit = com.example.pdf.PdfPaintKit(context, false)
        SurveyType.entries.forEach { survey ->
            val draft = KhatianDraftState(
                surveyType = survey,
                basicInfo = BasicInfo(
                    division = "ঢাকা",
                    district = "গাজীপুর",
                    upazila = "সদর",
                    mouzaName = "চান্দনা",
                    jlNumber = "৪২",
                    khatianNumber = "১২৫",
                    surveyName = survey.titleBn,
                ),
                owners = manyOwners,
                dags = manyDags,
            )
            val dim = com.example.pdf.PdfPageGeometry.resolvePageDimensions(
                surveyType = survey,
                settings = settings,
                customLandscape = draft.customConfig.useLandscape,
            )
            val plans = engine.buildPagePlans(draft, settings, dim, kit)
            assertTrue("Expected multi-page pagination for ${survey.code}, got ${plans.size}", plans.size >= 2)

            val bitmaps = engine.renderPreviewBitmaps(draft, settings)
            assertEquals("Bitmap count should match page plan count for ${survey.code}", plans.size, bitmaps.size)
            bitmaps.forEach { bmp ->
                assertTrue("Bitmap width must be > 0", bmp.width > 100)
                assertTrue("Bitmap height must be > 0", bmp.height > 100)
                bmp.recycle()
            }
        }
    }

    @Test
    fun `clearAllData wipes all temporary draft fields in viewmodel`() {
        val vm = KhatianViewModel()
        vm.selectSurvey(SurveyType.RS)
        vm.updateBasicInfo { it.copy(district = "ঢাকা", mouzaName = "মিরপুর", khatianNumber = "৮৮") }
        vm.upsertOwner(OwnerEntry(name = "কামাল হোসেন"))
        vm.upsertDag(DagEntry(dagNumber = "৫০১", totalDagArea = "২০"))

        assertEquals(1, vm.draft.value.owners.size)
        assertEquals(1, vm.draft.value.dags.size)

        vm.clearAllData()

        assertEquals("", vm.draft.value.basicInfo.district)
        assertEquals("", vm.draft.value.basicInfo.mouzaName)
        assertEquals(0, vm.draft.value.owners.size)
        assertEquals(0, vm.draft.value.dags.size)
    }

    @Test
    fun `khatian form uses pure black and white borders and 600 dpi default`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settings = SessionSettings()
        assertEquals(com.example.model.PdfQuality.ULTRA_600, settings.pdfQuality)
        assertEquals(600, settings.pdfQuality.previewDpi)

        val kit = com.example.pdf.PdfPaintKit(context, settings.printInkSaverGrayscale)
        assertEquals(android.graphics.Color.BLACK, kit.borderStroke.color)
        assertEquals(android.graphics.Color.BLACK, kit.outerDoubleBorder.color)
        assertEquals(android.graphics.Color.BLACK, kit.dashedBorder.color)
        assertEquals(android.graphics.Color.WHITE, kit.headerFill.color)

        val engine = KhatianPdfEngine(context)
        val draft = KhatianDraftState(
            surveyType = SurveyType.RS,
            basicInfo = BasicInfo(district = "কুড়িগ্রাম", mouzaName = "দক্ষিণ ছাট গোপালপুর", jlNumber = "২০", khatianNumber = "২৭৮"),
        )
        val bitmaps = engine.renderPreviewBitmaps(draft, settings)
        assertTrue(bitmaps.isNotEmpty())
        bitmaps.forEach { bmp ->
            assertTrue("Max dimension must stay within safe GPU texture bounds", maxOf(bmp.width, bmp.height) <= 3840)
            bmp.recycle()
        }
    }
}
