package com.example.byciclecomputer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.*
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.navigation.compose.composable
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.*







class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private val deviceAddress = "14:33:5C:2E:46:46"

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBluetoothPermission()

        val db = AppDatabase.getDatabase(this)

        userDao = db.userDao()

        setContent {
            BluetoothScreen()
        }
    }


    @SuppressLint("MissingPermission")
    @Composable
    fun BluetoothScreen() {
        var isDone by remember { mutableStateOf(true) }
        var hasUpdate by remember { mutableStateOf(false) }
        var showDialog by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false)}
        var isDataLoaded by remember { mutableStateOf(false) }
        var isConnected by remember { mutableStateOf(false) }
        var showConnectionError by remember { mutableStateOf(false) }
        var tireSize by remember { mutableStateOf("") }
        var bodyWeight by remember { mutableStateOf("") }
        var dailyGoal by remember { mutableStateOf("") }
        var time by remember { mutableDoubleStateOf(0.0) }
        var name by remember { mutableStateOf("") }
        val week by remember { mutableStateOf(Week(
            monday = Day(0.0, 0.0, 0.0,0.0),
            tuesday = Day(0.0, 0.0, 0.0,0.0),
            wednesday = Day(0.0, 0.0, 0.0,0.0),
            thursday = Day(0.0, 0.0, 0.0,0.0),
            friday = Day(0.0, 0.0, 0.0,0.0),
            saturday = Day(0.0, 0.0, 0.0,0.0),
            sunday = Day(0.0, 0.0, 0.0,0.0)
        )) }
        var distance by remember { mutableStateOf(0.0) }
        var calories by remember { mutableStateOf(0.0) }
        var k by remember { mutableStateOf(0f) }
        var rpm by remember { mutableStateOf(0f) }
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            loadUserData { userData ->
                userData?.let {
                    tireSize = it.tireSize.toString()
                    bodyWeight = it.bodyWeight.toString()
                    dailyGoal = it.dailyGoal.toString()
                    name = it.name
                    week.loadFromUserData(it)

                    resetDataAtStartOfWeek(context, week) {
                        saveUserData(bodyWeight.toDoubleOrNull() ?: 0.0, tireSize.toDoubleOrNull() ?: 0.0, name ?: "",dailyGoal.toDoubleOrNull() ?: 1.0, week)
                    }
                }
                isDataLoaded = true
            }
        }


        val validTireSize = tireSize.toDoubleOrNull() ?: 0.0
        val validBodyWeight = bodyWeight.toDoubleOrNull() ?: 0.0

        LaunchedEffect(isDone) {
            while (!isDone) {
                delay(1000)
                time++
                val newData = readBluetoothLine() ?: "Greška pri čitanju"
                parseBluetoothData(newData) { parsedRpm, parsedK ->
                    rpm = parsedRpm
                    k = parsedK
                }
                distance = calculateDistance(k, validTireSize)
                calories += roundToTwoDecimals(calculateCalories( rpm, validTireSize, validBodyWeight))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF191923))
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "Pozdrav $name, jesi li za vožnju biciklom?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFBFEF9),
                        fontSize = 25.sp,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { showSettings = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.settings_24px),
                        contentDescription = "Postavke",
                        tint = Color(0xFFA9A9A9),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if(isConnected) {
                InfoCard(
                    title = { Text("Brzina", color = Color(0xFFA9A9A9)) },
                    value = "%.2f".format(calculateVelocity(rpm, validTireSize))
                )
                InfoCard(title = { Text("Distanca", color = Color(0xFFA9A9A9), fontFamily = FontFamily.SansSerif) }, value = "%.2f".format(distance))
                InfoCard(title = { Text("Kalorije", color = Color(0xFFA9A9A9),fontFamily = FontFamily.SansSerif) }, value = "%.2f".format(calories))
                InfoCard(title = { Text("Trajanje", color = Color(0xFFA9A9A9),fontFamily = FontFamily.SansSerif) }, value = formatTime(time))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            isDone = !isDone
                            sendBluetoothData("B", if (isDone) "1" else "0")
                            if (isDone) {
                                showDialog = true
                                updateWeek(distance, calories, time, week)
                                hasUpdate = true
                            }
                            saveUserData(
                                bodyWeight.toDoubleOrNull() ?: 0.0,
                                tireSize.toDoubleOrNull() ?: 0.0,
                                name ?: "",
                                dailyGoal.toDoubleOrNull() ?: 1.0,
                                week
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0E79B2),
                            contentColor = Color(0xFFFBFEF9)
                        ),
                        modifier = Modifier
                            .width(250.dp)
                            .padding(horizontal = 40.dp)
                    ) {
                        Text(if (isDone) "Započni" else "Završi")
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                    },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            distance = 0.0
                            calories = 0.0
                            time = 0.0
                            rpm = 0f
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E79B2)),
                            ) {
                            Text("OK", color = Color(0xFFFBFEF9))
                        }
                    },
                    containerColor = Color(0xFF273043),
                    title = { Text("Rezultati treninga", color = Color(0xFFFBFEF9),fontFamily = FontFamily.SansSerif) },
                    text = {
                        Column {
                            Text("Prosječna brzina: ${"%.2f".format(distance / time * 3600)} km/h", color = Color(0xFFFBFEF9),fontFamily = FontFamily.SansSerif)
                            Text("Distanca: ${"%.2f".format(distance)} km", color = Color(0xFFFBFEF9),fontFamily = FontFamily.SansSerif)
                            Text("Kalorije: ${"%.2f".format(calories)} kcal", color = Color(0xFFFBFEF9),fontFamily = FontFamily.SansSerif)
                            Text("Trajanje: ${formatTime(time)}", color = Color(0xFFFBFEF9),fontFamily = FontFamily.SansSerif)
                        }
                    },
                )
            }

            if (showSettings) {
                AlertDialog(
                    onDismissRequest = {
                        showSettings = false
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSettings = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E79B2)),
                        ) {
                            Text("OK", color = Color(0xFFFBFEF9))
                        }
                    },
                    containerColor = Color(0xFF273043),
                    text = {
                        Column {
                            OutlinedTextField(
                                value = tireSize,
                                onValueChange = {
                                    tireSize = it
                                    saveUserData(bodyWeight.toDoubleOrNull() ?: 0.0, it.toDoubleOrNull() ?: 0.0, name ?: "", dailyGoal.toDoubleOrNull() ?: 1.0, week)
                                    if (isConnected) sendBluetoothData("T", tireSize)
                                },
                                label = { Text("Unesi promjer gume (m)", color = Color(0xFFFBFEF9), fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(color = Color.White)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = bodyWeight,
                                onValueChange = {
                                    bodyWeight = it
                                    saveUserData(it.toDoubleOrNull() ?: 0.0, tireSize.toDoubleOrNull() ?: 0.0, name ?: "", dailyGoal.toDoubleOrNull() ?: 1.0, week)
                                    if (isConnected) sendBluetoothData("M", bodyWeight)
                                },
                                label = { Text("Unesi kilažu (kg)", color = Color(0xFFFBFEF9), fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(color = Color.White)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = dailyGoal,
                                onValueChange = {
                                    dailyGoal = it
                                    saveUserData(bodyWeight.toDoubleOrNull() ?: 0.0, tireSize.toDoubleOrNull() ?: 0.0, name ?: "", it.toDoubleOrNull() ?: 1.0, week)
                                    if (isConnected) sendBluetoothData("M", bodyWeight)
                                },
                                label = { Text("Unesi dnevni cilj (km)", color = Color(0xFFFBFEF9), fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(color = Color.White)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    saveUserData(bodyWeight.toDoubleOrNull() ?: 0.0, tireSize.toDoubleOrNull() ?: 0.0, it, dailyGoal.toDoubleOrNull() ?: 1.0, week)
                                },
                                label = { Text("Unesi svoje ime", color = Color(0xFFFBFEF9), fontFamily = FontFamily.SansSerif) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(color = Color.White)
                            )
                        }
                    }
                )
            }


            if(!isConnected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val coroutineScope = rememberCoroutineScope()

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isConnected = connectToBluetooth(bodyWeight, tireSize, {
                                    showConnectionError = true
                                })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E79B2)),
                        modifier = Modifier
                            .width(250.dp)
                            .padding(horizontal = 40.dp)
                    ) {
                        Text("Poveži se sa uređajem", color = Color(0xFFFBFEF9),fontFamily = FontFamily.SansSerif)
                    }
                }
                if (showConnectionError) {
                    CannotConnect { showConnectionError = false }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val info = getThatDayInfo(week)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF273043))
            ){
                Spacer(modifier = Modifier.width(20.dp))
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = "Bike",
                            tint = Color(0xFF04e762),
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "${"%.2f".format(info[0])}",
                            color = Color(0xFFFBFEF9),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 22.sp
                        )
                        Text(
                            " km",
                            color = Color(0xFFA9A9A9),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Icon(
                            painter = painterResource(id = R.drawable.mode_heat_24px),
                            contentDescription = "Fire",
                            tint = Color(0xFFdc0073),
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "${"%.2f".format(info[1])}",
                            color = Color(0xFFFBFEF9),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 22.sp
                        )
                        Text(
                            " cal",
                            color = Color(0xFFA9A9A9),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.timer_24px),
                            contentDescription = "Bike",
                            tint = Color(0xFFf5b700),
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(5.dp))

                        Text("${"%.2f".format(info[2] / 60.0)}", color = Color(0xFFFBFEF9),fontFamily = FontFamily.SansSerif,fontSize = 22.sp)
                        Text(" min", color = Color(0xFFA9A9A9),fontFamily = FontFamily.SansSerif,fontSize = 22.sp)
                    }
                }
            }

            val goal = dailyGoal.toDoubleOrNull() ?: 1.0
            val percentage = info[0] / goal
            val progress = percentage.coerceIn(0.0, 1.0)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if(percentage < 1.0) Color(0xFF273043) else Color(0xFF0E79B2))
            ){

                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                )
                {
                    Text("${"%.2f".format(info[0])}", color = Color(0xFFFBFEF9),fontFamily = FontFamily.SansSerif,fontSize = 25.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text("/" + dailyGoal + " km", color = Color(0xFFA9A9A9), fontFamily = FontFamily.SansSerif,fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.width(100.dp))

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("${"%.2f".format(percentage * 100)} %", color = Color(0xFFFBFEF9),fontFamily = FontFamily.SansSerif,fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                    LinearProgressIndicator(
                        progress = progress.toFloat(),
                        modifier = Modifier
                            .width(150.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFFFBFEF9),
                        trackColor = Color(0xFF808080)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if(!hasUpdate){
                if (isDataLoaded) {
                    GraphScreen(week)
                } else {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }}
            else {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                loadUserData { userData ->
                    userData?.let {
                        week.loadFromUserData(it)
                        }
                    }
                hasUpdate = false;
                }
        }
    }


    fun Week.loadFromUserData(userData: UserData) {
        GetMonday().setAtributes(
            userData.calories[0],
            userData.distances[0],
            userData.averageVelocities[0],
            userData.durations[0]
        )
        GetTuesday().setAtributes(
            userData.calories[1],
            userData.distances[1],
            userData.averageVelocities[1],
            userData.durations[1]
        )
        GetWednesday().setAtributes(
            userData.calories[2],
            userData.distances[2],
            userData.averageVelocities[2],
            userData.durations[2]
        )
        GetThursday().setAtributes(
            userData.calories[3],
            userData.distances[3],
            userData.averageVelocities[3],
            userData.durations[3]
        )
        GetFriday().setAtributes(
            userData.calories[4],
            userData.distances[4],
            userData.averageVelocities[4],
            userData.durations[4]
        )
        GetSaturday().setAtributes(
            userData.calories[5],
            userData.distances[5],
            userData.averageVelocities[5],
            userData.durations[5]
        )
        GetSunday().setAtributes(
            userData.calories[6],
            userData.distances[6],
            userData.averageVelocities[6],
            userData.durations[6]
        )
    }


    fun parseBluetoothData(data: String, onParsed: (Float, Float) -> Unit) {
        val parsedValues = data.trim().split(" ").filter { it.isNotEmpty() }
        var rpm = 0f
        var k = 0f
        for (value in parsedValues) {
            when {
                value.startsWith("R:") -> rpm = value.substring(2).toFloatOrNull() ?: 0f
                value.startsWith("K:") -> k = value.substring(2).toFloatOrNull() ?: 0f
            }
        }
        onParsed(rpm, k)
    }


    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ), 1
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToBluetooth(
        bodyWeight: String,
        tireSize: String,
        onConnectionFailed: () -> Unit
    ): Boolean {
        val device: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.find { it.address == deviceAddress }
        device?.let {
            val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            try {
                bluetoothSocket = it.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                initBluetoothReader()
                sendBluetoothData("M", bodyWeight)
                sendBluetoothData("T", tireSize)
                return true
            } catch (e: IOException) {
                onConnectionFailed()
                return false
            }
        }
        onConnectionFailed()
        return false
    }



    private var bufferedReader: BufferedReader? = null

    private fun initBluetoothReader() {
        bluetoothSocket?.inputStream?.let {
            bufferedReader = BufferedReader(InputStreamReader(it))
        }
    }

    private fun readBluetoothLine(): String? {
        return try {
            val line = bufferedReader?.readLine()
            Log.d("Bluetooth", "Primljeno: $line")
            line
        } catch (e: IOException) {
            Log.e("Bluetooth", "Greška pri čitanju linije", e)
            null
        }
    }


    private fun sendBluetoothData(type: String, value: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outputStream: OutputStream? = bluetoothSocket?.outputStream
                val message = "$type:$value\n"
                outputStream?.write(message.toByteArray())
                outputStream?.flush()
                Log.d("Bluetooth", "Poslano: $message")
            } catch (e: IOException) {
                Log.e("Bluetooth", "Greška pri slanju", e)
            }
        }
    }

    private fun calculateExtent(tireSize: Double) : Double{
        return tireSize * 3.14
    }
    private fun calculateDistance(k : Float, tireSize : Double) : Double{
        if(k > 0) return roundToTwoDecimals(k * calculateExtent(tireSize) / 1000)
        return 0.0
    }

    private fun calculateVelocity( rpm: Float, tireSize: Double): Double {
        if (rpm > 0) return roundToTwoDecimals((calculateExtent(tireSize) * rpm) / 3.6)
        return 0.0
    }

    private fun calculateCalories( rpm : Float, tireSize : Double,bodyWeight : Double) : Double{
        var velocity = calculateVelocity(rpm,tireSize)
        var MET = 0.0
        if(velocity <= 16) MET = 4.0
        else if(velocity <= 19) MET = 6.8
        else if(velocity <= 22) MET = 8.0
        else if(velocity <= 26) MET = 10.0
        else MET = 12.0
        if(velocity > 0) return roundToTwoDecimals((bodyWeight * MET * 1 / 3600.0))
        return 0.0
    }

    private fun calculateNewAverageVelocity(
        distance: Double,
        newTime: Double,
        currentTime: Double
    ): Double {
        val totalTime = currentTime + newTime

        return if (totalTime > 0) distance / (totalTime.toDouble() / 3600.0) else 0.0
    }


    private fun updateWeek(distance: Double, calories: Double, time: Double, week: Week) {
        val day = getDayOfWeek()

        val dayData = when (day) {
            "ponedjeljak" -> week.GetMonday()
            "utorak" -> week.GetTuesday()
            "srijeda" -> week.GetWednesday()
            "četvrtak" -> week.GetThursday()
            "petak" -> week.GetFriday()
            "subota" -> week.GetSaturday()
            "nedjelja" -> week.GetSunday()
            else -> return
        }

        val newDistance = dayData.getDistance() + distance
        val newCalories = dayData.getCalories() + calories
        val newAverageVelocity = calculateNewAverageVelocity(newDistance, dayData.getTime(), time)

        dayData.setAtributes(newCalories, newDistance, newAverageVelocity, time)
    }



    private fun getDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "ponedjeljak"
            Calendar.TUESDAY -> "utorak"
            Calendar.WEDNESDAY -> "srijeda"
            Calendar.THURSDAY -> "četvrtak"
            Calendar.FRIDAY -> "petak"
            Calendar.SATURDAY -> "subota"
            Calendar.SUNDAY -> "nedjelja"
            else -> "Nepoznat dan"
        }
    }

    fun getThatDayInfo(week: Week): List<Double> {
        val day = getDayOfWeek()
        val dayData = when (day.lowercase()) {
            "ponedjeljak" -> week.GetMonday()
            "utorak" -> week.GetTuesday()
            "srijeda" -> week.GetWednesday()
            "četvrtak" -> week.GetThursday()
            "petak" -> week.GetFriday()
            "subota" -> week.GetSaturday()
            "nedjelja" -> week.GetSunday()
            else -> return emptyList()
        }

        return listOf(
            dayData.getDistance(),
            dayData.getCalories(),
            dayData.getTime()
        )
    }



    private fun saveUserData(bodyWeight: Double, tireSize: Double, name: String, dailyGoal: Double, week: Week) {
        CoroutineScope(Dispatchers.IO).launch {
            val calories = listOf(
                week.GetMonday().getCalories(),
                week.GetTuesday().getCalories(),
                week.GetWednesday().getCalories(),
                week.GetThursday().getCalories(),
                week.GetFriday().getCalories(),
                week.GetSaturday().getCalories(),
                week.GetSunday().getCalories()
            )

            val distances = listOf(
                week.GetMonday().getDistance(),
                week.GetTuesday().getDistance(),
                week.GetWednesday().getDistance(),
                week.GetThursday().getDistance(),
                week.GetFriday().getDistance(),
                week.GetSaturday().getDistance(),
                week.GetSunday().getDistance()
            )

            val averageVelocities = listOf(
                week.GetMonday().getAverageVelocity(),
                week.GetTuesday().getAverageVelocity(),
                week.GetWednesday().getAverageVelocity(),
                week.GetThursday().getAverageVelocity(),
                week.GetFriday().getAverageVelocity(),
                week.GetSaturday().getAverageVelocity(),
                week.GetSunday().getAverageVelocity()
            )

            val durations = listOf(
                week.GetMonday().getTime(),
                week.GetTuesday().getTime(),
                week.GetWednesday().getTime(),
                week.GetThursday().getTime(),
                week.GetFriday().getTime(),
                week.GetSaturday().getTime(),
                week.GetSunday().getTime()
            )

            val userData = UserData(
                bodyWeight = bodyWeight,
                tireSize = tireSize,
                dailyGoal = dailyGoal,
                name = name,
                calories = calories,
                distances = distances,
                averageVelocities = averageVelocities,
                durations = durations
            )

            userDao.insert(userData)
        }
    }


    @Composable
    private fun CannotConnect(onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color( 0xFF0E79B2)),
                ) {
                    Text("OK", color = Color(0xFFFBFEF9))
                }
            },
            title = { Text("Problem sa povezivanjem", color = Color(0xFFFBFEF9)) },
            text = { Text("Molim Vas pogledajte je li uređaj uključen i da se nalazite u blizinu uređaja", color = Color(0xFFFBFEF9)) },
            containerColor = Color(0xFF273043),
        )
    }

    private fun loadUserData(onDataLoaded: (UserData?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val userData = userDao.getUserData()
            withContext(Dispatchers.Main) {
                onDataLoaded(userData)
            }
        }
    }

    private fun formatTime(seconds: Double): String {
        val secondsInt = seconds.toInt()
        val hours = secondsInt / 3600
        val minutes = (secondsInt % 3600) / 60
        val secs = secondsInt % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

}


fun roundToTwoDecimals(value: Double): Double {
    return BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
}

@Composable
fun InfoCard(
    title: @Composable () -> Unit,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF273043))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            title()
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Glavni ekran", Icons.Default.Home)
    object Graphs : Screen("graphs", "Grafovi", Icons.Default.ShowChart)
    object Settings : Screen("settings", "Postavke", Icons.Default.Settings)
}



@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Graphs.route) { GraphsScreen() }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Graphs,
        Screen.Settings
    )
    val currentDestination by navController.currentBackStackEntryAsState()

    NavigationBar(
        containerColor = Color(0xFF273043)
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(imageVector = screen.icon, contentDescription = screen.label)
                },
                label = {
                    Text(screen.label)
                },
                selected = currentDestination?.destination?.route == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.LightGray,
                    selectedTextColor = Color.White,
                    indicatorColor = Color(0xFF3F4A60)
                )
            )
        }
    }
}


@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Glavni ekran", color = Color.White, fontSize = 24.sp)
    }
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Postavke", color = Color.White, fontSize = 24.sp)
    }
}

@Composable
fun GraphsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Grafovi", color = Color.White, fontSize = 24.sp)
    }
}



