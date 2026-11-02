package com.range3.warikan.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.range3.warikan.R
import com.range3.warikan.domain.Share

fun Long.yen() = "%,d円".format(this)

/** 差額の表示: +1,000円 / −1,000円 / ±0円 */
fun Long.signedYen() = when {
    this > 0 -> "+${yen()}"
    this < 0 -> "−${(-this).yen()}"
    else -> "±0円"
}

fun Share.describe() = when (this) {
    is Share.Fixed -> "金額固定 ${perPerson.yen()}"
    is Share.Sloped -> if (diff == 0L) "傾斜・基準" else "傾斜・基準との差額 ${diff.signedYen()}"
}

fun TextStyle.tabular() = copy(fontFeatureSettings = "tnum")

@Composable
fun Stepper(
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
            modifier = Modifier.widthIn(min = 88.dp),
        )
        FilledTonalIconButton(onClick = onIncrement) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = "増やす")
        }
    }
}

@Composable
fun AmountField(
    value: Long,
    onValueChange: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.tabular(),
) {
    val focusManager = LocalFocusManager.current
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
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = modifier,
    )
}

@Composable
fun NameDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("名前") },
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } },
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
