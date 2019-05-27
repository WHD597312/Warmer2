package com.peihou.warmer.pojo

class UserInfo {
     var userId: Int = 0
     var username: String? = null
     var phone: String? = null
     var password: String? = null
     var address: String? = null
     var headImgUrl: String? = null
     var creatorId: Int = 0
     var sharerName: String? = null
    override fun toString(): String {
        return "UserInfo(userId=$userId, username=$username, phone=$phone, password=$password, address=$address, headImgUrl=$headImgUrl, creatorId=$creatorId, sharerName=$sharerName)"
    }

}