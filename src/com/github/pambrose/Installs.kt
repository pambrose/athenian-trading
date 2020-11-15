/*
 * Copyright © 2020 Paul Ambrose (pambrose@mac.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.pambrose

import com.github.pambrose.EnvVar.FILTER_LOG
import com.github.pambrose.common.features.HerokuHttpsRedirect
import com.github.pambrose.common.response.respondWith
import com.github.pambrose.common.util.simpleClassName
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import mu.KLogging
import org.slf4j.event.Level

internal object Installs : KLogging() {

  fun Application.installs(
    production: Boolean,
    redirectHostname: String,
    forwardedHeaderSupportEnabled: Boolean,
    xforwardedHeaderSupportEnabled: Boolean
  ) {

    install(Locations)

    install(CallLogging) {
      level = Level.INFO

      if (FILTER_LOG.getEnv(true))
        filter { call ->
          call.request.path().let { it.startsWith("/") && !it.startsWith("/static/") }
        }

      format { call ->
        val request = call.request
        val response = call.response
        val logStr = request.toLogString()
        val remote = request.origin.remoteHost

        when (val status = response.status() ?: HttpStatusCode(-1, "Unknown")) {
          HttpStatusCode.Found -> "Redirect: $logStr -> ${response.headers[HttpHeaders.Location]} - $remote"
          else -> "$status: $logStr - $remote"
        }
      }
    }

    install(DefaultHeaders) {
      header("X-Engine", "Ktor")
    }

    install(Compression) {
      gzip {
        priority = 1.0
      }
      deflate {
        priority = 10.0
        minimumSize(1024) // condition
      }
    }

    install(StatusPages) {
      exception<Throwable> { cause ->
        logger.info(cause) { "Throwable caught: ${cause.simpleClassName}" }
        respondWith {
          stackTracePage(cause)
        }
      }

      status(HttpStatusCode.NotFound) {
        //call.respond(TextContent("${it.value} ${it.description}", Plain.withCharset(UTF_8), it))
        respondWith {
          page(false) {
            rootChoices("Invalid URL")
          }
        }
      }
    }

    if (forwardedHeaderSupportEnabled) {
      logger.info { "Enabling ForwardedHeaderSupport" }
      install(ForwardedHeaderSupport)
    } else {
      logger.info { "Not enabling ForwardedHeaderSupport" }
    }

    if (xforwardedHeaderSupportEnabled) {
      logger.info { "Enabling XForwardedHeaderSupport" }
      install(XForwardedHeaderSupport)
    } else {
      logger.info { "Not enabling XForwardedHeaderSupport" }
    }

    if (production && redirectHostname.isNotBlank()) {
      logger.info { "Installing HerokuHttpsRedirect using: $redirectHostname" }
      install(HerokuHttpsRedirect) {
        host = redirectHostname
        permanentRedirect = false
      }
    } else {
      logger.info { "Not installing HerokuHttpsRedirect" }
    }

  }
}