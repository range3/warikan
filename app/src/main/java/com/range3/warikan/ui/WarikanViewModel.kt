package com.range3.warikan.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.range3.warikan.domain.Bill
import com.range3.warikan.domain.Group
import com.range3.warikan.domain.Prepaid
import com.range3.warikan.domain.Share
import com.range3.warikan.domain.calculate

/** グループ追加時のひな形。 */
enum class GroupTemplate(val label: String, val share: Share) {
    Boss("上司", Share.Sloped(2_000)),
    Worker("社会人", Share.Sloped(0)),
    Student("学生", Share.Fixed(2_000)),
    Other("その他", Share.Sloped(0)),
}

class WarikanViewModel : ViewModel() {
    private var nextId = 0L
    private fun newId() = nextId++

    var bill by mutableStateOf(initialBill())
        private set

    val result by derivedStateOf { bill.calculate() }

    /** 傾斜の上乗せ・固定額を ± するときの刻み。 */
    val step: Long get() = bill.unit.coerceIn(100, 500)

    private fun initialBill() = Bill(
        groups = listOf(Group(newId(), "参加者", 0, Share.Sloped(0))),
    )

    fun setTotal(total: Long) {
        bill = bill.copy(total = total)
    }

    fun setUnit(unit: Long) {
        bill = bill.copy(unit = unit)
    }

    fun addPrepaid() {
        bill = bill.copy(prepaids = bill.prepaids + Prepaid(newId(), 10_000, 1))
    }

    fun updatePrepaid(id: Long, transform: (Prepaid) -> Prepaid) {
        bill = bill.copy(prepaids = bill.prepaids.map { if (it.id == id) transform(it) else it })
    }

    fun removePrepaid(id: Long) {
        bill = bill.copy(prepaids = bill.prepaids.filterNot { it.id == id })
    }

    fun addGroup(template: GroupTemplate) {
        bill = bill.copy(groups = bill.groups + Group(newId(), template.label, 1, template.share))
    }

    fun updateGroup(id: Long, transform: (Group) -> Group) {
        bill = bill.copy(groups = bill.groups.map { if (it.id == id) transform(it) else it })
    }

    /** 傾斜⇔固定の切り替え。固定にするときは今の1人あたり額を引き継ぐ。 */
    fun setFixed(id: Long, fixed: Boolean) {
        val current = result.groups[id]?.perPerson
        updateGroup(id) { g ->
            when {
                fixed && g.share is Share.Sloped ->
                    g.copy(share = Share.Fixed((current ?: 2_000).coerceAtLeast(0)))
                !fixed && g.share is Share.Fixed -> g.copy(share = Share.Sloped(0))
                else -> g
            }
        }
    }

    fun removeGroup(id: Long) {
        bill = bill.copy(groups = bill.groups.filterNot { it.id == id })
    }

    fun reset() {
        bill = initialBill().copy(unit = bill.unit)
    }
}
