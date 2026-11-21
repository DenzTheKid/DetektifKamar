package com.detektifkamar.app.data.repository

import com.detektifkamar.app.data.local.DatabaseChangeNotifier
import com.detektifkamar.app.data.local.DetektifDbHelper
import com.detektifkamar.app.data.local.SuspectDao
import com.detektifkamar.app.domain.model.Suspect
import com.detektifkamar.app.domain.model.SuspectCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface SuspectRepository {
    fun getAllSuspects(searchQuery: String? = null, category: SuspectCategory? = null): Flow<List<Suspect>>
    suspend fun getSuspectById(id: Long): Suspect?
    suspend fun getLikelySuspectsForDelta(deltaUt: Float, limit: Int = 6): List<Suspect>
    suspend fun getSuspectsForPoint(pointId: Long): List<Suspect>
    suspend fun linkSuspectToPoint(pointId: Long, suspectId: Long, customLabel: String? = null)
    suspend fun unlinkSuspectFromPoint(pointId: Long, suspectId: Long)
    suspend fun getCustomLabelForPoint(pointId: Long): String?
    suspend fun setCustomLabelForPoint(pointId: Long, label: String?)
}

class SuspectRepositoryImpl(
    private val dbHelper: DetektifDbHelper,
    private val changeNotifier: DatabaseChangeNotifier
) : SuspectRepository {

    private val suspectDao = SuspectDao(dbHelper)

    override fun getAllSuspects(searchQuery: String?, category: SuspectCategory?): Flow<List<Suspect>> =
        changeNotifier.changeEvents.map {
            suspectDao.getAllSuspects(searchQuery, category)
        }.flowOn(Dispatchers.IO)

    override suspend fun getSuspectById(id: Long): Suspect? = withContext(Dispatchers.IO) {
        suspectDao.getSuspectById(id)
    }

    override suspend fun getLikelySuspectsForDelta(deltaUt: Float, limit: Int): List<Suspect> = withContext(Dispatchers.IO) {
        suspectDao.getLikelySuspectsForDelta(deltaUt, limit)
    }

    override suspend fun getSuspectsForPoint(pointId: Long): List<Suspect> = withContext(Dispatchers.IO) {
        suspectDao.getSuspectsForPoint(pointId)
    }

    override suspend fun linkSuspectToPoint(pointId: Long, suspectId: Long, customLabel: String?) = withContext(Dispatchers.IO) {
        suspectDao.linkSuspectToPoint(pointId, suspectId, customLabel)
        changeNotifier.notifyChange()
    }

    override suspend fun unlinkSuspectFromPoint(pointId: Long, suspectId: Long) = withContext(Dispatchers.IO) {
        suspectDao.unlinkSuspectFromPoint(pointId, suspectId)
        changeNotifier.notifyChange()
    }

    override suspend fun getCustomLabelForPoint(pointId: Long): String? = withContext(Dispatchers.IO) {
        suspectDao.getCustomLabelForPoint(pointId)
    }

    override suspend fun setCustomLabelForPoint(pointId: Long, label: String?) = withContext(Dispatchers.IO) {
        suspectDao.setCustomLabelForPoint(pointId, label)
        changeNotifier.notifyChange()
    }
}
