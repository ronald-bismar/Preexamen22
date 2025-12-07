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

/**
 * Clase principal de la aplicación (Activity).
 * Es el punto de entrada de la app. Hereda de ComponentActivity para usar Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    // Referencias a la base de datos y al DAO (Data Access Object) para interactuar con ella.
    // 'lateinit' significa que se inicializarán más tarde (en el onCreate), no al momento de declarar.
    private lateinit var database: RegistroCivil
    private lateinit var ciudadanoDao: CiudadanoDao

    /**
     * onCreate: Método del ciclo de vida que se ejecuta al iniciar la actividad.
     * Aquí se configura la interfaz y se inicializan componentes esenciales.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos la instancia de la base de datos Room.
        database = RegistroCivil.getDatabase(this)
        // Obtenemos el DAO que nos permitirá hacer inserts/queries a la tabla de ciudadanos.
        ciudadanoDao = database.carnetDao()

        // setContent define el contenido visual de la actividad usando Compose.
        setContent {
            MaterialTheme {
                // Llamamos a nuestra pantalla principal pasando el DAO.
                RegistroCivilScreen(ciudadanoDao)
            }
        }
    }
}

/**
 * Función Composable: Representa la pantalla de la interfaz de usuario.
 * @param ciudadanoDao Objeto para realizar operaciones en la base de datos.
 */
