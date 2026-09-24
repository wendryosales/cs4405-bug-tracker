# Bug Tracker (CS 4405 Mobile Applications, Unit 3)

Offline-first Android issue tracker used as the example project for the Unit 3 written assignment.

- **Room** is the local source of truth (`IssueEntity`, `IssueDao`, `BugTrackerDatabase`).
- **Retrofit** syncs issues with the backend in both directions (`IssueApi`, `IssueRepository.synchronize()`).
- **WorkManager** retries failed syncs with exponential backoff (`SyncWorker`, `SyncScheduler`).
- **SavedStateHandle** keeps in-progress issue drafts across rotation and process death (`NewIssueViewModel`).

## Branching model
`main` (releases, tagged `v1.0`, `v1.1`) ← `develop` ← `feature/*`; urgent fixes on `hotfix/*` from a release tag.
