package com.dolphin.expenseease.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.databinding.FragmentBudgetBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class BudgetFragment : Fragment() {
    private val viewModel: BudgetViewModel by viewModels()
    private var _binding: FragmentBudgetBinding? = null
    private lateinit var budgetAdapter: BudgetAdapter
    private lateinit var budgetList: MutableList<Budget>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        budgetList = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        val root: View = binding.root
        for (i in 1..10) {
            addBudget()
        }
        initViews()
        return root
    }

    private fun initViews() {
        budgetAdapter = BudgetAdapter(requireContext(), budgetList)
        binding.recyclerBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBudgets.adapter = budgetAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.allBudgets.observe(viewLifecycleOwner) {
            budgetList.clear()
            budgetList.addAll(it)
            budgetAdapter.notifyDataSetChanged()
        }
    }

    private fun addBudget() {
        // Create a new Budget object
        val budget = Budget(amount = 100.0, type = "Food", month = "June", createdAt = Date().time)
        // Insert the budget into the database
        viewModel.insertBudget(budget)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}