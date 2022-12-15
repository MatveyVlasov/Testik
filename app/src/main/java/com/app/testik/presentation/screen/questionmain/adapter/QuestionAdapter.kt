package com.app.testik.presentation.screen.questionmain.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.screen.question.QuestionFragment

class QuestionAdapter(
    fragment: Fragment,
    private val questions: List<QuestionDelegateItem>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = questions.size

    override fun createFragment(position: Int): Fragment = QuestionFragment(questions[position])
}