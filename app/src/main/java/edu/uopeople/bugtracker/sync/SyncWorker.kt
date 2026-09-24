package edu.uopeople.bugtracker.sync

class SyncWorker(ctx: Context, params: WorkerParameters, private val repo: IssueRepository)
    : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = try {
        repo.synchronize()
        Result.success()
    } catch (e: IOException) {
        if (runAttemptCount < 5) Result.retry() else Result.failure()
    }
}

class SyncScheduler(private val workManager: WorkManager) {
    fun request() {
        val work = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork("issue-sync", ExistingWorkPolicy.APPEND_OR_REPLACE, work)
    }
}
