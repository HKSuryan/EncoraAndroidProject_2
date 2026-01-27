package com.example.takeanote1.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.takeanote1.data.local.entity.NoteEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    onCompleteClick: () -> Unit = {},
    showCompleteButton: Boolean = true,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
             ,

        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface
        ),

        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    )
    {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {

                    //  Checkbox for completed notes screen
                    if (!showCompleteButton) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onClick?.invoke() }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = note.topic,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val date = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(note.createdAt))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (note.reminderTime != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val reminderStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(note.reminderTime))
                Text(
                    text = "Reminder: $reminderStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (showCompleteButton && !note.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCompleteClick,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                ) {
                    Text("Mark Completed")
                }
            }
        }
    }
}
