package com.dolphin.expenseease.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.model.Alert
import com.dolphin.expenseease.databinding.FragmentBudgetBinding
import com.dolphin.expenseease.listeners.AddBudgetListener
import com.dolphin.expenseease.listeners.BudgetEditListener
import com.dolphin.expenseease.listeners.OnClickAlertListener
import com.dolphin.expenseease.ui.wallet.AddBalanceSheet
import com.dolphin.expenseease.utils.DialogUtils.showAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class BudgetFragment : Fragment() {
    private val viewModel: BudgetViewModel by viewModels()
    private var _binding: FragmentBudgetBinding? = null
    private lateinit var budgetAdapter: BudgetAdapter
    private lateinit var budgetList: MutableList<Budget>
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var updateIndex: Int = -1

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
        initViews()
        return root
    }

    private fun initViews() {
        budgetAdapter = BudgetAdapter(requireContext(), budgetList, object: BudgetEditListener {
            override fun onBudgetEdit(
                budget: Budget,
                index: Int
            ) {
                updateIndex = index
                addBudgetDialog(budget)
            }

            override fun onBudgetRemove(
                budget: Budget,
                index: Int
            ) {
                val alert = Alert(getString(R.string.delete), getString(R.string.del_msg))
                showAlertDialog(requireContext(), alert, object: OnClickAlertListener {
                    override fun onAcknowledge(isOkay: Boolean) {
                        if(isOkay) {
                            requireActivity().runOnUiThread {
                                budgetList.remove(budget)
                                budgetAdapter.notifyItemRemoved(index)
                                viewModel.deleteExpense(budget)
                            }
                        }
                    }
                })
            }
        })
        binding.recyclerBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBudgets.adapter = budgetAdapter

        binding.fab.setOnClickListener {
            addBudgetDialog()
        }
    }

    private fun addBudgetDialog(budgetToUpdate: Budget? = null) {
        val addBudgetBottomSheet = AddBudgetSheet(budgetToUpdate, object : AddBudgetListener {
            override fun onBudgetAdd(
                budgetType: String,
                allocatedAmount: Double,
                monthYear: String
            ) {
                coroutineScope.launch {
                    val budget = Budget(
                        amount = allocatedAmount,
                        type = budgetType,
                        monthYear = monthYear,
                        createdAt = Date().time
                    )
                    if(budgetToUpdate == null) {
                        viewModel.addBudget(budget)
                    } else {
                        requireActivity().runOnUiThread {
                            budgetList[updateIndex] = budget
                            budgetAdapter.notifyItemChanged(updateIndex)
                            viewModel.updateBudget(budget)
                        }
                    }
                }
            }
        })
        addBudgetBottomSheet.show(childFragmentManager, AddBalanceSheet.TAG)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.allBudgets.observe(viewLifecycleOwner) {
            budgetList.clear()
            budgetList.addAll(it)
            budgetAdapter.notifyDataSetChanged()
            setView(budgetList.isNotEmpty())
        }
    }

    private fun setView(hasItems: Boolean) {
        binding.textNoItems.visibility = if (hasItems) View.GONE else View.VISIBLE
        binding.recyclerBudgets.visibility = if (hasItems) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}