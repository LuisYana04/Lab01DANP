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
fun TituloApp() {
    Text(
        text = "Gestor de Tareas",
        style =
            MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
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

@Composable
fun AppTareas() {
    var tareas by remember { mutableStateOf(listOf<Tarea>()) }
    var texto by remember { mutableStateOf("") }
    var contadorId by remember { mutableStateOf(0) }
    var tareaEditando by remember { mutableStateOf<Tarea?>(null) }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        TituloApp()
        Spacer(modifier = Modifier.height(16.dp))
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
            tareas = tareas,
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
        items(tareas) { tarea ->
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
    TarjetaBase {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Checkbox(
                    checked = tarea.completada,
                    onCheckedChange = { onToggle() }
                )
                Text(
                    text = tarea.titulo,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { onEdit() },
                    color = if (tarea.completada) Color.Gray else Color.Black
                )
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
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        contenido()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MaterialTheme {
        AppTareas()
    }
}