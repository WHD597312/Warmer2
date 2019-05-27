package com.peihou.warmer.mvp

fun main(args:Array<String>){
    var mt=MyThread()
    var t1=Thread(mt)
    var t2=Thread(mt)
    var t3=Thread(mt)

    t1.start()
    t2.start()
    t3.start()
}