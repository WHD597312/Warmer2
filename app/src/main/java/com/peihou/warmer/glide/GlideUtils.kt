package com.peihou.warmer.glide

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.peihou.warmer.R
import retrofit2.http.Url
import java.io.File

object GlideUtils {

        /**
         * 加载本地资源圆形图片
         */
        fun loadCircleImg(context: Context,resId:Int,img:ImageView){
            var options= RequestOptions()
            options.error(R.mipmap.ic_launcher)
                    .placeholder(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .optionalTransform(CircleCrop())
            Glide.with(context).load(resId).apply(options).into(img)
        }

        /**
         * 加载文件圆形图片
         */
        fun loadCircleImg(context: Context,file: File,img:ImageView){
            var options= RequestOptions()
            options.error(R.mipmap.ic_launcher)
                    .placeholder(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .optionalTransform(CircleCrop())
            Glide.with(context).load(file).apply(options).into(img)
        }

        /**
         * 加载网络圆形图片
         */
        fun loadCircleImg(context: Context,url: String,img:ImageView){
            var options= RequestOptions()
            options.error(R.mipmap.ic_launcher)
                    .placeholder(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .optionalTransform(CircleCrop())
            Glide.with(context).load(url).apply(options).into(img)
        }

        /**
         * 加载文件图片
         */
        fun loadImg(context: Context,file:File,img: ImageView){
            var options=RequestOptions()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
            Glide.with(context).load(file).apply(options).into(img)
        }

        /**
         * 加载本地资源图片
         */
        fun loadImg(context: Context,resId: Int,img: ImageView){
            var options=RequestOptions()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
            Glide.with(context).load(resId).apply(options).into(img)
        }

        /**
         * 加载网络资源图片
         */
        fun loadImg(context: Context,url: String,img: ImageView){
            var options=RequestOptions()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
            Glide.with(context).load(url).apply(options).into(img)
        }

}