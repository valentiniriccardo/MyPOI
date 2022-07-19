package com.valentini.mypoi.ui.main

data class MyPlace(var myplace_name: String, var latitude: Double, var longitude: Double) {
    override fun toString(): String {
        return myplace_name
    }
}