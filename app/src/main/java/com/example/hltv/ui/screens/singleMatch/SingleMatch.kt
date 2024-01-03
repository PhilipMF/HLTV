package com.example.hltv.ui.screens.singleMatch

import android.graphics.Paint.Align
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hltv.R

@Composable
fun SingleMatchScreen(viewModel: SingleMatchViewModel){
val R = MaterialTheme
    val singlematches by viewModel.matches.collectAsState()
//teamCard(modifier = Modifier, R = R, text1 = singlematches.toString(), text2 = "test")
}

@Composable
fun EventImage(
    image: Painter? = null,
    overlayImage1: Painter,
    overlayImage2: Painter,
    scoreText: String
) {
    Box {
        Image(
            painter = image ?: painterResource(id = R.drawable.event_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)

        ) {

            Image(
                painter = overlayImage1,
                contentDescription = null,
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally


            ) {
                Text(
                    text = scoreText,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Live",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Red)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = overlayImage2,
                contentDescription = null,
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)

            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SingleMatchScreenPreview() {
    SingleMatchScreen(viewModel = SingleMatchViewModel())
    EventImage(overlayImage1 = painterResource(id = R.drawable.astralis_logo), overlayImage2 = painterResource(id = R.drawable.astralis_logo), scoreText = "10-10")

}