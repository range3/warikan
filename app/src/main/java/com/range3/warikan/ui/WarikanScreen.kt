@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.range3.warikan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.range3.warikan.R
import com.range3.warikan.domain.BillResult
import com.range3.warikan.domain.Group
import com.range3.warikan.domain.GroupResult
import com.range3.warikan.domain.Prepaid
import com.range3.warikan.domain.Share
import com.range3.warikan.ui.theme.WarikanTheme

private val UNITS = listOf(1L, 100L, 500L, 1_000L)

private fun Long.yen() = "%,d円".format(this)

private fun TextStyle.tabular() = copy(fontFeatureSettings = "tnum")

@Composable
fun WarikanScreen(vm: WarikanViewModel = viewModel()) {
    val bill = vm.bill
    val result = vm.result

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("割り勘") },
                actions = {
                    IconButton(onClick = vm::reset) {
                        Icon(painterResource(R.drawable.ic_restart), contentDescription = "リセット")
                    }
                },
            )
        },
        bottomBar = {
            SummaryBar(
                total = bill.total,
                unit = bill.unit,
                result = result,
                onUnitChange = vm::setUnit,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(padding)
                .imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "total") {
                TotalSection(total = bill.total, result = result, onTotalChange = vm::setTotal)
            }

            item(key = "prepaid-header") { SectionHeader("先払い（退席した人から預かった分）") }
            items(bill.prepaids, key = { "prepaid-${it.id}" }) { p ->
                PrepaidRow(
                    prepaid = p,
                    onChange = { new -> vm.updatePrepaid(p.id) { new } },
                    onRemove = { vm.removePrepaid(p.id) },
                )
            }
            item(key = "prepaid-add") {
                TextButton(onClick = vm::addPrepaid) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("先払いを追加")
                }
            }

            item(key = "group-header") { SectionHeader("グループ") }
            items(bill.groups, key = { "group-${it.id}" }) { g ->
                GroupCard(
                    group = g,
                    result = result.groups[g.id],
                    step = vm.step,
                    onChange = { transform -> vm.updateGroup(g.id, transform) },
                    onFixedChange = { vm.setFixed(g.id, it) },
                    onRemove = { vm.removeGroup(g.id) },
                )
            }
            item(key = "group-add") {
                AddGroupRow(onAdd = vm::addGroup)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun TotalSection(total: Long, result: BillResult, onTotalChange: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AmountField(
            value = total,
            onValueChange = onTotalChange,
            label = "支払金額",
            textStyle = MaterialTheme.typography.headlineMedium.tabular(),
            modifier = Modifier.fillMaxWidth(),
        )
        val equal = result.equalShare
        Text(
            if (equal == null) {
                "人数を入れると均等割の額が出ます"
            } else {
                "均等割なら 1人 ${equal.yen()}（丸めると ${result.equalShareRounded!!.yen()}）・${result.headcount}人"
            },
            style = MaterialTheme.typography.bodyLarge.tabular(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrepaidRow(prepaid: Prepaid, onChange: (Prepaid) -> Unit, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AmountField(
            value = prepaid.amount,
            onValueChange = { onChange(prepaid.copy(amount = it)) },
            label = "預かり金",
            modifier = Modifier.weight(1f),
        )
        Stepper(
            text = "${prepaid.people}人",
            onDecrement = { onChange(prepaid.copy(people = prepaid.people - 1)) },
            onIncrement = { onChange(prepaid.copy(people = prepaid.people + 1)) },
            decrementEnabled = prepaid.people > 0,
        )
        IconButton(onClick = onRemove) {
            Icon(painterResource(R.drawable.ic_delete), contentDescription = "先払いを削除")
        }
    }
}

@Composable
private fun GroupCard(
    group: Group,
    result: GroupResult?,
    step: Long,
    onChange: ((Group) -> Group) -> Unit,
    onFixedChange: (Boolean) -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = group.name,
                    onValueChange = { name -> onChange { it.copy(name = name) } },
                    label = { Text("グループ名") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove) {
                    Icon(painterResource(R.drawable.ic_delete), contentDescription = "グループを削除")
                }
            }

            LabeledRow("人数") {
                Stepper(
                    text = "${group.count}人",
                    onDecrement = { onChange { it.copy(count = it.count - 1) } },
                    onIncrement = { onChange { it.copy(count = it.count + 1) } },
                    decrementEnabled = group.count > 0,
                )
            }

            val share = group.share
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = share is Share.Sloped,
                    onClick = { onFixedChange(false) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("傾斜で分ける") }
                SegmentedButton(
                    selected = share is Share.Fixed,
                    onClick = { onFixedChange(true) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("金額を決める") }
            }

            when (share) {
                is Share.Sloped -> LabeledRow("上乗せ") {
                    Stepper(
                        text = "+${share.extra.yen()}",
                        onDecrement = {
                            onChange { it.copy(share = Share.Sloped((share.extra - step).coerceAtLeast(0))) }
                        },
                        onIncrement = { onChange { it.copy(share = Share.Sloped(share.extra + step)) } },
                        decrementEnabled = share.extra > 0,
                    )
                }

                is Share.Fixed -> LabeledRow("1人") {
                    Stepper(
                        text = share.perPerson.yen(),
                        onDecrement = {
                            onChange { it.copy(share = Share.Fixed((share.perPerson - step).coerceAtLeast(0))) }
                        },
                        onIncrement = { onChange { it.copy(share = Share.Fixed(share.perPerson + step)) } },
                        decrementEnabled = share.perPerson > 0,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            val perPerson = result?.perPerson
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "1人あたり",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Column(horizontalAlignment = Alignment.End) {
                    if (share is Share.Sloped && result?.exact != null && result.exact != perPerson) {
                        Text(
                            "計算上 ${result.exact.yen()}",
                            style = MaterialTheme.typography.bodySmall.tabular(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        perPerson?.yen() ?: "—",
                        style = MaterialTheme.typography.headlineMedium.tabular(),
                        color = if (perPerson != null && perPerson <= 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }
            if (perPerson != null && perPerson <= 0 && group.count > 0) {
                Text(
                    "0円以下になっています。上乗せや固定額を減らしてください",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun AddGroupRow(onAdd: (GroupTemplate) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GroupTemplate.entries.forEach { t ->
            AssistChip(
                onClick = { onAdd(t) },
                label = { Text(t.label) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
            )
        }
    }
}

@Composable
private fun SummaryBar(
    total: Long,
    unit: Long,
    result: BillResult,
    onUnitChange: (Long) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                UNITS.forEachIndexed { i, u ->
                    SegmentedButton(
                        selected = unit == u,
                        onClick = { onUnitChange(u) },
                        shape = SegmentedButtonDefaults.itemShape(i, UNITS.size),
                        icon = {},
                    ) { Text(if (u == 1L) "丸めない" else "%,d円".format(u)) }
                }
            }
            Row {
                SummaryItem("集金", result.collected.yen(), Modifier.weight(1f))
                SummaryItem("支払", total.yen(), Modifier.weight(1f))
                val surplus = result.surplus
                SummaryItem(
                    label = when {
                        surplus > 0 -> "余り"
                        surplus < 0 -> "不足"
                        else -> "過不足"
                    },
                    value = when {
                        surplus > 0 -> "+${surplus.yen()}"
                        surplus < 0 -> "−${(-surplus).yen()}"
                        else -> "ぴったり"
                    },
                    color = if (surplus < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
            if (result.hasNonPositiveShare) {
                Text(
                    "0円以下のグループがあります",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium.tabular(), color = color)
    }
}

@Composable
private fun LabeledRow(label: String, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        content()
    }
}

@Composable
private fun Stepper(
    text: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    decrementEnabled: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onDecrement, enabled = decrementEnabled) {
            Icon(painterResource(R.drawable.ic_remove), contentDescription = "減らす")
        }
        Text(
            text,
            style = MaterialTheme.typography.titleMedium.tabular(),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 72.dp),
        )
        FilledTonalIconButton(onClick = onIncrement) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = "増やす")
        }
    }
}

@Composable
private fun AmountField(
    value: Long,
    onValueChange: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.tabular(),
) {
    OutlinedTextField(
        value = if (value == 0L) "" else value.toString(),
        onValueChange = { text ->
            val digits = text.filter(Char::isDigit).take(9)
            onValueChange(digits.toLongOrNull() ?: 0L)
        },
        label = { Text(label) },
        suffix = { Text("円") },
        singleLine = true,
        textStyle = textStyle,
        visualTransformation = ThousandsSeparator,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        modifier = modifier,
    )
}

/** 入力中の数字に 3 桁区切りのカンマを表示する。 */
private object ThousandsSeparator : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val s = text.text
        fun hasCommaBefore(i: Int) = i > 0 && (s.length - i) % 3 == 0
        val out = buildString {
            s.forEachIndexed { i, c ->
                if (hasCommaBefore(i)) append(',')
                append(c)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + (1 until offset).count(::hasCommaBefore)

            override fun transformedToOriginal(offset: Int): Int =
                (0..s.length).last { originalToTransformed(it) <= offset }
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun WarikanScreenPreview() {
    WarikanTheme {
        WarikanScreen(
            WarikanViewModel().apply {
                setTotal(60_000)
                addPrepaid()
                addGroup(GroupTemplate.Boss)
                addGroup(GroupTemplate.Student)
                updateGroup(0) { it.copy(count = 6) }
            },
        )
    }
}
