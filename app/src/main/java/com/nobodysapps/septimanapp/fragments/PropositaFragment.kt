package com.nobodysapps.septimanapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.databinding.FragmentPropositaBinding
import com.nobodysapps.septimanapp.localization.localizedDisplayLanguage
import com.nobodysapps.septimanapp.viewModel.PropositaViewModel
import com.nobodysapps.septimanapp.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Shows the proposita (elective course topics) as expandable cards, with a toolbar toggle that
 * switches the whole list between Latin and German, mirroring [HorariumFragment].
 */
class PropositaFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: PropositaViewModel
    private var _binding: FragmentPropositaBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PropositaAdapter
    private var actionToggleLanguageId = -1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PropositaViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropositaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PropositaAdapter(emptyList(), viewModel.propositaLanguage)
        binding.propositaView.layoutManager = LinearLayoutManager(context)
        binding.propositaView.adapter = adapter

        viewModel.proposita.observe(viewLifecycleOwner) { proposita ->
            adapter.setLocale(viewModel.propositaLanguage)
            adapter.setProposita(proposita?.proposita ?: emptyList())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        actionToggleLanguageId = getActionId(menu, actionToggleLanguageId)
        val item = menu.add(Menu.NONE, actionToggleLanguageId, 11, getToggleLanguageActionTitle())
        item?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == actionToggleLanguageId) {
            viewModel.togglePropositaLanguage()
            item.title = getToggleLanguageActionTitle()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getToggleLanguageActionTitle(): String {
        return getString(
            R.string.action_proposita_language,
            localizedDisplayLanguage(context, viewModel.toggledPropositaLocale())
        )
    }

    private fun getActionId(menu: Menu, actionId: Int): Int {
        var id = actionId
        if (id < 0) {
            id = 1
            while (menu.findItem(id) != null) {
                id++
            }
        }
        return id
    }

    companion object {
        @JvmStatic
        fun newInstance() = PropositaFragment()
    }
}
