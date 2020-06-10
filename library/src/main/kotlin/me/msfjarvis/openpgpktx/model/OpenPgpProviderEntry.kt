/*
 * Copyright © 2019 The Android Password Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package me.msfjarvis.openpgpktx.model

import android.content.Intent
import android.graphics.drawable.Drawable

data class OpenPgpProviderEntry(
    val packageName: String,
    val simpleName: String,
    val icon: Drawable? = null,
    val intent: Intent? = null
) {
    override fun toString(): String = simpleName
}
