package com.valentini.mypoi.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

/**
 * A placeholder fragment containing a simple view.
 */
class HomeFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _placeholder_binding: HomeFragmentBinding? = null
    var tab = ARG_SECTION_NUMBER

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _placeholder_binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _placeholder_binding = HomeFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _placeholder_binding = null
    }
}