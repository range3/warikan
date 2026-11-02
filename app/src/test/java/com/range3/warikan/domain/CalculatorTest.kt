package com.range3.warikan.domain

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorTest {

    private fun sloped(id: Long, count: Int, diff: Long = 0) =
        Group(id, "g$id", count, Share.Sloped(diff))

    private fun fixed(id: Long, count: Int, perPerson: Long) =
        Group(id, "g$id", count, Share.Fixed(perPerson))

    @Test
    fun equalShare_includesPrepaidPeople() {
        val r = Bill(
            total = 50_000,
            prepaids = listOf(Prepaid(1, 10_000, 1)),
            groups = listOf(sloped(2, 11)),
        ).calculate()
        assertEquals(12, r.headcount)
        assertEquals(4_167L, r.equalShare)
    }

    @Test
    fun prepaidAndFixedAreSubtractedBeforeSloping() {
        // 60,000 − 先払い 10,000 − 学生 2,000×3 = 44,000 を 社員(+1,000)×4 と 新人×4 で分ける
        // 基準×8 + 4,000 = 44,000 → 基準 = 5,000
        val r = Bill(
            total = 60_000,
            prepaids = listOf(Prepaid(1, 10_000, 1)),
            groups = listOf(sloped(2, 4, diff = 1_000), sloped(3, 4), fixed(4, 3, 2_000)),
            unit = 1_000,
        ).calculate()
        assertEquals(6_000L, r.perPerson[2])
        assertEquals(5_000L, r.perPerson[3])
        assertEquals(2_000L, r.perPerson[4])
        assertEquals(0L, r.surplus)
    }

    @Test
    fun negativeDiff() {
        // 基準×6 − 2,000 = 40,000 → 基準 = 7,000
        val r = Bill(
            total = 40_000,
            groups = listOf(sloped(1, 4), sloped(2, 2, diff = -1_000)),
            unit = 1_000,
        ).calculate()
        assertEquals(7_000L, r.perPerson[1])
        assertEquals(6_000L, r.perPerson[2])
    }

    @Test
    fun roundUp_neverShort() {
        // 30,000 / 7 = 4,285.7…
        val bill = Bill(total = 30_000, groups = listOf(sloped(1, 7)))
        assertEquals(5_000L, bill.copy(unit = 1_000).calculate().perPerson[1])
        assertEquals(4_500L, bill.copy(unit = 500).calculate().perPerson[1])
        assertEquals(4_300L, bill.copy(unit = 100).calculate().perPerson[1])
        assertEquals(100L, bill.copy(unit = 100).calculate().surplus)
    }

    @Test
    fun roundHalfUp_canBeShort() {
        val bill = Bill(total = 30_000, groups = listOf(sloped(1, 7)), rounding = Rounding.HalfUp)
        assertEquals(4_000L, bill.copy(unit = 1_000).calculate().perPerson[1])
        assertEquals(-2_000L, bill.copy(unit = 1_000).calculate().surplus)
        assertEquals(4_500L, bill.copy(unit = 500).calculate().perPerson[1])
        assertEquals(4_300L, bill.copy(unit = 100).calculate().perPerson[1])
        // ちょうど半分は切り上げ: 4,250 → 4,500
        val half = Bill(total = 8_500, groups = listOf(sloped(1, 2)), unit = 500, rounding = Rounding.HalfUp)
        assertEquals(4_500L, half.calculate().perPerson[1])
    }

    @Test
    fun noSlopedPeople_showsShortage() {
        val r = Bill(
            total = 10_000,
            groups = listOf(fixed(1, 2, 2_000), sloped(2, 0)),
        ).calculate()
        assertNull(r.perPerson[2])
        assertEquals(-6_000L, r.surplus)
        assertFalse(r.hasNonPositiveShare)
    }

    @Test
    fun flagsNonPositiveShare() {
        val r = Bill(
            total = 10_000,
            groups = listOf(sloped(1, 1, diff = 20_000), sloped(2, 1)),
        ).calculate()
        assertTrue(r.hasNonPositiveShare)
    }

    @Test
    fun switchSlopedToFixed_keepsAmount() {
        val bill = Bill(
            total = 40_000,
            groups = listOf(sloped(1, 4), sloped(2, 2, diff = -1_000)),
            unit = 1_000,
        )
        val switched = bill.switchShare(2, toFixed = true)
        assertEquals(Share.Fixed(6_000), switched.groups[1].share)
        assertEquals(bill.calculate().perPerson, switched.calculate().perPerson)
    }

    @Test
    fun switchFixedToSloped_keepsAmounts() {
        // 学生 2,000×2 を引いた 36,000 を社員 6 人で → 6,000
        val bill = Bill(
            total = 40_000,
            groups = listOf(sloped(1, 6), fixed(2, 2, 2_000)),
            unit = 1_000,
        )
        val switched = bill.switchShare(2, toFixed = false)
        assertEquals(Share.Sloped(-4_000), switched.groups[1].share)
        assertEquals(bill.calculate().perPerson, switched.calculate().perPerson)
    }

    @Test
    fun switchFixedToSloped_withoutOtherSloped_isZeroDiff() {
        val bill = Bill(total = 10_000, groups = listOf(fixed(1, 2, 2_000)))
        assertEquals(Share.Sloped(0), bill.switchShare(1, toFixed = false).groups[0].share)
    }

    @Test
    fun emptyBill() {
        val r = Bill().calculate()
        assertEquals(0, r.headcount)
        assertNull(r.equalShare)
        assertEquals(0L, r.surplus)
    }

    @Test
    fun billSurvivesJsonRoundTrip() {
        val bill = Bill(
            total = 12_345,
            prepaids = listOf(Prepaid(1, 10_000, 1)),
            groups = listOf(sloped(2, 3, -1_000), fixed(3, 1, 2_000)),
            unit = 500,
            rounding = Rounding.HalfUp,
        )
        assertEquals(bill, Json.decodeFromString<Bill>(Json.encodeToString(bill)))
    }
}
