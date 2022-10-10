package com.example.jwtlogin.di

import com.example.jwtlogin.data.service.AuthenticationService
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ServiceModule::class]
)
object MockServiceModule {

    @Provides
    @Singleton
    fun provideMockService(): AuthenticationService {
        return mockk()
    }
}