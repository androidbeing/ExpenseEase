package com.dolphin.expenseease.ui.wallet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.data.model.Alert
import com.dolphin.expenseease.databinding.FragmentWalletBinding
import com.dolphin.expenseease.listeners.AddBalanceListener
import com.dolphin.expenseease.listeners.OnClickAlertListener
import com.dolphin.expenseease.listeners.WalletEditListener
import com.dolphin.expenseease.utils.DialogUtils.showAlertDialog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class WalletFragment : Fragment() {

    private val viewModel: WalletViewModel by viewModels()
    private var _binding: FragmentWalletBinding? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var balanceAdapter: BalanceAdapter
    private lateinit var balanceList: MutableList<MyWallet>
    private var updateIndex: Int = -1
    private var latestBalance = 0.0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initViews()
        return root
    }


    private fun initViews() {
        balanceList = mutableListOf()
        balanceAdapter = BalanceAdapter(requireContext(), balanceList, object : WalletEditListener {
            override fun onWalletEdit(
                wallet: MyWallet,
                index: Int
            ) {
                updateIndex = index
                addBalanceDialog(wallet)
            }

            override fun onWalletRemove(
                wallet: MyWallet,
                index: Int
            ) {
                val alert = Alert(getString(R.string.delete), getString(R.string.del_msg))
                showAlertDialog(requireContext(), alert, object : OnClickAlertListener {
                    override fun onAcknowledge(isOkay: Boolean) {
                        if (isOkay) {
                            requireActivity().runOnUiThread {
                                balanceList.remove(wallet)
                                balanceAdapter.notifyItemRemoved(index)
                                viewModel.deleteWallet(wallet)
                            }
                        }
                    }
                })
            }
        })
        binding.recyclerBalance.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBalance.adapter = balanceAdapter

        binding.fab.setOnClickListener {
            addBalanceDialog()
        }
    }

    private fun addBalanceDialog(walletToUpdate: MyWallet? = null) {
        val addBalanceBottomSheet = AddBalanceSheet(walletToUpdate, object : AddBalanceListener {
            override fun onBalanceAdd(wallet: MyWallet) {
                coroutineScope.launch {
                    Log.i("AAA", "Latest: ${latestBalance}")
                    val newBalanceValue = latestBalance + (wallet.addedAmount)
                    val newBalance = MyWallet(
                        id = wallet.id ?: 0,
                        balance = newBalanceValue!!,
                        addedAmount = wallet.addedAmount,
                        notes = wallet.notes,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    if (walletToUpdate == null) {
                        viewModel.addBalance(newBalance)
                    } else {
                        requireActivity().runOnUiThread {
                            balanceList[updateIndex] = newBalance
                            balanceAdapter.notifyItemChanged(updateIndex)
                            viewModel.updateWallet(newBalance)
                        }
                    }


                }
            }
        })
        addBalanceBottomSheet.show(childFragmentManager, AddBalanceSheet.TAG)
    }

    private fun initObservers() {
        viewModel.allBalances.observe(viewLifecycleOwner) { balances ->
            Log.i("AAA", "Balance Avl: ${Gson().toJson(balances)}")
            balanceList.clear()
            balanceList.addAll(balances.sortedByDescending { balance -> balance.createdAt })
            balanceAdapter.notifyDataSetChanged()
            setView(balanceList.isNotEmpty())
        }

        viewModel.getLatestBalance().observe(viewLifecycleOwner) { walletLatest ->
            if(walletLatest != null) {
                this.latestBalance = walletLatest?.balance ?: 0.0
            }
        }
    }

    private fun setView(hasItems: Boolean) {
        binding.txtNoItems.visibility = if (hasItems) View.GONE else View.VISIBLE
        binding.recyclerBalance.visibility = if (hasItems) View.VISIBLE else View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}