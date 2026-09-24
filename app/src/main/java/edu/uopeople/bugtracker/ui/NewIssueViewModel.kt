package edu.uopeople.bugtracker.ui

class NewIssueViewModel(
    private val repo: IssueRepository, private val state: SavedStateHandle
) : ViewModel() {
    val title = state.getStateFlow("title", "")
    val description = state.getStateFlow("description", "")
    fun onTitleChange(value: String) { state["title"] = value }

    fun submit(priority: Priority) = viewModelScope.launch {
        repo.create(title.value.trim(), description.value.trim(), priority)
        state["title"] = ""; state["description"] = ""
    }
}

// IssueListFragment: collect only while visible; resumes automatically
viewLifecycleOwner.lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.issues.collect { adapter.submitList(it) }
    }
}
