package com.nobodysapps.septimanapp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nobodysapps.septimanapp.model.Proposita
import com.nobodysapps.septimanapp.model.storage.PropositaStorage
import java.util.*

class PropositaViewModel(private val propositaStorage: PropositaStorage) : ViewModel() {

    var propositaLanguage: Locale = when (Locale.getDefault()) {
        Locale.GERMAN -> Locale.GERMAN
        else -> Locale("la")
    }
        set(value) {
            if (value != field) {
                field = value
                proposita.value = loadProposita()
            }
            field = value
        }

    var proposita: MutableLiveData<Proposita?> = MutableLiveData(loadProposita())

    fun togglePropositaLanguage() {
        propositaLanguage = toggledPropositaLocale()
    }

    fun toggledPropositaLocale(): Locale {
        val la = Locale("la")
        return when (propositaLanguage) {
            la -> Locale.GERMAN
            else -> la
        }
    }

    private fun loadProposita(): Proposita? {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return propositaStorage.loadProposita(currentYear)
    }
}
