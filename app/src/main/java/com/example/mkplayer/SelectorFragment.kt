package com.example.mkplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mkplayer.databinding.FragmentSelectorBinding

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
            activity?.supportFragmentManager?.beginTransaction()?.remove(fragment)
                ?.commitAllowingStateLoss()
        }

        private fun getBundle(texts: ArrayList<String>): Bundle {
            val bundle = Bundle()
            bundle.putStringArrayList(BUNDLE_KEY_SELECTOR_TEXTS, texts)
            return bundle
        }
    }

    interface SelectorItemClickedCallback {
        fun onClick(position: Int)
    }

    private val texts = mutableListOf<String>()
    private var listener: SelectorItemClickedCallback? = null
    private lateinit var binding: FragmentSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        args?.let {
            it.getStringArrayList(BUNDLE_KEY_SELECTOR_TEXTS)?.apply {
                texts.addAll(this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectorBinding.inflate(inflater, container, false)
        binding.list.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = SelectorAdapter(texts).apply {
                setOnItemClickListener(object :
                    SelectorAdapter.OnItemClickListener {
                    override fun onItemClick(v: View, position: Int) {
                        listener?.onClick(position)
                    }
                })
            }
        }
        return binding.root
    }

    fun setSelectorItemClickedCallback(listener: SelectorItemClickedCallback) {
        this.listener = listener
    }
}