@Composable
fun RegistroCivilScreen(ciudadanoDao: CiudadanoDao) {
    // Obtenemos el contexto actual (necesario para SharedPreferences y Toasts).
    val context = LocalContext.current
    
    // SharedPreferences: Almacenamiento ligero clave-valor. 
    // Lo usamos aquí para guardar datos temporalmente por si la app se cierra inesperadamente.
    val sharedPreferences = context.getSharedPreferences("RegistroCivilPrefs", Context.MODE_PRIVATE)

    // ESTADOS (State): Variables que Compose observa. Si cambian, la UI se actualiza automáticamente.
    // 'remember': Recuerda el valor a través de recomposiciones.
    // 'mutableStateOf': Crea un estado mutable observable.
    var fechaNac by remember { mutableStateOf("") }
    var paterno by remember { mutableStateOf("") }
    var materno by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var codigoGenerado by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) } // Guarda la imagen del QR
    var showSuccess by remember { mutableStateOf(false) } // Controla si mostrar mensaje de éxito
    var errorMessage by remember { mutableStateOf("") } // Guarda mensajes de error

    // Controlador para ocultar el teclado virtual.
    val keyboardController = LocalSoftwareKeyboardController.current

    // LaunchedEffect(Unit): Se ejecuta UNA VEZ cuando el componente entra en pantalla.
    // Útil para inicializar datos o recuperar estado guardado previamente.
    LaunchedEffect(Unit) {
        // Recuperamos los datos guardados en SharedPreferences (persistencia temporal).
        // Si no hay dato, devuelve "" (cadena vacía).
        fechaNac = sharedPreferences.getString("fecha_nac", "") ?: ""
        paterno = sharedPreferences.getString("paterno", "") ?: ""
        materno = sharedPreferences.getString("materno", "") ?: ""
        nombre = sharedPreferences.getString("nombre", "") ?: ""
        codigoGenerado = sharedPreferences.getString("codigo", "") ?: ""

        // Recuperar y reconstruir la imagen QR si existía.
        val qrBase64 = sharedPreferences.getString("qr_bitmap", "") ?: ""
        if (qrBase64.isNotEmpty()) {
            qrBitmap = base64ToBitmap(qrBase64)
        }
    }

    // Surface: Contenedor base con color de fondo.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        // Column: Organiza los elementos verticalmente, uno debajo del otro.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Margen externo
            horizontalAlignment = Alignment.CenterHorizontally // Centra los elementos horizontalmente
        ) {
            Spacer(modifier = Modifier.height(32.dp)) // Espacio vacío vertical

            Text(
                text = "REGISTRO CIVIL",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // --- CAMPOS DE ENTRADA ---
            // Cada campo está dentro de un Row (fila) con un Text (etiqueta) y un OutlinedTextField (input).
            
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
                        // Filtro: Solo permite dígitos y barras, y máximo 10 caracteres
                        if (newValue.length <= 10 && newValue.all { it.isDigit() || it == '/' }) {
                            fechaNac = newValue
                            // Si el usuario edita, limpiamos errores y estados anteriores
                            errorMessage = ""
                            codigoGenerado = ""
                            qrBitmap = null
                            showSuccess = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f) // Ocupa el espacio restante horizontalmente
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
                        paterno = it.uppercase() // Convertimos a mayúsculas automáticamente
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

            // Mensaje de error (se muestra solo si errorMessage no está vacío)
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar QR si existe (qrBitmap no es nulo)
            qrBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier.size(200.dp)
                ) {
                    // Muestra el Bitmap en la UI
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Código QR",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTONES ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // BOTÓN GENERAR
                Button(
                    onClick = {
                        // 1. Validaciones básicas: Campos vacíos
                        if (fechaNac.isEmpty() || paterno.isEmpty() ||
                            materno.isEmpty() || nombre.isEmpty()) {
                            errorMessage = "Todos los campos son obligatorios"
                            return@Button
                        }

                        // 2. Validación de formato de fecha
                        if (!validarFecha(fechaNac)) {
                            errorMessage = "Formato de fecha inválido. Use DD/MM/AAAA"
                            return@Button
                        }

                        keyboardController?.hide() // Ocultar teclado
                        errorMessage = ""

                        // 3. Lógica de negocio: Generar código único
                        codigoGenerado = generarCodigo(paterno, materno, nombre, fechaNac)

                        // 4. Generar imagen QR basada en el código
                        qrBitmap = generateQRCode(codigoGenerado)

                        // 5. GUARDAR EN SHAREDPREFERENCES (Persistencia local simple)
                        // 'edit()' abre el modo edición, 'apply()' guarda cambios asíncronamente.
                        with(sharedPreferences.edit()) {
                            putString("fecha_nac", fechaNac)
                            putString("paterno", paterno)
                            putString("materno", materno)
                            putString("nombre", nombre)
                            putString("codigo", codigoGenerado)
                            // Guardamos el QR como string Base64 porque SharedPreferences no guarda imágenes
                            putString("qr_bitmap", generateBase64(qrBitmap))
                            apply() 
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

                // BOTÓN ALMACENA
                Button(
                    onClick = {
                        // Usamos una Coroutine (Rutina) para operaciones de base de datos.
                        // Dispatchers.IO: Hilo optimizado para operaciones de Entrada/Salida (Bases de datos, Archivos, Red).
                        // Esto evita congelar la interfaz (Main Thread).
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
                                // Insertar en la base de datos (Room)
                                ciudadanoDao.insert(nuevoCiudadano)

                                // Cambiamos al hilo principal (Main) para actualizar la UI (Mostrar Toast, limpiar campos).
                                // NO se puede tocar la UI desde Dispatchers.IO.
                                withContext(Dispatchers.Main) {
                                    showSuccess = true
                                    Toast.makeText(
                                        context,
                                        "✓ Registro almacenado en la base de datos",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Limpiar SharedPreferences (ya está guardado en BD permanente)
                                    with(sharedPreferences.edit()) {
                                        clear()
                                        apply()
                                    }

                                    // Resetear formulario
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

                // BOTÓN SALIR (Limpia todo)
                Button(
                    onClick = {
                        // Limpiar variables de estado
                        fechaNac = ""
                        paterno = ""
                        materno = ""
                        nombre = ""
                        codigoGenerado = ""
                        qrBitmap = null
                        showSuccess = false
                        errorMessage = ""

                        // Limpiar SharedPreferences
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

            // Mensaje de éxito al guardar
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

/**
 * Función auxiliar para validar que la fecha tenga formato correcto (DD/MM/AAAA)
 * y que los valores sean lógicos.
 */
fun validarFecha(fecha: String): Boolean {
    if (fecha.length != 10) return false // Longitud exacta
    val partes = fecha.split("/")
    if (partes.size != 3) return false // Debe tener 3 partes

    // Convertir a números (retorna null si no es número)
    val dia = partes[0].toIntOrNull() ?: return false
    val mes = partes[1].toIntOrNull() ?: return false
    val anio = partes[2].toIntOrNull() ?: return false

    // Validar rangos
    return dia in 1..31 && mes in 1..12 && anio in 1900..2024
}

/**
 * Genera el código único concatenando las iniciales y la fecha.
 * Ejemplo: PATERNO: LOPEZ, MATERNO: PEREZ, NOMBRE: JUAN, FECHA: 01/01/2000
 * Resultado: LPJ-01012000
 */
fun generarCodigo(paterno: String, materno: String, nombre: String, fecha: String): String {
    // Obtiene el primer carácter o cadena vacía si no existe
    val inicialPaterno = if (paterno.isNotEmpty()) paterno[0] else ""
    val inicialMaterno = if (materno.isNotEmpty()) materno[0] else ""
    val inicialNombre = if (nombre.isNotEmpty()) nombre[0] else ""

    // Quita las barras de la fecha
    val fechaSinBarras = fecha.replace("/", "")

    return "$inicialPaterno$inicialMaterno$inicialNombre-$fechaSinBarras"
}

/**
 * Genera un Bitmap (imagen en memoria) de un código QR a partir de un texto.
 * Usa la librería ZXing.
 */
fun generateQRCode(text: String): Bitmap {
    val size = 512 // Tamaño en pixeles
    val qrCodeWriter = QRCodeWriter()
    // Crea una matriz de bits (true=negro, false=blanco)
    val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
    // Crea el bitmap vacío
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

    // Recorre la matriz y pinta los pixeles negros o blancos
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

/**
 * Convierte un Bitmap (imagen) a una cadena Base64 (texto).
 * Necesario para guardar la imagen en la base de datos o SharedPreferences.
 */
fun generateBase64(bitmap: Bitmap?): String {
    if (bitmap == null) return ""

    val outputStream = ByteArrayOutputStream()
    // Comprime el bitmap en formato PNG
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()

    // Codifica los bytes a texto Base64
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

/**
 * Función inversa: Convierte una cadena Base64 (texto) de vuelta a Bitmap (imagen).
 * Usado para recuperar la imagen guardada.
 */
fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}