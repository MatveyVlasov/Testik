package com.app.testik.presentation.screen.testspassedusers

import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.cursoradapter.widget.CursorAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentTestsPassedUsersBinding
import com.app.testik.domain.model.UserModel
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testspassedusers.adapter.TestPassedUserDelegateAdapter
import com.app.testik.presentation.screen.testspassedusers.model.TestsPassedUsersScreenEvent
import com.app.testik.util.*
import com.app.testik.util.cursor.UserCursorAdapter
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestsPassedUsersFragment : BaseFragment<FragmentTestsPassedUsersBinding>() {

    private val viewModel: TestsPassedUsersViewModel by viewModels()

    private val testsAdapter by lazy {
        CompositeAdapter.Builder()
            .setOnUpdateCallback(viewModel::updateList)
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(
                TestPassedUserDelegateAdapter { recordId, username ->
                    navigateToTestPassed(recordId = recordId, username = username)
                }
            )
            .build()
    }

    private lateinit var cursorAdapter: CursorAdapter

    private lateinit var searchView: SearchView

    override fun createBinding(inflater: LayoutInflater) = FragmentTestsPassedUsersBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { onBackPressed() }
    }

    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            rvTests.adapter = testsAdapter

            cursorAdapter = UserCursorAdapter(requireContext())

            searchView = toolbar.menu.findItem(R.id.search).actionView as SearchView

            searchView.queryHint = getString(R.string.search_by_author)

            (searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText).apply {
                setTextColor(getColor(R.color.black))
                setupSearchLayout()
            }

            searchView.suggestionsAdapter = cursorAdapter
        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    query?.let {
                        viewModel.getUsers(query)
                    }
                    return false
                }
            })


            searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    return false
                }

                override fun onSuggestionClick(position: Int): Boolean {
                    viewModel.selectUser(position)
                    hideKeyboard()
                    return true
                }
            })
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            binding.toolbar.setTitle(R.string.test_results)
                            testsAdapter.submitList(data.tests)

                            setupSearchHints(data.users)

                            searchView.isVisible = data.userSelected == null
                            data.userSelected?.let { user ->
                                binding.toolbar.title = user.username
                            }

                            val isListEmpty = data.tests.isEmpty()
                            binding.llNoTests.isVisible = isListEmpty
                            binding.tvDescription.isVisible = isListEmpty && data.userSelected == null
                            binding.rvTests.isVisible = !isListEmpty
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun handleEvent(event: TestsPassedUsersScreenEvent) {
        when (event) {
            is TestsPassedUsersScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestsPassedUsersScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestsPassedUsersScreenEvent.Loading -> Unit
        }
        setLoadingState(event is TestsPassedUsersScreenEvent.Loading)
    }

    private fun setupSearchHints(users: List<UserModel>) {
        val cursor = MatrixCursor(
            arrayOf(
                BaseColumns._ID,
                "username",
                "nameSurname",
                "avatar"
            )
        )

        users.forEachIndexed { index, user ->
            cursor.addRow(
                arrayOf(
                    index,
                    user.username,
                    user.getFullName(showUsername = false),
                    user.avatar
                )
            )
        }

        cursorAdapter.changeCursor(cursor)
    }

    private fun navigateToTestPassed(recordId: String, username: String) {
        navController.navigate(
            TestsPassedUsersFragmentDirections.toTestPassed(recordId = recordId, username = username)
        )
    }

    private fun onBackPressed() {
        if (searchView.isIconified) {
            navController.navigateUp()
            return
        }

        if (viewModel.screenUIState.userSelected != null) {
            viewModel.deselectUser()
            return
        }

        searchView.setQuery("", false)
        searchView.isIconified = true
    }
}