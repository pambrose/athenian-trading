package com.github.pambrose

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ClearValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.File
import java.io.StringReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

object GoogleApiUtils {
  internal const val TOKENS_DIRECTORY_PATH = "tokens"
  private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
  private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
  private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)

  private val fromDate = LocalDate.parse("1899-12-30")
  private const val secsInDay = 24 * 60 * 60

  fun codeFlow(): GoogleAuthorizationCodeFlow {
    val strReader = StringReader(System.getenv("AUTH_CREDENTIALS"))
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, strReader)
    return GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
                                               JSON_FACTORY,
                                               clientSecrets,
                                               SCOPES)
      .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
      .setAccessType("offline")
      .build()
  }

  fun nowDateTime() =
    LocalDateTime.now()
      .let {
        val diff = ChronoUnit.DAYS.between(fromDate, it)
        val secsFraction = it.get(ChronoField.SECOND_OF_DAY) * 1.0 / secsInDay
        diff + secsFraction
      }

  fun sheetsService(applicationName: String, credential: Credential) =
    Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
      .setApplicationName(applicationName)
      .build() ?: throw GoogleApiException("Null sheets service")

  fun googleAuthPageUrl(flow: GoogleAuthorizationCodeFlow, state: String, redirectUrl: String) =
    flow.newAuthorizationUrl()
      .setState(state)
      .setRedirectUri(redirectUrl).build()

  fun <R> Sheets.query(ssId: String, range: String, mapper: List<Any>.() -> R) =
    spreadsheets().values().get(ssId, range).execute()
      ?.run {
        getValues()?.map { mapper(it) } ?: emptyList()
      } ?: throw GoogleApiException("Null get response")

  fun Sheets.append(
    ssId: String,
    range: String,
    values: List<List<Any>>,
    valueInputOption: ValueInputOption = ValueInputOption.USER_ENTERED,
    insertDataOption: InsertDataOption = InsertDataOption.OVERWRITE
  ) =
    spreadsheets().values().append(ssId, range, ValueRange().setValues(values))
      .also {
        it.valueInputOption = valueInputOption.name
        it.insertDataOption = insertDataOption.name
      }.execute() ?: throw GoogleApiException("Null append response")

  fun Sheets.clear(ssId: String, range: String) =
    spreadsheets().values().clear(ssId, range, ClearValuesRequest()).execute()
      ?: throw GoogleApiException("Null clear response")

}

enum class ValueInputOption { USER_ENTERED, RAW }
enum class InsertDataOption { OVERWRITE, INSERT_ROWS }

class MissingCredential(msg: String) : Exception(msg)
class GoogleApiException(msg: String) : Exception(msg)
class InvalidRequestException(msg: String) : Exception(msg)