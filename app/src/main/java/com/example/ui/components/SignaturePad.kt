package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    initialSignaturePath: String? = null,
    onSignatureSaved: (String) -> Unit
) {
    val context = LocalContext.current
    val currentPath = remember { mutableStateOf(Path()) }
    val paths = remember { mutableStateListOf<Path>() }
    var hasDrawn by remember { mutableStateOf(false) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Assinatura do Fiscal / Responsável",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        paths.clear()
                        currentPath.value = Path()
                        hasDrawn = false
                        savedMessage = null
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("clear_signature_button")
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Limpar", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = {
                        val filePath = savePathsToBitmap(context, paths)
                        if (filePath != null) {
                            onSignatureSaved(filePath)
                            savedMessage = "Assinatura capturada com sucesso!"
                        }
                    },
                    enabled = hasDrawn,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("save_signature_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Salvar", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Salvar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(androidx.compose.ui.graphics.Color(0xFFF8FAFC))
                .border(1.dp, androidx.compose.ui.graphics.Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val path = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath.value = path
                            paths.add(path)
                            hasDrawn = true
                            savedMessage = null
                        },
                        onDrag = { change, _ ->
                            currentPath.value.lineTo(change.position.x, change.position.y)
                            // trigger recomposition
                            val last = paths.removeAt(paths.lastIndex)
                            paths.add(last)
                        }
                    )
                }
                .testTag("signature_canvas_box")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Baseline guide line
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                    start = Offset(20f, size.height - 30f),
                    end = Offset(size.width - 20f, size.height - 30f),
                    strokeWidth = 1f
                )

                for (p in paths) {
                    drawPath(
                        path = p,
                        color = androidx.compose.ui.graphics.Color(0xFF0F172A),
                        style = Stroke(
                            width = 4f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (!hasDrawn && initialSignaturePath == null) {
                Text(
                    text = "Toque e desenhe a assinatura aqui",
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (savedMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = savedMessage!!,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun savePathsToBitmap(context: Context, paths: List<Path>): String? {
    if (paths.isEmpty()) return null
    try {
        val width = 400
        val height = 150
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        for (p in paths) {
            canvas.drawPath(p.asAndroidPath(), paint)
        }

        val sigDir = File(context.filesDir, "signatures")
        if (!sigDir.exists()) sigDir.mkdirs()

        val sigFile = File(sigDir, "sig_${System.currentTimeMillis()}.png")
        FileOutputStream(sigFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return sigFile.absolutePath
    } catch (e: Exception) {
        return null
    }
}
