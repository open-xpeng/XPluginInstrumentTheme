package com.xiaopeng.xposed.instrument.theme.utils

object SrMapSurfaceRecoveryPlan {
    const val STEP_CREATE: String = "create"
    const val STEP_CHANGED: String = "changed"

    fun recoverySteps(): List<String> {
        return listOf(STEP_CREATE, STEP_CHANGED)
    }
}
