/*
 * Copyright © 2019 The Android Password Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
@file:JvmName("OpenPgpAppPreference")
package me.msfjarvis.openpgpktx.preference

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import me.msfjarvis.openpgpktx.R
import me.msfjarvis.openpgpktx.util.ProviderUtils
import me.msfjarvis.openpgpktx.util.getAttr

class OpenPgpAppPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = getAttr(context, androidx.preference.R.attr.preferenceStyle, android.R.attr.preferenceStyle),
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) {
    private val apps = ProviderUtils.getAppList(context)
    private val defaultName = context.resources.getString(R.string.openpgp_list_preference_none)

    override fun onClick() {
        val provider = getPersistedString(defaultName)
        val items = apps.map { it.simpleName }.toTypedArray()
        val selectedItem = if (provider.isEmpty()) {
            0
        } else {
            items.indexOf(apps.find { it.packageName == provider }?.simpleName ?: defaultName)
        }
        AlertDialog.Builder(context)
            .setTitle(title)
            .setSingleChoiceItems(items, selectedItem) { dialog, which ->
                persistString(apps.find { it.simpleName == items[which] }!!.packageName)
                notifyChanged()
                dialog.dismiss()
            }
            .show()
    }

    override fun getSummary(): CharSequence {
        val persisted = getPersistedString(defaultName)
        return apps.find { it.packageName == persisted }?.simpleName ?: defaultName
    }
}
