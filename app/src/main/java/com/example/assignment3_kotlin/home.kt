package com.example.assignment3_kotlin


import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView

/**
 * Home fragment
 */
class Home : Fragment() {
    private lateinit var homeName: EditText
    private lateinit var explicitBtn: Button

    /**
     * Create the home fragment view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Sets name to EditText homeName if it already exists in navigationView from previous entry
        homeName = view.findViewById(R.id.homeName)
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.nav_view)
        val textViewName = navigationView.getHeaderView(0).findViewById<TextView>(R.id.name)
        val currentName = textViewName.text
        if (!TextUtils.isEmpty(currentName)) {
            homeName.setText(currentName)
        }

        // On clicking Start button, hide keyboard, update name in navigationView, initiate gameFragment
        explicitBtn = view.findViewById(R.id.expButton)
        explicitBtn.setOnClickListener {
            hideKeyboard(requireContext(), it)
            val newName = homeName.text?.toString()?.trim()
            updateNameInNavigationView(newName ?: "")
            val gameFragment = Game()
            val bundle = Bundle()
            gameFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, gameFragment)
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    /**
     * Sets entered name to name in navigationView
     */
    private fun updateNameInNavigationView(newName: String) {
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.nav_view)
        val textViewName = navigationView.getHeaderView(0).findViewById<TextView>(R.id.name)
        textViewName.text = newName
    }

    /**
     * Hides keyboard
     */
    private fun hideKeyboard(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}