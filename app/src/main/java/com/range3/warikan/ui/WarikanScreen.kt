@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.range3.warikan.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.range3.warikan.R
import com.range3.warikan.domain.Bill
import com.range3.warikan.domain.BillResult
import com.range3.warikan.domain.Group
import com.range3.warikan.domain.Preset
import com.range3.warikan.domain.Prepaid
import com.range3.warikan.domain.Rounding
import com.range3.warikan.domain.Share
import com.range3.warikan.ui.theme.WarikanTheme

private val UNITS = listOf(1_000L, 500L, 100L)
private val ROUNDINGS = listOf(Rounding.Up to "切り上げ", Rounding.HalfUp to "四捨五入")

@Composable
fun WarikanScreen(vm: WarikanViewModel, onOpenSettings: () -> Unit) {
    val bill = vm.bill
    val result = vm.result
    var renaming by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("割り勘") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(painterResource(R.drawable.ic_tune), contentDescription = "グループの設定")
                    }
                    IconButton(onClick = vm::reset) {
                        Icon(painterResource(R.drawable.ic_restart), contentDescription = "リセット")
                    }
                },
            )
        },
        bottomBar = {
            SummaryBar(bill = bill, result = result, onUnitChange = vm::setUnit, onRoundingChange = vm::setRounding)
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
            if (bill.groups.isEmpty()) {
                item(key = "group-empty") {
                    Text(
                        "下のボタンから、来ている人のグループを追加してください",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(bill.groups, key = { "group-${it.id}" }) { g ->
                GroupCard(
                    group = g,
                    perPerson = result.perPerson[g.id],
                    onCountChange = { vm.changeCount(g.id, it) },
                    onStep = { vm.stepShare(g.id, it) },
                    onSwitch = { vm.switchShare(g.id, it) },
                    onRename = { renaming = g.id },
                    onRemove = { vm.removeGroup(g.id) },
                )
            }
            item(key = "group-add") {
                AddGroupChips(presets = vm.presets, onAdd = vm::addGroup)
            }
        }
    }

    renaming?.let { id ->
        val group = bill.groups.find { it.id == id }
        if (group == null) {
            renaming = null
        } else {
            NameDialog(
                title = "グループ名を変更",
                initial = group.name,
                onConfirm = {
                    vm.renameGroup(id, it)
                    renaming = null
                },
                onDismiss = { renaming = null },
            )
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
            if (equal == null) "人数を入れると均等割の額が出ます" else "均等割なら 1人 ${equal.yen()}（${result.headcount}人）",
            style = MaterialTheme.typography.bodyLarge.tabular(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrepaidRow(prepaid: Prepaid, onChange: (Prepaid) -> Unit, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
    perPerson: Long?,
    onCountChange: (Int) -> Unit,
    onStep: (Int) -> Unit,
    onSwitch: (toFixed: Boolean) -> Unit,
    onRename: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    group.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClickLabel = "名前を変更", onClick = onRename)
                        .padding(vertical = 8.dp),
                )
                IconButton(onClick = onRemove) {
                    Icon(painterResource(R.drawable.ic_delete), contentDescription = "${group.name}を削除")
                }
            }

            LabeledRow("人数") {
                Stepper(
                    text = "${group.count}人",
                    onDecrement = { onCountChange(-1) },
                    onIncrement = { onCountChange(+1) },
                    decrementEnabled = group.count > 0,
                )
            }

            val share = group.share
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
                SegmentedButton(
                    selected = share is Share.Sloped,
                    onClick = { onSwitch(false) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("傾斜") }
                SegmentedButton(
                    selected = share is Share.Fixed,
                    onClick = { onSwitch(true) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("金額固定") }
            }

            when (share) {
                is Share.Sloped -> LabeledRow("基準との差額") {
                    Stepper(text = share.diff.signedYen(), onDecrement = { onStep(-1) }, onIncrement = { onStep(+1) })
                }

                is Share.Fixed -> LabeledRow("1人の金額") {
                    Stepper(
                        text = share.perPerson.yen(),
                        onDecrement = { onStep(-1) },
                        onIncrement = { onStep(+1) },
                        decrementEnabled = share.perPerson > 0,
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(end = 8.dp),
            )

            val bad = perPerson != null && perPerson <= 0 && group.count > 0
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    "1人あたり",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    perPerson?.yen() ?: "—",
                    style = MaterialTheme.typography.headlineMedium.tabular(),
                    color = if (bad) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
            if (bad) {
                Text(
                    "0円以下になっています。差額や固定額を見直してください",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun AddGroupChips(presets: List<Preset>, onAdd: (Preset) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { p ->
            AssistChip(
                onClick = { onAdd(p) },
                label = { Text(p.name) },
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
    bill: Bill,
    result: BillResult,
    onUnitChange: (Long) -> Unit,
    onRoundingChange: (Rounding) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    UNITS.forEachIndexed { i, u ->
                        SegmentedButton(
                            selected = bill.unit == u,
                            onClick = { onUnitChange(u) },
                            shape = SegmentedButtonDefaults.itemShape(i, UNITS.size),
                            icon = {},
                        ) { Text("%,d円".format(u)) }
                    }
                }
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ROUNDINGS.forEachIndexed { i, (r, label) ->
                        SegmentedButton(
                            selected = bill.rounding == r,
                            onClick = { onRoundingChange(r) },
                            shape = SegmentedButtonDefaults.itemShape(i, ROUNDINGS.size),
                            icon = {},
                        ) { Text(label, maxLines = 1) }
                    }
                }
            }
            Row {
                SummaryItem("集金", result.collected.yen(), Modifier.weight(1f))
                SummaryItem("支払", bill.total.yen(), Modifier.weight(1f))
                val surplus = result.surplus
                SummaryItem(
                    label = when {
                        surplus > 0 -> "余り"
                        surplus < 0 -> "不足"
                        else -> "過不足"
                    },
                    value = if (surplus == 0L) "ぴったり" else surplus.signedYen(),
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
    color: Color = MaterialTheme.colorScheme.onSurface,
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

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun WarikanScreenPreview() {
    WarikanTheme {
        WarikanScreen(
            WarikanViewModel().apply {
                setTotal(60_000)
                addPrepaid()
                presets.filter { it.name in setOf("課長", "社員", "学生") }.forEach(::addGroup)
            },
            onOpenSettings = {},
        )
    }
}
