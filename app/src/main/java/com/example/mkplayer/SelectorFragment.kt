package com.example.mkplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO :: Databinding
class SelectorFragment private constructor() : Fragment() {

    companion object {

        fun getInstance(texts: ArrayList<String>): SelectorFragment {
            val selector = SelectorFragment()
            selector.arguments = getBundle(texts)
            return selector
        }

        fun show(activity: AppCompatActivity, container: Int, fragment: SelectorFragment) {
            activity.findViewById<View>(container).bringToFront()
            activity.supportFragmentManager
                .beginTransaction()
                .replace(container, fragment)
                .commitAllowingStateLoss()
        }

        fun hide(fragment: SelectorFragment) {
            val activity = fragment.activity
            activity?.let {
                it.supportFragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss()
            }
        }

        private fun getBundle(texts: ArrayList<String>): Bundle {
            val bundle = Bundle()
            bundle.putStringArrayList("texts", texts)
            return bundle
        }
    }

    interface SelectorItemClickedCallback {
        fun onClick(position: Int)
    }

    private var texts: ArrayList<String>? = null
    private var listener: SelectorItemClickedCallback? = null
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        args?.let {
            this.texts = it.getStringArrayList("texts")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector, container, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.list).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            // TODO :: texts!! !! 제거
            adapter = SelectorAdapter(texts!!).apply {
                setOnItemClickListener(object :
                    SelectorAdapter.OnItemClickListener {
                    override fun onItemClick(v: View, position: Int) {
                        listener?.onClick(position)
                    }
                })
            }
        }
        return view
    }

    fun setSelectorItemClickedCallback(listener: SelectorItemClickedCallback) {
        this.listener = listener
    }
}