package com.dillon.supercam.utils.apk.down

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.LongSparseArray
import com.dillon.supercam.utils.apk.constant.Constant
import com.dillon.supercam.utils.apk.util.DownUtil
import java.io.File

/**
 *  CreateDate: 2018/6/11
 *  Desc:  系统下载管理
 */
class DownLoadApkManager {


    private var mDownLoadManager: DownloadManager? = null
    var mApkPaths: LongSparseArray<String> = LongSparseArray()
    var downLoadID: Long = Constant.DOWN_LOAD_INIT_ID

    fun getDownLoadManager(context: Context): DownloadManager {
        if (mDownLoadManager == null) {
            synchronized(DownLoadApkManager::class) {
                if (mDownLoadManager == null) {
                    mDownLoadManager =
                        context.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE)
                                as DownloadManager
                }
            }
        }
        return mDownLoadManager!!
    }

    /**
     *  调用系统 下载方法 下载 apk
     *  @param context 上下文
     *  @param url  下载链接
     *  @param apkName 应用名
     *  @param apkDesc  apk 描述信息
     *  @return Long 下载状态
     */
    fun startDownLoad(
        context: Context, url: String, apkName: String, apkDesc: String): Long {
        val downLoadManagerIsEnabled = DownUtil().downLoadManagerIsEnabled(context)
        if (!downLoadManagerIsEnabled) return Constant.DOWN_LOAD_MANAGER_UNABLE_USE
        if (downLoadID != Constant.DOWN_LOAD_INIT_ID) return Constant.DOWN_LOAD_INIT_ID
        val clearApk = isClearApk(context, apkName)
        if (!clearApk) return Constant.DOWN_LOAD_APK_HAS_EXIST
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        // 检测当前SD 卡状态是否读写及文件路径是否存在可用，不可用时中断下载操作
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED || fileDir == null)
            return Constant.EXTERNAL_STORAGE_NOT_EXIST
        val req = DownloadManager.Request(Uri.parse(url))
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        //点击正在下载的Notification进入下载详情界面，如果设为true则可以看到下载任务的进度，如果设为false，则看不到我们下载的任务
        req.setVisibleInDownloadsUi(true)
        //设置文件的保存的位置/自定义文件路径
        val file = File(fileDir, apkName)
        req.setDestinationUri(Uri.fromFile(file))
        // 此方法表示在下载过程中通知栏会一直显示该下载，在下载完成后仍然会显示，
        // 直到用户点击该通知或者消除该通知。还有其他参数可供选择
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // 通过setAllowedNetworkTypes方法可以设置允许在何种网络下下载，
        // 也可以使用setAllowedOverRoaming方法，它更加灵活移动网络是否允许下载
        req.setAllowedOverRoaming(true)
        // 设置一些基本显示信息
        req.setTitle(apkName)
        req.setDescription(apkDesc)
        req.setMimeType("application/vnd.android.package-archive")
        //加入下载队列
        downLoadID = getDownLoadManager(context).enqueue(req)
        mApkPaths.put(downLoadID, file.absolutePath)
        return downLoadID

    }

    /**
     * 如果之前下载过已经存在apk,删除之前的apk
     *
     * @param apkName apk名字
     * @return true 继续执行下载任务， false 中断下载任务
     */
    private fun isClearApk(context: Context, apkName: String): Boolean {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), apkName)
        if (file.exists() ) {
            file.delete()
        }
        return true
    }

    companion object {
        var mInstance: DownLoadApkManager? = null

        fun getInstance(): DownLoadApkManager {
            if (mInstance == null) {
                synchronized(DownLoadApkManager::class) {
                    if (mInstance == null) {
                        mInstance = DownLoadApkManager()
                    }
                }
            }
            return mInstance!!
        }
    }
}