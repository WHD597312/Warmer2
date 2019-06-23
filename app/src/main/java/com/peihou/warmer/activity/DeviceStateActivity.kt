package com.peihou.warmer.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.chart.LineChartManager
import kotlinx.android.synthetic.main.activity_device_state.*
import me.jessyan.autosize.internal.CustomAdapt
import java.util.*

class DeviceStateActivity : BaseActivity(),CustomAdapt {
    override fun isBaseOnWidth(): Boolean {
        return false
    }

    override fun getSizeInDp(): Float {
        return 667f
    }

    private var xValues= mutableListOf<Int>()
    private var yValues= mutableListOf<Int>()
    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
        return R.layout.activity_device_state
    }
    var random= Random()
    override fun initView() {
        for (i in 0 .. 23){
            xValues.add(i)
        }
        for(i in 0..23){
            yValues.add(random.nextInt(60))
        }
        val lineChartManager = LineChartManager(line_chart)
        //创建多条折线的图表
        lineChartManager.showLineChart(xValues, yValues, "温度曲线", Color.WHITE)
        lineChartManager.setDescription("")
        lineChartManager.setYAxis(60f, 0f, 24)
        lineChartManager.setHightLimitLine(60f, "高温报警", 0)
    }

    @OnClick(R.id.img_back)
    fun onClick(view:View){
        when(view.id){
            R.id.img_back->
                finish()
        }
    }
}
