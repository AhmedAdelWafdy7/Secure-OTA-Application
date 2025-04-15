package com.zkrallah.sdv.domain.repositories

interface MainRepository {
    suspend fun getOnBoardingDone(): Boolean

    suspend fun setOnBoardingDone()
}