package com.peihou.warmer.mvp

class MyThread:Runnable {
    var ticket=1000
    override fun run() {
        for(i in 0 until 1000) {
            synchronized(this){
                if (ticket > 0) {
                    try {
                        Thread.sleep(500)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    println(Thread.currentThread().name + "卖第 " + ticket-- + "张票")
                }
            }
        }
    }
}