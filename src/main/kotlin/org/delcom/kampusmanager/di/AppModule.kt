package org.delcom.kampusmanager.di

import org.delcom.kampusmanager.data.repository.*
import org.delcom.kampusmanager.service.*
import org.koin.dsl.module

fun appModule(jwtSecret: String) = module {
    // Repositories
    single<IUserRepository>          { UserRepository() }
    single<IRefreshTokenRepository>  { RefreshTokenRepository() }
    single<IJadwalKuliahRepository>  { JadwalKuliahRepository() }
    single<IKegiatanRepository>      { KegiatanRepository() }

    // Services
    single { AuthService(jwtSecret, get(), get()) }
    single { UserService(get()) }
    single { JadwalKuliahService(get(), get()) }
    single { KegiatanService(get(), get()) }
}
