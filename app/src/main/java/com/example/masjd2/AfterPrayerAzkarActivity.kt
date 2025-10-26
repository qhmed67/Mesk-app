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

class AfterPrayerAzkarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Masjd2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1B2951)
                ) {
                    AfterPrayerAzkarPage(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun AfterPrayerAzkarPage(onBackClick: () -> Unit) {
    val azkarItems = listOf(
        AzkarItem(
            id = 1,
            title = "أَسْـتَغْفِرُ الله، أَسْـتَغْفِرُ الله، أَسْـتَغْفِرُ الله",
            content = "أَسْـتَغْفِرُ الله، أَسْـتَغْفِرُ الله، أَسْـتَغْفِرُ الله.",
            times = "",
            reference = "أذكار بعد السلام من الصلاة المفروضة"
        ),
        AzkarItem(
            id = 2,
            title = "اللّهُـمَّ أَنْـتَ السَّلامُ",
            content = "اللّهُـمَّ أَنْـتَ السَّلامُ ، وَمِـنْكَ السَّلام ، تَبارَكْتَ يا ذا الجَـلالِ وَالإِكْـرام .",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 3,
            title = "لا إلهَ إلاّ اللّه وحدَهُ لا شريكَ لهُ",
            content = "لا إلهَ إلاّ اللّه وحدَهُ لا شريكَ لهُ، لهُ المُـلْكُ ولهُ الحَمْد، وهوَ على كلّ شَيءٍ قَدير، اللّهُـمَّ لا مانِعَ لِما أَعْطَـيْت، وَلا مُعْطِـيَ لِما مَنَـعْت، وَلا يَنْفَـعُ ذا الجَـدِّ مِنْـكَ الجَـد.",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 4,
            title = "لا إلهَ إلاّ اللّه, وحدَهُ لا شريكَ لهُ",
            content = "لا إلهَ إلاّ اللّه, وحدَهُ لا شريكَ لهُ، لهُ الملكُ ولهُ الحَمد، وهوَ على كلّ شيءٍ قدير، لا حَـوْلَ وَلا قـوَّةَ إِلاّ بِاللهِ، لا إلهَ إلاّ اللّـه، وَلا نَعْـبُـدُ إِلاّ إيّـاه, لَهُ النِّعْـمَةُ وَلَهُ الفَضْل وَلَهُ الثَّـناءُ الحَـسَن، لا إلهَ إلاّ اللّهُ مخْلِصـينَ لَـهُ الدِّينَ وَلَوْ كَـرِهَ الكـافِرون.",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 5,
            title = "سُـبْحانَ اللهِ، والحَمْـدُ لله ، واللهُ أكْـبَر",
            content = "سُـبْحانَ اللهِ، والحَمْـدُ لله ، واللهُ أكْـبَر.",
            times = "",
            reference = ""
        ),
        AzkarItem(
            id = 6,
            title = "لا إلهَ إلاّ اللّهُ وَحْـدَهُ لا شريكَ لهُ",
            content = "لا إلهَ إلاّ اللّهُ وَحْـدَهُ لا شريكَ لهُ، لهُ الملكُ ولهُ الحَمْد، وهُوَ على كُلّ شَيءٍ قَـدير.",
            times = "مرة واحدة",
            reference = ""
        ),
        AzkarItem(
            id = 7,
            title = "بِسْمِ اللهِ الرَّحْمنِ الرَّحِيم",
            content = "قُلْ هُوَ ٱللَّهُ أَحَدٌ، ٱللَّهُ ٱلصَّمَدُ، لَمْ يَلِدْ وَلَمْ يُولَدْ، وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ.\n\nبِسْمِ اللهِ الرَّحْمنِ الرَّحِيم\nقُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ، مِن شَرِّ مَا خَلَقَ، وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ، وَمِن شَرِّ ٱلنَّفَّٰثَٰتِ فِى ٱلْعُقَدِ، وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ.\n\nبِسْمِ اللهِ الرَّحْمنِ الرَّحِيم\nقُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ، مَلِكِ ٱلنَّاسِ، إِلَٰهِ ٱلنَّاسِ، مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ، ٱلَّذِى يُوَسْوِسُ فِى صُدُورِ ٱلنَّاسِ، مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ.",
            times = "ثلاث مرات",
            reference = "ثلاث مرات بعد صلاتي الفجر والمغرب. ومرة بعد الصلوات الأخرى."
        ),
        AzkarItem(
            id = 8,
            title = "أَعُوذُ بِاللهِ مِنْ الشَّيْطَانِ الرَّجِيمِ",
            content = "اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            times = "مرة واحدة",
            reference = "[آية الكرسى - البقرة 255]"
        ),
        AzkarItem(
            id = 9,
            title = "لا إلهَ إلاّ اللّهُ وحْـدَهُ لا شريكَ لهُ",
            content = "لا إلهَ إلاّ اللّهُ وحْـدَهُ لا شريكَ لهُ، لهُ المُلكُ ولهُ الحَمْد، يُحيـي وَيُمـيتُ وهُوَ على كُلّ شيءٍ قدير.",
            times = "عشرة مرات",
            reference = "عَشْر مَرّات بَعْدَ المَغْرِب وَالصّـبْح."
        ),
        AzkarItem(
            id = 10,
            title = "اللّهُـمَّ إِنِّـي أَسْأَلُـكَ عِلْمـاً نافِعـاً",
            content = "اللّهُـمَّ إِنِّـي أَسْأَلُـكَ عِلْمـاً نافِعـاً وَرِزْقـاً طَيِّـباً ، وَعَمَـلاً مُتَقَـبَّلاً.",
            times = "مرة واحدة",
            reference = "بَعْد السّلامِ من صَلاةِ الفَجْر."
        ),
        AzkarItem(
            id = 11,
            title = "اللَّهُمَّ أَجِرْنِي مِنْ النَّار",
            content = "اللَّهُمَّ أَجِرْنِي مِنْ النَّار.",
            times = "سبعة مرات",
            reference = "بعد صلاة الصبح والمغرب."
        ),
        AzkarItem(
            id = 12,
            title = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
            content = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ.",
            times = "مرة واحدة",
            reference = ""
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.starry_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(-1f),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    text = "أذكار بعد الصلاة",
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
                    AfterPrayerAzkarItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun AfterPrayerAzkarItemCard(item: AzkarItem) {
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
            if (item.reference.isNotEmpty()) {
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
}
