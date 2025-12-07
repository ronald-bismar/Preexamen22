package com.example.preexamen22

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.preexamen22.room.CiudadanoDao
import com.example.preexamen22.room.CiudadanoEntity
import com.example.preexamen22.room.RegistroCivil
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    private lateinit var database: RegistroCivil
    private lateinit var ciudadanoDao: CiudadanoDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = RegistroCivil.getDatabase(this)
        ciudadanoDao = database.carnetDao()

        setContent {
            MaterialTheme {
                RegistroCivilScreen(ciudadanoDao)
            }
        }
    }
}

@Composable
fun RegistroCivilScreen(ciudadanoDao: CiudadanoDao) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("RegistroCivilPrefs", Context.MODE_PRIVATE)

    var fechaNac by remember { mutableStateOf("") }
    var paterno by remember { mutableStateOf("") }
    var materno by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var codigoGenerado by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    // 🔥 CARGAR DATOS PERSISTENTES AL INICIAR LA APP
    LaunchedEffect(Unit) {
        fechaNac = sharedPreferences.getString("fecha_nac", "") ?: ""
        paterno = sharedPreferences.getString("paterno", "") ?: ""
        materno = sharedPreferences.getString("materno", "") ?: ""
        nombre = sharedPreferences.getString("nombre", "") ?: ""
        codigoGenerado = sharedPreferences.getString("codigo", "") ?: ""

        // Restaurar QR si existe
        val qrBase64 = sharedPreferences.getString("qr_bitmap", "") ?: ""
        if (qrBase64.isNotEmpty()) {
            qrBitmap = base64ToBitmap(qrBase64)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "REGISTRO CIVIL",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // FECHA_NAC
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FECHA_NAC",
                    fontSize = 16.sp,
                    modifier = Modifier.width(140.dp)
                )

                OutlinedTextField(
                    value = fechaNac,
                    onValueChange = { newValue ->
                        if (newValue.length <= 10 && newValue.all { it.isDigit() || it == '/' }) {
                            fechaNac = newValue
                            errorMessage = ""
                            codigoGenerado = ""
                            qrBitmap = null
                            showSuccess = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    placeholder = { Text("DD/MM/AAAA", fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color.Black
                    )
                )
            }

            // PATERNO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PATERNO",
                    fontSize = 16.sp,
                    modifier = Modifier.width(140.dp)
                )

                OutlinedTextField(
                    value = paterno,
                    onValueChange = {
                        paterno = it.uppercase()
                        errorMessage = ""
                        codigoGenerado = ""
                        qrBitmap = null
                        showSuccess = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color.Black
                    )
                )
            }

            // MATERNO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MATERNO",
                    fontSize = 16.sp,
                    modifier = Modifier.width(140.dp)
                )

                OutlinedTextField(
                    value = materno,
                    onValueChange = {
                        materno = it.uppercase()
                        errorMessage = ""
                        codigoGenerado = ""
                        qrBitmap = null
                        showSuccess = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color.Black
                    )
                )
            }

            // NOMBRE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NOMBRE",
                    fontSize = 16.sp,
                    modifier = Modifier.width(140.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it.uppercase()
                        errorMessage = ""
                        codigoGenerado = ""
                        qrBitmap = null
                        showSuccess = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color.Black
                    )
                )
            }

            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar QR si existe
            qrBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier.size(200.dp)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Código QR",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        // Validaciones
                        if (fechaNac.isEmpty() || paterno.isEmpty() ||
                            materno.isEmpty() || nombre.isEmpty()) {
                            errorMessage = "Todos los campos son obligatorios"
                            return@Button
                        }

                        // Validar formato de fecha
                        if (!validarFecha(fechaNac)) {
                            errorMessage = "Formato de fecha inválido. Use DD/MM/AAAA"
                            return@Button
                        }

                        keyboardController?.hide()
                        errorMessage = ""

                        // Generar código
                        codigoGenerado = generarCodigo(paterno, materno, nombre, fechaNac)

                        // Generar QR
                        qrBitmap = generateQRCode(codigoGenerado)

                        // 💾 GUARDAR EN SHAREDPREFERENCES (PERSISTENCIA)
                        with(sharedPreferences.edit()) {
                            putString("fecha_nac", fechaNac)
                            putString("paterno", paterno)
                            putString("materno", materno)
                            putString("nombre", nombre)
                            putString("codigo", codigoGenerado)
                            putString("qr_bitmap", generateBase64(qrBitmap))
                            apply() // Guarda de forma asíncrona
                        }

                        Toast.makeText(
                            context,
                            "✓ Datos guardados temporalmente",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "GENERA",
                        fontSize = 11.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (codigoGenerado.isNotEmpty() && qrBitmap != null) {
                                val qrBase64 = generateBase64(qrBitmap)
                                val nuevoCiudadano = CiudadanoEntity(
                                    fecha_nac = fechaNac,
                                    paterno = paterno,
                                    materno = materno,
                                    nombre = nombre,
                                    codigo = codigoGenerado,
                                    qr = qrBase64
                                )
                                ciudadanoDao.insert(nuevoCiudadano)

                                withContext(Dispatchers.Main) {
                                    showSuccess = true
                                    Toast.makeText(
                                        context,
                                        "✓ Registro almacenado en la base de datos",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // 🗑️ LIMPIAR SHAREDPREFERENCES después de guardar en BD
                                    with(sharedPreferences.edit()) {
                                        clear()
                                        apply()
                                    }

                                    // Limpiar campos después de guardar
                                    fechaNac = ""
                                    paterno = ""
                                    materno = ""
                                    nombre = ""
                                    codigoGenerado = ""
                                    qrBitmap = null
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Primero debe generar el código"
                                }
                            }
                        }
                    },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "ALMACENA",
                        fontSize = 11.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        // Limpiar campos
                        fechaNac = ""
                        paterno = ""
                        materno = ""
                        nombre = ""
                        codigoGenerado = ""
                        qrBitmap = null
                        showSuccess = false
                        errorMessage = ""

                        // 🗑️ LIMPIAR SHAREDPREFERENCES
                        with(sharedPreferences.edit()) {
                            clear()
                            apply()
                        }

                        Toast.makeText(
                            context,
                            "✓ Datos temporales eliminados",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF424242)
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "SALIR",
                        fontSize = 11.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Mensaje de éxito
            if (showSuccess) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "✓ Registro guardado en la base de datos",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun validarFecha(fecha: String): Boolean {
    if (fecha.length != 10) return false
    val partes = fecha.split("/")
    if (partes.size != 3) return false

    val dia = partes[0].toIntOrNull() ?: return false
    val mes = partes[1].toIntOrNull() ?: return false
    val anio = partes[2].toIntOrNull() ?: return false

    return dia in 1..31 && mes in 1..12 && anio in 1900..2024
}

fun generarCodigo(paterno: String, materno: String, nombre: String, fecha: String): String {
    val inicialPaterno = if (paterno.isNotEmpty()) paterno[0] else ""
    val inicialMaterno = if (materno.isNotEmpty()) materno[0] else ""
    val inicialNombre = if (nombre.isNotEmpty()) nombre[0] else ""

    // Remover las barras de la fecha para obtener solo números
    val fechaSinBarras = fecha.replace("/", "")

    return "$inicialPaterno$inicialMaterno$inicialNombre-$fechaSinBarras"
}

fun generateQRCode(text: String): Bitmap {
    val size = 512
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }

    return bitmap
}

fun generateBase64(bitmap: Bitmap?): String {
    if (bitmap == null) return ""

    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()

    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}