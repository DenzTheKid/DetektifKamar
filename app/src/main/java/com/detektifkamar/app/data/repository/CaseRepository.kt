package com.detektifkamar.app.data.repository

import com.detektifkamar.app.data.local.CaseDao
import com.detektifkamar.app.data.local.CaseSummary
import com.detektifkamar.app.data.local.DatabaseChangeNotifier
import com.detektifkamar.app.data.local.DetektifDbHelper
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.domain.model.EvidencePoint
import com.detektifkamar.app.domain.model.EvidencePointWithReading
import com.detektifkamar.app.domain.model.Furniture
import com.detektifkamar.app.domain.model.InvestigationCase
import com.detektifkamar.app.domain.model.PointReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface CaseRepository {
    fun getAllCases(): Flow<List<InvestigationCase>>
    fun getCaseFlow(id: Long): Flow<InvestigationCase?>
    suspend fun getCaseById(id: Long): InvestigationCase?
    suspend fun createCase(case: InvestigationCase): Long
    suspend fun updateCase(case: InvestigationCase)
    suspend fun updateCaseBaseline(caseId: Long, baselineUt: Float)
    suspend fun deleteCase(id: Long)

    fun getFurnitureForCase(caseId: Long): Flow<List<Furniture>>
    suspend fun addFurniture(furniture: Furniture): Long
    suspend fun deleteFurniture(id: Long)

    fun getPointsForCase(caseId: Long, baselineUt: Float?): Flow<List<EvidencePointWithReading>>
    suspend fun getPointById(id: Long): EvidencePoint?
    suspend fun addPoint(point: EvidencePoint): Long
    suspend fun deletePoint(id: Long)

    suspend fun addReading(reading: PointReading): Long
    suspend fun getReadingsForPoint(pointId: Long): List<PointReading>

    suspend fun getCaseSummary(caseId: Long, baselineUt: Float): CaseSummary
    fun getAllArchivedPoints(
        searchQuery: String? = null,
        categoryFilter: DisturbanceCategory? = null,
        sortByLargestDelta: Boolean = false
    ): Flow<List<EvidencePointWithReading>>
}

class CaseRepositoryImpl(
    private val dbHelper: DetektifDbHelper,
    private val changeNotifier: DatabaseChangeNotifier
) : CaseRepository {

    private val caseDao = CaseDao(dbHelper)

    override fun getAllCases(): Flow<List<InvestigationCase>> =
        changeNotifier.changeEvents.map {
            caseDao.getAllCases()
        }.flowOn(Dispatchers.IO)

    override fun getCaseFlow(id: Long): Flow<InvestigationCase?> =
        changeNotifier.changeEvents.map {
            caseDao.getCaseById(id)
        }.flowOn(Dispatchers.IO)

    override suspend fun getCaseById(id: Long): InvestigationCase? = withContext(Dispatchers.IO) {
        caseDao.getCaseById(id)
    }

    override suspend fun createCase(case: InvestigationCase): Long = withContext(Dispatchers.IO) {
        val id = caseDao.insertCase(case)
        changeNotifier.notifyChange()
        id
    }

    override suspend fun updateCase(case: InvestigationCase) = withContext(Dispatchers.IO) {
        caseDao.updateCase(case)
        changeNotifier.notifyChange()
    }

    override suspend fun updateCaseBaseline(caseId: Long, baselineUt: Float) = withContext(Dispatchers.IO) {
        caseDao.updateBaseline(caseId, baselineUt)
        changeNotifier.notifyChange()
    }

    override suspend fun deleteCase(id: Long) = withContext(Dispatchers.IO) {
        caseDao.deleteCase(id)
        changeNotifier.notifyChange()
    }

    override fun getFurnitureForCase(caseId: Long): Flow<List<Furniture>> =
        changeNotifier.changeEvents.map {
            caseDao.getFurnitureByCaseId(caseId)
        }.flowOn(Dispatchers.IO)

    override suspend fun addFurniture(furniture: Furniture): Long = withContext(Dispatchers.IO) {
        val id = caseDao.insertFurniture(furniture)
        changeNotifier.notifyChange()
        id
    }

    override suspend fun deleteFurniture(id: Long) = withContext(Dispatchers.IO) {
        caseDao.deleteFurniture(id)
        changeNotifier.notifyChange()
    }

    override fun getPointsForCase(caseId: Long, baselineUt: Float?): Flow<List<EvidencePointWithReading>> =
        changeNotifier.changeEvents.map {
            caseDao.getPointsWithLatestReadings(caseId, baselineUt)
        }.flowOn(Dispatchers.IO)

    override suspend fun getPointById(id: Long): EvidencePoint? = withContext(Dispatchers.IO) {
        caseDao.getPointById(id)
    }

    override suspend fun addPoint(point: EvidencePoint): Long = withContext(Dispatchers.IO) {
        val id = caseDao.insertPoint(point)
        changeNotifier.notifyChange()
        id
    }

    override suspend fun deletePoint(id: Long) = withContext(Dispatchers.IO) {
        caseDao.deletePoint(id)
        changeNotifier.notifyChange()
    }

    override suspend fun addReading(reading: PointReading): Long = withContext(Dispatchers.IO) {
        val id = caseDao.insertReading(reading)
        changeNotifier.notifyChange()
        id
    }

    override suspend fun getReadingsForPoint(pointId: Long): List<PointReading> = withContext(Dispatchers.IO) {
        caseDao.getReadingsForPoint(pointId)
    }

    override suspend fun getCaseSummary(caseId: Long, baselineUt: Float): CaseSummary = withContext(Dispatchers.IO) {
        caseDao.getCaseSummary(caseId, baselineUt)
    }

    override fun getAllArchivedPoints(
        searchQuery: String?,
        categoryFilter: DisturbanceCategory?,
        sortByLargestDelta: Boolean
    ): Flow<List<EvidencePointWithReading>> =
        changeNotifier.changeEvents.map {
            caseDao.getAllEvidencePointsWithLatestReading(searchQuery, categoryFilter, sortByLargestDelta)
        }.flowOn(Dispatchers.IO)
}
