package com.peihou.warmer.utils

import android.text.TextUtils

object Mobile {
    /**
     * 验证手机格式
     */
    fun isMobile(number: String): Boolean {
        /*
    移动：134、135、136、137、138、139、150、151、152、157(TD)、158、159、178(新)、182、184、187、188
    联通：130、131、132、152、155、156、185、186
    电信：133、153、170、173、177、180、181、189、（1349卫通）
    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
    */
        val num = "[1][345789]\\d{9}"//"[1]"代表第1位为数字1，"[345789]"代表第二位可以为3、4、5、7、8，9中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        return if (TextUtils.isEmpty(num)) {
            false
        } else {
            //matches():字符串是否在给定的正则表达式匹配
            number.matches(num.toRegex())
        }
    }
}
