package com.hx.hongbao

import com.hx.hongbao.gen.DaoMaster
import com.hx.hongbao.gen.DaoSession

class GreenDaoManager private constructor() {
    var master: DaoMaster? = null
        private set
    var session: DaoSession? = null
        private set

    init {
        if (mInstance == null) {
            val devOpenHelper = DaoMaster.DevOpenHelper(MyApplication.context, "user.db")
            master = DaoMaster(devOpenHelper.writableDatabase)
            session = master!!.newSession()
        }
    }

    val newSession: DaoSession?
        get() {
            session = master!!.newSession()
            return session
        }

    companion object {
        @Volatile private var mInstance: GreenDaoManager? = null

        val instance: GreenDaoManager?
            get() {
                if (mInstance == null) {
                    synchronized(GreenDaoManager::class.java) {
                        if (mInstance == null) {
                            mInstance = GreenDaoManager()
                        }
                    }
                }
                return mInstance
            }
    }
}
