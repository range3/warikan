package com.range3.warikan.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorTest {

    private fun sloped(id: Long, count: Int, extra: Long = 0) =
        Group(id, "g$id", count, Share.Sloped(extra))

    private fun fixed(id: Long, count: Int, perPerson: Long) =
        Group(id, "g$id", count, Share.Fixed(perPerson))

    @Test
    fun equalShare_includesPrepaidPeople() {
        val r = Bill(
            total = 50_000,
            prepaids = listOf(Prepaid(1, 10_000, 1)),
            groups = listOf(sloped(2, 11)),
            unit = 500,
        ).calculate()
        assertEquals(12, r.headcount)
        assertEquals(4_167L, r.equalShare)
        assertEquals(4_500L, r.equalShareRounded)
    }

    @Test
    fun singleSlopedGroup_isEqualSplitOfRemainder() {
        val r = Bill(total = 30_000, groups = listOf(sloped(1, 7)), unit = 1).calculate()
        assertEquals(GroupResult(4_286, 4_286), r.groups[1])
        assertEquals(30_002L, r.collected)
        assertEquals(2L, r.surplus)
    }

    @Test
    fun prepaidAndFixedAreSubtractedBeforeSloping() {
        // 60,000 − 先払い 10,000 − 学生 2,000×3 = 44,000 を 社会人(+1,000)×4 と 若手×4 で分ける
        // base×8 + 4,000 = 44,000 → base = 5,000
        val r = Bill(
            total = 60_000,
            prepaids = listOf(Prepaid(1, 10_000, 1)),
            groups = listOf(sloped(2, 4, extra = 1_000), sloped(3, 4), fixed(4, 3, 2_000)),
            unit = 1_000,
        ).calculate()
        assertEquals(6_000L, r.groups[2]?.perPerson)
        assertEquals(5_000L, r.groups[3]?.perPerson)
        assertEquals(2_000L, r.groups[4]?.perPerson)
        assertEquals(60_000L, r.collected)
        assertEquals(0L, r.surplus)
    }

    @Test
    fun roundsEachSlopedGroupUpToUnit() {
        val r = Bill(
            total = 40_000,
            groups = listOf(sloped(1, 3, extra = 500), sloped(2, 6)),
            unit = 500,
        ).calculate()
        // base×9 + 1,500 = 40,000 → base = 4,277.7…
        assertEquals(GroupResult(4_778, 5_000), r.groups[1])
        assertEquals(GroupResult(4_278, 4_500), r.groups[2])
        assertEquals(42_000L, r.collected)
        assertEquals(2_000L, r.surplus)
    }

    @Test
    fun noSlopedPeople_showsShortage() {
        val r = Bill(
            total = 10_000,
            groups = listOf(fixed(1, 2, 2_000), sloped(2, 0)),
            unit = 500,
        ).calculate()
        assertNull(r.groups[2]?.perPerson)
        assertEquals(-6_000L, r.surplus)
        assertFalse(r.hasNonPositiveShare)
    }

    @Test
    fun flagsNonPositiveShare() {
        val r = Bill(
            total = 10_000,
            groups = listOf(sloped(1, 1, extra = 20_000), sloped(2, 1)),
            unit = 500,
        ).calculate()
        assertTrue(r.hasNonPositiveShare)
    }

    @Test
    fun emptyBill() {
        val r = Bill().calculate()
        assertEquals(0, r.headcount)
        assertNull(r.equalShare)
        assertEquals(0L, r.surplus)
    }
}
