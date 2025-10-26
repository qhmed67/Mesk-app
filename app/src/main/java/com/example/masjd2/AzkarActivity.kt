package com.example.masjd2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.masjd2.ui.theme.Masjd2Theme
import android.content.Intent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image

data class AzkarCategory(
    val title: String,
    val iconRes: Int,
    val color: Color
)

class AzkarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Masjd2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1B2951)
                ) {
                    AzkarPage(
                        onMorningAzkarClick = {
                            val intent = Intent(this@AzkarActivity, MorningAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onEveningAzkarClick = {
                            val intent = Intent(this@AzkarActivity, EveningAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onAfterPrayerAzkarClick = {
                            val intent = Intent(this@AzkarActivity, AfterPrayerAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onSleepAzkarClick = {
                            val intent = Intent(this@AzkarActivity, SleepAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onHomeAzkarClick = {
                            val intent = Intent(this@AzkarActivity, HomeAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onFoodAzkarClick = {
                            val intent = Intent(this@AzkarActivity, FoodAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onBathroomAzkarClick = {
                            val intent = Intent(this@AzkarActivity, BathroomAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onComprehensiveAzkarClick = {
                            val intent = Intent(this@AzkarActivity, ComprehensiveAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onRainAzkarClick = {
                            val intent = Intent(this@AzkarActivity, RainAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onJoyAzkarClick = {
                            val intent = Intent(this@AzkarActivity, JoyAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onSorrowAzkarClick = {
                            val intent = Intent(this@AzkarActivity, SorrowAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onFearAzkarClick = {
                            val intent = Intent(this@AzkarActivity, FearAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onIllnessAzkarClick = {
                            val intent = Intent(this@AzkarActivity, IllnessAzkarActivity::class.java)
                            startActivity(intent)
                        },
                        onTravelAzkarClick = {
                            val intent = Intent(this@AzkarActivity, TravelAzkarActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AzkarPage(
    onMorningAzkarClick: () -> Unit, 
    onEveningAzkarClick: () -> Unit,
    onAfterPrayerAzkarClick: () -> Unit,
    onSleepAzkarClick: () -> Unit,
    onHomeAzkarClick: () -> Unit,
    onFoodAzkarClick: () -> Unit,
    onBathroomAzkarClick: () -> Unit,
    onComprehensiveAzkarClick: () -> Unit,
    onRainAzkarClick: () -> Unit,
    onJoyAzkarClick: () -> Unit,
    onSorrowAzkarClick: () -> Unit,
    onFearAzkarClick: () -> Unit,
    onIllnessAzkarClick: () -> Unit,
    onTravelAzkarClick: () -> Unit
) {
    val azkarCategories = listOf(
        AzkarCategory("أذكار المساء", R.drawable.partly_cloudy_night_24px, Color(0xFF5C6BC0)),
        AzkarCategory("أذكار الصباح", R.drawable.wb_sunny_24px, Color(0xFFFFB74D)),
        AzkarCategory("أذكار قبل النوم", R.drawable.hotel_24px, Color(0xFF9C27B0)),
        AzkarCategory("أذكار بعد الصلاة", R.drawable.mosque_24px, Color(0xFF4CAF50)),
        AzkarCategory("أذكار دخول و خروج المنزل", R.drawable.directions_walk_24px, Color(0xFF795548)),
        AzkarCategory("أذكار الطعام و الشراب", R.drawable.flatware_24px, Color(0xFFFF5722)),
        AzkarCategory("أذكار دخول الخلاء و الخروج منه", R.drawable.bathtub_24px, Color(0xFF607D8B)),
        AzkarCategory("أذكار الركوب", R.drawable.directions_bus_24px, Color(0xFF3F51B5)),
        AzkarCategory("أذكار الخوف", R.drawable.sentiment_stressed_24px, Color(0xFFE91E63)),
        AzkarCategory("أذكار الحزن", R.drawable.sentiment_dissatisfied_24px, Color(0xFF9E9E9E)),
        AzkarCategory("أذكار الفرح", R.drawable.sentiment_satisfied_24px, Color(0xFFFFC107)),
        AzkarCategory("أذكار المطر", R.drawable.rainy_24px, Color(0xFF2196F3)),
        AzkarCategory("أذكار المرض", R.drawable.sick_24px, Color(0xFFF44336)),
        AzkarCategory("أذكار أخرى", R.drawable.others, Color(0xFF9E9E9E))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.starry_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modern Title with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
            Icon(
                painter = painterResource(id = R.drawable.menu_book_24px),
                contentDescription = "أذكار",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "الأذكار",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            
            // Grid of Azkar categories
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(azkarCategories) { category ->
                    AzkarCard(
                        category = category,
                        onClick = {
                            when (category.title) {
                                "أذكار الصباح" -> onMorningAzkarClick()
                                "أذكار المساء" -> onEveningAzkarClick()
                                "أذكار بعد الصلاة" -> onAfterPrayerAzkarClick()
                                "أذكار قبل النوم" -> onSleepAzkarClick()
                                "أذكار دخول و خروج المنزل" -> onHomeAzkarClick()
                                "أذكار الطعام و الشراب" -> onFoodAzkarClick()
                                "أذكار دخول الخلاء و الخروج منه" -> onBathroomAzkarClick()
                                "أذكار الركوب" -> onTravelAzkarClick()
                                "أذكار الخوف" -> onFearAzkarClick()
                                "أذكار الحزن" -> onSorrowAzkarClick()
                                "أذكار الفرح" -> onJoyAzkarClick()
                                "أذكار المطر" -> onRainAzkarClick()
                                "أذكار المرض" -> onIllnessAzkarClick()
                                "أذكار أخرى" -> onComprehensiveAzkarClick()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AzkarCard(
    category: AzkarCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF20424f).copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            category.color.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon with colored background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = category.color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = category.iconRes),
                        contentDescription = category.title,
                        tint = category.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = category.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
