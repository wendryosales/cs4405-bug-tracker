package edu.uopeople.bugtracker.data.local

enum class Priority { LOW, MEDIUM, HIGH, CRITICAL }
enum class Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
enum class SyncState { SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE }

@Entity(tableName = "issues", indices = [Index("remoteId"), Index("syncState")])
data class IssueEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val remoteId: Long? = null,
    val title: String,
    val description: String,
    val priority: Priority,
    val status: Status = Status.OPEN,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val syncState: SyncState = SyncState.PENDING_CREATE,
    val syncAttempts: Int = 0
)

@Database(entities = [IssueEntity::class], version = 1, exportSchema = true)
abstract class BugTrackerDatabase : RoomDatabase() {
    abstract fun issueDao(): IssueDao
}
