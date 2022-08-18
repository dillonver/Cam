package com.dillon.supercam.utils

import android.accessibilityservice.AccessibilityService
import android.app.*
import android.app.Notification.VISIBILITY_PRIVATE
import android.content.*
import android.content.Context.POWER_SERVICE
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.pm.*
import android.graphics.*
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Pair
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.exifinterface.media.ExifInterface
import com.blankj.utilcode.util.*
import com.dillon.supercam.R
import com.dillon.supercam.WelcomeAct
import com.dillon.supercam.base.App
import com.dillon.supercam.base.BAct
import com.dillon.supercam.bean.AppInfo
import com.dillon.supercam.bean.MediaInfo
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.ui.home.HomeAct
import com.dillon.supercam.ui.setting.SettingsAct
import com.dillon.supercam.ui.setting.SpecialSetAct
import com.dillon.supercam.ui.shortcut.OnePxAct
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.*
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern


/**
 * @author dillon
 * @description:
 * @date :2019/8/23 14:22
 */
object CoreUtils {

    //更改图标、名称
    fun showHideApp(oldCls: String, newCls: String) {
        try {

            val pm = Utils.getApp().packageManager
            pm.setComponentEnabledSetting(
                ComponentName(
                    Utils.getApp().baseContext,
                    oldCls
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                ComponentName(
                    Utils.getApp().baseContext,
                    newCls
                ),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            ViewUtils.runOnUiThreadDelayed({
                ActivityUtils.startActivity(HomeAct::class.java)
                ActivityUtils.startActivity(SettingsAct::class.java)
                ActivityUtils.startActivity(SpecialSetAct::class.java)
            }, 1 * 1000L)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //获取App启动名，带包名
    fun appLauncherName(pkg: String): String? {
        var packageinfo: PackageInfo? = null
        try {
            packageinfo =
                Utils.getApp().packageManager.getPackageInfo(pkg, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        if (packageinfo == null) {
            return null
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        val resolveIntent = Intent(Intent.ACTION_MAIN, null)
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        resolveIntent.setPackage(packageinfo.packageName)
        // 通过getPackageManager()的queryIntentActivities方法遍历
        val resolveInfoList =
            Utils.getApp().packageManager
                .queryIntentActivities(resolveIntent, 0)
        for (resolveInfo in resolveInfoList) {
            LogUtils.i("resolveInfo:$resolveInfo")
        }
        val resolveInfo = resolveInfoList.iterator().next()
        if (resolveInfo != null) { // packagename = 参数packname
            val packageName = resolveInfo.activityInfo.packageName
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            val className = resolveInfo.activityInfo.name
            return resolveInfo.activityInfo.name
        }
        return null
    }


    //APP风格
    fun appStyle(act: BAct) {
        try {
            //设置状态栏透明并高亮，即白底黑字
            BarUtils.setStatusBarColor(act, Color.argb(0, 0, 0, 0))
            BarUtils.setStatusBarLightMode(act, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun makeNotification(
        context: Service,
        mChannelId: String,
        mChannelName: String
    ) {
        makeNotification(context, mChannelId, mChannelName, true)
    }


    fun makeNotification(
        context: Service,
        mChannelId: String,
        mChannelName: String,
        sameNoticeId: Boolean
    ) {
        try {
            val intent = Intent(context, WelcomeAct::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val manager =
                Utils.getApp().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                mChannelId, mChannelName,
                NotificationManager.IMPORTANCE_NONE
            )
            manager.createNotificationChannel(channel)
            val notification = Notification.Builder(context, mChannelId)
                .setSmallIcon(R.drawable.ic_logo)
                .setAutoCancel(true)
                .setVisibility(VISIBILITY_PRIVATE)
                .setContentIntent(pendingIntent)
                .build()
            if (sameNoticeId) {
                //同一个ID为了替换Notice,正常情况ID需要设置唯一
                context.startForeground(999, notification)
            } else {
                context.startForeground(((Math.random() * 1000) + 1).toInt(), notification)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //检测悬浮窗权限
    fun checkDrawOverlaysPermission(): Boolean {
        return XXPermissions.isGranted(Utils.getApp(), Permission.SYSTEM_ALERT_WINDOW)
    }


    //忽略电池优化
    fun ignoreBatteryOptimization(activity: Activity) {
        val powerManager = activity.getSystemService(POWER_SERVICE) as PowerManager?
        val hasIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager!!.isIgnoringBatteryOptimizations(activity.packageName)
        } else {
            true
        }
        if (!hasIgnored) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivity(intent)
        }
    }

    //查看其它应用使用情况
    fun checkUsagePermission(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED

    }

    fun startCommonService(cls: Class<*>) {
        ServiceUtils.startService(cls)
    }

    fun stopCommonService(cls: Class<*>): Boolean {
        return ServiceUtils.stopService(cls)
    }


    /**
     * 跳转设置界面
     */
    fun goAppSetting(context: Context) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)

    }

    //打开手机通知设置页面
    fun goAppNotificationSetting(context: Context) {
        if (RomUtils.isXiaomi()) {
            goAppSetting(context)
        } else {
            val intent = Intent()
            if (Build.VERSION.SDK_INT >= 26) {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
            } else {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", context.packageName)
                intent.putExtra("app_uid", context.applicationInfo.uid)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

    }


    //获取前台应用
//    fun getForegroundApp(context: Context): String? {
//        val endTime = System.currentTimeMillis()//结束时间
//        val statTime = endTime - 1 * 60 * 60 * 1000//开始时间
//        val usageStatsManager =
//            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
//        val usageStatsList =
//            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, statTime, endTime)
//        if (usageStatsList == null || usageStatsList.isEmpty()) {
//            return null
//        }
//        val usageStatsMap = TreeMap<Long, UsageStats>()
//        for (usageStats in usageStatsList) {
//            usageStatsMap[usageStats.lastTimeUsed] = usageStats
//        }
//        if (usageStatsMap.isEmpty()) {
//            return null
//        }
//        return usageStatsMap[usageStatsMap.lastKey()]?.packageName
//    }

    private fun getAppInfo(pm: PackageManager, pi: PackageInfo?): AppInfo? {
        if (pi == null) return null
        val appInfo = AppInfo()
        val ai = pi.applicationInfo
        appInfo.appPkg = pi.packageName
        appInfo.appName = ai.loadLabel(pm).toString()
        appInfo.packagePath = ai.sourceDir
        appInfo.firstInstallTime = pi.firstInstallTime
        appInfo.lastUpdateTime = pi.lastUpdateTime
        appInfo.versionName = pi.versionName
        appInfo.versionCode = pi.versionCode
        appInfo.isSystem = ApplicationInfo.FLAG_SYSTEM and ai.flags != 0
        return appInfo
    }

    //获取手机上所有APP列表
    private fun getAppInfoList(): List<AppInfo>? {
        val appInfoList = mutableListOf<AppInfo>()
        val pm = Utils.getApp().packageManager ?: return null
        val installedPackages = pm.getInstalledPackages(0)
        for (pi in installedPackages) {
            val ai = getAppInfo(pm, pi) ?: continue
            appInfoList.add(ai)
        }
        return appInfoList
    }

    //获取非系统APP列表
    fun notSystemAppInfoList(): MutableList<AppInfo> {
        val appInfoList = getAppInfoList()
        val appInfoListNew = mutableListOf<AppInfo>()
        if (appInfoList != null && appInfoList.isNotEmpty()) {
            for (app in appInfoList) {
                if (!app.isSystem) {
                    appInfoListNew.add(app)
                }
            }
        }
        return appInfoListNew
    }

    fun notSystemAppNameList(): MutableList<String> {
        val appInfoList = notSystemAppInfoList()
        val appNameList = mutableListOf<String>()
        if (appInfoList.size > 0) {
            for (appInfo in appInfoList) {
                appNameList.add(appInfo.appName + "")
            }
        }
        return appNameList
    }

    fun isEmail(email: String?): Boolean {
        if (null == email || "" == email) return false
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        val p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*")//复杂匹配
        val m = p.matcher(email)
        return m.matches()
    }


    //打开设置权限页面
    fun openNotificationListenSettings(context: Context) {
        try {
            val intent: Intent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                } else {
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    //检测获取系统通知权限
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }


    //当前的屏幕锁有五种设置，分别是没有设置屏幕锁，滑动解锁，图案解锁，PIN码解锁，密码解锁。
//如果没有设置屏幕锁，返回值会一直为true。如果用户设置了屏幕锁（包括后四种锁中的任何一种），屏幕不亮时返回false，屏幕亮时，解锁前返回false，解锁后返回true
    private fun isUserPresent(): Boolean {
        val keyguardManager =
            Utils.getApp().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return !keyguardManager.isKeyguardLocked

    }


    fun encryptRSA(data: String?): ByteArray? {
        if (data == null) {
            return null
        }
        val keySize = 1024
        return EncryptUtils.encryptRSA(
            data.toByteArray(),
            EncodeUtils.base64Decode(CommonK.PUBLIC_KEY.toByteArray()),
            keySize,
            "RSA/None/PKCS1Padding"
        )
    }

    fun decryptRSA(encryptData: ByteArray?): String? {
        if (encryptData == null) {
            return null
        }
        val keySize = 1024
        val decryptData = EncryptUtils.decryptRSA(
            encryptData,
            EncodeUtils.base64Decode(CommonK.PRIVATE_KEY.toByteArray()),
            keySize,
            "RSA/None/PKCS1Padding"
        )
        return EncodeUtils.base64Encode2String(decryptData)
    }

    fun encryptRSA2String(data: String?): String? {
        if (data == null) {
            return null
        }
        val keySize = 1024
        val encryptData = EncryptUtils.encryptRSA(
            data.toByteArray(),
            EncodeUtils.base64Decode(CommonK.PUBLIC_KEY.toByteArray()),
            keySize,
            "RSA/None/PKCS1Padding"
        )
        return EncodeUtils.base64Encode2String(encryptData)
    }

    //生成密钥对   keySize = 1024、2048
    @Throws(NoSuchAlgorithmException::class)
    fun genKeyPair(size: Int): Pair<String, String> {
        val secureRandom = SecureRandom()

        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")

        keyPairGenerator.initialize(size, secureRandom)

        val keyPair = keyPairGenerator.generateKeyPair()

        val publicKey = keyPair.public

        val privateKey = keyPair.private

        val publicKeyBytes = publicKey.encoded
        val privateKeyBytes = privateKey.encoded

        val publicKeyBase64 = EncodeUtils.base64Encode2String(publicKeyBytes)
        val privateKeyBase64 = EncodeUtils.base64Encode2String(privateKeyBytes)

        return Pair.create(publicKeyBase64, privateKeyBase64)
    }


    /**
     * 判断文件大小处于限制内
     *
     * @param fileLen 文件长度
     * @param fileSize 限制大小
     * @param fileUnit 限制的单位（B,K,M,G）
     * @return
     */
    fun checkFileSizeIsLimit(fileLen: Long?, fileSize: Int, fileUnit: String): Boolean {
        var fileSizeCom = 0.0
        when {
            "B" == fileUnit.toUpperCase(Locale.getDefault()) -> fileSizeCom = fileLen!!.toDouble()
            "K" == fileUnit.toUpperCase(Locale.getDefault()) -> fileSizeCom =
                fileLen!!.toDouble() / 1024
            "M" == fileUnit.toUpperCase(Locale.getDefault()) -> fileSizeCom =
                fileLen!!.toDouble() / (1024 * 1024)
            "G" == fileUnit.toUpperCase(Locale.getDefault()) -> fileSizeCom =
                fileLen!!.toDouble() / (1024 * 1024 * 1024)
        }
        return fileSizeCom <= fileSize

    }

    //获取相机程序列表
    fun getCameraList(): MutableList<String>? {
        val cameraList = mutableListOf<String>()
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        val resolveInfoList = Utils.getApp().packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (resolveInfoList.size == 0) {
            LogUtils.i("no Camera")
            return null
        }
        for (i in resolveInfoList.indices) {
            val activityInfo = resolveInfoList[i].activityInfo
            cameraList.add(activityInfo.packageName)
        }
        return cameraList
    }

    fun isCameraTarget(pkg: String?): Boolean {
        val list = getCameraList()
        if (pkg == null || list.isNullOrEmpty()) {
            return false
        }
        for (i in list.indices) {
            if (pkg == list[i]) {
                return true
            }
        }
        return false
    }

    //获取浏览器包名，默认返回一个
    private fun getBrowserList(): MutableList<String>? {
        val browserList = mutableListOf<String>()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.data = Uri.parse("http://")
        val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Utils.getApp().packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_ALL
            )
        } else {
            Utils.getApp().packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        }
        if (resolveInfoList.size == 0) {
            LogUtils.i("no browser")
            return null
        }
        for (i in resolveInfoList.indices) {
            val activityInfo = resolveInfoList[i].activityInfo
            browserList.add(activityInfo.packageName)

        }
        return browserList
    }

    //判断是否浏览器
    fun isBrowser(pkg: String?): Boolean {
        val list = getBrowserList()
        if (pkg == null || list.isNullOrEmpty()) {
            return false
        }
        for (i in list.indices) {
            if (pkg == list[i]) {
                return true
            }
        }
        return false
    }

    fun isBrowserTarget(pkg: String?): Boolean {
        val list = CommonK.BROWSER_TARGET
        if (pkg == null || list.isNullOrEmpty()) {
            return false
        }
        for (i in list.indices) {
            if (pkg == list[i]) {
                return true
            }
        }
        return false
    }

    //判断是否是截图目标APP
    fun isChatTarget(pkg: String?): Boolean {
        val list = CommonK.CHAT_TARGET
        if (pkg == null || list.isNullOrEmpty()) {
            return false
        }
        for (i in list.indices) {
            if (pkg == list[i]) {
                return true
            }
        }
        return false
    }

    //判断辅助服务是否开启
    fun isAccessibilitySettingsOn(
        clazz: Class<out AccessibilityService>
    ): Boolean {
        var accessibilityEnabled = 0
        val service = Utils.getApp().packageName + "/" + clazz.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                Utils.getApp().applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                Utils.getApp().applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }


    fun getProcessName(pid: Int): String? {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            var processName: String = reader.readLine()
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim { it <= ' ' }
            }
            return processName
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
        return null
    }


    //字符串简单加密，翻转，+8 -8
    val Type_Encrypt = 1
    val Type_Decrypt = 2
    fun simpleEncryptAndDecryptString(data: String?, type: Int): String? {
        if (data.isNullOrBlank()) return null
        val newData = StringBuilder(data.trim()).reverse()
        if (newData.isBlank()) return null
        val s = StringBuilder()
        if (Type_Encrypt == type) {
            for (element in newData) {
                var a = element.toInt()
                a += 8
                s.append(a.toChar())
            }
            return s.toString()
        }
        if (Type_Decrypt == type) {
            for (element in newData) {
                var a = element.toInt()
                a -= 8
                s.append(a.toChar())
            }
            return s.toString()
        }
        return null
    }

    //简单字符串加密 翻转,对换
    fun simpleEncrypt(data: String?, type: Int): String? {
        if (LocalUtils.getLocalUser()?.uID.isNullOrBlank()) return null
        if (data.isNullOrBlank() || data.length < 5) return null //限制最低5位
        if (Type_Encrypt == type) {
            val uiDEc = simpleEncryptAndDecryptString(
                LocalUtils.getLocalUser()?.uID,
                Type_Encrypt
            ) + ""
            val newData = StringUtils.reverse(data)
            val len = newData.length
            val p1: String
            val p2: String
            if (len > 50) {
                p1 = newData.substring(0, 25)
                p2 = newData.substring(25, len)
            } else {
                p1 = newData.substring(0, 2)
                p2 = newData.substring(2, len)
            }
            return p2 + uiDEc + p1

        }
        if (Type_Decrypt == type) {
            val dataLen = data.length
            val mData = if (dataLen - 6 > 50) {
                data.replace(data.substring(dataLen - 31, dataLen - 25), "")
            } else {
                data.replace(data.substring(dataLen - 8, dataLen - 2), "")
            }
            val newData = StringUtils.reverse(mData)
            val len = newData.length
            val p1: String
            val p2: String
            if (len > 50) {
                p1 = newData.substring(0, 25)
                p2 = newData.substring(25, len)
            } else {
                p1 = newData.substring(0, 2)
                p2 = newData.substring(2, len)
            }

            return p2 + p1
        }
        return null

    }

    fun simpleEncrypt(data: String?, pass: String?, type: Int): String? {
        if (data.isNullOrBlank() || pass.isNullOrBlank()) return null
        val aPass = StringUtils.reverse("<$pass>")
        val bPass = StringUtils.reverse("<dillon>")
        val aPassLen = aPass.length
        val aPassDevPos = aPassLen / 2
        val aPassP1 = aPass.substring(0, aPassDevPos)
        val aPassP2 = aPass.substring(aPassDevPos, aPassLen)
        val bPassLen = bPass.length
        val bPassDevPos = bPassLen / 2
        val bPassP1 = bPass.substring(0, bPassDevPos)
        val bPassP2 = bPass.substring(bPassDevPos, bPassLen)
        if (Type_Encrypt == type) {
            val newData = StringUtils.reverse(data)
            val newDataLen = newData.length
            val newDataDevPos = newDataLen / 2
            val newDataP1 = newData.substring(0, newDataDevPos)
            val newDataP2 = newData.substring(newDataDevPos, newDataLen)
            val finalStr = aPassP1 + bPassP2 + newDataP2 + aPassP2 + bPassP1 + newDataP1
            return simpleEncryptAndDecryptString(
                EncodeUtils.base64Encode2String(finalStr.toByteArray()),
                Type_Encrypt
            )
        }
        if (Type_Decrypt == type) {
            val dataOriStr = simpleEncryptAndDecryptString(data, Type_Decrypt) ?: return null
            val newData = String(EncodeUtils.base64Decode(dataOriStr))
            val tempStr = newData.replace(aPassP1, "").replace(aPassP2, "").replace(bPassP1, "")
                .replace(bPassP2, "")
            val reverseData = StringUtils.reverse(tempStr)
            val len = reverseData.length
            val devPos = len / 2
            val p1 = reverseData.substring(0, devPos)
            val p2 = reverseData.substring(devPos, len)
            return p2 + p1
        }
        return null

    }

    fun isDevPass(inputPass: String): Boolean {
        val cryptPass =
            simpleEncryptAndDecryptString(
                inputPass,
                Type_Encrypt
            )
        if (cryptPass == CommonK.devPass) {
            return true
        }
        return false
    }


    //文件大小展示
    fun fileSizeShowString(size: Long): String {
        var fileSize = BigDecimal(size)
        val param = BigDecimal(1024)
        var count = 0
        while (fileSize > param && count < 5) {
            fileSize = fileSize.divide(param)
            count++
        }
        val df = DecimalFormat("#.##")
        var result = df.format(fileSize) + ""
        when (count) {
            0 -> result += "B"
            1 -> result += "KB"
            2 -> result += "MB"
            3 -> result += "GB"
            4 -> result += "TB"
            5 -> result += "PB"
        }
        return result
    }

    var sortTypeDescending = 2 //2：risk降序
    var sortTypeDefault = 1 //1：risk升序
    fun getSortGroupList(
        sortType: Int,
        oldList: MutableList<MediaInfo>?
    ): MutableList<MediaInfo> {
        val targetList = mutableListOf<MediaInfo>()
        if (!oldList.isNullOrEmpty()) {
            for (group in oldList) {
                targetList.add(group)
            }
        }
        if (targetList.isNotEmpty()) {
            when (sortType) {
                sortTypeDescending -> {
                    targetList.sortByDescending { FileUtils.getFileLastModified(it.file) }
                }
                sortTypeDefault -> {
                    targetList.sortBy { FileUtils.getFileLastModified(it.file) }
                }
                else -> {
                    return targetList

                }
            }
        }
        return targetList
    }


    fun checkPasswordToUnLock(): Boolean {
        val keyguardManager =
            Utils.getApp().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.isKeyguardSecure
        return keyguardManager.isKeyguardSecure
    }

    //启用组件
    fun enableComponent(componentName: ComponentName?) {
        if (null == componentName) {
            return
        }
        val pm = Utils.getApp().packageManager
        val state: Int = pm.getComponentEnabledSetting(componentName)
        if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            //已经启用
            return
        }
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    //禁用组件
    fun disableComponent(componentName: ComponentName?) {
        if (null == componentName) {
            return
        }
        val pm = Utils.getApp().packageManager
        val state: Int = pm.getComponentEnabledSetting(componentName)
        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            //已经禁用
            return
        }
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun shareMediaFile(context: Context, mediaInfo: MediaInfo?) {
        if (mediaInfo == null) return
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                UriUtils.file2Uri(mediaInfo.file!!)
            )
            when {
                CommonK.Type_Media_Photo == mediaInfo.type -> {
                    shareIntent.type = "image/*"
                }
                CommonK.Type_Media_Video == mediaInfo.type -> {
                    shareIntent.type = "video/*"
                }
                else -> {
                    shareIntent.type = "*/*"
                }
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share to ："))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun openVideo(context: Context, mediaInfo: MediaInfo?) {
        if (mediaInfo == null) return
        if (!mediaInfo.path!!.endsWith(".mp4")) return
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(UriUtils.file2Uri(mediaInfo.file!!), "video/*")
        context.startActivity(intent)
    }

    //检测电源优化是否已经忽略
    fun checkIgnoreBatteryOptimization(): Boolean {
        val powerManager = Utils.getApp().getSystemService(POWER_SERVICE) as PowerManager?
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager!!.isIgnoringBatteryOptimizations(Utils.getApp().packageName)
        } else {
            true
        }

    }

    //检测免打扰
    fun checkDoNotDisturb(): Boolean {
        val notificationManager =
            Utils.getApp().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (RomUtils.isOppo() && DeviceUtils.getModel().contains("A57")) {
                return true
            }
            return notificationManager.isNotificationPolicyAccessGranted
        } else {
            return true
        }
    }


    //检测悬浮窗
    fun checkOverDraw(): Boolean {
        return XXPermissions.isGranted(Utils.getApp(), Permission.SYSTEM_ALERT_WINDOW)
    }

    fun checkOverDraw(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appOpsMgr = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsMgr.checkOpNoThrow(
                "android:system_alert_window", Process.myUid(), context
                    .packageName
            )
            Settings.canDrawOverlays(context) || mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED
        } else {
            Settings.canDrawOverlays(context)
        }
    }

    fun checkInstallFromGoogle(): Boolean {
        try {
            val installer = Utils.getApp().packageManager
                .getInstallerPackageName(Utils.getApp().packageName)
            LogUtils.i(installer)
            if (installer == "com.android.vending") {
                // 确保是google play 安装的
                LogUtils.i("google play")
                return true
            }
        } catch (e: java.lang.Exception) {
            LogUtils.e(e.toString())
        }
        return false
    }

    fun simpleLog(logStr: String) {
        LogUtils.i(logStr)
    }


    fun getMediaInfoListByType(selectType: String): MutableList<MediaInfo> {
        var fileList = mutableListOf<File>()
        val tempMediaInfoList = mutableListOf<MediaInfo>()

        when (selectType) {
            CommonK.Type_Media_Photo -> {
                fileList = FileUtils.listFilesInDir(App.savePhotoDir)
                LogUtils.i("PhotoListSize:" + fileList.size)

            }
            CommonK.Type_Media_Video -> {
                fileList = FileUtils.listFilesInDir(App.saveVideoDir)
                LogUtils.i("VideoListSize:" + fileList.size)
            }

            CommonK.Type_Media_Audio -> {
                fileList = FileUtils.listFilesInDir(App.saveAudioDir)
                LogUtils.i("AudioListSize:" + fileList.size)
            }
        }

        for (file in fileList) {
            val filePath = file.absolutePath
            if (filePath.endsWith(".jpg") || filePath.endsWith(".mp4") || filePath.endsWith(".m4a")) {
                val group = MediaInfo()
                group.file = file
                group.setOtherParams(file)
                tempMediaInfoList.add(group)
            }
        }
        return getSortGroupList(sortTypeDescending, tempMediaInfoList)
    }

    fun getMediaInfoListByPage(
        sourceList: MutableList<MediaInfo>?,
        pageSize: Int,
        page: Int?        //page从1开始 0或空都重置为1
    ): MutableList<MediaInfo> {
        val tempPage = if (0 == page || null == page) {
            1
        } else {
            page
        }
        if (sourceList.isNullOrEmpty()) return mutableListOf<MediaInfo>()
        val allDataSize = sourceList.size
        val targetList = mutableListOf<MediaInfo>()
        if (allDataSize - pageSize * (tempPage - 1) < pageSize) {
            for (index in pageSize * (tempPage - 1) until allDataSize) {
                targetList.add(sourceList[index])
            }
        } else {
            for (index in pageSize * (tempPage - 1) until pageSize * tempPage) {
                targetList.add(sourceList[index])
            }
        }
        return targetList
    }


    fun isPro(): Boolean {
        if (CommonK.proPkg == AppUtils.getAppPackageName()) {
            return true
        }
        if (1000 > LocalUtils.getProExp()) {
            return false
        }
        return true
    }

    fun saveMediaExternal(context: Context, mediaInfo: MediaInfo?) {
        if (null == mediaInfo) return
        Thread {
            val saveDir: String
            val desPath: String

            when {
                CommonK.Type_Media_Photo == mediaInfo.type -> {
                    saveDir =
                        PathUtils.getExternalPicturesPath() + "/" + AppUtils.getAppName() + "/"
                    desPath = saveDir + mediaInfo.title

                }
                CommonK.Type_Media_Video == mediaInfo.type -> {
                    saveDir =
                        PathUtils.getExternalMoviesPath() + "/" + AppUtils.getAppName() + "/"
                    desPath = saveDir + mediaInfo.title
                }
                else -> {
                    //录音文件外部存放
                    saveDir =
                        PathUtils.getExternalMusicPath() + "/" + AppUtils.getAppName() + "/"
                    desPath = saveDir + mediaInfo.title

                }
            }

            FileUtils.createOrExistsDir(saveDir)

            val success = FileUtils.copy(mediaInfo.path, desPath)
            if (success) {
                ToastUtils.showLong(context.getString(R.string.external_success) + desPath)
                FileUtils.notifySystemToScan(desPath)
            } else {
                ToastUtils.showShort(context.getString(R.string.external_failed))
            }
        }.start()

    }

    fun getCountry(): String {
        return Utils.getApp().resources.configuration.locale.country
    }

    //检测语言是否为中文
    fun checkZh(): Boolean {
        val locale = Utils.getApp().resources.configuration.locale
        val language = locale.language.toLowerCase(Locale.getDefault())
        return language.contains("zh")
    }

    //删除目录下非Love,filter:"_loved"
    fun deleteFilesInDirWithoutLove(dirPath: String?): Boolean {
        val dir = FileUtils.getFileByPath(dirPath) ?: return false
        // dir doesn't exist then return true
        if (!dir.exists()) return true
        // dir isn't a directory then return false
        if (!dir.isDirectory) return false
        val files = dir.listFiles()
        if (files != null && files.isNotEmpty()) {
            for (file in files) {
                if (file.isFile && !SPUtils.getInstance().getBoolean(file.name, false)) {
                    if (!file.delete()) return false
                }
            }
        }
        return true
    }

    //删除目录下全部
    fun deleteFilesInDir(dir: String): Boolean {
        return FileUtils.deleteAllInDir(dir)
    }

    fun ringerModeSilent() {
        try {
            if (checkDoNotDisturb()) {
                val audioManager: AudioManager? =
                    Utils.getApp().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                if (audioManager != null) {
                    if (AudioManager.RINGER_MODE_SILENT != audioManager.ringerMode) {
                        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                        audioManager.getStreamVolume(AudioManager.STREAM_RING)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ringerModeNormal() {
        try {
            if (checkDoNotDisturb()) {
                val audioManager: AudioManager? =
                    Utils.getApp().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                if (audioManager != null) {
                    if (AudioManager.RINGER_MODE_NORMAL != audioManager.ringerMode) {
                        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                        audioManager.getStreamVolume(AudioManager.STREAM_RING)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ringerModeVibrate() {
        try {
            if (checkDoNotDisturb()) {
                val audioManager: AudioManager? =
                    Utils.getApp().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                if (audioManager != null) {
                    if (AudioManager.RINGER_MODE_VIBRATE != audioManager.ringerMode) {
                        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                        audioManager.getStreamVolume(AudioManager.STREAM_RING)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //imageData Base64Encode数据
    fun base64ToBitmap(imageData: String?): Bitmap? {
        if (imageData.isNullOrBlank()) return null
        return ImageUtils.bytes2Bitmap(EncodeUtils.base64Decode(imageData))
    }

    fun getExifOrientation(filepath: String?): Int {
        if (filepath.isNullOrBlank()) return 0
        var degree = 0
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(filepath)
        } catch (ex: IOException) {
            LogUtils.i("cannot read exif$ex")
        }
        if (exif != null) {
            val orientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            if (orientation != -1) {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            }
        }
        return degree
    }

    fun getExifOrientation(inputStream: InputStream): Int {
        var degree = 0
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(inputStream)
        } catch (ex: IOException) {
            LogUtils.i("cannot read exif$ex")
        }
        if (exif != null) {
            val orientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            if (orientation != -1) {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            }
        }
        return degree
    }


    fun getBitmapFromUrl(url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        var bm: Bitmap? = null
        try {
            val iconUrl = URL(url)
            val conn: URLConnection = iconUrl.openConnection()
            val http: HttpURLConnection = conn as HttpURLConnection
            val length: Int = http.contentLength
            conn.connect()
            // 获得图像的字符流
            val `is`: InputStream = conn.getInputStream()
            val bis = BufferedInputStream(`is`, length)
            bm = BitmapFactory.decodeStream(bis)
            bis.close()
            `is`.close() // 关闭流
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bm
    }

    fun drawBitmapRect(
        oldBitmap: Bitmap,
        left: Float,
        top: Float,
        width: Float,
        height: Float
    ): Bitmap? {
        val tempBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(tempBitmap)
        //图像上画矩形
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE//不填充
        paint.strokeWidth = 5f  //线的宽度
        canvas.drawRect(left, top, left + width, top + height, paint)
        return tempBitmap
    }

    fun savePhoto(b: Bitmap?, name: String): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePhotoAndroidQ(b, name)
        } else {
            savePhotoCommon(b, name)

        }
    }

    private fun savePhotoCommon(b: Bitmap?, name: String): String? {
        if (null == b) return null
        val photoPath = PathUtils.getExternalPicturesPath() + "/" + name + ".jpg"
        try {
            val fileOutputStream = FileOutputStream(photoPath)
            val bos = BufferedOutputStream(fileOutputStream)
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return photoPath
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePhotoAndroidQ(b: Bitmap?, name: String): String? {
        if (null == b) return null
        var photoPath: String?=null
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DESCRIPTION, "This is an image")
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.TITLE, "$name.png")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/SuperCam")

        val external: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val resolver: ContentResolver = Utils.getApp().contentResolver
        val insertUri: Uri? = resolver.insert(external, values)
        var os: OutputStream? = null
        if (insertUri != null) {
            try {
                os = resolver.openOutputStream(insertUri)
                b.compress(Bitmap.CompressFormat.PNG, 90, os)
                photoPath = "DCIM/SuperCam/$name.png"
            } catch (e: IOException) {
                e.printStackTrace()
                photoPath = null
            } finally {
                try {
                    os?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return photoPath
    }


    fun shareFile(context: Context, mediaInfo: MediaInfo?) {
        if (mediaInfo == null) return
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                UriUtils.file2Uri(mediaInfo.file!!)
            )
            when {
                CommonK.Type_Media_Photo == mediaInfo.type -> {
                    shareIntent.type = "image/*"
                }
                CommonK.Type_Media_Video == mediaInfo.type -> {
                    shareIntent.type = "video/*"
                }
                else -> {
                    shareIntent.type = "*/*"
                }
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share to ："))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 读取asset目录下文件。
     * @return content
     */
    fun readFileFromAsset(mContext: Context, file: String?, code: Charset): String {
        var len = 0
        var buf: ByteArray? = null
        var result = ""
        try {
            val `in` = mContext.assets.open(file!!)
            len = `in`.available()
            buf = ByteArray(len)
            `in`.read(buf, 0, len)
            result = String(buf, code)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 读取asset目录下文件。
     */
    fun readFileFromAsset(context: Context, filename: String?): ByteArray? {
        try {
            val ins = context.assets.open(filename!!)
            val data = ByteArray(ins.available())
            ins.read(data)
            ins.close()
            return data
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    //恢复设置的状态
    fun recoverMode() {
        when (LocalUtils.getRecoverMode()) {
            CommonK.Mode_silent -> {
                ringerModeSilent()
            }
            CommonK.Mode_vibrate -> {
                ringerModeVibrate()
            }
            CommonK.Mode_sound -> {
                ringerModeNormal()
            }
            else -> {
                ringerModeSilent()
            }

        }
    }

    fun initShortcuts(context: Context) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
        val shortcutInfoList = mutableListOf<ShortcutInfo>()
        val mIntent =
            Intent(Intent.ACTION_MAIN, Uri.EMPTY, Utils.getApp().baseContext, OnePxAct::class.java)
        mIntent.flags = FLAG_ACTIVITY_CLEAR_TASK
        //构建动态快捷方式的详细信息
        val shortcutPhoto = ShortcutInfo.Builder(context, CommonK.shortCutPhoto)
            .setShortLabel(context.getString(R.string.shortcut_photo))
            .setLongLabel(context.getString(R.string.shortcut_photo))
            .setIcon(
                Icon.createWithResource(
                    context,
                    R.drawable.ic_round_sentiment_satisfied_alt_24
                )
            )
            .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutPhoto))
            .build()

        val shortcutVideo = ShortcutInfo.Builder(context, CommonK.shortCutVideo)
            .setShortLabel(context.getString(R.string.shortcut_video))
            .setLongLabel(context.getString(R.string.shortcut_video))
            .setIcon(
                Icon.createWithResource(
                    context,
                    R.drawable.ic_round_sentiment_satisfied_alt_24
                )
            )
            .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutVideo))
            .build()

        val shortcutAudio = ShortcutInfo.Builder(context, CommonK.shortCutAudio)
            .setShortLabel(context.getString(R.string.shortcut_audio))
            .setLongLabel(context.getString(R.string.shortcut_audio))
            .setIcon(
                Icon.createWithResource(
                    context,
                    R.drawable.ic_round_sentiment_satisfied_alt_24
                )
            )
            .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutAudio))
            .build()


        val shortcutEnter = ShortcutInfo.Builder(context, CommonK.shortCutEnter)
            .setShortLabel(context.getString(R.string.shortcut_enter))
            .setLongLabel(context.getString(R.string.shortcut_enter))
            .setIcon(
                Icon.createWithResource(
                    context,
                    R.drawable.ic_round_sentiment_satisfied_alt_24
                )
            )
            .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutEnter))
            .build()

        shortcutInfoList.add(shortcutEnter)
        shortcutInfoList.add(shortcutPhoto)
        shortcutInfoList.add(shortcutVideo)
        shortcutInfoList.add(shortcutAudio)
        shortcutManager.dynamicShortcuts = shortcutInfoList

    }


    fun updateDynamicShortcuts(context: Context, shortcutType: String, capturing: Boolean) {

        LogUtils.i(shortcutType, "capturing: $capturing")

        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
        val mIntent =
            Intent(Intent.ACTION_MAIN, Uri.EMPTY, Utils.getApp().baseContext, OnePxAct::class.java)
        mIntent.flags = FLAG_ACTIVITY_CLEAR_TASK
        when (shortcutType) {
            CommonK.shortCutPhoto -> {

                val shortcutPhoto = if (capturing) {
                    ShortcutInfo.Builder(context, CommonK.shortCutPhoto)
                        .setShortLabel(context.getString(R.string.shortcut_photo))
                        .setLongLabel(context.getString(R.string.shortcut_photo))
                        .setIcon(
                            Icon.createWithResource(
                                context,
                                R.drawable.ic_round_sentiment_very_satisfied_24
                            )
                        )
                        .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutPhoto))
                        .build()
                } else {
                    ShortcutInfo.Builder(context, CommonK.shortCutPhoto)
                        .setShortLabel(context.getString(R.string.shortcut_photo))
                        .setLongLabel(context.getString(R.string.shortcut_photo))
                        .setIcon(
                            Icon.createWithResource(
                                context,
                                R.drawable.ic_round_sentiment_satisfied_alt_24
                            )
                        )
                        .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutPhoto))
                        .build()
                }
                shortcutManager.updateShortcuts(listOf(shortcutPhoto))

            }

            CommonK.shortCutVideo -> {
                val shortcutVideo = if (capturing) {
                    ShortcutInfo.Builder(context, CommonK.shortCutVideo)
                        .setShortLabel(context.getString(R.string.shortcut_video))
                        .setLongLabel(context.getString(R.string.shortcut_video))
                        .setIcon(
                            Icon.createWithResource(
                                context,
                                R.drawable.ic_round_sentiment_very_satisfied_24
                            )
                        )
                        .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutVideo))
                        .build()
                } else {
                    ShortcutInfo.Builder(context, CommonK.shortCutVideo)
                        .setShortLabel(context.getString(R.string.shortcut_video))
                        .setLongLabel(context.getString(R.string.shortcut_video))
                        .setIcon(
                            Icon.createWithResource(
                                context,
                                R.drawable.ic_round_sentiment_satisfied_alt_24
                            )
                        )
                        .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutVideo))
                        .build()
                }
                shortcutManager.updateShortcuts(listOf(shortcutVideo))


            }

            CommonK.shortCutAudio -> {
                val shortcutAudio = if (capturing) {
                    ShortcutInfo.Builder(context, CommonK.shortCutAudio)
                        .setShortLabel(context.getString(R.string.shortcut_audio))
                        .setLongLabel(context.getString(R.string.shortcut_audio))
                        .setIcon(
                            Icon.createWithResource(
                                context,
                                R.drawable.ic_round_sentiment_very_satisfied_24
                            )
                        )
                        .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutAudio))
                        .build()
                } else {
                    ShortcutInfo.Builder(context, CommonK.shortCutAudio)
                        .setShortLabel(context.getString(R.string.shortcut_audio))
                        .setLongLabel(context.getString(R.string.shortcut_audio))
                        .setIcon(
                            Icon.createWithResource(
                                context,
                                R.drawable.ic_round_sentiment_satisfied_alt_24
                            )
                        )
                        .setIntent(mIntent.putExtra(CommonK.shortCutType, CommonK.shortCutAudio))
                        .build()
                }

                shortcutManager.updateShortcuts(listOf(shortcutAudio))

            }

        }
    }


    fun saveTempPhoto(b: Bitmap, name: String): String {
        val photoPath = PathUtils.getExternalAppFilesPath() + "/temp/" + name
        try {
            val fileOutputStream = FileOutputStream(photoPath)
            val bos = BufferedOutputStream(fileOutputStream)
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return photoPath
    }

    fun checkAllNecessaryPermissions(): Boolean {
        val permissionList = arrayListOf<String>()
        permissionList.add(Permission.CAMERA)
        permissionList.add(Permission.RECORD_AUDIO)
        permissionList.add(Permission.SYSTEM_ALERT_WINDOW)
        return XXPermissions.isGranted(
            Utils.getApp(),
            permissionList
        ) && checkIgnoreBatteryOptimization()

//        if (Build.VERSION.SDK_INT >= 30) {
//            permissionList.add(Permission.MANAGE_EXTERNAL_STORAGE)
//            XXPermissions.isGranted(
//                Utils.getApp(),
//                permissionList
//            ) && checkIgnoreBatteryOptimization()
//        } else {
//            XXPermissions.isGranted(
//                Utils.getApp(),
//                permissionList
//            ) && checkIgnoreBatteryOptimization()
//        }
    }

    fun checkScreenOn(): Boolean {
        return (Utils.getApp().getSystemService(POWER_SERVICE) as PowerManager).isInteractive
    }


    /**
     * 半角转换为全角
     *
     * @param input
     * @return
     */
    fun toDBC(input: String?): String {
        if (input == null) return ""
        val c = input.toCharArray()
        for (i in c.indices) {
            if (c[i].toInt() == 12288) { // 全角空格为12288，半角空格为32
                c[i] = 32.toChar()
                continue
            }
            if (c[i].toInt() in 65281..65374) // 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
                c[i] = (c[i] - 65248)
        }
        return String(c)
    }

    fun checkMsgLatestEnable(): Boolean {
        if (!App.checkMsgLatestPause && checkScreenOn()) return true
        return false
    }


    fun saveFileByUri(uri: Uri, name: String): File {
        FileUtils.createOrExistsDir(App.saveTempDir)
        val srcFile = UriUtils.uri2File(uri)
        val desFile = File(App.saveTempDir, name)
        FileUtils.copy(srcFile, desFile)
        return desFile

    }

    fun makeBase64Safe(base64String: String): String {
        if (LocalUtils.getLocalUser()?.uID.isNullOrBlank()) return base64String
        val mid = base64String.length / 2
        val newBase64String = base64String.reversed()
        val base64StringP1 = newBase64String.substring(0, mid)
        val base64StringP2 = newBase64String.substring(mid)
        return base64StringP1 + LocalUtils.getLocalUser()!!.uID + base64StringP2
    }

    fun recoverBase64Safe(base64String: String): String {
        if (LocalUtils.getLocalUser()?.uID.isNullOrBlank()) return base64String
        return base64String.replace(LocalUtils.getLocalUser()!!.uID!!, "").reversed()

    }

    //为了兼容android10+的图片裁剪问题
    fun makeAppDirPath(dirName: String): String {
        val tempDir =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + File.separator.toString() + dirName)
        tempDir.mkdirs();
        return tempDir.absolutePath
    }

    fun getMediaDuration(filePath: String?): Int {
        if (filePath.isNullOrBlank()) return 0
        var duration = 0
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(filePath)
            mediaPlayer.prepare()
            duration = mediaPlayer.duration
            if (0 != duration) {
//                val s = duration / 1000
//                //设置文件时长，单位 "分:秒" 格式
//                val total = (s / 60).toString() + ":" + s % 60
//                LogUtils.i(total)
                //记得释放资源
                mediaPlayer.release()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return duration
    }
}


