package com.nocturnal.myalarmwithcompose



import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturnal.myalarmwithcompose.receiver.AlarmReceiver
import java.util.*

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnhancedAlarmApp()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
    @Composable
    fun EnhancedAlarmApp() {
        var hour by remember { mutableStateOf(0) }
        var minute by remember { mutableStateOf(0) }
        var showTimePicker by remember { mutableStateOf(false) }
        val alarms = remember { mutableStateListOf<Pair<Int, Int>>() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Alarm App", fontSize = 20.sp) },

                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {

                    if (showTimePicker) {
                        TimePickerDialog(
                            initialHour = hour,
                            initialMinute = minute,
                            onTimePicked = { h, m ->
                                hour = h
                                minute = m
                                alarms.add(Pair(h, m))
                                showTimePicker = false
                                setAlarm(h, m)
                            },
                            onDismiss = { showTimePicker = false }
                        )
                    }

                    AlarmTimeSelector(hour, minute, onPickTime = { showTimePicker = true })

                    Spacer(modifier = Modifier.height(16.dp))

                    AlarmList(alarms)
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showTimePicker = true }, containerColor = Color(
                    0xFF2196F3
                )
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                        contentDescription = "Add Alarm",
                        tint = Color.White

                    )
                }
            }
        )
    }


    @Composable
    fun AlarmTimeSelector(hour: Int, minute: Int, onPickTime: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4B9BF1))
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Selected Alarm Time", color = Color.White, fontSize = 18.sp)
                Text(
                    text = String.format("%02d:%02d", hour, minute),
                    color = Color.White,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onPickTime, colors = ButtonDefaults.buttonColors(Color(0xFFFFEB3B)),) {
                    Text("Pick a Time", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    @Composable
    fun AlarmList(alarms: List<Pair<Int, Int>>) {
        if (alarms.isNotEmpty()) {
            Text(
                "Active Alarms",
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 20.sp,
                color = Color(0xFF6200EA)
            )

            LazyColumn {
                items(alarms.size) { index ->
                    AlarmItem(alarms[index].first, alarms[index].second)
                }
            }
        } else {
            Text("No alarms set.", fontSize = 16.sp, color = Color.Gray)
        }
    }

    @Composable
    fun AlarmItem(hour: Int, minute: Int) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),

            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d:%02d", hour, minute),
                    fontSize = 18.sp,
                    color = Color.Black
                )
                IconButton(onClick = { TODO("hadxi khasni nzid lih bax ytmsa7" +
                        "") }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                        contentDescription = "Delete Alarm",
                        tint = Color.Red
                    )
                }
            }
        }
    }

    @Composable
    fun TimePickerDialog(
        initialHour: Int,
        initialMinute: Int,
        onTimePicked: (Int, Int) -> Unit,
        onDismiss: () -> Unit
    ) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, initialHour)
        calendar.set(Calendar.MINUTE, initialMinute)

        MaterialTheme {
            android.app.TimePickerDialog(
                this@MainActivity,
                { _, h, m -> onTimePicked(h, m) },
                initialHour,
                initialMinute,
                true
            ).show()
        }
    }

    private fun setAlarm(hour: Int, minute: Int) {
        checkAndRequestExactAlarmPermission(this)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(this, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
    }
}

private fun checkAndRequestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        }
    }
}


