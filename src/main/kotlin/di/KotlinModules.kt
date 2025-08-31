package com.example.di

import com.example.service.MomentoRepository
import com.example.service.MomentoService
import org.koin.dsl.module

val repositoryModule = module {
    single { MomentoRepository() }
}

val serviceModule = module {
    single { MomentoService(get()) }
}

val appModules = listOf(repositoryModule, serviceModule)