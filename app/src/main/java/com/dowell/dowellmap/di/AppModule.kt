package com.dowell.dowellmap.di

import android.content.Context
import com.dowell.dowellmap.data.SearchRepository
import com.dowell.dowellmap.data.UserDatastore
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRepository(): SearchRepository {
        return SearchRepository()
    }

    @Singleton
    @Provides
    fun provideGson(): GsonBuilder {
        return GsonBuilder()
    }

    @Singleton
    @Provides
    fun provideUserPreferences(@ApplicationContext context: Context): UserDatastore {
        return UserDatastore(context)
    }

}