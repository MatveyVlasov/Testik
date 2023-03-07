package com.app.testik.presentation.screen.testspassedusers.model

import com.app.testik.domain.model.UserModel
import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class TestsPassedUsersScreenUIState(
    val testId: String = "",
    val tests: List<DelegateAdapterItem> = emptyList(),
    val users: List<UserModel> = emptyList(),
    val userSelected: UserModel? = null,
    val lastQuery: String = ""
)