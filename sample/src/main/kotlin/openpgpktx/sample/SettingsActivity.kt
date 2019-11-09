/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package openpgpktx.sample

import android.content.Intent
import android.content.SharedPreferences
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

    class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        private var providerPref: OpenPgpAppPreference? = null
        private var keyPref: OpenPgpKeyPreference? = null
        private var openPgpApiPref: Preference? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
            providerPref = findPreference<OpenPgpAppPreference>("provider_app")
            keyPref = findPreference<OpenPgpKeyPreference>("pgp_key")
            openPgpApiPref = findPreference("openpgp_api")
            keyPref?.openPgpProvider = preferenceManager.sharedPreferences.getString("provider_app", "")
            // Re-setting default values to show usage and silence IDE warnings about possible weaker access
            keyPref?.defaultUserId = "Harsh Shandilya <msfjarvis@gmail.com>"
            keyPref?.intentRequestCode = 9999
            openPgpApiPref?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), OpenPgpApiActivity::class.java))
                true
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (keyPref?.handleOnActivityResult(requestCode, resultCode, data) == true) {
                return
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                "provider_app" -> {
                    keyPref?.openPgpProvider = sharedPreferences?.getString(key, "")
                }
            }
        }
    }
}
