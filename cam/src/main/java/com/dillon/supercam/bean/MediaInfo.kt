package com.dillon.supercam.bean

import com.blankj.utilcode.util.TimeUtils
import com.dillon.supercam.key.CommonK
import com.dillon.supercam.utils.CoreUtils
import java.io.File

open class MediaInfo : BInfo() {
    var file: File? = null
    var title: String? = null
    var path: String? = null
    var type: String? = null//mediaType
    var sizeStr: String? = null
    var dateStr: String? = null
    var details: String? = null
    var cameraStr: String? = null

    fun setOtherParams(file: File?) {
        if (file == null) return
        val fileName: String = file.name
        cameraStr = when {
            fileName.contains(".f") -> {
                CommonK.Front
            }
            fileName.contains(".b") -> {
                CommonK.Back
            }
            else -> {
                CommonK.Back
            }
        }

        title = when {

            fileName.contains("_loved") && fileName.contains(".f") -> {
                fileName.replace("_loved", "")
                fileName.replace(".f", "")
            }
            fileName.contains("_loved") && fileName.contains(".b") -> {
                fileName.replace("_loved", "")
                fileName.replace(".b", "")
            }
            fileName.contains(".f") -> {
                fileName.replace(".f", "")
            }
            fileName.contains(".b") -> {
                fileName.replace(".b", "")
            }
            else -> {
                fileName//正常走不到这一步
            }
        }


        type = when {
            file.absolutePath.endsWith(".mp4") -> {
                CommonK.Type_Media_Video
            }
            file.absolutePath.endsWith(".jpg") -> {
                CommonK.Type_Media_Photo
            }
            file.absolutePath.endsWith(".m4a") -> {
                CommonK.Type_Media_Audio
            }
            else -> {
                CommonK.Type_Media_Photo
            }
        }

        path = file.absolutePath
        sizeStr = CoreUtils.fileSizeShowString(file.length())
        dateStr = TimeUtils.millis2String(file.lastModified())
        details = "Name:  $title\n\nSize:  $sizeStr\n\nDate:  $dateStr\n\nPath:  $path"

    }


}