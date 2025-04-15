package com.zkrallah.sdv.di

import com.zkrallah.sdv.data.datastore.DataStore
import com.zkrallah.sdv.data.repositories.HomeRepositoryImpl
import com.zkrallah.sdv.data.repositories.MainRepositoryImpl
import com.zkrallah.sdv.domain.repositories.HomeRepository
import com.zkrallah.sdv.domain.repositories.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModule {
    @Provides
    @Singleton
    fun provideMainRepository(
        dataStore: DataStore
    ): MainRepository {
        return MainRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(): HomeRepository {
        return HomeRepositoryImpl()
    }
}