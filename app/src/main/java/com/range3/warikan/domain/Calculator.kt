package com.range3.warikan.domain

data class BillResult(
    /** 先払いした人も含めた全員の人数。 */
    val headcount: Int,
    /** 完全均等割の1人あたり額（目安。1円単位で切り上げ）。 */
    val equalShare: Long?,
    /** グループ id → 1人あたり額。傾斜グループに人がいなければ null。 */
    val perPerson: Map<Long, Long?>,
    /** 先払い + 各グループから集める合計。 */
    val collected: Long,
    /** collected − total。正なら集金係の手元に余り、負なら不足。 */
    val surplus: Long,
    /** 1人あたり 0円以下になる傾斜グループがある（差額や固定額が大きすぎる）。 */
    val hasNonPositiveShare: Boolean,
)

/**
 * 傾斜グループの基準額を分数 numerator / count で表したもの。
 * 傾斜グループ全員で払う額 Σ count × (基準額 + diff) が「支払金額 − 先払い − 固定グループ合計」と一致する基準額。
 */
private data class SlopedBase(val numerator: Long, val count: Long)

private fun Bill.slopedBase(): SlopedBase {
    val prepaidTotal = prepaids.sumOf { it.amount }
    var fixedTotal = 0L
    var diffTotal = 0L
    var count = 0L
    for (g in groups) {
        when (val s = g.share) {
            is Share.Fixed -> fixedTotal += s.perPerson * g.count
            is Share.Sloped -> {
                diffTotal += s.diff * g.count
                count += g.count
            }
        }
    }
    return SlopedBase(total - prepaidTotal - fixedTotal - diffTotal, count)
}

fun Bill.calculate(): BillResult {
    val headcount = prepaids.sumOf { it.people } + groups.sumOf { it.count }
    val base = slopedBase()

    val perPerson = groups.associate { g ->
        g.id to when (val s = g.share) {
            is Share.Fixed -> s.perPerson
            is Share.Sloped -> if (base.count == 0L) {
                null
            } else {
                roundToUnit(base.numerator + s.diff * base.count, base.count, unit, rounding)
            }
        }
    }
    val collected = prepaids.sumOf { it.amount } +
        groups.sumOf { g -> (perPerson[g.id] ?: 0L) * g.count }

    return BillResult(
        headcount = headcount,
        equalShare = if (headcount > 0) ceilDiv(total, headcount.toLong()) else null,
        perPerson = perPerson,
        collected = collected,
        surplus = collected - total,
        hasNonPositiveShare = groups.any { g ->
            g.share is Share.Sloped && g.count > 0 && (perPerson[g.id] ?: 1L) <= 0L
        },
    )
}

/**
 * 傾斜⇔金額固定を切り替える。今の1人あたり額がなるべく変わらないように値を引き継ぐ。
 * - 傾斜 → 固定: 今の1人あたり額を固定額にする
 * - 固定 → 傾斜: 基準額との差を 100円単位で差額にする（他の傾斜グループの額も変わらない）
 */
fun Bill.switchShare(groupId: Long, toFixed: Boolean): Bill {
    val group = groups.find { it.id == groupId } ?: return this
    val newShare = when (val s = group.share) {
        is Share.Sloped -> if (toFixed) {
            Share.Fixed((calculate().perPerson[groupId] ?: 0L).coerceAtLeast(0))
        } else {
            return this
        }

        is Share.Fixed -> if (!toFixed) {
            val base = slopedBase()
            val diff = if (base.count == 0L) {
                0L
            } else {
                roundToUnit(s.perPerson * base.count - base.numerator, base.count, 100, Rounding.HalfUp)
            }
            Share.Sloped(diff)
        } else {
            return this
        }
    }
    return copy(groups = groups.map { if (it.id == groupId) it.copy(share = newShare) else it })
}

/** numerator / denominator を unit 円単位に丸める。 */
internal fun roundToUnit(numerator: Long, denominator: Long, unit: Long, rounding: Rounding): Long =
    when (rounding) {
        Rounding.Up -> ceilDiv(numerator, denominator * unit) * unit
        Rounding.HalfUp ->
            Math.floorDiv(2 * numerator + denominator * unit, 2 * denominator * unit) * unit
    }

internal fun ceilDiv(a: Long, b: Long): Long = -Math.floorDiv(-a, b)
