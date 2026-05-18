package com.xiaopeng.xposed.instrument.theme.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class SrMapSurfaceRecoveryPlanTest {

    @Test
    fun `recovery replays create before changed`() {
        assertEquals(
            listOf("create", "changed"),
            SrMapSurfaceRecoveryPlan.recoverySteps()
        )
    }
}
