package com.peihou.warmer.test

import com.peihou.warmer.pojo.Device
import com.peihou.warmer.utils.TenTwoUtil
import java.util.*


fun main(array: Array<String>){
    var x=TenTwoUtil.changeToTwo(192)
    for (i in x)
        print("${i} ")
    println("--------------------")
    var x2=TenTwoUtil.changeToTen2(x)
    print(x2)
    var startIndex=2
    var bytes=ByteArray(45)
    for (i in 1 until 12){
        bytes[startIndex++]= (i+1).toByte()
        bytes[startIndex++]= (i+2).toByte()
        bytes[startIndex++]= (i+3).toByte()
    }
    var array=IntArray(44)
    for (i in 3 ..38 step 3){
        array[i]=i+1
    }
    println("--------------------")
    var random= Random()
    var xxxx=random.nextInt(43)
    println("${xxxx}")
    var maps=mutableMapOf<String,String>()
    maps.put("123456789","sssss")
}
