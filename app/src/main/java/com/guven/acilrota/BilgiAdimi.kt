package com.guven.acilrota

data class BilgiAdimi(
    val baslik: String,
    val aciklama: String,
    val gorselResId: Int
)

data class AcilDurum(
    val isim: String,
    val adimlar: List<BilgiAdimi>
)