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

import com.github.pambrose.common.util.isNull
import java.util.concurrent.atomic.AtomicReference

object ServiceCache {

  private val userCache = AtomicReference<List<User>>()
  private val unitsCache = AtomicReference<List<Units>>()

  fun users(action: () -> List<User>): List<User> {
    if (userCache.get().isNull())
      userCache.compareAndSet(null, action())
    return userCache.get()
  }

  fun refreshUsers(action: () -> List<User>): List<User> {
    userCache.set(action())
    return userCache.get()
  }

  fun units(action: () -> List<Units>): List<Units> {
    if (unitsCache.get().isNull())
      unitsCache.compareAndSet(null, action())
    return unitsCache.get()
  }

  fun refreshUnits(action: () -> List<Units>): List<Units> {
    unitsCache.set(action())
    return unitsCache.get()
  }
}