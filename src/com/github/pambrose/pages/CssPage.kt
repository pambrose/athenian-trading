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

package com.github.pambrose.pages

import com.github.pambrose.PipelineCall
import com.github.pambrose.pages.DIVS.SPACED_TABLE
import com.github.pambrose.respondCss
import io.ktor.server.application.*
import kotlinx.css.*

enum class DIVS {
  SPACED_TABLE
}

object CssPage {

  suspend fun PipelineCall.cssPage() =
    call.respondCss {
      rule("html, body, table") {
        fontSize = 24.px
        fontFamily = "verdana, arial, helvetica, sans-serif"
      }
      rule("ul") {
        paddingLeft = 0.px
        listStyleType = ListStyleType.none
      }
      rule("li") {
        paddingBottom = 15.px
      }
      rule(".${SPACED_TABLE.name} td") {
        paddingRight = 15.px
      }
      // Turn links red on mouse hovers.
      rule("a:hover") {
        color = Color.red
      }
    }
}