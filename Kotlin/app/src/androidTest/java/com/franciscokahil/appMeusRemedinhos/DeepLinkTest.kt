package com.franciscokahil.appMeusRemedinhos

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DeepLinkTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testDeepLinkHighlightsEvent() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val database = AppDatabase.getDatabase(context)
        
        val eventId = UUID.randomUUID().toString()
        val eventTitle = "Deep Link Test Event"
        
        runBlocking {
            database.eventDao().insertEvent(
                EventEntity(id = eventId, title = eventTitle, time = "15:00", icon = "🧪")
            )
        }
        
        // Create a deep link intent
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("meusremedinhos://event/$eventId")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage(context.packageName)
        }
        
        // Start the intent
        context.startActivity(intent)
        
        // Wait for UI and check if title is displayed
        composeTestRule.onNodeWithText(eventTitle).assertIsDisplayed()
    }
}
