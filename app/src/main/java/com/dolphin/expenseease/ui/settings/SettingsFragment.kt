package com.dolphin.expenseease.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dolphin.expenseease.databinding.FragmentSettingsBinding
import com.dolphin.expenseease.utils.CurrencyManager

class SettingsFragment: Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        updateCurrencyDisplay()

        // Button to re-detect currency
        binding.btnRedetectCurrency.setOnClickListener {
            initCurrencySettings()
        }

        return root
    }

    private fun initCurrencySettings() {
        CurrencyManager.clearCurrency()
        CurrencyManager.forceDetectAndSaveCurrency(requireContext())
        updateCurrencyDisplay()

        val currencyCode = CurrencyManager.getCurrencyCode(requireContext())
        val currencySymbol = CurrencyManager.getCurrencySymbol(requireContext())
        Toast.makeText(
            requireContext(),
            "Currency detected: $currencyCode ($currencySymbol)",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateCurrencyDisplay() {
        val currencyName = CurrencyManager.getCurrencyName(requireActivity())
        val currencySymbol = CurrencyManager.getCurrencySymbol(requireActivity())
        val currencyCode = CurrencyManager.getCurrencyCode(requireActivity())

        Log.d("SettingsFragment", "Currency: $currencyCode ($currencySymbol) - $currencyName")

        binding.valCurrencyName.text = currencyName
        binding.valCurrencyCode.text = currencyCode
        binding.valCurrencySymbol.text = currencySymbol
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}