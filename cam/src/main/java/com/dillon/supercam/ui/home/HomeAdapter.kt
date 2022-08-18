package com.dillon.supercam.ui.home

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.dillon.supercam.R
import com.dillon.supercam.bean.MediaInfo
import com.dillon.supercam.key.CommonK

import com.dillon.supercam.utils.glide.GlideRoundedCornersTransform
import com.dillon.supercam.utils.view.shimmer.Shimmer
import com.dillon.supercam.utils.view.shimmer.ShimmerTextView

class HomeAdapter(context: Context, itemManagerListener: ItemManagerListener) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    private var mContext = context
    private var mItemManagerListener = itemManagerListener
    private var mDataList: MutableList<MediaInfo>? = null
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return if (mDataList == null) 0 else mDataList!!.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = inflater.inflate(R.layout.layout_recycler_item, parent, false)
        return ViewHolder(itemView)

    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val mediaInfo = mDataList!![position]
        holder.setData(mediaInfo)
        holder.ivIcon.setOnClickListener { mItemManagerListener.show(mediaInfo, position) }
        holder.ivSave.setOnClickListener { mItemManagerListener.save(mediaInfo, position) }
        holder.ivShare.setOnClickListener { mItemManagerListener.share(mediaInfo, position) }
        holder.ivDelete.setOnClickListener { mItemManagerListener.delete(mediaInfo, position) }
        holder.ivLoved.setOnClickListener { mItemManagerListener.love(mediaInfo, position) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        var ivPlay: ImageView = itemView.findViewById(R.id.iv_play)
        var tvTitle: ShimmerTextView = itemView.findViewById(R.id.tv_title)
        var mSize: TextView = itemView.findViewById(R.id.tv_size)
        var ivLoved: ImageView = itemView.findViewById(R.id.iv_loved)
        var ivShare: ImageView = itemView.findViewById(R.id.iv_share)
        var ivSave: ImageView = itemView.findViewById(R.id.iv_save)
        var ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)


        fun setData(mediaInfo: MediaInfo) {
            tvTitle.text = mediaInfo.title
            mSize.text = mediaInfo.sizeStr
            val loved = SPUtils.getInstance().getBoolean(mediaInfo.file!!.name, false)
            if (loved) {
                Glide.with(Utils.getApp().baseContext).load(R.drawable.ic_round_favorite_24)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivLoved)
            } else {
                Glide.with(Utils.getApp().baseContext).load(R.drawable.ic_round_favorite_border_24)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivLoved)
            }
            val shimmer = Shimmer()
            shimmer.setDuration(7000)
            shimmer.start(tvTitle)


            when {
                CommonK.Type_Media_Photo == mediaInfo.type -> {
                    ivPlay.visibility = View.INVISIBLE
                    val options = RequestOptions()
                        .error(R.drawable.ic_round_error_24) //加载失败图片
                        .priority(Priority.HIGH) //优先级
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .transform(
                            GlideRoundedCornersTransform(
                                Utils.getApp().baseContext.resources.getDimension(
                                    R.dimen.dp_8
                                ), GlideRoundedCornersTransform.CornerType.ALL
                            )
                        )//圆角
                    Glide.with(Utils.getApp().baseContext).load(mediaInfo.file).apply(options)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivIcon)
                }
                CommonK.Type_Media_Video == mediaInfo.type -> {
                    ivPlay.visibility = View.VISIBLE
                    val options = RequestOptions()
                        .error(R.drawable.ic_round_error_24) //加载失败图片
                        .priority(Priority.HIGH) //优先级
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .transform(
                            GlideRoundedCornersTransform(
                                Utils.getApp().baseContext.resources.getDimension(
                                    R.dimen.dp_8
                                ), GlideRoundedCornersTransform.CornerType.ALL
                            )
                        )//圆角
                    Glide.with(Utils.getApp().baseContext).load(mediaInfo.file).apply(options)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivIcon)
                }
                CommonK.Type_Media_Audio == mediaInfo.type -> {
                    ivPlay.visibility = View.VISIBLE
                    val options = RequestOptions()
                        .error(R.drawable.ic_round_error_24) //加载失败图片
                        .priority(Priority.HIGH) //优先级
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .transform(
                            GlideRoundedCornersTransform(
                                Utils.getApp().baseContext.resources.getDimension(
                                    R.dimen.dp_8
                                ), GlideRoundedCornersTransform.CornerType.ALL
                            )
                        )//圆角
                    Glide.with(Utils.getApp().baseContext).load(R.drawable.ic_round_mic_24)
                        .apply(options)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivIcon)
                }
                else -> {
                    ivPlay.visibility = View.INVISIBLE

                }
            }
        }

    }

    fun notifyDataSetChanged(dataList: MutableList<MediaInfo>?) {
        mDataList = dataList
        super.notifyDataSetChanged()
    }


    interface ItemManagerListener {
        fun show(mediaInfo: MediaInfo, position: Int)
        fun love(mediaInfo: MediaInfo, position: Int)
        fun share(mediaInfo: MediaInfo, position: Int)
        fun save(mediaInfo: MediaInfo, position: Int)
        fun delete(mediaInfo: MediaInfo, position: Int)
    }
}