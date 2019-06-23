package com.peihou.warmer.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import kotlinx.android.synthetic.main.activity_advance_set.*
import kotlinx.android.synthetic.main.item_advance.view.*


class AdvanceSetActivity : BaseActivity() {
    var list= mutableListOf<String>()
    var adapter:SetAdapter?=null
    var deviceMac:String?=null
    override fun initParms(parms: Bundle?) {
        deviceMac=parms?.getString("deviceMac")
    }

    override fun bindLayout(): Int {
       return R.layout.activity_advance_set
    }

    override fun initView() {
        list.add("回差温度设置")
        list.add(("系统类型"))
        list.add("温度校正")
        list.add("内置传感器的设置范围")
        list.add("外置置传感器的设置范围")
        list.add("外置传感器限温设置点")
        list.add("输出延时时间")
        list.add("低温保护")
        list.add("掉电记忆")
        list.add("输出测试")
        rl_set.layoutManager=LinearLayoutManager(this)
        adapter=SetAdapter(this,list)
        rl_set.adapter=adapter
    }

    override fun onBackPressed() {
        var intent=Intent()
        intent.putExtra("advanceArray",advanceArray)
        setResult(100,intent)
        super.onBackPressed()
    }
    @OnClick(R.id.img_back)
    fun onClick(view: View){
        when(view.id){
            R.id.img_back->{
                var intent=Intent()
                intent.putExtra("advanceArray",advanceArray)
                setResult(100,intent)
                finish()
            }
        }
    }
    inner class SetAdapter(private var context: Context,private var list: List<String>):RecyclerView.Adapter<ViewHolder>(){
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            var view:View=View.inflate(context,R.layout.item_advance,null)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tv_set?.setText(list[position])
            when(position){
                0->holder.img_set?.setImageResource(R.mipmap.set_01)
                1->holder.img_set?.setImageResource(R.mipmap.set_02)
                2->holder.img_set?.setImageResource(R.mipmap.set_03)
                3->holder.img_set?.setImageResource(R.mipmap.set_04)
                4->holder.img_set?.setImageResource(R.mipmap.set_04)
                5->holder.img_set?.setImageResource(R.mipmap.set_04)
                6->holder.img_set?.setImageResource(R.mipmap.set_05)
                7->holder.img_set?.setImageResource(R.mipmap.set_06)
                8->holder.img_set?.setImageResource(R.mipmap.set_07)
                9->holder.img_set?.setImageResource(R.mipmap.set_08)
            }
            holder.itemView.setOnClickListener {
                when(position){
                    0,2,3,4,5,6->{
                        var intent = Intent(this@AdvanceSetActivity, Set01Activity::class.java)
                        intent.putExtra("position",position)
                        intent.putExtra("deviceMac",deviceMac)
                        startActivityForResult(intent,100)
                    }
                    1->{
                        var intent=Intent(this@AdvanceSetActivity,Set02Activity::class.java)
                        intent.putExtra("deviceMac",deviceMac)
                        startActivityForResult(intent,100)            }
                    7->{
                        var intent=Intent(this@AdvanceSetActivity,Set03Activity::class.java)
                        intent.putExtra("deviceMac",deviceMac)
                        startActivityForResult(intent,100)
                    }
                    8,9->{
                        var intent = Intent(this@AdvanceSetActivity, Set04Activity::class.java)
                        intent.putExtra("deviceMac",deviceMac)
                        intent.putExtra("position",position)
                        startActivityForResult(intent,100)
                    }
                }
            }
        }
    }

    private var advanceArray: IntArray = IntArray(15)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==100){
            advanceArray=data.getIntArrayExtra("advanceArray")
        }
    }
    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var img_set:ImageView?=null
        var tv_set:TextView?=null
        init {
            img_set=itemView.img_set
            tv_set=itemView.tv_set
        }
    }
}
