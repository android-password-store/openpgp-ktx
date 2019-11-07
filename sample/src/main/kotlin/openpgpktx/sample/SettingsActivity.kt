/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package openpgpktx.sample

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import me.msfjarvis.openpgpktx.preference.OpenPgpAppPreference
import me.msfjarvis.openpgpktx.preference.OpenPgpKeyPreference
import me.msfjarvis.openpgpktx.util.OpenPgpServiceConnection
import org.openintents.openpgp.IOpenPgpService2

class SettingsActivity : AppCompatActivity() {
    private var serviceConnection: OpenPgpServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        serviceConnection = OpenPgpServiceConnection(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("provider_app", ""),
            object : OpenPgpServiceConnection.OnBound {
                override fun onBound(service: IOpenPgpService2?) {
                    Toast.makeText(applicationContext, "onBound", Toast.LENGTH_SHORT).show()
                }

                override fun onError(e: Exception?) {
                    Toast.makeText(applicationContext, "onError", Toast.LENGTH_SHORT).show()
                }
            }
        )
        serviceConnection?.bindToService()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceConnection?.unbindFromService()
        serviceConnection = null
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val providerPref = findPreference<OpenPgpAppPreference>("provider_app")
            val keyPref = findPreference<OpenPgpKeyPreference>("pgp_key")
            keyPref?.setOpenPgpProvider(preferenceManager.sharedPreferences.getString("provider_app", ""))
            providerPref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                keyPref?.setOpenPgpProvider(newValue as String)
                true
            }
        }
    }
}
