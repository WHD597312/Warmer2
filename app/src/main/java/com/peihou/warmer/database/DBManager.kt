package com.peihou.warmer.database

import android.content.Context
import com.peihou.warmer.database.dao.DaoMaster
import com.peihou.warmer.database.dao.DaoSession
import org.greenrobot.greendao.identityscope.IdentityScopeType

class DBManager private constructor(){
    private val dbName:String="warmer"
    private var openHelper: DaoMaster.DevOpenHelper?=null
    private var daoMaster:DaoMaster?=null
    private get() {
        if (field==null){
            synchronized(DBManager::class.java){
                if (field==null){
                    field= DaoMaster(openHelper?.readableDb)
                }
            }
        }
        return field
    }

    var daoSesson: DaoSession?=null
    get() {
        if (field==null){
            synchronized(DBManager::class.java){
                if (daoMaster==null){
                    daoMaster=daoMaster
                }
                if (field==null){
                    field=daoMaster?.newSession(IdentityScopeType.Session)
                }
            }
        }
        return field
    }
    private constructor(context: Context):this(){
        openHelper= DaoMaster.DevOpenHelper(context, dbName,null)
    }
    companion object {
        private var instance:DBManager?=null
        fun getInstance(context:Context):DBManager?{
            if (instance==null){
                synchronized(DBManager::class.java){
                    if (instance==null){
                        instance= DBManager(context)
                    }
                }
            }
            return instance
        }
    }

}