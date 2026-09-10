package com.example.gardenirrigation.model

data class IrrigationStage(
    val id: Int,
    val name: String,
    val durationMinutes: Int
) {
    val durationSeconds: Long
        get() = durationMinutes * 60L
}

object IrrigationData {
    val stages = listOf(
        IrrigationStage(1, "تخته ۱ — دهنه ۱ (۳ بره)", 17),
        IrrigationStage(2, "تخته ۱ — دهنه ۲ (۲ بره)", 13),
        IrrigationStage(3, "تخته ۱ — دهنه ۳ (۳ بره)", 18),
        IrrigationStage(4, "تخته ۱ — دهنه ۴ (۳ بره)", 15),
        IrrigationStage(5, "تخته ۱ — دهنه ۵ (۳ بره)", 17),
        IrrigationStage(6, "تخته ۱ — دهنه ۶ (۳ بره)", 20),
        IrrigationStage(7, "تخته ۱ — بره گردوها", 13),
        IrrigationStage(8, "تخته ۱ — بره بادام‌ها", 25),

        IrrigationStage(9, "تخته ۲ — دهنه ۱ (۴ بره)", 17),
        IrrigationStage(10, "تخته ۲ — دهنه ۲ (۴ بره)", 20),
        IrrigationStage(11, "تخته ۲ — دهنه ۳ (۳ بره)", 17),
        IrrigationStage(12, "تخته ۲ — دهنه ۴ (۳ بره)", 17),
        IrrigationStage(13, "تخته ۲ — دهنه ۵ (۳ بره)", 19),

        IrrigationStage(14, "تخته ۳ — دهنه ۱ (ورگان + ۴ بره)", 20),
        IrrigationStage(15, "تخته ۳ — دهنه ۲ (۴ بره)", 20),
        IrrigationStage(16, "تخته ۳ — دهنه ۳ (۴ بره)", 20),
        IrrigationStage(17, "تخته ۳ — دهنه ۴ (۴ بره)", 20),
        IrrigationStage(18, "ورگان", 25)
    )
}
