package com.example.nuevas_plataformas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nuevas_plataformas.ui.theme.Nuevas_PlataformasTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.BorderStroke



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppTareas()
            }
        }
    }
}


data class Tarea(
    val id: Int,
    val titulo: String,
    val completada: Boolean = false

    )
@Composable
fun TituloApp(modifier: Modifier = Modifier) {
    Text(
        text = "Gestor de Tareas",
        style = MaterialTheme.typography.headlineMedium.copy(
            textDecoration = TextDecoration.None
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
@Composable
fun BotonPrimario(
    texto: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(texto)
    }
}
val Context.dataStore by preferencesDataStore(name = "tareas")
@Composable
fun AppTareas() {
    val context = LocalContext.current

    val TAREAS_KEY = stringPreferencesKey("tareas")
    var tareas by remember { mutableStateOf(listOf<Tarea>()) }
    var texto by remember { mutableStateOf("") }
    var contadorId by remember { mutableStateOf(0) }
    var tareaEditando by remember { mutableStateOf<Tarea?>(null) }
    var filtro by remember { mutableStateOf("todas") }
    val tareasFiltradas = when (filtro) {
        "completadas" -> tareas.filter { it.completada }
        "pendientes" -> tareas.filter { !it.completada }
        else -> tareas
    }

    var cargado by remember { mutableStateOf(false) }
    var temaOscuro by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (temaOscuro) darkColorScheme() else lightColorScheme()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(16.dp)

        ) {

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TituloApp(modifier = Modifier.weight(1f))
                BotonTema(temaOscuro = temaOscuro, onToggle = { temaOscuro = !temaOscuro })
            }

        }
    }

    LaunchedEffect(Unit) {
        val preferences = context.dataStore.data.first()
        val tareasGuardadas = preferences[TAREAS_KEY] ?: ""
        if (tareasGuardadas.isNotEmpty()) {
            val lista = convertirDesdeJson(tareasGuardadas)
            tareas = lista
            contadorId = (lista.maxOfOrNull { it.id } ?: -1) + 1
        }
        cargado = true
    }

    LaunchedEffect(tareas, cargado) {
        if (cargado) {
            context.dataStore.edit { preferences ->
                preferences[TAREAS_KEY] = convertirAJson(tareas)
            }
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { filtro = "todas" }) {
                Text("Todas")
            }
            Button(onClick = { filtro = "completadas" }) {
                Text("Completadas")
            }
            Button(onClick = { filtro = "pendientes" }) {
                Text("Pendientes")
            }
        }

        CampoTexto(
            valor = texto,
            onValorChange = { texto = it },
            label = "Nueva tarea"
        )
        Spacer(modifier = Modifier.height(8.dp))
        BotonPrimario(
            texto = if (tareaEditando == null) "Agregar tarea" else "Guardar cambios"
        ) {
            if (texto.isNotBlank()) {
                if (tareaEditando == null) {
                    tareas = tareas + Tarea(contadorId++, texto)
                } else {
                    tareas = tareas.map {
                        if (it.id == tareaEditando!!.id)
                            it.copy(titulo = texto)
                        else it
                    }
                    tareaEditando = null
                }
                texto = ""
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ListaTareas(
            tareas = tareasFiltradas,
            onToggle = { tarea ->
                tareas = tareas.map {
                    if (it.id == tarea.id) it.copy(completada = !it.completada)
                    else it
                }
            },
            onDelete = { tarea ->
                tareas = tareas.filter { it.id != tarea.id }
            },
            onEdit = { tarea ->
                texto = tarea.titulo
                tareaEditando = tarea
            }

        )
    }
}
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth()
    )
}
@Composable
fun ListaTareas(
    tareas: List<Tarea>,
    onToggle: (Tarea) -> Unit,
    onDelete: (Tarea) -> Unit,
    onEdit: (Tarea) -> Unit

) {
    LazyColumn {

        items(tareas, key = { it.id }) { tarea ->
            ItemTarea(
                tarea = tarea,
                onToggle = { onToggle(tarea) },
                onDelete = { onDelete(tarea) },
                onEdit = { onEdit(tarea) }
            )
        }
    }
}
@Composable
fun ItemTarea(
    tarea: Tarea,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val colorFondo = if (tarea.completada)  // <- aquí
        MaterialTheme.colorScheme.surfaceVariant
    else
        MaterialTheme.colorScheme.surface

    val colorBorde = if (tarea.completada)  // <- y aquí
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    else
        MaterialTheme.colorScheme.outline

    TarjetaBase {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Checkbox(
                    checked = tarea.completada,
                    onCheckedChange = { onToggle() }
                )
                Text(
                    text = tarea.titulo,
                    style = if (tarea.completada)
                        MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = TextDecoration.LineThrough
                )
                    else MaterialTheme.typography.bodyLarge,
                    color = if (tarea.completada)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onEdit() }
                )
                if (tarea.completada) {
                    Text(
                        text = "Completada",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}
@Composable
fun TarjetaBase(
    modifier: Modifier = Modifier,
    colorFondo: Color = MaterialTheme.colorScheme.surface,
    colorBorde: Color = MaterialTheme.colorScheme.outline,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(1.dp, colorBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        contenido()
    }
}
fun convertirAJson(tareas: List<Tarea>): String {
    val gson = Gson()
    return gson.toJson(tareas)
}

fun convertirDesdeJson(json: String): List<Tarea> {
    val gson = Gson()
    val tipo = object : TypeToken<List<Tarea>>() {}.type
    return gson.fromJson(json, tipo)
}
@Composable
fun BotonTema(temaOscuro: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (temaOscuro) Icons.Default.WbSunny else Icons.Default.DarkMode,
            contentDescription = if (temaOscuro) "Modo claro" else "Modo oscuro"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MaterialTheme {
        AppTareas()
    }
}