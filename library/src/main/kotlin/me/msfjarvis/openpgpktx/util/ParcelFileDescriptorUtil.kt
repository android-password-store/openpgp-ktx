/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package me.msfjarvis.openpgpktx.util

import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.AutoCloseInputStream
import android.os.ParcelFileDescriptor.AutoCloseOutputStream
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object ParcelFileDescriptorUtil {
    @Throws(IOException::class)
    fun pipeFrom(inputStream: InputStream): ParcelFileDescriptor {
        val pipe = ParcelFileDescriptor.createPipe()
        val readSide = pipe[0]
        val writeSide = pipe[1]
        TransferThread(inputStream, AutoCloseOutputStream(writeSide))
            .start()
        return readSide
    }

    @Throws(IOException::class)
    fun pipeTo(outputStream: OutputStream, output: ParcelFileDescriptor?): TransferThread {
        val t = TransferThread(AutoCloseInputStream(output), outputStream)
        t.start()
        return t
    }

    class TransferThread(val `in`: InputStream, private val out: OutputStream) :
        Thread("IPC Transfer Thread") {
        override fun run() {
            val buf = ByteArray(4096)
            var len: Int
            try {
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            } catch (e: IOException) {
                Log.e(OpenPgpApi.TAG, "IOException when writing to out", e)
            } finally {
                try {
                    `in`.close()
                } catch (ignored: IOException) {
                }
                try {
                    out.close()
                } catch (ignored: IOException) {
                }
            }
        }

        init {
            isDaemon = true
        }
    }
}
