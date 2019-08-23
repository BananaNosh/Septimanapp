package com.nobodysapps.septimanapp.activity

import androidx.appcompat.app.AppCompatActivity
import com.nobodysapps.septimanapp.application.SeptimanappApplication

abstract class SeptimanappActivity: AppCompatActivity() {
    fun getSeptimanappApplication() : SeptimanappApplication = (application as SeptimanappApplication)
}