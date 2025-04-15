package com.zkrallah.sdv.di

import android.content.Context

import com.zkrallah.sdv.data.datastore.DataStore
import com.zkrallah.sdv.data.datastore.DataStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext appContext: Context
    ): DataStore {
        return DataStoreImpl(appContext)
    }

    @Provides
    @Singleton
    fun provideContext(
        @ApplicationContext appContext: Context
    ): Context {
        return appContext
    }
}