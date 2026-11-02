package com.range3.warikan.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 先に払って退席した人たちから預かったお金。 */
@Serializable
data class Prepaid(
    val id: Long,
    val amount: Long,
    val people: Int,
)

/** グループの払い方。 */
@Serializable
sealed interface Share {
    /** 金額固定: 1人あたりの金額を直接決める（学生・部長など）。 */
    @Serializable
    @SerialName("fixed")
    data class Fixed(val perPerson: Long) : Share

    /** 傾斜: 残りを傾斜グループで分ける。[diff] は基準より1人あたり何円多く払うか（マイナス可）。 */
    @Serializable
    @SerialName("sloped")
    data class Sloped(val diff: Long) : Share
}

@Serializable
data class Group(
    val id: Long,
    val name: String,
    val count: Int,
    val share: Share,
    /** どのプリセットから作ったか。連番の名前付けに使う。 */
    val presetId: Long? = null,
)

@Serializable
enum class Rounding {
    /** 切り上げ: 集金係が損をしない。 */
    Up,

    /** 四捨五入: 少し不足が出ることがある。 */
    HalfUp,
}

@Serializable
data class Bill(
    val total: Long = 0,
    val prepaids: List<Prepaid> = emptyList(),
    val groups: List<Group> = emptyList(),
    /** 傾斜グループの1人あたり額を丸める単位（100 / 500 / 1,000円）。 */
    val unit: Long = 1_000,
    val rounding: Rounding = Rounding.Up,
)

/** チップから追加するグループのひな形。設定画面で編集する。 */
@Serializable
data class Preset(
    val id: Long,
    val name: String,
    val share: Share,
)

val DefaultPresets = listOf(
    Preset(1, "役員", Share.Fixed(15_000)),
    Preset(2, "部長", Share.Fixed(10_000)),
    Preset(3, "課長", Share.Sloped(3_000)),
    Preset(4, "係長", Share.Sloped(2_000)),
    Preset(5, "シニア", Share.Sloped(1_000)),
    Preset(6, "社員", Share.Sloped(0)),
    Preset(7, "新人", Share.Sloped(-1_000)),
    Preset(8, "学生", Share.Fixed(2_000)),
    Preset(9, "その他", Share.Sloped(0)),
)
