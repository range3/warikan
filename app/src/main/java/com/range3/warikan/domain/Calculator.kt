package com.range3.warikan.domain

data class GroupResult(
    /** 丸め前の1人あたり額（1円単位で切り上げ）。傾斜グループに人がいない場合は null。 */
    val exact: Long?,
    /** 実際に集める1人あたり額。 */
    val perPerson: Long?,
)

data class BillResult(
    /** 先払いした人も含めた全員の人数。 */
    val headcount: Int,
    /** 完全均等割の1人あたり額（1円単位で切り上げ）。 */
    val equalShare: Long?,
    /** 完全均等割を丸め単位で切り上げた額。 */
    val equalShareRounded: Long?,
    val groups: Map<Long, GroupResult>,
    /** 先払い + 各グループから集める合計。 */
    val collected: Long,
    /** collected − total。正なら幹事の手元に余り、負なら不足。 */
    val surplus: Long,
    /** 1人あたり 0円以下になる傾斜グループがある（上乗せや固定額が大きすぎる）。 */
    val hasNonPositiveShare: Boolean,
)

fun Bill.calculate(): BillResult {
    val headcount = prepaids.sumOf { it.people } + groups.sumOf { it.count }
    val equalShare = if (headcount > 0) ceilDiv(total, headcount.toLong()) else null

    val prepaidTotal = prepaids.sumOf { it.amount }
    val fixedTotal = groups.sumOf { g ->
        (g.share as? Share.Fixed)?.let { it.perPerson * g.count } ?: 0L
    }
    val sloped = groups.filter { it.share is Share.Sloped }
    val slopedCount = sloped.sumOf { it.count }.toLong()

    // 傾斜グループ全員で払う額 = Σ count × (base + extra) を満たす base を分数 (baseNum / slopedCount) で持つ
    val baseNum = total - prepaidTotal - fixedTotal -
        sloped.sumOf { it.count * (it.share as Share.Sloped).extra }

    val results = groups.associate { g ->
        g.id to when (val share = g.share) {
            is Share.Fixed -> GroupResult(share.perPerson, share.perPerson)
            is Share.Sloped -> if (slopedCount == 0L) {
                GroupResult(null, null)
            } else {
                val num = baseNum + share.extra * slopedCount
                GroupResult(
                    exact = ceilDiv(num, slopedCount),
                    perPerson = ceilDiv(num, slopedCount * unit) * unit,
                )
            }
        }
    }

    val collected = prepaidTotal + groups.sumOf { g ->
        (results[g.id]?.perPerson ?: 0L) * g.count
    }

    return BillResult(
        headcount = headcount,
        equalShare = equalShare,
        equalShareRounded = equalShare?.let { ceilDiv(it, unit) * unit },
        groups = results,
        collected = collected,
        surplus = collected - total,
        hasNonPositiveShare = sloped.any { g ->
            g.count > 0 && (results[g.id]?.perPerson ?: 1L) <= 0L
        },
    )
}

internal fun ceilDiv(a: Long, b: Long): Long = -Math.floorDiv(-a, b)
