package com.range3.warikan.domain

/** 先に払って退席した人たちから預かったお金。 */
data class Prepaid(
    val id: Long,
    val amount: Long,
    val people: Int,
)

/** グループの払い方。 */
sealed interface Share {
    /** 1人あたりの金額を直接決める（学生など）。 */
    data class Fixed(val perPerson: Long) : Share

    /** 残りを傾斜グループで分ける。[extra] は基準より1人あたり何円多く払うか。 */
    data class Sloped(val extra: Long) : Share
}

data class Group(
    val id: Long,
    val name: String,
    val count: Int,
    val share: Share,
)

data class Bill(
    val total: Long = 0,
    val prepaids: List<Prepaid> = emptyList(),
    val groups: List<Group> = emptyList(),
    /** 集金の丸め単位（円）。傾斜グループの1人あたり額をこの単位に切り上げる。 */
    val unit: Long = 500,
)
