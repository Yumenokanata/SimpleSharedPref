package indi.yume.tools.simplesharedpref.extensions

import android.content.pm.PackageManager
import android.util.Base64
import indi.yume.tools.simplesharedpref.Pipe
import java.nio.charset.Charset
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec


private val IV = byteArrayOf(12, -42, 72, 74, 71, -45, 70, 65, -12, 109, 32, 101, -14, 0, -29, 74)
private val SALT: ByteArray =
    byteArrayOf(79, -89, 74, -116, -46, -102, -13, 34, -47, -60, 57, 85, -91, -42, 32, -127, -19, -51, -24, 39)


private const val AES_TRANS = "AES/CBC/PKCS5Padding"
private const val RSA_TRANS = "RSA/ECB/PKCS1Padding"

fun cipherAES(psw: String, charset: Charset = Charsets.UTF_8, salt: ByteArray = SALT, iv: ByteArray = IV): Pipe<String, String> =
    cipherAES(generateKey(psw, salt), charset, iv)

fun cipherAES(key: Key, charset: Charset = Charsets.UTF_8, iv: ByteArray = IV): Pipe<String, String> =
    Pipe(get = { w -> String(decrypt(Base64.decode(w.toByteArray(charset), Base64.DEFAULT), key, AES_TRANS, iv)) },
        reverseGet = { r -> Base64.encodeToString(encrypt(r.toByteArray(charset), key, AES_TRANS, iv), Base64.DEFAULT) })

fun cipherRSA(psw: String, charset: Charset = Charsets.UTF_16, iv: ByteArray = IV): Pipe<String, String> =
    cipherRSA(generateRSAKey(psw.toByteArray()), charset, iv)

fun cipherRSA(key: Key, charset: Charset = Charsets.UTF_16, iv: ByteArray = IV): Pipe<String, String> =
    Pipe(get = { w -> String(decrypt(Base64.decode(w.toByteArray(charset), Base64.DEFAULT), key, RSA_TRANS, iv)) },
        reverseGet = { r -> Base64.encodeToString(encryptRSA(r.toByteArray(charset), key), Base64.DEFAULT) })

@Throws(
    NoSuchAlgorithmException::class,
    NoSuchPaddingException::class,
    InvalidKeyException::class,
    InvalidAlgorithmParameterException::class,
    IllegalBlockSizeException::class,
    BadPaddingException::class
)
internal fun decrypt(
    paramArrayOfByte: ByteArray,
    secretKey: Key,
    transformation: String,
    iv: ByteArray = IV
): ByteArray {
    val localCipher = Cipher.getInstance(transformation)
    localCipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
    return localCipher.doFinal(paramArrayOfByte)
}

@Throws(
    NoSuchAlgorithmException::class,
    NoSuchPaddingException::class,
    InvalidKeyException::class,
    InvalidAlgorithmParameterException::class,
    IllegalBlockSizeException::class,
    BadPaddingException::class
)
internal fun encrypt(
    paramArrayOfByte: ByteArray,
    secretKey: Key,
    transformation: String,
    iv: ByteArray = IV
): ByteArray {
    val localCipher = Cipher.getInstance(transformation)
    localCipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
    return localCipher.doFinal(paramArrayOfByte)
}

fun encryptRSA(data: ByteArray, secretKey: Key): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return cipher.doFinal(data)
}

fun generateRSAKey(keyByte: ByteArray): Key =
    KeyFactory
        .getInstance("RSA")
        .generatePublic(X509EncodedKeySpec(keyByte))

@Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class, PackageManager.NameNotFoundException::class)
fun generateKey(password: String, salt: ByteArray = SALT): Key {
    val pbeKeySpec = PBEKeySpec(password.toCharArray(), salt, 1024, 256)
    return SecretKeyFactory.getInstance("PBEWITHSHAAND256BITAES-CBC-BC").generateSecret(pbeKeySpec)
}


