package com.detektifkamar.app.data.repository

import com.detektifkamar.app.data.local.DeviceReferenceDao
import com.detektifkamar.app.domain.model.DeviceCategory
import com.detektifkamar.app.domain.model.DeviceReference
import com.detektifkamar.app.domain.repository.DeviceReferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DeviceReferenceRepositoryImpl(
    private val deviceReferenceDao: DeviceReferenceDao
) : DeviceReferenceRepository {

    override fun getReferencesFlow(
        searchQuery: String,
        category: DeviceCategory?
    ): Flow<List<DeviceReference>> = flow {
        val list = deviceReferenceDao.search(searchQuery, category)
        emit(list)
    }.flowOn(Dispatchers.IO)

    override suspend fun getReferenceById(id: Long): DeviceReference? {
        return withContext(Dispatchers.IO) {
            deviceReferenceDao.getById(id)
        }
    }
}
