package com.app.testik.presentation.screen.questionmain.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.screen.question.QuestionFragment

class QuestionAdapter(
    fragment: Fragment,
    private val questions: List<QuestionDelegateItem>
) : FragmentStateAdapter(fragment) {

    private val fragments = List(questions.size) {  QuestionFragment(questions[it]) }

    override fun getItemCount(): Int = questions.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun getFragment(position: Int): QuestionFragment = fragments[position]
}