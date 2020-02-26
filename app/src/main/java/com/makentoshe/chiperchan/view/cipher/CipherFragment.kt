package com.makentoshe.chiperchan.view.cipher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.makentoshe.chiperchan.R
import com.makentoshe.chiperchan.common.ui.ParameterUi
import com.makentoshe.chiperchan.model.Cipher
import com.makentoshe.chiperchan.ui.cipher.CipherFragmentUi
import toothpick.ktp.delegate.inject

class CipherFragment : Fragment() {

    val arguments = Arguments(this)
    private val cipherFactory by inject<Cipher.Factory>()
    private var action: Action = Action.Encode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) return
        action = savedInstanceState.get(Action::class.java.simpleName) as Action
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return CipherFragmentUi(cipherFactory).create(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val encodeButton = view.findViewById<Button>(R.id.cipher_fragment_encode)
        val decodeButton = view.findViewById<Button>(R.id.cipher_fragment_decode)
        val inputLayout = view.findViewById<TextInputLayout>(R.id.cipher_fragment_input)

        encodeButton.setOnClickListener {
            encodeButton.isEnabled = false
            decodeButton.isEnabled = true
            action = Action.Encode
            inputLayout.hint = getString(R.string.encode)
        }

        decodeButton.setOnClickListener {
            decodeButton.isEnabled = false
            encodeButton.isEnabled = true
            action = Action.Decode
            inputLayout.hint = getString(R.string.decode)
        }

        when (action) {
            is Action.Encode -> encodeButton.performClick()
            is Action.Decode -> decodeButton.performClick()
        }

        val container = view.findViewById<ViewGroup>(R.id.cipher_fragment_container)
        val parameterUi = ParameterUi(container)
        val parameters = cipherFactory.getParameters()
        parameters.forEach { parameter ->
            parameterUi.createView(requireContext(), parameter).also(container::addView)
        }

        inputLayout?.editText?.addTextChangedListener {
            val sas = parameters.map { parameter ->
                val view = container.children.find { it.id == parameter.viewId }
                when (parameter.spec.type) {
                    Cipher.Type.Int -> {
                        parameter.name to (view as TextInputLayout).editText?.text?.toString()?.toInt()
                    }
                    Cipher.Type.String -> {
                        parameter.name to (view as TextInputLayout).editText?.text?.toString()
                    }
                    Cipher.Type.Boolean -> {
                        parameter.name to "null"
                    }
                }
            }

            println(sas)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(Action::class.java.simpleName, action)
    }

    class Factory {
        fun build(cipherTitle: String): CipherFragment {
            val fragment = CipherFragment()
            fragment.arguments.cipherTitle = cipherTitle
            return fragment
        }
    }

    class Arguments(private val cipherFragment: CipherFragment) {

        init {
            val fragment = cipherFragment as Fragment
            if (fragment.arguments == null) {
                fragment.arguments = Bundle()
            }
        }

        private val fragmentArguments: Bundle
            get() = cipherFragment.requireArguments()

        var cipherTitle: String
            set(value) = fragmentArguments.putString(TITLE, value)
            get() = fragmentArguments.getString(TITLE)!!

        companion object {
            private const val TITLE = "Title"
        }
    }
}