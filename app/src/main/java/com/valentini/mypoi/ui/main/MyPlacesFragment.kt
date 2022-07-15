package com.valentini.mypoi.ui.main

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ha.databinding.MyplacesFragmentBinding


class MyPlacesFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _placeholder_binding: MyplacesFragmentBinding? = null

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _placeholder_binding = MyplacesFragmentBinding.inflate(inflater, container, false)

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
        fun newInstance(sectionNumber: Int): MyPlacesFragment {
            return MyPlacesFragment().apply {
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

    fun createATextView(
        layout_widh: Int, layout_height: Int, align: Int,
        text: String?, fontSize: Int, margin: Int, padding: Int
    ): TextView {
        val textView_item_name = TextView(this@MyPlacesFragment.context)

        // LayoutParams layoutParams = new LayoutParams(
        // LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // layoutParams.gravity = Gravity.LEFT;
        val _params = RelativeLayout.LayoutParams(
            layout_widh, layout_height
        )
        _params.setMargins(margin, margin, margin, margin)
        _params.addRule(align)
        textView_item_name.layoutParams = _params
        textView_item_name.text = text
        textView_item_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        //textView_item_name.setTextColor(a)
        // textView1.setBackgroundColor(0xff66ff66); // hex color 0xAARRGGBB
        textView_item_name.setPadding(padding, padding, padding, padding)
        return textView_item_name
    }
}