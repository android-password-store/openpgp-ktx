/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package openpgpktx.sample.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import openpgpktx.sample.util.ProviderUtils
import openpgpktx.sample.util.getAttr

class OpenPgpAppPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = getAttr(context, androidx.preference.R.attr.dialogPreferenceStyle, android.R.attr.dialogPreferenceStyle),
    defStyleRes: Int = 0
) : ListPreference(context, attrs, defStyleAttr, defStyleRes) {
    private val apps = ProviderUtils.getAppList(context)

    override fun getEntries(): Array<CharSequence> {
        return apps.map { it.simpleName }.toTypedArray()
    }

    override fun getEntryValues(): Array<CharSequence> {
        return apps.map { it.packageName }.toTypedArray()
    }

}
