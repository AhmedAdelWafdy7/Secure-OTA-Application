package com.zkrallah.sdv.data.repositories

import com.zkrallah.sdv.data.datastore.DataStore
import com.zkrallah.sdv.domain.repositories.MainRepository

class MainRepositoryImpl(
    private val dataStore: DataStore
) : MainRepository {
    override suspend fun getOnBoardingDone(): Boolean {
        return dataStore.getIsOnBoardingFinished()
    }

    override suspend fun setOnBoardingDone() {
        dataStore.setIsOnBoardingFinished(true)
    }
}