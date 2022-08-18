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

import android.R.attr
import android.content.Context
import androidx.annotation.CheckResult
import androidx.annotation.StyleRes
import com.dillon.dialogs.utils.MDUtil.isColorDark
import com.dillon.dialogs.utils.MDUtil.resolveColor

@StyleRes @CheckResult
internal fun inferTheme(
  context: Context,
  dialogBehavior: DialogBehavior
): Int {
  val isThemeDark = !inferThemeIsLight(context)
  return dialogBehavior.getThemeRes(isThemeDark)
}

@CheckResult internal fun inferThemeIsLight(context: Context): Boolean {
  return resolveColor(context = context, attr = attr.textColorPrimary).isColorDark()
}
