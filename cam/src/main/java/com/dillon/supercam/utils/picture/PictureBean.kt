package com.dillon.supercam.utils.picture


import java.io.Serializable

class PictureBean : Serializable {
    var filePath: String? = null     //图片地址

    var cropFilePath: String? = null     //裁剪图片地址

    var cropFileFrom = 0 //0 //默认标记  1 //拍照标记 2 //相册标记

}