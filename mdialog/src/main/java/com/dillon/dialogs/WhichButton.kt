/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dillon.dialogs

import com.dillon.dialogs.internal.button.DialogActionButtonLayout.Companion.INDEX_NEGATIVE
import com.dillon.dialogs.internal.button.DialogActionButtonLayout.Companion.INDEX_NEUTRAL
import com.dillon.dialogs.internal.button.DialogActionButtonLayout.Companion.INDEX_POSITIVE

enum class WhichButton(val index: Int) {
  POSITIVE(INDEX_POSITIVE),
  NEGATIVE(INDEX_NEGATIVE),
  NEUTRAL(INDEX_NEUTRAL);

  companion object {
    fun fromIndex(index: Int) = when (index) {
      INDEX_POSITIVE -> POSITIVE
      INDEX_NEGATIVE -> NEGATIVE
      INDEX_NEUTRAL -> NEUTRAL
      else -> throw IndexOutOfBoundsException("$index is not an action button index.")
    }
  }
}
