package com.zkrallah.sdv.data.datastore

interface DataStore {
    suspend fun getIsOnBoardingFinished(): Boolean

    suspend fun setIsOnBoardingFinished(isOnBoardingFinished: Boolean)
}