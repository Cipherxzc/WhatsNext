package com.cipherxzc.whatsnext.ui.main.utils

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImportanceDropdownMenu(
    importance: Int?,
    setImportance: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val importanceLevels = (0..10).toList()

    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable { expanded = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("重要程度：", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = importance?.toString() ?: "未设置",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 3.dp)
        )
        Icon(Icons.Default.ArrowDropDown, contentDescription = "选择重要性")
    }

    DropdownMenu(
        modifier = Modifier
            .width(200.dp)
            .padding(8.dp),
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        importanceLevels.forEach { level ->
            Box(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                DropdownMenuItem(
                    text = { Text("$level") },
                    onClick = {
                        setImportance(level)
                        expanded = false
                    }
                )
            }
        }
    }
}