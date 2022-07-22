package com.valentini.mypoi.ui.main

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.valentini.mypoi.MainActivity
import com.valentini.mypoi.R
import com.valentini.mypoi.databinding.MyplacesFragmentBinding


open class MyPlacesFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var myplacesFragmentBinding: MyplacesFragmentBinding? = null //todo aggiustare ordine

    //private lateinit var privatePlacesList  : ArrayList<Marker>

    private val binding get() = myplacesFragmentBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this)[PageViewModel::class.java].apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }

        //Toast.makeText(requireContext(), "" + (activity as MainActivity).getInt(), Toast.LENGTH_LONG).show()


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = requireActivity().findViewById(R.id.markers_viewlist)

        val llm = LinearLayoutManager(activity);
        llm.orientation = LinearLayoutManager.VERTICAL;
        recyclerView.layoutManager = llm;

        val markerListTest = (activity as MainActivity).markerListTest //todo
        /*Toast.makeText(requireContext(), "Dimensione lista " + markerListTest.size
                 , Toast.LENGTH_LONG).show()*/

        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            var gestureDetector =
                GestureDetector(requireActivity(), object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                        return true
                    }
                })

            override fun onInterceptTouchEvent(
                Recyclerview: RecyclerView,
                motionEvent: MotionEvent
            ): Boolean {
                val childView = Recyclerview.findChildViewUnder(motionEvent.x, motionEvent.y)
                if (childView != null && gestureDetector.onTouchEvent(motionEvent)) {

                    val lat = childView!!.findViewById<TextView>(R.id.marker_label_latitude).text.toString().split(" ")[1].replace(",", ".").toDouble()
                    val long = childView!!.findViewById<TextView>(R.id.marker_label_longitude).text.toString().split(" ")[1].replace(",", ".").toDouble()

                    Toast.makeText(requireContext(), "lat $lat long $long", Toast.LENGTH_LONG).show()

                    (context as MainActivity).googleMapFragment.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, long), 13f))
                    (context as MainActivity).binding.tabs.getTabAt(1)
                        ?.select()
                }
                return false
            }

            override fun onTouchEvent(Recyclerview: RecyclerView, motionEvent: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        //privatePlacesList =
        recyclerView.adapter = MarkerAdapter(requireContext(), (activity as MainActivity).markerListTest)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        myplacesFragmentBinding = MyplacesFragmentBinding.inflate(inflater, container, false)

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
        myplacesFragmentBinding = null
    }

    open fun insertInRecycleView(markerIndex: Int) {
        //privatePlacesList.add(marker)

        val recyclerView: RecyclerView = requireActivity().findViewById(R.id.markers_viewlist)
        recyclerView.adapter!!.notifyItemInserted(markerIndex)
        //recyclerView.adapter!!.notifyDataSetChanged()
    }

    open fun removeFromRecycleView(markerIndex: Int) {
        //val indexOfRemovedMarker = (activity as MainActivity).markerListTest.indexOf(marker)
        //(activity as MainActivity).markerListTest.remove(marker)

        val recyclerView: RecyclerView = requireActivity().findViewById(R.id.markers_viewlist)
        recyclerView.adapter!!.notifyItemRemoved(markerIndex)
        //recyclerView.adapter!!.notifyDataSetChanged()
    }

    open fun updateInRecycleView(oldindex: Int) {
        //val indexOfItemChanged = privatePlacesList.indexOf(oldmarker)
        //privatePlacesList.set(oldindex, newmarker)

        val recyclerView: RecyclerView = requireActivity().findViewById(R.id.markers_viewlist)
        recyclerView.adapter!!.notifyItemChanged(oldindex)
        //recyclerView.adapter!!.notifyDataSetChanged()
    }


}