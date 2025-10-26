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

class ComprehensiveAzkarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Masjd2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1B2951)
                ) {
                    ComprehensiveAzkarPage(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun ComprehensiveAzkarPage(onBackClick: () -> Unit) {
    val azkarItems = listOf(
        AzkarItem(
            id = 1,
            title = "الذكر عند هبوب الرياح",
            content = "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَهَا، وَخَيْرَ مَا فِيهَا، وَخَيْرَ مَا أُرْسِلَتْ بِهِ، وَأَعُوذُ بِكَ مِنْ شَرِّهَا، وَشَرِّ مَا فِيهَا، وَشَرِّ مَا أُرْسِلَتْ بِهِ.",
            times = "",
            reference = "رواه مسلم (رقم 899)."
        ),
        AzkarItem(
            id = 2,
            title = "الذكر عند العطاس",
            content = "إذا عطس: الْحَمْدُ لِلَّهِ. فيُقال له: يَرْحَمُكَ اللَّهُ. فيرد: يَهْدِيكُمُ اللَّهُ وَيُصْلِحُ بَالَكُمْ.",
            times = "",
            reference = "رواه البخاري (رقم 6224) ومسلم (رقم 2991)."
        ),
        AzkarItem(
            id = 3,
            title = "الذكر عند رؤية المُبتلى",
            content = "الْحَمْدُ لِلَّهِ الَّذِي عَافَانِي مِمَّا ابْتَلَاكَ بِهِ، وَفَضَّلَنِي عَلَى كَثِيرٍ مِمَّنْ خَلَقَ تَفْضِيلًا.",
            times = "",
            reference = "رواه الترمذي (رقم 3431)، وصححه الألباني."
        ),
        AzkarItem(
            id = 4,
            title = "الذكر عند المصيبة",
            content = "إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ، اللَّهُمَّ أْجُرْنِي فِي مُصِيبَتِي، وَاخْلُفْ لِي خَيْرًا مِنْهَا.",
            times = "",
            reference = "رواه مسلم (رقم 918)."
        ),
        AzkarItem(
            id = 5,
            title = "الذكر عند لبس الثوب الجديد",
            content = "اللَّهُمَّ لَكَ الْحَمْدُ، أَنْتَ كَسَوْتَنِيهِ، أَسْأَلُكَ خَيْرَهُ وَخَيْرَ مَا صُنِعَ لَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّهِ وَشَرِّ مَا صُنِعَ لَهُ.",
            times = "",
            reference = "رواه أبو داود (رقم 4020)، وصححه الألباني."
        ),
        AzkarItem(
            id = 6,
            title = "عند سماع الرعد",
            content = "سُبْحَانَ الَّذِي يُسَبِّحُ الرَّعْدُ بِحَمْدِهِ وَالْمَلَائِكَةُ مِنْ خِيفَتِهِ.",
            times = "",
            reference = "[الرعد: 13] أخرجه الإمام مالك في الموطأ (حديث رقم 957) بإسناد صحيح."
        ),
        AzkarItem(
            id = 7,
            title = "عند سماع وفاة مسلم",
            content = "إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ.",
            times = "",
            reference = "[البقرة: 156] متفق عليه."
        ),
        AzkarItem(
            id = 8,
            title = "عند الغضب",
            content = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ.",
            times = "",
            reference = "رواه البخاري (رقم 3282) ومسلم (رقم 2610). ✅ صحيح النبي ﷺ قال: \"إني لأعلم كلمة لو قالها لذهب عنه ما يجد...\""
        ),
        AzkarItem(
            id = 9,
            title = "عند زيارة المريض",
            content = "لَا بَأْسَ طَهُورٌ إِنْ شَاءَ اللَّهُ.",
            times = "",
            reference = "رواه البخاري (رقم 5662). ✅ صحيح"
        ),
        AzkarItem(
            id = 10,
            title = "عند رؤية الكعبة لأول مرة",
            content = "اللَّهُمَّ زِدْ هَذَا الْبَيْتَ تَشْرِيفًا وَتَعْظِيمًا وَتَكْرِيمًا وَمَهَابَةً، وَزِدْ مَنْ شَرَّفَهُ وَكَرَّمَهُ مِمَّنْ حَجَّهُ أَوِ اعْتَمَرَهُ تَشْرِيفًا وَتَعْظِيمًا وَتَكْرِيمًا وَبِرًّا.",
            times = "",
            reference = "رواه الشافعي في الأم (2/173) بإسناد حسن."
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
                    text = "أذكار أخرى",
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
                    ComprehensiveAzkarItemCard(item = item)
                }
                
                // Special container at the end
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1D3D3D).copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هل لي نصيب من دعوة ؟ ❤️",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComprehensiveAzkarItemCard(item: AzkarItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1D3D3D).copy(alpha = 0.9f)
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
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
