# Left Nav/Media Auto-Switch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the left-side navigation sub-card automatically show the media card when no navigation guidance is active, then switch back to the navigation card when guidance appears.

**Architecture:** Add a tiny pure-Kotlin resolver that decides the effective left sub-card from two inputs: the raw host-selected card index and whether navigation is active. Reuse that resolver from both the host `MainFragment` hook path and the module `MapFullFragment` observer path so the behavior is global but limited to the left navigation card slot.

**Tech Stack:** Kotlin, Android/Xposed hooks, AndroidX ViewModel/LiveData, JUnit 4, Gradle

---

### Task 1: Add a Testable Left-Card Resolver

**Files:**
- Modify: `C:\Users\wjc13\Documents\XPluginInstrumentTheme\app\build.gradle.kts`
- Create: `C:\Users\wjc13\Documents\XPluginInstrumentTheme\app\src\main\java\com\xiaopeng\xposed\instrument\theme\utils\LeftSubCardAutoSwitch.kt`
- Create: `C:\Users\wjc13\Documents\XPluginInstrumentTheme\app\src\test\java\com\xiaopeng\xposed\instrument\theme\utils\LeftSubCardAutoSwitchTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.xiaopeng.xposed.instrument.theme.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class LeftSubCardAutoSwitchTest {

    @Test
    fun `keeps non-navigation cards unchanged`() {
        assertEquals(2, LeftSubCardAutoSwitch.resolve(rawCardIndex = 2, isNavigationActive = false))
        assertEquals(4, LeftSubCardAutoSwitch.resolve(rawCardIndex = 4, isNavigationActive = true))
    }

    @Test
    fun `switches navigation card to media when navigation is inactive`() {
        assertEquals(1, LeftSubCardAutoSwitch.resolve(rawCardIndex = 0, isNavigationActive = false))
    }

    @Test
    fun `keeps navigation card when navigation is active`() {
        assertEquals(0, LeftSubCardAutoSwitch.resolve(rawCardIndex = 0, isNavigationActive = true))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.xiaopeng.xposed.instrument.theme.utils.LeftSubCardAutoSwitchTest"`

Expected: FAIL because `LeftSubCardAutoSwitch` does not exist yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.xiaopeng.xposed.instrument.theme.utils

object LeftSubCardAutoSwitch {
    const val NAVIGATION_CARD_INDEX: Int = 0
    const val MEDIA_CARD_INDEX: Int = 1

    fun resolve(rawCardIndex: Int, isNavigationActive: Boolean): Int {
        if (rawCardIndex != NAVIGATION_CARD_INDEX) {
            return rawCardIndex
        }
        return if (isNavigationActive) NAVIGATION_CARD_INDEX else MEDIA_CARD_INDEX
    }
}
```

Add the unit test dependency:

```kotlin
dependencies {
    implementation(libs.common.joor)
    testImplementation("junit:junit:4.13.2")
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.xiaopeng.xposed.instrument.theme.utils.LeftSubCardAutoSwitchTest"`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/build.gradle.kts app/src/main/java/com/xiaopeng/xposed/instrument/theme/utils/LeftSubCardAutoSwitch.kt app/src/test/java/com/xiaopeng/xposed/instrument/theme/utils/LeftSubCardAutoSwitchTest.kt
git commit -m "test: add left sub-card auto-switch resolver"
```

### Task 2: Apply the Resolver to MainFragment and MapFullFragment

**Files:**
- Modify: `C:\Users\wjc13\Documents\XPluginInstrumentTheme\app\src\main\java\com\xiaopeng\xposed\instrument\theme\hook\MainFragmentHook.kt`
- Modify: `C:\Users\wjc13\Documents\XPluginInstrumentTheme\app\src\main\java\com\xiaopeng\xposed\instrument\theme\fragments\MapFullFragment.kt`
- Modify: `C:\Users\wjc13\Documents\XPluginInstrumentTheme\app\src\main\java\com\xiaopeng\xposed\instrument\theme\hook\MainActivityHook.kt`
- Test: `C:\Users\wjc13\Documents\XPluginInstrumentTheme\app\src\test\java\com\xiaopeng\xposed\instrument\theme\utils\LeftSubCardAutoSwitchTest.kt`

- [ ] **Step 1: Write the failing behavior check**

Use the resolver test from Task 1 as the safety net, then verify there is no runtime wiring yet by reviewing these missing responsibilities:

```kotlin
// MainFragmentHook currently only resumes the main map and does not observe NaviViewModel.
// MapFullFragment currently forwards leftSubCardLiveData directly to showSubCardView(it).
// There is no shared raw-card + nav-active state anywhere in the module.
```

- [ ] **Step 2: Implement the minimal runtime wiring**

Add hook-managed state for `MainFragment`:

```kotlin
private const val KEY_RAW_LEFT_SUB_CARD = "left_raw_sub_card"
private const val KEY_NAV_ACTIVE = "left_nav_active"
```

Hook these host methods:

```kotlin
XposedHelpers.findAndHookMethod(MainFragment::class.java, "onViewCreated", View::class.java, Bundle::class.java, mXCMethodOnViewCreated)
XposedHelpers.findAndHookMethod(MainFragment::class.java, "showLeftSubCardView", Int::class.javaPrimitiveType, mXCMethodShowLeftSubCardView)
```

In `onViewCreated`, observe `NaviViewModel.naviTBtVisibility` from the fragment activity and re-render the stored raw left sub-card when navigation visibility changes.

In `showLeftSubCardView`, store the raw card index in an additional instance field, then replace `param.args[0]` with:

```kotlin
LeftSubCardAutoSwitch.resolve(rawCardIndex = rawCardIndex, isNavigationActive = isNavigationActive)
```

In `MapFullFragment`, add the same two pieces of state:

```kotlin
private var mRawLeftSubCardIndex: Int? = null
private var mIsNavigationActive: Boolean = false
```

Replace the direct left-sub-card observer with a small render function:

```kotlin
private fun renderLeftSubCard(rawCardIndex: Int) {
    mRawLeftSubCardIndex = rawCardIndex
    val effectiveCardIndex = LeftSubCardAutoSwitch.resolve(rawCardIndex, mIsNavigationActive)
    mLeftInfoViewGroup.showSubCardView(effectiveCardIndex)
}
```

Observe `SRNaviViewModel.naviTBtVisibility` and re-render the stored raw left card whenever the nav-active flag changes.

Keep the earlier `SRInfoViewModel` eager initialization in `MainActivityHook`, because `MapFullFragment` still depends on startup-time state replay.

- [ ] **Step 3: Run targeted verification**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.xiaopeng.xposed.instrument.theme.utils.LeftSubCardAutoSwitchTest"
./gradlew :app:assembleDebug
```

Expected:
- unit test PASS
- debug build PASS

- [ ] **Step 4: Perform manual regression checklist**

Verify on device:

```text
1. Left raw card is navigation, no active guidance -> media card is shown.
2. Start navigation -> left card switches back to navigation.
3. End navigation -> left card returns to media.
4. Switch left card to car-condition / energy / odometer -> card stays unchanged.
5. Repeat checks in both MainFragment and MapFullFragment.
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MainFragmentHook.kt app/src/main/java/com/xiaopeng/xposed/instrument/theme/fragments/MapFullFragment.kt app/src/main/java/com/xiaopeng/xposed/instrument/theme/hook/MainActivityHook.kt
git commit -m "feat: auto-switch left nav card to media"
```
