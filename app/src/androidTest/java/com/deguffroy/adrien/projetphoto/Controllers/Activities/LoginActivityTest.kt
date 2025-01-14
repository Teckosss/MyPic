package com.deguffroy.adrien.projetphoto.Controllers.Activities


import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.deguffroy.adrien.projetphoto.R
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(LoginActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION"
        )

    // ONLY TO TEST SIGN IN FLOW, FIRST SIGN OUT USER ON DEVICE & DELETE ACCOUNT IN FIREBASE
    /*@Test
    fun loginSignInWithEmailTest(){
        val signUpButton = onView(allOf(withId(R.id.email_button), isDisplayed()))
        signUpButton.perform(click())

        val editTextEmail = onView(allOf(withId(R.id.email), isDisplayed()))
        editTextEmail.perform(replaceText("a@a.aa"))

        val nextButton = onView(allOf(withId(R.id.button_next), withText("Next"), isDisplayed()))
        nextButton.perform(click())

        val editTextName = onView(allOf(withId(R.id.name), isDisplayed()))
        editTextName.perform(replaceText("Adrien"))

        val editTextPassword = onView(allOf(withId(R.id.password), isDisplayed()))
        editTextPassword.perform(replaceText("aaaaaaa"))

        val doneButton = onView(allOf(withId(R.id.button_create), withText("Save"), isDisplayed()))
        doneButton.perform(click())
    }*/

    // If this test is not perform with loginSignInWithEmailTest before, ensure that user is already connected in application
    @Test
    fun checkIsUIDisplayedTest(){
        onView(allOf(withId(R.id.main_activity_layout), isDisplayed()))
        onView(allOf(withId(R.id.fragment_view), isDisplayed()))
        onView(allOf(withId(R.id.main_activity_fab), isDisplayed()))
        onView(allOf(withId(R.id.bottom_navigation_view), isDisplayed()))
    }

    // If this test is not perform with loginSignInWithEmailTest before, ensure that user is already connected in application
    @Test
    fun clickRecyclerView(){
        Thread.sleep(3000)
        onView(allOf(withId(R.id.fragment_home_recycler_view), isDisplayed())).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        val imageView = onView(allOf(withId(R.id.detail_activity_image), isDisplayed()))
        imageView.perform(click())

        onView(allOf(withId(R.id.fullscreen_image), isDisplayed()))

        Espresso.pressBack()

        imageView.check(ViewAssertions.matches(isDisplayed()))

        Espresso.pressBack()

        onView(allOf(withId(R.id.main_activity_layout), isDisplayed()))

    }
}
