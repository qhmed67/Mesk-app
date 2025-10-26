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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import com.example.masjd2.R

class SleepAzkarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Masjd2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1B2951)
                ) {
                    SleepAzkarPage(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun SleepAzkarPage(onBackClick: () -> Unit) {
    val azkarItems = listOf(
        AzkarItem(
            id = 1,
            title = "بِاسْمِكَ رَبِّـي وَضَعْـتُ جَنْـبي",
            content = "بِاسْمِكَ رَبِّـي وَضَعْـتُ جَنْـبي ، وَبِكَ أَرْفَعُـه، فَإِن أَمْسَـكْتَ نَفْسـي فارْحَـمْها ، وَإِنْ أَرْسَلْتَـها فاحْفَظْـها بِمـا تَحْفَـظُ بِه عِبـادَكَ الصّـالِحـين.",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 2,
            title = "اللّهُـمَّ إِنَّـكَ خَلَـقْتَ نَفْسـي",
            content = "اللّهُـمَّ إِنَّـكَ خَلَـقْتَ نَفْسـي وَأَنْـتَ تَوَفّـاهـا لَكَ ممَـاتـها وَمَحْـياها ، إِنْ أَحْيَيْـتَها فاحْفَظْـها ، وَإِنْ أَمَتَّـها فَاغْفِـرْ لَـها . اللّهُـمَّ إِنَّـي أَسْـأَلُـكَ العـافِـيَة.",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 3,
            title = "اللّهُـمَّ قِنـي عَذابَـكَ يَـوْمَ تَبْـعَثُ عِبـادَك",
            content = "اللّهُـمَّ قِنـي عَذابَـكَ يَـوْمَ تَبْـعَثُ عِبـادَك.",
            times = "ثلاث مرات",
            reference = ""
        ),
        AzkarItem(
            id = 4,
            title = "بِاسْـمِكَ اللّهُـمَّ أَمـوتُ وَأَحْـيا",
            content = "بِاسْـمِكَ اللّهُـمَّ أَمـوتُ وَأَحْـيا.",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 5,
            title = "الـحَمْدُ للهِ الَّذي أَطْـعَمَنا وَسَقـانا",
            content = "الـحَمْدُ للهِ الَّذي أَطْـعَمَنا وَسَقـانا، وَكَفـانا، وَآوانا، فَكَـمْ مِمَّـنْ لا كـافِيَ لَـهُ وَلا مُـؤْوي.",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 6,
            title = "اللّهُـمَّ عالِـمَ الغَـيبِ وَالشّـهادةِ",
            content = "اللّهُـمَّ عالِـمَ الغَـيبِ وَالشّـهادةِ فاطِـرَ السّماواتِ وَالأرْضِ رَبَّ كُـلِّ شَـيءٍ وَمَليـكَه، أَشْهـدُ أَنْ لا إِلـهَ إِلاّ أَنْت، أَعـوذُ بِكَ مِن شَـرِّ نَفْسـي، وَمِن شَـرِّ الشَّيْـطانِ وَشِـرْكِه، وَأَنْ أَقْتَـرِفَ عَلـى نَفْسـي سوءاً أَوْ أَجُـرَّهُ إِلـى مُسْـلِم .",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 7,
            title = "اللّهُـمَّ أَسْـلَمْتُ نَفْـسي إِلَـيْكَ",
            content = "اللّهُـمَّ أَسْـلَمْتُ نَفْـسي إِلَـيْكَ، وَفَوَّضْـتُ أَمْـري إِلَـيْكَ، وَوَجَّـهْتُ وَجْـهي إِلَـيْكَ، وَأَلْـجَـاْتُ ظَهـري إِلَـيْكَ، رَغْبَـةً وَرَهْـبَةً إِلَـيْكَ، لا مَلْجَـأَ وَلا مَنْـجـا مِنْـكَ إِلاّ إِلَـيْكَ، آمَنْـتُ بِكِتـابِكَ الّـذي أَنْزَلْـتَ وَبِنَبِـيِّـكَ الّـذي أَرْسَلْـت.",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 8,
            title = "سُبْحَانَ اللَّهِ",
            content = "سُبْحَانَ اللَّهِ.",
            times = "",
            reference = ""
        ),
        AzkarItem(
            id = 9,
            title = "الْحَمْدُ لِلَّهِ",
            content = "الْحَمْدُ لِلَّهِ.",
            times = "",
            reference = ""
        ),
        AzkarItem(
            id = 10,
            title = "اللَّهُ أَكْبَرُ",
            content = "اللَّهُ أَكْبَرُ.",
            times = "",
            reference = ""
        ),
        AzkarItem(
            id = 11,
            title = "يجمع كفيه ثم ينفث فيهما",
            content = "يجمع كفيه ثم ينفث فيهما والقراءة فيهما: {قل هو الله أحد} و{قل أعوذ برب الفلق} و{قل أعوذ برب الناس} ومسح ما استطاع من الجسد يبدأ بهما على رأسه ووجه وما أقبل من جسده.",
            times = "ثلاث مرات",
            reference = ""
        ),
        AzkarItem(
            id = 12,
            title = "سورة البقرة: أَعُوذُ بِاللهِ مِنْ الشَّيْطَانِ الرَّجِيمِ",
            content = "آمَنَ الرَّسُولُ بِمَا أُنْزِلَ إِلَيْهِ مِنْ رَبِّهِ وَالْمُؤْمِنُونَ ۚ كُلٌّ آمَنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لَا نُفَرِّقُ بَيْنَ أَحَدٍ مِنْ رُسُلِهِ ۚ وَقَالُوا سَمِعْنَا وَأَطَعْنَا ۖ غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ. لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ رَبَّنَا لَا تُؤَاخِذْنَا إِنْ نَّسِينَآ أَوْ أَخْطَأْنَا رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَّذِينَ مِنْ قَبْلِنَا رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا أَنْتَ مَوْلَانَا فَانْصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ.",
            times = "مرة واحدة",
            reference = "[البقرة 285 - 286]\nمن قرأ آيتين من آخر سورة البقرة في ليلة كفتاه."
        ),
        AzkarItem(
            id = 13,
            title = "آية الكرسى: أَعُوذُ بِاللهِ مِنْ الشَّيْطَانِ الرَّجِيمِ",
            content = "اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            times = "مرة واحدة",
            reference = "[البقرة 255]\nأجير من الجن حتى يصبح."
        ),
        AzkarItem(
            id = 14,
            title = "أذكار من قلق في فراشه ولم ينم",
            content = "عن بريدة رضي الله عنه، قال: شكا خالد بن الوليد رضي الله عنه إلى النبي صلى الله عليه وسلم فقال: يا رسول الله! ما أنام الليل من الأرق، فقال النبي صلى الله عليه وسلم: \"إذا أويت إلى فراشك فقل: اللهم رب السموات السبع وما أظلت، ورب الأرضين وما أقلت، ورب الشياطين وما أضلت، كن لي جارا من خلقك كلهم جميعا أن يفرط علي أحد منهم أو أن يبغي علي، عز جارك، وجل ثناؤك ولا إله غيرك، ولا إله إلا أنت\"\n\nعن عمرو بن شعيب، عن أبيه، عن جده: أن رسول الله صلى الله عليه وسلم كان يعلمهم من الفزع كلمات: \"أعوذ بكلمات الله التامة من غضبه وشر عباده، ومن همزات الشياطين وأن يحضرون\"",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 15,
            title = "أذكار الأحلام",
            content = "عن أبي قتادة رضي الله عنه قال: قال رسول الله صلى الله عليه وسلم: \"الرؤيا الصالحة\" وفي رواية \"الرؤيا الحسنة من الله، والحلم من الشيطان، فمن رأى شيئا يكرهه فلينفث عن شماله ثلاثا وليتعوذ من الشيطان، فإنها لا تضره\".",
            times = "مرة واحدة",
            reference = ""
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
                    text = "أذكار النوم"
                    ,fontSize = 24.sp,
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
                    SleepAzkarItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun SleepAzkarItemCard(item: AzkarItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF20424f).copy(alpha = 0.9f)
        ),
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
            if (item.reference.isNotEmpty()) {
                Text(
                    text = item.reference,
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E),
                    textAlign = TextAlign.End,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
