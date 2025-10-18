package org.example.tools

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

actual fun getGoogleCalendarService(): GoogleCalendarService = JvmGoogleCalendarService()

class JvmGoogleCalendarService : GoogleCalendarService {
    private val applicationName = "Trip AI Calendar"
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val tokensDirectoryPath = "${System.getProperty("user.home")}/.trip-ai/tokens"
    private val scopes = listOf(CalendarScopes.CALENDAR)
    private val credentialsFilePath = "/credentials.json"

    /**
     * Creates an authorized Credential object.
     * @return An authorized Credential object.
     * @throws Exception If credentials.json is not found
     */
    private fun getCredentials(): Credential {
        // Load client secrets from resources
        val inputStream = JvmGoogleCalendarService::class.java.getResourceAsStream(credentialsFilePath)
            ?: throw FileNotFoundException("Resource not found: $credentialsFilePath. Please place credentials.json in src/jvmMain/resources/")

        val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(inputStream))

        // Build flow and trigger user authorization request
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets, scopes
        )
            .setDataStoreFactory(FileDataStoreFactory(File(tokensDirectoryPath)))
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    /**
     * Build and return an authorized Calendar client service.
     */
    private fun getCalendarService(): Calendar {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential = getCredentials()
        return Calendar.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(applicationName)
            .build()
    }

    override suspend fun createEvent(eventName: String, dateTime: LocalDateTime): String {
        return try {
            val service = getCalendarService()

            // Convert LocalDateTime to RFC3339 format
            val instant = dateTime.toInstant(TimeZone.currentSystemDefault())
            val eventDateTime = EventDateTime()
                .setDateTime(com.google.api.client.util.DateTime(instant.toEpochMilliseconds()))
                .setTimeZone(TimeZone.currentSystemDefault().id)

            // Create the event
            val event = Event()
                .setSummary(eventName)
                .setStart(eventDateTime)
                .setEnd(eventDateTime) // Using same time for start and end (instant event)

            val calendarId = "primary"
            val createdEvent = service.events().insert(calendarId, event).execute()

            "Calendar event created successfully: ${createdEvent.htmlLink}"
        } catch (e: FileNotFoundException) {
            "Error: credentials.json not found. Please follow setup instructions:\n" +
                    "1. Go to Google Cloud Console (console.cloud.google.com)\n" +
                    "2. Create a new project or select existing one\n" +
                    "3. Enable Google Calendar API\n" +
                    "4. Create OAuth 2.0 Client ID credentials\n" +
                    "5. Download credentials.json\n" +
                    "6. Place it in composeApp/src/jvmMain/resources/credentials.json"
        } catch (e: Exception) {
            "Error creating calendar event: ${e.message}"
        }
    }
}
