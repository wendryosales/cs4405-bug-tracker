package edu.uopeople.bugtracker.data

class IssueRepository(
    private val dao: IssueDao, private val api: IssueApi, private val sync: SyncScheduler
) {
    val issues: Flow<List<IssueEntity>> = dao.observeAll()

    suspend fun create(title: String, description: String, priority: Priority) {
        dao.upsert(IssueEntity(title = title, description = description, priority = priority))
        sync.request()
    }
    suspend fun update(issue: IssueEntity) {
        val state = if (issue.remoteId == null) PENDING_CREATE else PENDING_UPDATE
        dao.upsert(issue.copy(updatedAt = System.currentTimeMillis(), syncState = state))
        sync.request()
    }
    suspend fun delete(issue: IssueEntity) {
        if (issue.remoteId == null) dao.delete(issue.id)
        else { dao.upsert(issue.copy(syncState = PENDING_DELETE)); sync.request() }
    }

    suspend fun synchronize() { push(); pull() }

    private suspend fun push() = dao.getUnsynced().forEach { issue ->
        try {
            when (issue.syncState) {
                PENDING_CREATE -> { val saved = api.create(issue.id, issue.toDto())
                    dao.upsert(issue.copy(remoteId = saved.id, syncState = SYNCED)) }
                PENDING_UPDATE -> { api.update(issue.remoteId!!, issue.toDto())
                    dao.upsert(issue.copy(syncState = SYNCED)) }
                PENDING_DELETE -> { api.delete(issue.remoteId!!); dao.delete(issue.id) }
                SYNCED -> Unit
            }
        } catch (e: HttpException) {
            dao.upsert(issue.copy(syncAttempts = issue.syncAttempts + 1))
        }
    }

    private suspend fun pull() {
        val remote = api.getIssues()
        for (dto in remote) {
            val local = dao.getByRemoteId(dto.id)
            if (local == null || local.syncState == SYNCED)
                dao.upsert(dto.toEntity(localId = local?.id ?: UUID.randomUUID().toString()))
        }
        dao.deleteSyncedNotIn(remote.map { it.id })
    }
}
