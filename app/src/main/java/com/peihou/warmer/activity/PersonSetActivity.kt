package com.peihou.warmer.activity


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.custom.dialog.ChangeDialog
import com.peihou.warmer.custom.view.MyDecoration
import kotlinx.android.synthetic.main.activity_person_set.*
import kotlinx.android.synthetic.main.item_person.view.*

class PersonSetActivity : BaseActivity() {

    private var list= mutableListOf<String>()
    var adapter:MyAdapter?=null
    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
        return R.layout.activity_person_set
    }
    private fun getDimen(dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }

    override fun initView() {
//        list.add("常见问题")
        list.add("检查最新版本")
        list.add("清除缓存")
        list.add("更改密码")

        var deration=MyDecoration()
        deration.setDeiverHeight(getDimen(R.dimen.dp_1))
                .setMargin(getDimen(R.dimen.dp_30))
                .setColor(Color.parseColor("#d8dbd5"))
        rl_view.addItemDecoration(deration)
        rl_view.layoutManager=LinearLayoutManager(this)
        adapter=MyAdapter(this,list)
        rl_view.adapter=adapter
    }
    @OnClick(R.id.img_back,R.id.btn_exit)
    fun onClick(view: View){
        when(view.id){
            R.id.img_back->finish()
            R.id.btn_exit->exit()
        }
    }

    var changeDialog:ChangeDialog?=null
    fun changeDialog(code:Int){
        if (changeDialog!=null && changeDialog?.isShowing==true){
            return
        }
        changeDialog= ChangeDialog(this)
        changeDialog?.setCanceledOnTouchOutside(false)
        if (code==1){
            changeDialog?.mode=1
            changeDialog?.title="清除缓存"
            changeDialog?.tips="确定清除缓存?"
        }
        changeDialog?.show()
        changeDialog?.setOnNegativeClickListener {
            changeDialog?.dismiss()
        }
        changeDialog?.setOnPositiveClickListener {
            changeDialog?.dismiss()
        }
        backgroundAlpha(0.6f)
        changeDialog?.setOnDismissListener {
            backgroundAlpha(1.0f)
        }
    }
    /**
     * 退出
     */
    fun exit(){
        var intent=Intent(this,LoginActivity::class.java)
        intent.putExtra("exit",1)
        startActivity(intent)
        application.removeAllActivity()
    }
    inner class MyAdapter(private var context:Context,private var list: List<String>):RecyclerView.Adapter<ViewHolder>(){
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            var view:View=View.inflate(context,R.layout.item_person,null)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: ViewHolder, poition: Int) {
            var content=list[poition]
            holder.tv_name?.text=content

            holder.rl_layout?.setOnClickListener {
                if (poition==1){
                  changeDialog(1)
                }
                else if (poition==2){
                    var intent=Intent(context,RegisterActivity::class.java)
                    intent.putExtra("code",3)
                    intent.putExtra("phone","15769310630")
                    startActivityForResult(intent,100)
                }
            }
        }
    }
    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var tv_name:TextView?=null
        var rl_layout:RelativeLayout?=null
        init {
            tv_name=itemView.tv_name
            rl_layout=itemView.rl_layout
        }
    }

}
