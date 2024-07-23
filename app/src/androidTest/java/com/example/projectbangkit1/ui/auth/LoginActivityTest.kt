package com.example.projectbangkit1.ui.auth

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
import com.example.projectbangkit1.R
import com.example.projectbangkit1.ui.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest{
    private val email = "ramaalex1221@gmail.com"
    private val password = "12345678"

    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

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
    fun loginTest() {
        onView(withId(R.id.imageView)).check(matches(isDisplayed()))
        onView(withId(R.id.emailInput)).perform(typeText(email), closeSoftKeyboard())
        onView(withId(R.id.passInput)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())
    }

    @Test
    fun registTest() {
        onView(withId(R.id.signup)).perform(click())
        Intents.intended(hasComponent(RegisterActivity::class.java.name))
        onView(withId(R.id.imageView)).check(matches(isDisplayed()))
    }
}