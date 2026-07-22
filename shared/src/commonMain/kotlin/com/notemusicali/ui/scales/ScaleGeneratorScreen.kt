package com.notemusicali.ui.scales

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.music.ArpeggioType
import com.notemusicali.music.ItalianNotation
import com.notemusicali.music.NoteSequence
import com.notemusicali.music.ScaleDirection
import com.notemusicali.music.ScaleGenerator
import com.notemusicali.music.ScaleType
import com.notemusicali.ui.components.BackTopBar
import com.notemusicali.ui.components.StaffFullView
import notemusicali.shared.generated.resources.Res
import notemusicali.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val NOTE_NAMES = (0..11).map { ItalianNotation.fromNoteIndex(it) }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScaleGeneratorScreen(
    onBack: () -> Unit,
    onPractice: (NoteSequence) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Scale, 1=Arpeggio
    var selectedNoteIndex by remember { mutableIntStateOf(0) } // C
    var selectedOctave by remember { mutableIntStateOf(4) }
    var selectedScaleType by remember { mutableStateOf(ScaleType.MAJOR) }
    var selectedArpeggioType by remember { mutableStateOf(ArpeggioType.MAJOR) }
    var selectedDirection by remember { mutableStateOf(ScaleDirection.ASCENDING) }

    val preview = remember(selectedTab, selectedNoteIndex, selectedOctave, selectedScaleType, selectedArpeggioType, selectedDirection) {
        if (selectedTab == 0) {
            ScaleGenerator.generateScale(selectedNoteIndex, selectedOctave, selectedScaleType, selectedDirection)
        } else {
            ScaleGenerator.generateArpeggio(selectedNoteIndex, selectedOctave, selectedArpeggioType, selectedDirection)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            BackTopBar(title = stringResource(Res.string.scales_title), onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Tab: Scale / Arpeggio
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text(stringResource(Res.string.tab_scales), modifier = Modifier.padding(12.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text(stringResource(Res.string.tab_arpeggios), modifier = Modifier.padding(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Root note selector
                Text(stringResource(Res.string.root_note), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    NOTE_NAMES.forEachIndexed { idx, name ->
                        FilterChip(
                            selected = selectedNoteIndex == idx,
                            onClick = { selectedNoteIndex = idx },
                            label = { Text(name.display, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                labelColor = Color.White.copy(alpha = 0.7f),
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Octave selector
                Text(stringResource(Res.string.octave), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (2..6).forEach { oct ->
                        FilterChip(
                            selected = selectedOctave == oct,
                            onClick = { selectedOctave = oct },
                            label = { Text("$oct") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                labelColor = Color.White.copy(alpha = 0.7f),
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scale/Arpeggio type
                if (selectedTab == 0) {
                    Text(stringResource(Res.string.scale_type), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ScaleType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedScaleType == type,
                                onClick = { selectedScaleType = type },
                                label = { Text(type.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White.copy(alpha = 0.7f),
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }
                    }
                } else {
                    Text(stringResource(Res.string.arpeggio_type), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ArpeggioType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedArpeggioType == type,
                                onClick = { selectedArpeggioType = type },
                                label = { Text(type.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White.copy(alpha = 0.7f),
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Direction
                Text(stringResource(Res.string.direction), fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScaleDirection.entries.forEach { dir ->
                        FilterChip(
                            selected = selectedDirection == dir,
                            onClick = { selectedDirection = dir },
                            label = { Text(dir.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                labelColor = Color.White.copy(alpha = 0.7f),
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Preview
                if (preview.notes.isNotEmpty()) {
                    Text(preview.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        StaffFullView(
                            notes = preview.notes,
                            currentIndex = -1,
                            lightTheme = true,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onPractice(preview) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.start_practice), fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
