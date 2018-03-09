package com.example.dima.booxchange.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.View
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.example.dima.booxchange.R

class MyLibraryActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
  lateinit var title: EditText
  lateinit var author: EditText
  lateinit var edition: EditText
  lateinit var isbn: EditText
  lateinit var info: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_my_library)

    title = findViewById(R.id.title)
    author = findViewById(R.id.author)
    edition = findViewById(R.id.edition)
    isbn = findViewById(R.id.isbn)
    info = findViewById(R.id.info)

    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)

    val fab = findViewById<FloatingActionButton>(R.id.fab)
    fab.setOnClickListener { view ->
      Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show()
    }

    val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
    val toggle = ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
    drawer.addDrawerListener(toggle)
    toggle.syncState()

    val navigationView = findViewById<NavigationView>(R.id.nav_view)
    navigationView.setNavigationItemSelectedListener(this)
  }

  override fun onBackPressed() {
    val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START)
    } else {
      super.onBackPressed()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.my_library, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId


    return if (id == R.id.action_settings) {
      true
    } else super.onOptionsItemSelected(item)

  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    // Handle navigation view item clicks here.
    val id = item.itemId

    if (id == R.id.profile) {
      val newAct = Intent(this, MyProfileActivity::class.java)
      startActivity(newAct)
    } else if (id == R.id.messages) {

    } else if (id == R.id.library) {
      val newAct = Intent(this, MyLibraryActivity::class.java)
      startActivity(newAct)
    } else if (id == R.id.offers) {

    } else if (id == R.id.settings) {

    } else if (id == R.id.logout) {

    }

    val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
    drawer.closeDrawer(GravityCompat.START)
    return true
  }

  fun AddBook(view: View) {
    val str_title = title.text.toString()
    val str_author = author.text.toString()
    val str_edition = edition.text.toString()
    val str_isbn = isbn.text.toString()
    val str_info = info.text.toString()
    val type = "adding"
//    val backgroundConn = BackgroundConn(this)
//    backgroundConn.execute(type, str_title, str_author, str_edition, str_isbn, str_info)
  }
}
