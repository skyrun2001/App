package com.focusguard.app.di

import com.focusguard.app.data.repository.AppRestrictionRepositoryImpl
import com.focusguard.app.data.repository.StepCounterRepositoryImpl
import com.focusguard.app.domain.repository.AppRestrictionRepository
import com.focusguard.app.domain.repository.StepCounterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppRestrictionRepository(
        impl: AppRestrictionRepositoryImpl,
    ): AppRestrictionRepository

    @Binds
    @Singleton
    abstract fun bindStepCounterRepository(
        impl: StepCounterRepositoryImpl,
    ): StepCounterRepository
}
