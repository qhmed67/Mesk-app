package com.example.masjd2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.masjd2.ui.theme.Masjd2Theme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import androidx.compose.foundation.Image
import com.example.masjd2.R

class MorningAzkarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Masjd2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1B2951)
                ) {
                    MorningAzkarPage(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun MorningAzkarPage(onBackClick: () -> Unit) {
    val azkarItems = listOf(
        AzkarItem(
            id = 1,
            title = "أَعُوذُ بِاللهِ مِنْ الشَّيْطَانِ الرَّجِيمِ",
            content = "اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            times = "مرة واحدة",
            reference = "[آية الكرسى - البقرة 255]\nمن قالها حين يصبح أجير من الجن حتى يمسى ومن قالها حين يمسى أجير من الجن حتى يصبح."
        ),
        AzkarItem(
            id = 2,
            title = "بِسْمِ اللهِ الرَّحْمنِ الرَّحِيم",
            content = "قُلْ هُوَ ٱللَّهُ أَحَدٌ، ٱللَّهُ ٱلصَّمَدُ، لَمْ يَلِدْ وَلَمْ يُولَدْ، وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ.",
            times = "ثلاث مرات",
            reference = "من قالها حين يصبح وحين يمسى كفته من كل شىء (الإخلاص والمعوذتين)."
        ),
        AzkarItem(
            id = 3,
            title = "بِسْمِ اللهِ الرَّحْمنِ الرَّحِيم",
            content = "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ، مِن شَرِّ مَا خَلَقَ، وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ، وَمِن شَرِّ ٱلنَّفَّٰثَٰتِ فِى ٱلْعُقَدِ، وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ.",
            times = "ثلاث مرات",
            reference = "من قالها حين يصبح وحين يمسى كفته من كل شىء (الإخلاص والمعوذتين)."
        ),
        AzkarItem(
            id = 4,
            title = "بِسْمِ اللهِ الرَّحْمنِ الرَّحِيم",
            content = "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ، مَلِكِ ٱلنَّاسِ، إِلَٰهِ ٱلنَّاسِ، مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ، ٱلَّذِٰذِى يُوَسْوِسُ فِى صُدُورِ ٱلنَّاسِ، مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ.",
            times = "ثلاث مرات",
            reference = "من قالها حين يصبح وحين يمسى كفته من كل شىء (الإخلاص والمعوذتين)."
        ),
        AzkarItem(
            id = 5,
            title = "أَصْـبَحْنا وَأَصْـبَحَ المُـلْكُ لله",
            content = "أَصْـبَحْنا وَأَصْـبَحَ المُـلْكُ لله وَالحَمدُ لله ، لا إلهَ إلاّ اللّهُ وَحدَهُ لا شَريكَ لهُ، لهُ المُـلكُ ولهُ الحَمْـد، وهُوَ على كلّ شَيءٍ قدير ، رَبِّ أسْـأَلُـكَ خَـيرَ ما في هـذا اليوم وَخَـيرَ ما بَعْـدَه ، وَأَعـوذُ بِكَ مِنْ شَـرِّ ما في هـذا اليوم وَشَرِّ ما بَعْـدَه، رَبِّ أَعـوذُبِكَ مِنَ الْكَسَـلِ وَسـوءِ الْكِـبَر ، رَبِّ أَعـوذُ بِكَ مِنْ عَـذابٍ في النّـارِ وَعَـذابٍ في القَـبْر.",
            times = "مرة واحدة",
            reference = "من قالها حين يصبح أجير من الجن حتى يمسى ومن قالها حين يمسى أجير من الجن حتى يصبح."
        ),
        AzkarItem(
            id = 6,
            title = "اللّهـمَّ أَنْتَ رَبِّـي لا إلهَ إلاّ أَنْتَ",
            content = "اللّهـمَّ أَنْتَ رَبِّـي لا إلهَ إلاّ أَنْتَ ، خَلَقْتَنـي وَأَنا عَبْـدُك ، وَأَنا عَلـى عَهْـدِكَ وَوَعْـدِكَ ما اسْتَـطَعْـت ، أَعـوذُبِكَ مِنْ شَـرِّ ما صَنَـعْت ، أَبـوءُ لَـكَ بِنِعْـمَتِـكَ عَلَـيَّ وَأَبـوءُ بِذَنْـبي فَاغْفـِرْ لي فَإِنَّـهُ لا يَغْـفِرُ الذُّنـوبَ إِلاّ أَنْتَ .",
            times = "مرة واحدة",
            reference = "من قالها موقنا بها حين يمسى ومات من ليلته دخل الجنة وكذلك حين يصبح."
        ),
        AzkarItem(
            id = 7,
            title = "رَضيـتُ بِاللهِ رَبَّـاً وَبِالإسْلامِ ديـناً",
            content = "رَضيـتُ بِاللهِ رَبَّـاً وَبِالإسْلامِ ديـناً وَبِمُحَـمَّدٍ صلى الله عليه وسلم نَبِيّـاً.",
            times = "ثلاث مرات",
            reference = "من قالها حين يصبح وحين يمسى كان حقا على الله أن يرضيه يوم القيامة."
        ),
        AzkarItem(
            id = 8,
            title = "اللّهُـمَّ إِنِّـي أَصْبَـحْتُ أُشْـهِدُك",
            content = "اللّهُـمَّ إِنِّـي أَصْبَـحْتُ أُشْـهِدُك ، وَأُشْـهِدُ حَمَلَـةَ عَـرْشِـك ، وَمَلَائِكَتَكَ ، وَجَمـيعَ خَلْـقِك ، أَنَّـكَ أَنْـتَ اللهُ لا إلهَ إلاّ أَنْـتَ وَحْـدَكَ لا شريكَ لَـك ، وَأَنَّ ُ مُحَمّـداً عَبْـدُكَ وَرَسـولُـك.",
            times = "",
            reference = "من قالها أعتقه الله من النار."
        ),
        AzkarItem(
            id = 9,
            title = "اللّهُـمَّ ما أَصْبَـحَ بي مِـنْ نِعْـمَةٍ",
            content = "اللّهُـمَّ ما أَصْبَـحَ بي مِـنْ نِعْـمَةٍ أَو بِأَحَـدٍ مِـنْ خَلْـقِك ، فَمِـنْكَ وَحْـدَكَ لا شريكَ لَـك ، فَلَـكَ الْحَمْـدُ وَلَـكَ الشُّكْـر.",
            times = "مرة واحدة",
            reference = "من قالها حين يصبح أدى شكر يومه."
        ),
        AzkarItem(
            id = 10,
            title = "حَسْبِـيَ اللّهُ لا إلهَ إلاّ هُوَ",
            content = "حَسْبِـيَ اللّهُ لا إلهَ إلاّ هُوَ عَلَـيهِ تَوَكَّـلتُ وَهُوَ رَبُّ العَرْشِ العَظـيم.",
            times = "سبعة مرات",
            reference = "من قالها كفاه الله ما أهمه من أمر الدنيا والأخرة."
        ),
        AzkarItem(
            id = 11,
            title = "بِسـمِ اللهِ الذي لا يَضُـرُّ مَعَ اسمِـهِ",
            content = "بِسـمِ اللهِ الذي لا يَضُـرُّ مَعَ اسمِـهِ شَيءٌ في الأرْضِ وَلا في السّمـاءِ وَهـوَ السّمـيعُ العَلـيم.",
            times = "ثلاث مرات",
            reference = "لم يضره من الله شيء."
        ),
        AzkarItem(
            id = 12,
            title = "اللّهُـمَّ بِكَ أَصْـبَحْنا وَبِكَ أَمْسَـينا",
            content = "اللّهُـمَّ بِكَ أَصْـبَحْنا وَبِكَ أَمْسَـينا ، وَبِكَ نَحْـيا وَبِكَ نَمُـوتُ وَإِلَـيْكَ النُـشُور.",
            times = "مرة واحدة",
            reference = "من قالها حين يصبح أجير من الجن حتى يمسى ومن قالها حين يمسى أجير من الجن حتى يصبح."
        ),
        AzkarItem(
            id = 13,
            title = "أَصْبَـحْـنا عَلَى فِطْرَةِ الإسْلاَمِ",
            content = "أَصْبَـحْـنا عَلَى فِطْرَةِ الإسْلاَمِ، وَعَلَى كَلِمَةِ الإِخْلاَصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ، وَعَلَى مِلَّةِ أَبِينَا إبْرَاهِيمَ حَنِيفاً مُسْلِماً وَمَا كَانَ مِنَ المُشْرِكِينَ.",
            times = "مرة واحدة",
            reference = "من قالها حين يصبح أجير من الجن حتى يمسى ومن قالها حين يمسى أجير من الجن حتى يصبح."
        ),
        AzkarItem(
            id = 14,
            title = "سُبْحـانَ اللهِ وَبِحَمْـدِهِ عَدَدَ خَلْـقِه",
            content = "سُبْحـانَ اللهِ وَبِحَمْـدِهِ عَدَدَ خَلْـقِه ، وَرِضـا نَفْسِـه ، وَزِنَـةَ عَـرْشِـه ، وَمِـدادَ كَلِمـاتِـه.",
            times = "ثلاث مرات",
            reference = "من قالها حين يصبح وحين يمسى كفته من كل شىء."
        ),
        AzkarItem(
            id = 15,
            title = "اللّهُـمَّ عافِـني في بَدَنـي",
            content = "اللّهُـمَّ عافِـني في بَدَنـي ، اللّهُـمَّ عافِـني في سَمْـعي ، اللّهُـمَّ عافِـني في بَصَـري ، لا إلهَ إلاّ أَنْـتَ.",
            times = "ثلاث مرات",
            reference = "لم يضره من الله شيء."
        ),
        AzkarItem(
            id = 16,
            title = "اللّهُـمَّ إِنّـي أَعـوذُ بِكَ مِنَ الْكُـفر",
            content = "اللّهُـمَّ إِنّـي أَعـوذُ بِكَ مِنَ الْكُـفر ، وَالفَـقْر ، وَأَعـوذُ بِكَ مِنْ عَذابِ القَـبْر ، لا إلهَ إلاّ أَنْـتَ.",
            times = "ثلاث مرات",
            reference = "لم يضره من الله شيء."
        ),
        AzkarItem(
            id = 17,
            title = "اللّهُـمَّ إِنِّـي أسْـأَلُـكَ العَـفْوَ وَالعـافِـيةَ",
            content = "اللّهُـمَّ إِنِّـي أسْـأَلُـكَ العَـفْوَ وَالعـافِـيةَ في الدُّنْـيا وَالآخِـرَة ، اللّهُـمَّ إِنِّـي أسْـأَلُـكَ العَـفْوَ وَالعـافِـيةَ في ديني وَدُنْـيايَ وَأهْـلي وَمالـي ، اللّهُـمَّ اسْتُـرْ عـوْراتي وَآمِـنْ رَوْعاتـي ، اللّهُـمَّ احْفَظْـني مِن بَـينِ يَدَيَّ وَمِن خَلْفـي وَعَن يَمـيني وَعَن شِمـالي ، وَمِن فَوْقـي ، وَأَعـوذُ بِعَظَمَـتِكَ أَن أُغْـتالَ مِن تَحْتـي.",
            times = "مرة واحدة",
            reference = "من قالها حين يصبح أجير من الجن حتى يمسى ومن قالها حين يمسى أجير من الجن حتى يصبح."
        ),
        AzkarItem(
            id = 18,
            title = "يَا حَيُّ يَا قيُّومُ بِرَحْمَتِكَ",
            content = "يَا حَيُّ يَا قيُّومُ بِرَحْمَتِكَ أسْتَغِيثُ أصْلِحْ لِي شَأنِي كُلَّهُ وَلاَ تَكِلْنِي إلَى نَفْسِي طَـرْفَةَ عَيْنٍ.",
            times = "ثلاث مرات",
            reference = "لم يضره من الله شيء."
        ),
        AzkarItem(
            id = 19,
            title = "أَصْبَـحْـنا وَأَصْبَـحْ المُـلكُ للهِ رَبِّ العـالَمـين",
            content = "أَصْبَـحْـنا وَأَصْبَـحْ المُـلكُ للهِ رَبِّ العـالَمـين ، اللّهُـمَّ إِنِّـي أسْـأَلُـكَ خَـيْرَ هـذا الـيَوْم ، فَـتْحَهُ ، وَنَصْـرَهُ ، وَنـورَهُ وَبَـرَكَتَـهُ ، وَهُـداهُ ، وَأَعـوذُ بِـكَ مِـنْ شَـرِّ ما فـيهِ وَشَـرِّ ما بَعْـدَه.",
            times = "مرة واحدة",
            reference = "من قالها حين يصبح أجير من الجن حتى يمسى ومن قالها حين يمسى أجير من الجن حتى يصبح."
        ),
        AzkarItem(
            id = 20,
            title = "اللّهُـمَّ عالِـمَ الغَـيْبِ وَالشّـهادَةِ",
            content = "اللّهُـمَّ عالِـمَ الغَـيْبِ وَالشّـهادَةِ فاطِـرَ السّماواتِ وَالأرْضِ رَبَّ كـلِّ شَـيءٍ وَمَليـكَه ، أَشْهَـدُ أَنْ لا إِلـهَ إِلاّ أَنْت ، أَعـوذُ بِكَ مِن شَـرِّ نَفْسـي وَمِن شَـرِّ الشَّيْـطانِ وَشِرْكِهِ ، وَأَنْ أَقْتَـرِفَ عَلـى نَفْسـي سوءاً أَوْ أَجُـرَّهُ إِلـى مُسْـلِم.",
            times = "مرة واحدة",
            reference = "من قالها حين يصبح أجير من الجن حتى يمسى ومن قالها حين يمسى أجير من الجن حتى يصبح."
        ),
        AzkarItem(
            id = 21,
            title = "أَعـوذُ بِكَلِمـاتِ اللّهِ التّـامّـاتِ",
            content = "أَعـوذُ بِكَلِمـاتِ اللّهِ التّـامّـاتِ مِنْ شَـرِّ ما خَلَـق.",
            times = "ثلاث مرات",
            reference = "لم يضره من الله شيء."
        ),
        AzkarItem(
            id = 22,
            title = "اللَّهُمَّ صَلِّ وَسَلِّمْ وَبَارِكْ على نَبِيِّنَا مُحمَّد",
            content = "اللَّهُمَّ صَلِّ وَسَلِّمْ وَبَارِكْ على نَبِيِّنَا مُحمَّد.",
            times = "عشرة مرات",
            reference = "من صلى على حين يصبح وحين يمسى ادركته شفاعتى يوم القيامة."
        ),
        AzkarItem(
            id = 23,
            title = "اللَّهُمَّ إِنَّا نَعُوذُ بِكَ مِنْ أَنْ نُشْرِكَ بِكَ",
            content = "اللَّهُمَّ إِنَّا نَعُوذُ بِكَ مِنْ أَنْ نُشْرِكَ بِكَ شَيْئًا نَعْلَمُهُ ، وَنَسْتَغْفِرُكَ لِمَا لَا نَعْلَمُهُ.",
            times = "ثلاث مرات",
            reference = "لم يضره من الله شيء."
        ),
        AzkarItem(
            id = 24,
            title = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ الْهَمِّ وَالْحَزَنِ",
            content = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ الْهَمِّ وَالْحَزَنِ، وَأَعُوذُ بِكَ مِنْ الْعَجْزِ وَالْكَسَلِ، وَأَعُوذُ بِكَ مِنْ الْجُبْنِ وَالْبُخْلِ، وَأَعُوذُ بِكَ مِنْ غَلَبَةِ الدَّيْنِ، وَقَهْرِ الرِّجَالِ.",
            times = "ثلاث مرات",
            reference = "لم يضره من الله شيء."
        ),
        AzkarItem(
            id = 25,
            title = "أسْتَغْفِرُ اللهَ العَظِيمَ الَّذِي لاَ إلَهَ إلاَّ هُوَ",
            content = "أسْتَغْفِرُ اللهَ العَظِيمَ الَّذِي لاَ إلَهَ إلاَّ هُوَ، الحَيُّ القَيُّومُ، وَأتُوبُ إلَيهِ.",
            times = "ثلاث مرات",
            reference = "لم يضره من الله شيء."
        ),
        AzkarItem(
            id = 26,
            title = "يَا رَبِّ , لَكَ الْحَمْدُ كَمَا يَنْبَغِي",
            content = "يَا رَبِّ , لَكَ الْحَمْدُ كَمَا يَنْبَغِي لِجَلَالِ وَجْهِكَ , وَلِعَظِيمِ سُلْطَانِكَ.",
            times = "ثلاث مرات",
            reference = "لم يضره من الله شيء."
        ),
        AzkarItem(
            id = 27,
            title = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا",
            content = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا.",
            times = "مرة واحدة",
            reference = "من قالها حين يصبح أجير من الجن حتى يمسى ومن قالها حين يمسى أجير من الجن حتى يصبح."
        ),
        AzkarItem(
            id = 28,
            title = "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَهَ إِلا أَنْتَ",
            content = "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَهَ إِلا أَنْتَ ، عَلَيْكَ تَوَكَّلْتُ ، وَأَنْتَ رَبُّ الْعَرْشِ الْعَظِيمِ , مَا شَاءَ اللَّهُ كَانَ ، وَمَا لَمْ يَشَأْ لَمْ يَكُنْ ، وَلا حَوْلَ وَلا قُوَّةَ إِلا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ , أَعْلَمُ أَنَّ اللَّهَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ ، وَأَنَّ اللَّهَ قَدْ أَحَاطَ بِكُلِّ شَيْءٍ عِلْمًا , اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي ، وَمِنْ شَرِّ كُلِّ دَابَّةٍ أَنْتَ آخِذٌ بِنَاصِيَتِهَا ، إِنَّ رَبِّي عَلَى صِرَاطٍ مُسْتَقِيمٍ.",
            times = "مرة واحدة",
            reference = "ذكر طيب."
        ),
        AzkarItem(
            id = 29,
            title = "لَا إلَه إلّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            content = "لَا إلَه إلّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءِ قَدِيرِ.",
            times = "مائة مرة",
            reference = "كانت له عدل عشر رقاب، وكتبت له مئة حسنة، ومحيت عنه مئة سيئة، وكانت له حرزا من الشيطان."
        ),
        AzkarItem(
            id = 30,
            title = "سُبْحـانَ اللهِ وَبِحَمْـدِهِ",
            content = "سُبْحـانَ اللهِ وَبِحَمْـدِهِ.",
            times = "مائة مرة",
            reference = "حُطَّتْ خَطَايَاهُ وَإِنْ كَانَتْ مِثْلَ زَبَدِ الْبَحْرِ. لَمْ يَأْتِ أَحَدٌ يَوْمَ الْقِيَامَةِ بِأَفْضَلَ مِمَّا جَاءَ بِهِ إِلَّا أَحَدٌ قَالَ مِثْلَ مَا قَالَ أَوْ زَادَ عَلَيْهِ."
        ),
        AzkarItem(
            id = 31,
            title = "أسْتَغْفِرُ اللهَ وَأتُوبُ إلَيْهِ",
            content = "أسْتَغْفِرُ اللهَ وَأتُوبُ إلَيْهِ",
            times = "مائة مرة",
            reference = "مائة حسنة، ومُحيت عنه مائة سيئة، وكانت له حرزاً من الشيطان حتى يمسى."
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.starry_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().zIndex(-1f),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF20424f).copy(alpha = 0.8f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "أذكار الصباح",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            
            // Azkar items list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(azkarItems) { item ->
                    AzkarItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun AzkarItemCard(item: AzkarItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3D3D).copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF85F2F2),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            if (item.content.isNotEmpty()) {
                Text(
                    text = item.content,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    lineHeight = 28.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Times if specified
            if (item.times.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.times,
                    fontSize = 14.sp,
                    color = Color(0xFF85F2CC),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Reference
            Text(
                text = item.reference,
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.End,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}