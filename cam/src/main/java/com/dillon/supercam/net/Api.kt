package com.dillon.supercam.net

/**
 * Created by dillon on 2017/6/29.
 */

object Api {

    private const val HOST =
        "https://xxxx"

    var appSuggestSend = "$HOST/appSuggestSend"
    var configGet = "$HOST/configGet"
    var userRegister = "$HOST/userRegister"
    var userLogin = "$HOST/userLogin"
    var userGet = "$HOST/userGet"
    var userUpdate = "$HOST/userUpdate"

    var uploadLocation = "$HOST/locationUpload"
    var travelsGet = "$HOST/travelsGet"

    var monitorListGet = "$HOST/monitorListGet"
    var sharerListGet = "$HOST/sharerListGet"
    var sharerDel = "$HOST/sharerDel"
    var monitorDel = "$HOST/monitorDel"


    var msgPush = "$HOST/msgPush"
    var msgUpdate = "$HOST/msgUpdate"
    var msgReceive = "$HOST/msgReceive"

    var actCodeCheck = "$HOST/actCodeCheck"


}
