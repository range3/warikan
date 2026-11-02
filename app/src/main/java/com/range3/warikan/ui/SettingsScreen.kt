@file:OptIn(ExperimentalMaterial3Api::class)

package com.range3.warikan.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.range3.warikan.R
import com.range3.warikan.domain.Preset
import com.range3.warikan.domain.Share

/** 設定画面で編集中のプリセット。NEW は新規追加。 */
private const val NEW = -1L

@Composable
fun SettingsScreen(vm: WarikanViewModel, onBack: () -> Unit) {
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val presets = vm.presets

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("グループの設定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "戻る")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editingId = NEW },
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                text = { Text("グループを追加") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 88.dp,
            ),
        ) {
            item {
                Text(
                    "飲み会の画面に追加ボタンとして並ぶグループです。偉い人が上になるように並べてください。" +
                        "傾斜グループの金額は「基準」(±0) との差額で決まります。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
            itemsIndexed(presets, key = { _, p -> p.id }) { i, p ->
                ListItem(
                    headlineContent = { Text(p.name) },
                    supportingContent = { Text(p.share.describe()) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { vm.movePreset(p.id, -1) }, enabled = i > 0) {
                                Icon(painterResource(R.drawable.ic_arrow_up), contentDescription = "上へ")
                            }
                            IconButton(onClick = { vm.movePreset(p.id, +1) }, enabled = i < presets.lastIndex) {
                                Icon(painterResource(R.drawable.ic_arrow_down), contentDescription = "下へ")
                            }
                            IconButton(onClick = { vm.removePreset(p.id) }) {
                                Icon(painterResource(R.drawable.ic_delete), contentDescription = "${p.name}を削除")
                            }
                        }
                    },
                    modifier = Modifier.clickable(onClickLabel = "編集") { editingId = p.id },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                TextButton(onClick = { confirmReset = true }, modifier = Modifier.padding(16.dp)) {
                    Text("初期設定に戻す")
                }
            }
        }
    }

    editingId?.let { id ->
        val preset = presets.find { it.id == id }
        PresetDialog(
            preset = preset,
            onSave = { name, share ->
                if (preset == null) vm.addPreset(name, share) else vm.updatePreset(preset.copy(name = name, share = share))
                editingId = null
            },
            onMakeBase = {
                vm.makeBasePreset(id)
                editingId = null
            },
            onDismiss = { editingId = null },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("初期設定に戻しますか？") },
            text = { Text("グループの設定を最初の状態に戻します。") },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetPresets()
                    confirmReset = false
                }) { Text("戻す") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("キャンセル") } },
        )
    }
}

@Composable
private fun PresetDialog(
    preset: Preset?,
    onSave: (String, Share) -> Unit,
    onMakeBase: () -> Unit,
    onDismiss: () -> Unit,
) {
    val initial = preset?.share ?: Share.Sloped(0)
    var name by rememberSaveable { mutableStateOf(preset?.name ?: "") }
    var fixed by rememberSaveable { mutableStateOf(initial is Share.Fixed) }
    var fixedAmount by rememberSaveable { mutableLongStateOf((initial as? Share.Fixed)?.perPerson ?: 2_000) }
    var diff by rememberSaveable { mutableLongStateOf((initial as? Share.Sloped)?.diff ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (preset == null) "グループを追加" else "グループを編集") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名前") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !fixed,
                        onClick = { fixed = false },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text("傾斜") }
                    SegmentedButton(
                        selected = fixed,
                        onClick = { fixed = true },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text("金額固定") }
                }
                if (fixed) {
                    AmountField(
                        value = fixedAmount,
                        onValueChange = { fixedAmount = it },
                        label = "1人の金額",
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("基準との差額", style = MaterialTheme.typography.labelLarge)
                        Stepper(
                            text = diff.signedYen(),
                            onDecrement = { diff -= 500 },
                            onIncrement = { diff += 500 },
                        )
                    }
                    val original = preset?.share
                    if (original is Share.Sloped && original.diff != 0L) {
                        TextButton(onClick = onMakeBase) { Text("このグループを基準（±0）にする") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), if (fixed) Share.Fixed(fixedAmount) else Share.Sloped(diff)) },
                enabled = name.isNotBlank(),
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },
    )
}
