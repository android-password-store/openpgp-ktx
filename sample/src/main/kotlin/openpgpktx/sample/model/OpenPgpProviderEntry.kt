/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package openpgpktx.sample.model

import android.content.Intent
import android.graphics.drawable.Drawable

data class OpenPgpProviderEntry(val packageName: String, val simpleName: String, val icon: Drawable? = null, val intent: Intent? = null) {
    override fun toString(): String = simpleName
}
