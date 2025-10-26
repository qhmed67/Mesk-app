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

class HomeAzkarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Masjd2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1B2951)
                ) {
                    HomeAzkarPage(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeAzkarPage(onBackClick: () -> Unit) {
    val azkarItems = listOf(
        AzkarItem(
            id = 1,
            title = "أذكار الدخول للمنزل",
            content = "قد جاء حديثان -أحدهما صحيح والآخر حسن- فيما يقوله المسلم إذا دخل بيته:\n\nفأما الأول: فما رواه مسلم في صحيحه عَنْ جَابِرِ بْنِ عَبْدِ اللَّهِ، سَمِعَ النَّبِيَّ -صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ- يَقُولُ: إِذَا دَخَلَ الرَّجُلُ بَيْتَهُ فَذَكَرَ اللَّهَ عِنْدَ دُخُولِهِ، وَعِنْدَ طَعَامِهِ، قَالَ الشَّيْطَانُ: لَا مَبِيتَ لَكُمْ وَلَا عَشَاءَ. وَإِذَا دَخَلَ فَلَمْ يُذْكِرِ اللَّهَ عِنْدَ دُخُولِهِ قَالَ الشَّيْطَانُ: أَدْرَكْتُمُ الْمَبِيتَ. فَإِذَا لَمْ يَذْكُرِ اللَّهَ عِنْدَ طَعَامِهِ قَالَ: أَدْرَكْتُمُ الْمَبِيتَ وَالْعَشَاءَ.\n\nوأما الثاني: فما رواه الترمذي عَنْ أَنَسِ بْنِ مَالِكٍ، قَالَ: قَالَ لِي رَسُولُ اللهِ -صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ-: يَا بُنَيَّ، إِذَا دَخَلْتَ عَلَى أَهْلِكَ فَسَلِّمْ يَكُونُ بَرَكَةً عَلَيْكَ وَعَلَى أَهْلِ بَيْتِكَ. قال الترمذي: هَذَا حَدِيثٌ حَسَنٌ غَرِيبٌ. وحسنه الألباني في صحيح الترغيب والترهيب.\nفدل هذان الحديثان على ذكر الله مطلقًا، وعلى السلام.",
            times = "",
            reference = "رواه مسلم في صحيحه، ورواه الترمذي وحسنه الألباني"
        ),
        AzkarItem(
            id = 2,
            title = "أذكار الخروج من المنزل",
            content = "عن أمِّ سلمة رضي الله عنها قالت: ما خرج النبي صلى الله عليه وسلم من بيتي قطُّ إلا رفع طَرْفَهُ إلى السماء فقال: (اللَّهُمَّ أَعُوذُ بِكَ أَنْ أَضِلَّ، أَوْ أُضَلَّ، أَوْ أَزِلَّ، أَوْ أُزَلَّ، أَوْ أَظْلِمَ، أَوْ أُظْلَمَ، أَوْ أَجْهَلَ، أَوْ يُجْهَلَ عَلَيَّ)، وفي حديث أنس بن مالك رضي الله عنه أن النبي صلى الله عليه وسلم قال: (إِذَا خَرَجَ الرَّجُلُ مِنْ بَيْتِهِ فَقَالَ: بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، ولاَ حَوْلَ وَلاَ قُوَّةَ إِلاَّ بِاللَّهِ. قَالَ: يُقَالُ حِينَئِذٍ: هُدِيتَ، وَكُفِيتَ، وَوُقِيتَ، فَتَتَنَحَّى لَهُ الشَّيَاطِينُ، فَيَقُولُ لَهُ شَيْطَانٌ آخَرُ: كَيْفَ لَكَ بِرَجُلٍ قَدْ هُدِيَ وَكُفِيَ وَوُقِيَ؟)",
            times = "",
            reference = "رواهما أبو داود في سننه، وصححهما الألباني"
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
                    text = "أذكار المنزل",
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
                    HomeAzkarItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun HomeAzkarItemCard(item: AzkarItem) {
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
                    fontSize = 15.sp,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    lineHeight = 26.sp,
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
