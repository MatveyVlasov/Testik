package com.app.testik.presentation.screen.testlist

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.cursoradapter.widget.CursorAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentTestListBinding
import com.app.testik.domain.model.UserModel
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testlist.adapter.TestInfoDelegateAdapter
import com.app.testik.presentation.screen.testlist.model.TestListScreenEvent
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestListFragment : BaseFragment<FragmentTestListBinding>() {

    private val viewModel: TestListViewModel by viewModels()

    private val testsAdapter by lazy {
        CompositeAdapter.Builder()
            .setOnUpdateCallback(viewModel::updateList)
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(
                TestInfoDelegateAdapter { navigateToTest(it) }
            )
            .build()
    }

    private lateinit var cursorAdapter: CursorAdapter

    private lateinit var searchView: SearchView

    override fun createBinding(inflater: LayoutInflater) = FragmentTestListBinding.inflate(inflater)

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

            val cursor = MatrixCursor(
                arrayOf(
                    BaseColumns._ID,
                    "username",
                    "nameSurname",
                    "avatar"
                )
            )

            cursorAdapter = object : CursorAdapter(requireContext(), cursor, 0) {
                override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
                    return LayoutInflater.from(context).inflate(R.layout.item_search, parent, false)
                }

                override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
                    if (view == null || context == null || cursor == null) return

                    val tvUsername = view.findViewById(R.id.tvUsername) as TextView
                    val tvNameSurname = view.findViewById(R.id.tvNameSurname) as TextView
                    val ivAvatar = view.findViewById(R.id.ivAvatar) as CircleImageView

                    tvUsername.text = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                    tvNameSurname.text = cursor.getString(cursor.getColumnIndexOrThrow("nameSurname"))
                    loadAvatar(context, ivAvatar, cursor.getString(cursor.getColumnIndexOrThrow("avatar")))
                }
            }

            searchView = toolbar.menu.findItem(R.id.search).actionView as SearchView

            searchView.queryHint = getString(R.string.search_by_author)

            (searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText).apply {
                setTextColor(getColor(R.color.black))
                val params = (layoutParams as ViewGroup.MarginLayoutParams)
                params.updateMargins(left = params.marginStart - 36.toPx())
                updatePadding(left = paddingStart + 28.toPx())
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
                            binding.toolbar.setTitle(data.category.description)
                            testsAdapter.submitList(data.tests)

                            setupSearchHints(data.users)

                            searchView.isVisible = data.userSelected == null
                            data.userSelected?.let { user ->
                                binding.toolbar.title = user.username
                            }

                            val isListEmpty = data.tests.isEmpty()
                            binding.llNoTests.isVisible = isListEmpty
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

    private fun handleEvent(event: TestListScreenEvent) {
        when (event) {
            is TestListScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestListScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestListScreenEvent.Loading -> Unit
        }
        setLoadingState(event is TestListScreenEvent.Loading)
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

    private fun navigateToTest(testId: String) {
        navController.navigate(
            TestListFragmentDirections.toTestInfo(testId)
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