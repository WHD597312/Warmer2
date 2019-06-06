package com.peihou.warmer.test

import com.peihou.warmer.pojo.Device

fun main(array: Array<String>){
    var array1=Array<Int>(16){i: Int -> i*2}
    var array2= arrayOfNulls<Int>(16)
    var array3=IntArray(5)
    var bytes=ByteArray(20)

    var groupDevices=ArrayList<ArrayList<Device>>()
    var child=ArrayList<Device>()
    groupDevices.add(child)
    var map=HashMap<String,Any>()
    map["sss"]=1


    var a=128
    bytes[10]=a.toByte()
    array3[0]=0
    array3[1]=1
    for (i in 0 until bytes.size)
        println(bytes[i])

}
