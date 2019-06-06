package com.peihou.warmer.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.custom.view.MyDecoration
import com.peihou.warmer.custom.view.MyHeadRefreshView
import com.peihou.warmer.custom.view.MyLoadMoreView
import com.peihou.warmer.pojo.Device
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_device.view.*
import java.util.ArrayList

class MainActivity :BaseActivity(){
    var adapter:DeviceAdapter?=null
    private var devices=ArrayList<Device>()
    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
       return R.layout.activity_main
    }

    private fun getDimen(dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }
    override fun initView() {
        var decoration= MyDecoration()
        decoration.setDeiverHeight(getDimen(R.dimen.dp_20))
                .setColor(Color.parseColor("#f5f5f5"))
                .setMargin(0f)
        rv_device.layoutManager=LinearLayoutManager(this)
        rv_device.addItemDecoration(decoration)
        adapter=DeviceAdapter(this,devices)
        rv_device.adapter=adapter
        swipeRefresh.setHeaderView(MyHeadRefreshView(this))
        swipeRefresh.setFooterView(MyLoadMoreView(this))
        swipeRefresh.setRefreshListener(object : BaseRefreshListener {
            override fun loadMore() {

            }

            override fun refresh() {

            }

        })

    }

    override fun onBackPressed() {
        application.removeAllActivity()
    }
    @OnClick(R.id.img_header,R.id.img_add)
    fun onClick(view:View){
        when(view.id){
            R.id.img_header->startActivity(PersonSetActivity::class.java)
            R.id.img_add->{
                var intent=Intent(this,AddDeviceActivity::class.java)
                startActivityForResult(intent,100)
            }
        }
    }
    inner class DeviceAdapter(private var context: Context,private var devices:ArrayList<Device>):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType==0){
                var view:View=View.inflate(context,R.layout.item_device_weather,null)
                return WeatherHolder(view)
            }else{
                var itemView:View=View.inflate(context,R.layout.item_device,null)
                return DeviceHolder(itemView)
            }
        }

        override fun getItemCount(): Int {
            return 10
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
           Log.i("ViewHolder","-->${position}")
            if (position>0){
                var deviceHolder:DeviceHolder= viewHolder as DeviceHolder
                deviceHolder.bind()
            }
        }


        override fun getItemViewType(position: Int): Int {
            if (position==0){
                return 0
            }else
                return 1
        }
    }
    inner class WeatherHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }
    inner class DeviceHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        fun bind(){
            itemView.layout_item.setOnClickListener {
                startActivity(DeviceActivity::class.java)
            }
        }
    }
}