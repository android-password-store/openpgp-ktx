/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package openpgpktx.sample.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import openpgpktx.sample.R
import openpgpktx.sample.model.OpenPgpProviderEntry

object ProviderUtils {
    private const val OPENKEYCHAIN_PACKAGE = "org.sufficientlysecure.keychain"
    private const val MARKET_INTENT_URI_BASE = "market://details?id=%s"
    private const val PACKAGE_NAME_APG = "org.thialfihar.android.apg"
    private val PROVIDER_BLACKLIST = arrayOf(PACKAGE_NAME_APG)
    private val MARKET_INTENT = Intent(Intent.ACTION_VIEW, Uri.parse(String.format(MARKET_INTENT_URI_BASE, OPENKEYCHAIN_PACKAGE)))

    fun getAppList(context: Context): ArrayList<OpenPgpProviderEntry> {
        val apps = ArrayList<OpenPgpProviderEntry>()

        // Add the 'None' option
        apps.add(0,
            OpenPgpProviderEntry(
                "",
                "None",
                AppCompatResources.getDrawable(context, android.R.drawable.sym_def_app_icon)
            )
        )

        // Start searching for the real ones
        val intent = Intent("org.openintents.openpgp.IOpenPgpService2")
        var resInfo = context.packageManager.queryIntentServices(intent, 0)
        for (resolveInfo in resInfo) {
            resolveInfo.serviceInfo?.apply {
                if (packageName in PROVIDER_BLACKLIST) return@apply
                val simpleName = loadLabel(context.packageManager).toString()
                val icon = loadIcon(context.packageManager)
                apps.add(
                    OpenPgpProviderEntry(
                        packageName, simpleName, icon
                    )
                )
            } ?: continue
        }
        if (apps.size == 1) {
            resInfo = context.packageManager
                .queryIntentActivities(MARKET_INTENT, 0)
            for (resolveInfo in resInfo) {
                resolveInfo.activityInfo?.apply {
                    val marketIntent = Intent(MARKET_INTENT)
                    marketIntent.setPackage(packageName)
                    val icon = loadIcon(context.packageManager)
                    val marketName = applicationInfo.loadLabel(context.packageManager).toString()
                    val simpleName = String.format(
                        context.getString(R.string.openpgp_install_openkeychain_via),
                        marketName
                    )
                    apps.add(
                        OpenPgpProviderEntry(
                            OPENKEYCHAIN_PACKAGE, simpleName, icon, marketIntent
                        )
                    )
                }
            }
        }
        return apps
    }
}
