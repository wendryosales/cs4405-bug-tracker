package edu.uopeople.bugtracker.data.local

@Dao
interface IssueDao {
    @Query("SELECT * FROM issues WHERE syncState != 'PENDING_DELETE'" +
           " ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<IssueEntity>>
    @Query("SELECT * FROM issues WHERE syncState != 'SYNCED' AND syncAttempts < 5")
    suspend fun getUnsynced(): List<IssueEntity>
    @Query("SELECT * FROM issues WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: Long): IssueEntity?
    @Upsert suspend fun upsert(issue: IssueEntity)
    @Query("DELETE FROM issues WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM issues WHERE syncState = 'SYNCED' AND remoteId NOT IN (:ids)")
    suspend fun deleteSyncedNotIn(ids: List<Long>)
}

interface IssueApi {
    @GET("issues") suspend fun getIssues(): List<IssueDto>
    @POST("issues") suspend fun create(@Body issue: IssueDto): IssueDto
    @PUT("issues/{id}") suspend fun update(@Path("id") id: Long,
                                           @Body issue: IssueDto): IssueDto
    @DELETE("issues/{id}") suspend fun delete(@Path("id") id: Long): Response<Unit>
}
