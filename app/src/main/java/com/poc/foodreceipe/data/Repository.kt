
package com.poc.foodreceipe.data

import com.poc.foodreceipe.data.database.LocalDataSource
import com.poc.foodreceipe.data.remote.RemoteDataSource
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class Repository @Inject constructor(
    remoteDataSource: RemoteDataSource,
    localDataSource: LocalDataSource
) {
    val remote: RemoteDataSource = remoteDataSource
    val local: LocalDataSource = localDataSource
}