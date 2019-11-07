/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package me.msfjarvis.openpgpktx.util

import android.content.Context
import android.util.TypedValue

/**
 * @return The resource ID value in the `context` specified by `attr`. If it does
 * not exist, `fallbackAttr`.
 */
fun getAttr(context: Context, attr: Int, fallbackAttr: Int): Int {
    val value = TypedValue()
    context.theme.resolveAttribute(attr, value, true)
    return if (value.resourceId != 0) {
        attr
    } else fallbackAttr
}
