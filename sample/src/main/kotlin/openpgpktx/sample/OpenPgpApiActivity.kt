/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package openpgpktx.sample

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import me.msfjarvis.openpgpktx.OpenPgpError
import me.msfjarvis.openpgpktx.util.OpenPgpApi
import me.msfjarvis.openpgpktx.util.OpenPgpServiceConnection
import me.msfjarvis.openpgpktx.util.OpenPgpUtils.convertKeyIdToHex
import org.openintents.openpgp.IOpenPgpService2
import org.openintents.openpgp.OpenPgpDecryptionResult
import org.openintents.openpgp.OpenPgpSignatureResult

class OpenPgpApiActivity : AppCompatActivity() {
    private var mMessage: EditText? = null
    private var mCiphertext: EditText? = null
    private var mDetachedSignature: EditText? = null
    private var mEncryptUserIds: EditText? = null
    private var mGetKeyEdit: EditText? = null
    private var mGetKeyIdsEdit: EditText? = null
    private var mServiceConnection: OpenPgpServiceConnection? = null
    private var mSignKeyId: Long = 0
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.openpgp_provider)
        mMessage = findViewById(R.id.crypto_provider_demo_message)
        mCiphertext = findViewById(R.id.crypto_provider_demo_ciphertext)
        mDetachedSignature = findViewById(R.id.crypto_provider_demo_detached_signature)
        mEncryptUserIds = findViewById(R.id.crypto_provider_demo_encrypt_user_id)
        val cleartextSign = findViewById<Button>(R.id.crypto_provider_demo_cleartext_sign)
        val detachedSign = findViewById<Button>(R.id.crypto_provider_demo_detached_sign)
        val encrypt = findViewById<Button>(R.id.crypto_provider_demo_encrypt)
        val signAndEncrypt =
            findViewById<Button>(R.id.crypto_provider_demo_sign_and_encrypt)
        val decryptAndVerify =
            findViewById<Button>(R.id.crypto_provider_demo_decrypt_and_verify)
        val verifyDetachedSignature =
            findViewById<Button>(R.id.crypto_provider_demo_verify_detached_signature)
        mGetKeyEdit = findViewById(R.id.crypto_provider_demo_get_key_edit)
        mGetKeyIdsEdit = findViewById(R.id.crypto_provider_demo_get_key_ids_edit)
        val getKey = findViewById<Button>(R.id.crypto_provider_demo_get_key)
        val getKeyIds = findViewById<Button>(R.id.crypto_provider_demo_get_key_ids)
        val backup = findViewById<Button>(R.id.crypto_provider_demo_backup)
        cleartextSign.setOnClickListener {
            cleartextSign(
                Intent()
            )
        }
        detachedSign.setOnClickListener {
            detachedSign(
                Intent()
            )
        }
        encrypt.setOnClickListener {
            encrypt(
                Intent()
            )
        }
        signAndEncrypt.setOnClickListener {
            signAndEncrypt(
                Intent()
            )
        }
        decryptAndVerify.setOnClickListener {
            decryptAndVerify(
                Intent()
            )
        }
        verifyDetachedSignature.setOnClickListener {
            decryptAndVerifyDetached(
                Intent()
            )
        }
        getKey.setOnClickListener {
            getKey(
                Intent()
            )
        }
        getKeyIds.setOnClickListener {
            getKeyIds(
                Intent()
            )
        }
        backup.setOnClickListener {
            backup(
                Intent()
            )
        }
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val providerPackageName = settings.getString("provider_app", "")
        mSignKeyId = settings.getLong("pgp_key", 0)
        when {
            TextUtils.isEmpty(providerPackageName) -> {
                Toast.makeText(this, "No OpenPGP app selected!", Toast.LENGTH_LONG).show()
                finish()
            }
            mSignKeyId == 0L -> {
                Toast.makeText(this, "No key selected!", Toast.LENGTH_LONG).show()
                finish()
            }
            else -> { // bind to service
                mServiceConnection = OpenPgpServiceConnection(
                    this@OpenPgpApiActivity.applicationContext,
                    providerPackageName,
                    object : OpenPgpServiceConnection.OnBound {
                        override fun onBound(service: IOpenPgpService2?) {
                            Log.d(OpenPgpApi.TAG, "onBound!")
                        }

                        override fun onError(e: Exception?) {
                            Log.e(OpenPgpApi.TAG, "exception when binding!", e)
                        }
                    }
                )
                mServiceConnection?.bindToService()
            }
        }
    }

    private fun handleError(error: OpenPgpError) {
        runOnUiThread {
            Toast.makeText(
                this@OpenPgpApiActivity,
                "onError id:" + error.errorId + "\n\n" + error.message,
                Toast.LENGTH_LONG
            ).show()
            Log.e(Constants.TAG, "onError getErrorId:" + error.errorId)
            Log.e(Constants.TAG, "onError getMessage:" + error.message)
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(
                this@OpenPgpApiActivity,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Takes input from message or ciphertext EditText and turns it into a ByteArrayInputStream
     */
    private fun getInputstream(ciphertext: Boolean): InputStream {
        val `is`: InputStream
        val inputStr: String = if (ciphertext) {
            mCiphertext!!.text.toString()
        } else {
            mMessage!!.text.toString()
        }
        `is` = ByteArrayInputStream(inputStr.toByteArray(StandardCharsets.UTF_8))
        return `is`
    }

    private inner class MyCallback(
        internal var returnToCiphertextField: Boolean,
        internal var os: ByteArrayOutputStream?,
        internal var requestCode: Int
    ) : OpenPgpApi.IOpenPgpCallback {
        override fun onReturn(result: Intent?) {
            when (result!!.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
                OpenPgpApi.RESULT_CODE_SUCCESS -> {
                    showToast("RESULT_CODE_SUCCESS")
                    // encrypt/decrypt/sign/verify
                    if (os != null) {
                        try {
                            Log.d(
                                OpenPgpApi.TAG, "result: " + os!!.toByteArray().size +
                                    " str=" + os!!.toString("UTF-8")
                            )
                            if (returnToCiphertextField) {
                                mCiphertext!!.setText(os!!.toString("UTF-8"))
                            } else {
                                mMessage!!.setText(os!!.toString("UTF-8"))
                            }
                        } catch (e: UnsupportedEncodingException) {
                            Log.e(Constants.TAG, "UnsupportedEncodingException", e)
                        }
                    }
                    when (requestCode) {
                        REQUEST_CODE_DECRYPT_AND_VERIFY, REQUEST_CODE_DECRYPT_AND_VERIFY_DETACHED -> {
                            // RESULT_SIGNATURE and RESULT_DECRYPTION are never null!
                            val signatureResult: OpenPgpSignatureResult? = result.getParcelableExtra(
                                OpenPgpApi.RESULT_SIGNATURE
                            )
                            showToast(signatureResult.toString())
                            val decryptionResult: OpenPgpDecryptionResult? =
                                result.getParcelableExtra(
                                    OpenPgpApi.RESULT_DECRYPTION
                                )
                            showToast(decryptionResult.toString())
                        }
                        REQUEST_CODE_DETACHED_SIGN -> {
                            val detachedSig = result.getByteArrayExtra(
                                OpenPgpApi.RESULT_DETACHED_SIGNATURE
                            )
                            Log.d(OpenPgpApi.TAG, "RESULT_DETACHED_SIGNATURE: " + detachedSig?.size + " str=" + detachedSig?.contentToString())
                            mDetachedSignature?.setText(detachedSig?.contentToString())
                        }
                        REQUEST_CODE_GET_KEY_IDS -> {
                            val keyIds = result.getLongArrayExtra(
                                OpenPgpApi.RESULT_KEY_IDS
                            )
                            val out = StringBuilder("keyIds: ")
                            for (keyId in keyIds) {
                                out.append(convertKeyIdToHex(keyId)).append(", ")
                            }
                            showToast(out.toString())
                        }
                        else -> {
                        }
                    }
                }
                OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED -> {
                    showToast("RESULT_CODE_USER_INTERACTION_REQUIRED")
                    val pi = result.getParcelableExtra<PendingIntent>(
                        OpenPgpApi.RESULT_INTENT
                    )
                    try {
                        this@OpenPgpApiActivity.startIntentSenderFromChild(
                            this@OpenPgpApiActivity, pi.intentSender,
                            requestCode, null, 0, 0, 0
                        )
                    } catch (e: SendIntentException) {
                        Log.e(Constants.TAG, "SendIntentException", e)
                    }
                }
                OpenPgpApi.RESULT_CODE_ERROR -> {
                    showToast("RESULT_CODE_ERROR")
                    val error: OpenPgpError = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR)
                    handleError(error)
                }
            }
        }
    }

    private fun cleartextSign(data: Intent) {
        data.action = OpenPgpApi.ACTION_CLEARTEXT_SIGN
        data.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, mSignKeyId)
        val `is` = getInputstream(false)
        val os = ByteArrayOutputStream()
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            `is`,
            os,
            MyCallback(true, os, REQUEST_CODE_CLEARTEXT_SIGN)
        )
    }

    private fun detachedSign(data: Intent) {
        data.action = OpenPgpApi.ACTION_DETACHED_SIGN
        data.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, mSignKeyId)
        val `is` = getInputstream(false)
        // no output stream needed, detached signature is returned as RESULT_DETACHED_SIGNATURE
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            `is`,
            null,
            MyCallback(true, null, REQUEST_CODE_DETACHED_SIGN)
        )
    }

    private fun encrypt(data: Intent) {
        data.action = OpenPgpApi.ACTION_ENCRYPT
        if (!TextUtils.isEmpty(mEncryptUserIds!!.text.toString())) {
            data.putExtra(
                OpenPgpApi.EXTRA_USER_IDS,
                mEncryptUserIds!!.text.toString().split(",").toTypedArray()
            )
        }
        data.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true)
        val `is` = getInputstream(false)
        val os = ByteArrayOutputStream()
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            `is`,
            os,
            MyCallback(true, os, REQUEST_CODE_ENCRYPT)
        )
    }

    private fun signAndEncrypt(data: Intent) {
        data.action = OpenPgpApi.ACTION_SIGN_AND_ENCRYPT
        data.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, mSignKeyId)
        if (!TextUtils.isEmpty(mEncryptUserIds!!.text.toString())) {
            data.putExtra(
                OpenPgpApi.EXTRA_USER_IDS,
                mEncryptUserIds!!.text.toString().split(",").toTypedArray()
            )
        }
        data.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true)
        val `is` = getInputstream(false)
        val os = ByteArrayOutputStream()
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            `is`,
            os,
            MyCallback(true, os, REQUEST_CODE_SIGN_AND_ENCRYPT)
        )
    }

    private fun decryptAndVerify(data: Intent) {
        data.action = OpenPgpApi.ACTION_DECRYPT_VERIFY
        val `is` = getInputstream(true)
        val os = ByteArrayOutputStream()
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            `is`,
            os,
            MyCallback(false, os, REQUEST_CODE_DECRYPT_AND_VERIFY)
        )
    }

    private fun decryptAndVerifyDetached(data: Intent) {
        data.action = OpenPgpApi.ACTION_DECRYPT_VERIFY
        data.putExtra(
            OpenPgpApi.EXTRA_DETACHED_SIGNATURE,
            mDetachedSignature!!.text.toString().toByteArray()
        )
        // use from text from mMessage
        val `is` = getInputstream(false)
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            `is`,
            null,
            MyCallback(
                false,
                null,
                REQUEST_CODE_DECRYPT_AND_VERIFY_DETACHED
            )
        )
    }

    private fun getKey(data: Intent) {
        data.action = OpenPgpApi.ACTION_GET_KEY
        data.putExtra(
            OpenPgpApi.EXTRA_KEY_ID,
            java.lang.Long.decode(mGetKeyEdit!!.text.toString())
        )
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            null,
            null,
            MyCallback(false, null, REQUEST_CODE_GET_KEY)
        )
    }

    private fun getKeyIds(data: Intent) {
        data.action = OpenPgpApi.ACTION_GET_KEY_IDS
        data.putExtra(
            OpenPgpApi.EXTRA_USER_IDS,
            mGetKeyIdsEdit!!.text.toString().split(",").toTypedArray()
        )
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            null,
            null,
            MyCallback(false, null, REQUEST_CODE_GET_KEY_IDS)
        )
    }

    fun getAnyKeyIds(data: Intent) {
        data.action = OpenPgpApi.ACTION_GET_KEY_IDS
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            null,
            null,
            MyCallback(false, null, REQUEST_CODE_GET_KEY_IDS)
        )
    }

    private fun backup(data: Intent) {
        data.action = OpenPgpApi.ACTION_BACKUP
        data.putExtra(
            OpenPgpApi.EXTRA_KEY_IDS,
            longArrayOf(java.lang.Long.decode(mGetKeyEdit!!.text.toString()))
        )
        data.putExtra(OpenPgpApi.EXTRA_BACKUP_SECRET, true)
        data.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true)
        val os = ByteArrayOutputStream()
        val api = OpenPgpApi(this, mServiceConnection!!.service!!)
        api.executeApiAsync(
            data,
            null,
            os,
            MyCallback(true, os, REQUEST_CODE_BACKUP)
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        Log.d(Constants.TAG, "onActivityResult resultCode: $resultCode")
        // try again after user interaction
        if (resultCode == RESULT_OK) { /*
             * The data originally given to one of the methods above, is again
             * returned here to be used when calling the method again after user
             * interaction. The Intent now also contains results from the user
             * interaction, for example selected key ids.
             */
            when (requestCode) {
                REQUEST_CODE_CLEARTEXT_SIGN -> {
                    cleartextSign(data)
                }
                REQUEST_CODE_DETACHED_SIGN -> {
                    detachedSign(data)
                }
                REQUEST_CODE_ENCRYPT -> {
                    encrypt(data)
                }
                REQUEST_CODE_SIGN_AND_ENCRYPT -> {
                    signAndEncrypt(data)
                }
                REQUEST_CODE_DECRYPT_AND_VERIFY -> {
                    decryptAndVerify(data)
                }
                REQUEST_CODE_DECRYPT_AND_VERIFY_DETACHED -> {
                    decryptAndVerifyDetached(data)
                }
                REQUEST_CODE_GET_KEY -> {
                    getKey(data)
                }
                REQUEST_CODE_GET_KEY_IDS -> {
                    getKeyIds(data)
                }
                REQUEST_CODE_BACKUP -> {
                    backup(data)
                }
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mServiceConnection != null) {
            mServiceConnection!!.unbindFromService()
        }
    }

    companion object {
        const val REQUEST_CODE_CLEARTEXT_SIGN = 9910
        const val REQUEST_CODE_ENCRYPT = 9911
        const val REQUEST_CODE_SIGN_AND_ENCRYPT = 9912
        const val REQUEST_CODE_DECRYPT_AND_VERIFY = 9913
        const val REQUEST_CODE_GET_KEY = 9914
        const val REQUEST_CODE_GET_KEY_IDS = 9915
        const val REQUEST_CODE_DETACHED_SIGN = 9916
        const val REQUEST_CODE_DECRYPT_AND_VERIFY_DETACHED = 9917
        const val REQUEST_CODE_BACKUP = 9918
    }
}
