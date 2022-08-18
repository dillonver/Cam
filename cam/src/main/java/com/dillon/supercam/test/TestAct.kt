package com.dillon.supercam.test

import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.LogUtils
import com.dillon.supercam.base.BAct
import com.dillon.supercam.databinding.ActivityTestBinding
import java.lang.Exception
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class TestAct : BAct() {
    private lateinit var binding: ActivityTestBinding

    override fun initView() {
        super.initView()
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initData() {
        super.initData()

        binding.tvTest.setOnClickListener {
            val testData = "qqztest"
            val testKey = "16ad5a4f4690441f800950986462df9afffc343c39bf448f9550fe39b2153269"

            val result1 = encryptToStr(testData,testKey)
            LogUtils.i(result1)

            val result2 = decryptFromStr(result1,testKey)
            LogUtils.i(result2)

            LogUtils.i(result1?.equals(result2))

        }

    }

    private val IV = null
    private val Mode = "AES"

    fun encrypt(content: String, password: String): ByteArray? {
        return try {
            val key = getKey(password) ?: return null //根据密码生成key
            val cipher = Cipher.getInstance(Mode) // 创建密码器"算法/模式/补码方式"
            val byteContent = content.toByteArray(charset("utf-8"))
            cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                IvParameterSpec(null)
            )
            cipher.doFinal(byteContent)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun encryptToStr(content: String, password: String): String? {
        val bytes = encrypt(content, password) ?: return null
        return EncodeUtils.base64Encode2String(bytes)
    }

    fun decrypt(content: ByteArray?, password: String): ByteArray? {
        return try {
            val key = getKey(password) ?: return null //根据密码生成key
            val cipher = Cipher.getInstance(Mode) //创建密码器"算法/模式/补码方式"
            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                IvParameterSpec(IV)
            )
            cipher.doFinal(content)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decryptFromStr(encodeStr: String?, password: String): String? {
        val encodeBytes: ByteArray = EncodeUtils.base64Decode(encodeStr)
        val bytes = decrypt(encodeBytes, password) ?: return null
        return String(bytes)
    }

    //根据密码生成16byte的key
    private fun getKey(password: String): SecretKeySpec? {
        return try {
            val passwdBytes = password.toByteArray(charset("utf-8"))
            if (passwdBytes.size < 16) {
                return null
            }
            //简单转换为16byte，建议用复杂的转换以防被知道密码后破解
            val bytes = ByteArray(16)
            for (i in 0..15) {
                bytes[i] = passwdBytes[i]
            }
            SecretKeySpec(bytes, "AES")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}


