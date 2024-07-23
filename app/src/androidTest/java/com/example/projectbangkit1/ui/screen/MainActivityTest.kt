package com.example.projectbangkit1.ui.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.projectbangkit1.R
import com.example.projectbangkit1.ui.EspressoIdlingResource
import com.example.projectbangkit1.ui.auth.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest{
    private val email = "Wow"
    @get:Rule
    val activity = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @Before
    fun setUp() {
        Intents.init()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        Intents.release()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun logoutTest() {
        onView(withId(R.id.action_logout)).perform(click())
        onView(withId(android.R.id.button1)).perform(click())
        Intents.intended(hasComponent(LoginActivity::class.java.name))
        onView(withId(R.id.imageView)).check(matches(isDisplayed()))
    }

    @Test
    fun addStoryTest() {
        onView(withId(R.id.fab_add)).perform(click())
        Intents.intended(hasComponent(AddStoryActivity::class.java.name))
        onView(withId(R.id.addStory)).check(matches(isDisplayed()))
        onView(withId(R.id.addStory)).perform(closeSoftKeyboard())
        onView(withId(R.id.textInput)).perform(typeText(email), closeSoftKeyboard())
        onView(withId(R.id.btnUpload)).perform(click())
    }
}