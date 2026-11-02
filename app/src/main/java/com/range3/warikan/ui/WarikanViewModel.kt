package com.range3.warikan.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.range3.warikan.data.AppStore
import com.range3.warikan.data.SavedState
import com.range3.warikan.domain.Bill
import com.range3.warikan.domain.DefaultPresets
import com.range3.warikan.domain.Group
import com.range3.warikan.domain.Preset
import com.range3.warikan.domain.Prepaid
import com.range3.warikan.domain.Rounding
import com.range3.warikan.domain.Share
import com.range3.warikan.domain.calculate
import com.range3.warikan.domain.switchShare
import kotlinx.coroutines.launch

class WarikanViewModel(private val store: AppStore? = null) : ViewModel() {

    var bill by mutableStateOf(Bill())
        private set

    var presets by mutableStateOf(DefaultPresets)
        private set

    /** 保存済みの状態を読み終えたか。読み終える前に保存して上書きしないためのフラグ。 */
    var loaded by mutableStateOf(store == null)
        private set

    val result by derivedStateOf { bill.calculate() }

    private var nextId = 1_000L
    private fun newId() = nextId++

    init {
        if (store != null) {
            viewModelScope.launch {
                store.load()?.let {
                    bill = it.bill
                    presets = it.presets
                }
                val maxId = (bill.groups.map { it.id } + bill.prepaids.map { it.id } + presets.map { it.id })
                    .maxOrNull() ?: 0L
                nextId = maxOf(nextId, maxId + 1)
                loaded = true
            }
        }
    }

    private fun update(newBill: Bill = bill, newPresets: List<Preset> = presets) {
        bill = newBill
        presets = newPresets
        if (loaded && store != null) {
            val snapshot = SavedState(newBill, newPresets)
            viewModelScope.launch { store.save(snapshot) }
        }
    }

    // --- 飲み会の入力 ---

    fun setTotal(total: Long) = update(bill.copy(total = total))

    fun setUnit(unit: Long) = update(bill.copy(unit = unit))

    fun setRounding(rounding: Rounding) = update(bill.copy(rounding = rounding))

    fun addPrepaid() = update(bill.copy(prepaids = bill.prepaids + Prepaid(newId(), 10_000, 1)))

    fun updatePrepaid(id: Long, transform: (Prepaid) -> Prepaid) =
        update(bill.copy(prepaids = bill.prepaids.map { if (it.id == id) transform(it) else it }))

    fun removePrepaid(id: Long) = update(bill.copy(prepaids = bill.prepaids.filterNot { it.id == id }))

    /** プリセットからカードを1枚追加する。同じ名前があれば「その他 2」のように連番にする。 */
    fun addGroup(preset: Preset) {
        val taken = bill.groups.map { it.name }.toSet()
        val name = generateSequence(1) { it + 1 }
            .map { n -> if (n == 1) preset.name else "${preset.name} $n" }
            .first { it !in taken }
        update(bill.copy(groups = bill.groups + Group(newId(), name, 1, preset.share, preset.id)))
    }

    private fun updateGroup(id: Long, transform: (Group) -> Group) =
        update(bill.copy(groups = bill.groups.map { if (it.id == id) transform(it) else it }))

    fun changeCount(id: Long, delta: Int) =
        updateGroup(id) { it.copy(count = (it.count + delta).coerceAtLeast(0)) }

    /** 差額・固定額を丸め単位ぶん増減する。 */
    fun stepShare(id: Long, direction: Int) = updateGroup(id) { g ->
        val step = bill.unit * direction
        g.copy(
            share = when (val s = g.share) {
                is Share.Fixed -> Share.Fixed((s.perPerson + step).coerceAtLeast(0))
                is Share.Sloped -> Share.Sloped(s.diff + step)
            },
        )
    }

    fun switchShare(id: Long, toFixed: Boolean) = update(bill.switchShare(id, toFixed))

    fun renameGroup(id: Long, name: String) = updateGroup(id) { it.copy(name = name) }

    fun removeGroup(id: Long) = update(bill.copy(groups = bill.groups.filterNot { it.id == id }))

    /** 入力をクリアする。丸めの設定とプリセットは残す。 */
    fun reset() = update(Bill(unit = bill.unit, rounding = bill.rounding))

    // --- プリセット（設定画面） ---

    fun addPreset(name: String, share: Share) = update(newPresets = presets + Preset(newId(), name, share))

    fun updatePreset(preset: Preset) =
        update(newPresets = presets.map { if (it.id == preset.id) preset else it })

    fun removePreset(id: Long) = update(newPresets = presets.filterNot { it.id == id })

    fun movePreset(id: Long, delta: Int) {
        val from = presets.indexOfFirst { it.id == id }
        val to = from + delta
        if (from < 0 || to !in presets.indices) return
        update(newPresets = presets.toMutableList().apply { add(to, removeAt(from)) })
    }

    /** このプリセットが差額 0 になるよう、傾斜プリセット全体の差額をずらす。 */
    fun makeBasePreset(id: Long) {
        val offset = (presets.find { it.id == id }?.share as? Share.Sloped)?.diff ?: return
        update(
            newPresets = presets.map { p ->
                val s = p.share
                if (s is Share.Sloped) p.copy(share = Share.Sloped(s.diff - offset)) else p
            },
        )
    }

    fun resetPresets() = update(newPresets = DefaultPresets)
}
