package com.dillon.supercam.utils.view.camera

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Camera
import android.hardware.Camera.*
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils

@Suppress("DEPRECATION")
class CameraView : SurfaceView, SurfaceHolder.Callback, AutoFocusCallback, ErrorCallback,
    View.OnClickListener {
    private val video720 = intArrayOf(1280, 720)
    private val video1080 = intArrayOf(1920, 1080)
    private val video2K = intArrayOf(2560, 1440)

    private var screenOrientation = Configuration.ORIENTATION_PORTRAIT
    private var mOpenBackCamera = true
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mRunInBackground = false
    private var isAttachedWindow = false
    private var mCamera: Camera? = null
    private var mParam: Parameters? = null
    private var previewBuffer: ByteArray? = null
    private var mCameraId = 0
    private var previewFormat = ImageFormat.NV21
    private var mContext: Context? = null
    private var callServiceListener: CallServiceListener? = null

    private var photoPath: String? = null
    private var videoPath: String? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, callServiceListener: CallServiceListener) : super(context) {
        init(context, callServiceListener)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init(context)
    }


    private fun init(context: Context) {
        this.mContext = context

        cameraState = CameraState.START
        cameraStateListener?.onCameraStateChange(cameraState)
        openCamera()
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenOrientation = Configuration.ORIENTATION_LANDSCAPE
        }
        mSurfaceHolder = holder
        mSurfaceHolder?.addCallback(this)
        mSurfaceTexture = SurfaceTexture(10)
        setOnClickListener(this)
        post {
            if (!isAttachedWindow) {
                mRunInBackground = true
                startPreview()
            }
        }
    }

    private fun init(context: Context, callServiceListener: CallServiceListener) {
        this.mContext = context
        this.callServiceListener = callServiceListener
        cameraState = CameraState.START
        cameraStateListener?.onCameraStateChange(cameraState)
        openCamera()
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenOrientation = Configuration.ORIENTATION_LANDSCAPE
        }
        mSurfaceHolder = holder
        mSurfaceHolder?.addCallback(this)
        mSurfaceTexture = SurfaceTexture(10)
        setOnClickListener(this)
        post {
            if (!isAttachedWindow) {
                mRunInBackground = true
                startPreview()
            }
        }
    }

    override fun onAttachedToWindow() {
        try {
            super.onAttachedToWindow()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        isAttachedWindow = true
    }

    private fun openCamera() {
        mCameraId = if (mOpenBackCamera) {
            findCamera(false)
        } else {
            findCamera(true)
        }
        if (mCameraId == -1) {
            mCameraId = 0
        }
        try {
            mCamera = open(mCameraId)
        } catch (ee: Exception) {
            LogUtils.i("camera open fail")
            mCamera = null
            cameraState = CameraState.ERROR
            cameraStateListener?.onCameraStateChange(cameraState)
            callServiceListener?.stopService()
        }

    }

    private fun findCamera(front: Boolean): Int {
        val cameraCount: Int
        try {
            val cameraInfo = CameraInfo()
            cameraCount = getNumberOfCameras()
            for (camIdx in 0 until cameraCount) {
                getCameraInfo(camIdx, cameraInfo)
                val facing = if (front) 1 else 0
                if (cameraInfo.facing == facing) {
                    return camIdx
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    fun setDefaultCamera(backCamera: Boolean): Boolean {
        if (mOpenBackCamera == backCamera) return false
        if (isRecording) {
            LogUtils.i("open camera fail -- isRecording")
            return false
        }
        mOpenBackCamera = backCamera
        if (mCamera != null) {
            closeCamera()
            openCamera()
            startPreview()
        }
        return true
    }

    fun closeCamera() {
        stopRecord()
        stopPreview()
        releaseCamera()
    }

    private fun releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera!!.setErrorCallback(null)
                mCamera!!.setPreviewCallback(null)
                mCamera!!.setPreviewCallbackWithBuffer(null)
                mCamera!!.stopPreview()
                mCamera!!.release()
                mCamera = null
            }
        } catch (ee: Exception) {
            callServiceListener?.stopService()
        }
    }

    private val isSupportCameraLight: Boolean
        get() {
            var mIsSupportCameraLight = false
            try {
                if (mCamera != null) {
                    val parameter = mCamera!!.parameters
                    val a: Any? = parameter.supportedFlashModes
                    mIsSupportCameraLight = a != null
                }
            } catch (e: Exception) {
                mIsSupportCameraLight = false
                e.printStackTrace()
            }
            return mIsSupportCameraLight
        }

    private val previewCallback = PreviewCallback { data, camera ->
        if (data == null) {
            releaseCamera()
            return@PreviewCallback
        }
        //you can code media here
        if (cameraState != CameraState.PREVIEW) {
            cameraState = CameraState.PREVIEW
            cameraStateListener?.onCameraStateChange(cameraState)
        }
        mCamera!!.addCallbackBuffer(previewBuffer)
    }

    //设置Camera各项参数
    private fun startPreview() {
        if (mCamera == null) return
        try {
            mCamera?.setErrorCallback(this)
            mCamera?.enableShutterSound(false)
            mParam = mCamera!!.parameters
            mParam?.previewFormat = previewFormat
            mParam?.setRotation(0)
            val previewSize = CameraUtil.getCameraSize(
                mParam?.supportedPreviewSizes, 2500,
                mCamera!!.Size(video2K[0], video2K[1])
            )
            LogUtils.i("previewSize", GsonUtils.toJson(previewSize))
            mParam?.setPreviewSize(previewSize.width, previewSize.height)
            val yuvBufferSize =
                previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(previewFormat) / 8
            previewBuffer = ByteArray(yuvBufferSize)
            val pictureSize = CameraUtil.getCameraSize(
                mParam?.supportedPictureSizes, 2500,
                mCamera!!.Size(video2K[0], video2K[1])
            )
            LogUtils.i("pictureSize", GsonUtils.toJson(pictureSize))

            mParam?.setPictureSize(pictureSize.width, pictureSize.height)
            if (CameraUtil.isSupportedFormats(
                    mParam?.supportedPictureFormats,
                    ImageFormat.JPEG
                )
            ) {
                mParam?.pictureFormat = ImageFormat.JPEG
                mParam?.jpegQuality = 100
            }
            if (CameraUtil.isSupportedFocusMode(
                    mParam?.supportedFocusModes,
                    Parameters.FOCUS_MODE_AUTO
                )
            ) {
                mParam?.focusMode = Parameters.FOCUS_MODE_AUTO
            }
            if (screenOrientation != Configuration.ORIENTATION_LANDSCAPE) {
                mParam?.set("orientation", "portrait")
                mCamera?.setDisplayOrientation(90)
            } else {
                mParam?.set("orientation", "landscape")
                mCamera?.setDisplayOrientation(0)
            }
            if (mRunInBackground) {
                mCamera?.setPreviewTexture(mSurfaceTexture)
                mCamera?.addCallbackBuffer(previewBuffer)
                mCamera?.setPreviewCallbackWithBuffer(previewCallback);//设置摄像头预览帧回调
            } else {
                mCamera?.setPreviewDisplay(mSurfaceHolder)
                mCamera?.setPreviewCallback(previewCallback);//设置摄像头预览帧回调
            }
            mCamera?.parameters = mParam
            mCamera?.startPreview()
            if (cameraState != CameraState.START) {
                cameraState = CameraState.START
                cameraStateListener?.onCameraStateChange(cameraState)
            }
        } catch (e: Exception) {
            releaseCamera()
            return
        }
        try {
            val mode = mCamera!!.parameters.focusMode
            if ("auto" == mode || "macro" == mode) {
                mCamera!!.autoFocus(null)
            }
        } catch (e: Exception) {
        }
    }

    private fun stopPreview() {
        if (mCamera == null) return
        try {
            if (mRunInBackground) {
                mCamera!!.setPreviewCallbackWithBuffer(null)
                mCamera!!.stopPreview()
            } else {
                mCamera!!.setPreviewCallback(null)
                mCamera!!.stopPreview()
            }
            if (cameraState != CameraState.STOP) {
                cameraState = CameraState.STOP
                cameraStateListener?.onCameraStateChange(cameraState)
            }
        } catch (ee: Exception) {
            callServiceListener?.stopService()
        }
    }

    override fun onClick(v: View) {
        mCamera?.autoFocus(null)
    }

    override fun onError(i: Int, camera: Camera) {
        callServiceListener?.stopService()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        stopPreview()
        startPreview()
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopPreview()
        if (mRunInBackground) startPreview()
    }

    private var cameraState: CameraState? = null
    private var cameraStateListener: CameraStateListener? = null


    enum class CameraState {
        START, PREVIEW, STOP, ERROR
    }

    fun setOnCameraStateListener(listener: CameraStateListener?) {
        cameraStateListener = listener
    }

    interface CameraStateListener {
        fun onCameraStateChange(paramCameraState: CameraState?)
    }

    /**
     * ___________________________________前/后台运行______________________________________
     */
    fun setRunBack(b: Boolean) {
        if (mCamera == null) return
        if (b == mRunInBackground) return
        if (!b && !isAttachedWindow) {
            LogUtils.i("can not visit -- unAttachedWindow ")
            return
        }
        mRunInBackground = b
        visibility = if (b) View.GONE else View.VISIBLE
    }

    /**
     * ___________________________________开关闪光灯______________________________________
     */
    fun switchLight(open: Boolean) {
        if (mCamera == null) return
        try {
            val parameter = mCamera!!.parameters
            if (open) {
                if (parameter.flashMode == "off") {
                    parameter.flashMode = "torch"
                    mCamera!!.parameters = parameter
                } else {
                    parameter.flashMode = "off"
                    mCamera!!.parameters = parameter
                }
            } else {
                if (parameter.flashMode != null &&
                    parameter.flashMode == "torch"
                ) {
                    parameter.flashMode = "off"
                    mCamera!!.parameters = parameter
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ___________________________________以下为拍照模块______________________________________
     */
    fun capture() {
        if (mCamera == null) {
            callServiceListener?.stopService()
            return
        }
        mCamera?.autoFocus(this)
    }

    override fun onAutoFocus(
        success: Boolean,
        camera: Camera
    ) {
        if (success) {
            try {
                mCamera?.takePicture(
                    null,
                    null,
                    { data: ByteArray, _: Camera? ->
                        Thread {
                            var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                            val matrix = Matrix()
                            if (mOpenBackCamera) {
                                matrix.setRotate(90f)
                            } else {
                                matrix.setRotate(270f)
                                matrix.postScale(-1f, 1f)
                            }
                            bitmap = Bitmap.createBitmap(
                                bitmap,
                                0,
                                0,
                                bitmap.width,
                                bitmap.height,
                                matrix,
                                true
                            )
                            photoPath = CameraUtil.savePhoto(bitmap)
                            LogUtils.i(photoPath)
                            startPreview()
                            callServiceListener?.stopService()
                        }.start()
                    }
                )
            } catch (e: Exception) {
                if (isRecording) {
                    LogUtils.i("can not photo -- isRecording")
                }
                FileUtils.delete(photoPath)
                callServiceListener?.stopService()
            }
        } else {
            LogUtils.i("can not photo -- onAutoFocus fail")
            FileUtils.delete(photoPath)
            callServiceListener?.stopService()
        }

    }

    /**
     * ___________________________________以下为视频录制模块______________________________________
     */
    private var mediaRecorder = MediaRecorder()
    private var isRecording = false

    fun startRecord(): Boolean {
        return startRecord(-1, null)
    }

    fun startRecord(
        maxDurationMs: Int,
        onInfoListener: MediaRecorder.OnInfoListener?
    ): Boolean { //videoType 1为前，2为后
        if (mCamera == null) {
            callServiceListener?.stopService()
            return false
        }
        isRecording = try {
            mCamera!!.unlock()
            mediaRecorder.reset()
            mediaRecorder.setCamera(mCamera)
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            val videoSize = CameraUtil.getCameraSize(
                mParam!!.supportedVideoSizes, 2500,
                mCamera!!.Size(video2K[0], video2K[1])
            )
            LogUtils.i("videoSize", GsonUtils.toJson(videoSize))
            mediaRecorder.setVideoSize(videoSize.width, videoSize.height)
            mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024)
            //        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());//设置录制预览surface

            if (mOpenBackCamera) {
                mediaRecorder.setOrientationHint(90)
            } else {
                if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) mediaRecorder.setOrientationHint(
                    90
                ) else mediaRecorder.setOrientationHint(270)
            }
            if (maxDurationMs != -1) {
                mediaRecorder.setMaxDuration(maxDurationMs)
                mediaRecorder.setOnInfoListener(onInfoListener)
            }
            videoPath = CameraUtil.getVideoOutputPath()
            mediaRecorder.setOutputFile(videoPath)

            mediaRecorder.prepare()
            mediaRecorder.start()
            true
        } catch (e: Exception) {
            FileUtils.delete(videoPath)
            callServiceListener?.stopService()
            e.printStackTrace()
            return false
        }
        return true
    }

    fun stopRecord() {
        try {
            if (!isRecording) return
            mediaRecorder.setPreviewDisplay(null)
            mediaRecorder.stop()
            isRecording = false
            LogUtils.i(videoPath)
            callServiceListener?.stopService()
        } catch (e: Exception) {
            FileUtils.delete(videoPath)
            callServiceListener?.stopService()
            e.printStackTrace()
        }
    }

    /**_________________________________________________________________________________________ */


    interface CallServiceListener {
        fun stopService()
    }
}