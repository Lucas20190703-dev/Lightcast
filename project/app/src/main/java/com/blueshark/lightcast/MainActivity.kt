package com.blueshark.lightcast

import android.app.ActivityOptions
import android.app.SearchManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.ui.setupActionBarWithNavController
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.TopPodcast
import com.blueshark.lightcast.networking.logoutAccount
import com.blueshark.lightcast.service.MusicPlayerRemote
import com.blueshark.lightcast.ui.bookmark.BookmarkFragmentDirections
import com.blueshark.lightcast.ui.component.slidinguppanel.SlidingUpPanelLayout
import com.blueshark.lightcast.ui.episodeoverview.EpisodeOverviewFragmentDirections
import com.blueshark.lightcast.ui.inprogress.InProgressFragmentDirections
import com.blueshark.lightcast.ui.listenlater.ListenLaterFragmentDirections
import com.blueshark.lightcast.ui.newrelease.NewReleaseFragmentDirections
import com.blueshark.lightcast.ui.overview.PodcastOverviewFragmentDirections
import com.blueshark.lightcast.ui.player.QuickControlFragment
import com.blueshark.lightcast.ui.search.SearchSuggestFragment
import com.blueshark.lightcast.ui.splash.SplashActivity
import com.blueshark.lightcast.ui.subscription.SubscriptionFragmentDirections
import com.blueshark.lightcast.utils.PreferenceUtil
import com.blueshark.lightcast.utils.SearchViewFormatter
import com.blueshark.lightcast.utils.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.simplemobiletools.commons.extensions.hideKeyboard
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.custom_toolbar.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Reader


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{

    private var currentNavController: LiveData<NavController>? = null
    private var currentFragmentName : String? = null

    private lateinit var mToolbar : Toolbar

    private lateinit var mSearchButton : ImageButton
    private lateinit var mSearchView : SearchView

    private lateinit var mDrawerToggle : ActionBarDrawerToggle

    private lateinit var panelLayout : SlidingUpPanelLayout

    private lateinit var mMiniControlFrameLayout : FrameLayout

    private lateinit var mSearchSuggestLayout: FrameLayout
    private lateinit var mSearchSuggestFragment: SearchSuggestFragment

    private var timer : CountDownTimer? = null
    private var prevQuery : String? = null
    private lateinit var searchItem : MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            setupToolbarAndBottomNav()
        }
        panelLayout = findViewById(R.id.sliding_layout)
        setPanelSlideListeners(panelLayout)

        mMiniControlFrameLayout = findViewById(R.id.quickcontrols_container)
        mSearchSuggestLayout = findViewById(R.id.search_suggester_container)

        GlobalScope.launch {
            delay(200)
            val fragment1 = QuickControlFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.quickcontrols_container, fragment1).commit()
        }

        GlobalScope.launch {
            delay(200)
            mSearchSuggestFragment = SearchSuggestFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.search_suggester_container, mSearchSuggestFragment).commitAllowingStateLoss()
        }
    }

    override fun onResume() {
        super.onResume()
        mSearchSuggestLayout.visibility = View.GONE
        val isShowMiniController = PreferenceUtil.getInstance().showMiniController
        val playState = PreferenceUtil.getInstance().playingState

        if (playState != "idle") {
            showCastMiniController()
        } else {
            hideCastMiniController()
        }
    }
    private fun setupToolbarAndBottomNav() {
        mToolbar = findViewById(R.id.toolbar)

        mainDrawerNavigationView.setNavigationItemSelectedListener(this)

        val hView = mainDrawerNavigationView.getHeaderView(0)
        val navUser = hView.findViewById(R.id.nav_header_user_name) as TextView
        val navEmail = hView.findViewById(R.id.nav_header_email) as TextView
        val userInfo = PreferenceUtil.getInstance().userInformation
        navUser.text = userInfo.username
        navEmail.text = userInfo.email
        setupBottomNavigationBar()
        mDrawerToggle = ActionBarDrawerToggle(this, mainDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mainDrawerLayout.addDrawerListener(mDrawerToggle)
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.mainBottomNavigationView)

        val navGraphIds = listOf(R.navigation.nav_release, R.navigation.nav_in_progress,
            R.navigation.nav_subscription, R.navigation.nav_listen_later,
            R.navigation.nav_bookmark)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.mainFragmentContainer,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
            setSupportActionBar(mToolbar)
            setupActionBarWithNavController(navController)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowTitleEnabled(false)
                setDisplayShowCustomEnabled(true)
            }
        })
        currentNavController = controller
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        when (currentFragmentName) {
            getString(R.string.search_podcast_overview), getString(R.string.search_episode_overview), getString(R.string.search) -> {

            }
            else -> {
                menuInflater.inflate(R.menu.main, menu)
                searchItem = menu.findItem(R.id.action_search)
                onSetupSearchView()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home && mDrawerToggle.isDrawerIndicatorEnabled) {
            if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mainDrawerLayout.closeDrawer(GravityCompat.START)
            } else {
                mainDrawerLayout.openDrawer(GravityCompat.START)
            }

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupToolbarAndBottomNav()
    }

    fun exitApp() { //To exit the application call this function from fragment
        this.finishAffinity()
    }

    override fun onSupportNavigateUp(): Boolean { //Setup appBarConfiguration for back arrow
        return currentNavController?.value?.navigateUp() ?: false
    }

    override fun onBackPressed() {
        when { //If drawer layout is open close that on back pressed
            mainDrawerLayout.isDrawerOpen(GravityCompat.START) -> {
                mainDrawerLayout.closeDrawer(GravityCompat.START)
            }
            else -> {
                //super.onBackPressed() //If drawer is already in closed condition then go back
                currentNavController?.value?.navigateUp()
            }
        }
    }

    private fun onSetupSearchView() {
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = getString(R.string.search_placeholder)
        searchView.findViewById<AutoCompleteTextView>(R.id.search_src_text).threshold = 1
        searchView.setPadding(0, 15, 0, 15)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideKeyboard()
                gotoSearchFragment(query)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query == null || query.isEmpty()) {
                    mSearchSuggestLayout.visibility = View.GONE
                    prevQuery = query
                } else {
                    mSearchSuggestLayout.visibility = View.VISIBLE
                    if (prevQuery != query) {
                        prevQuery = query
                        timer?.cancel()
                        timer = object : CountDownTimer(500, 250) {
                            override fun onTick(millisUntilFinished: Long) {

                            }
                            override fun onFinish() {
                                mSearchSuggestFragment.onQuery(1, query)
                            }
                        }
                        timer?.start()
                    }
                }
                return false
            }
        })

        searchView.setOnCloseListener {
            if (searchItem.isActionViewExpanded) {
                searchItem.collapseActionView()
            }
            mSearchSuggestFragment.initialize()
            mSearchSuggestLayout.visibility = View.GONE
            false
        }

//        searchView.setOnSuggestionListener(object: SearchView.OnSuggestionListener {
//            override fun onSuggestionSelect(position: Int): Boolean {
//                return false
//            }
//
//            override fun onSuggestionClick(position: Int): Boolean {
//                hideKeyboard()
//                val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
//                val selection = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
//                searchView.setQuery(selection, false)
//
//                // Do something with selection
//                return true
//            }
//        })

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        SearchViewFormatter()
            .setSearchBackGroundResource(R.drawable.search_view_background)
            .setSearchIconResource(R.drawable.ic_search_white_24dp, inside = false, outside = true)
            .setSearchTextColorResource(R.color.color_text_dark)
            .setSearchHintColorResource(R.color.colorPrimary)
            .setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
            .setTextViewPadding(40, 0, 0, 10 )
            .setSearchCloseIconResource(R.drawable.ic_search_clear_24dp)
            .format(searchView)
    }

    fun toggleActionBarAndBottomNavigation(visible : Boolean = true) {
        when (visible) {
            true -> {
                mainBottomNavigationView.animate()
                    .translationY(0f)
                    .setInterpolator(LinearInterpolator())
                    .setDuration(200)
                    .start()

//                mainAppBarLayout.animate()
//                    .translationY(0f)
//                    .setInterpolator(LinearInterpolator())
//                    .setDuration(200)
//                    .start()

            }
            else -> {
                mainBottomNavigationView.animate()
                    .translationY(mainBottomNavigationView.height.toFloat())
                    .setInterpolator(LinearInterpolator())
                    .setDuration(200)
                    .start()
//                mainAppBarLayout.animate()
//                    .translationY(-mainAppBarLayout.height.toFloat())
//                    .setInterpolator(LinearInterpolator())
//                    .setDuration(200)
//                    .start()
            }
        }
    }

    fun toggleBottomNavigation(visible: Boolean) {
        when (visible) {
            true -> {
                mainBottomNavigationView.animate()
                    .translationY(0f)
                    .setInterpolator(LinearInterpolator())
                    .setDuration(20)
                    .start()
            }
            else -> {
                mainBottomNavigationView.animate()
                    .translationY(mainBottomNavigationView.height.toFloat())
                    .setInterpolator(LinearInterpolator())
                    .setDuration(20)
                    .start()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_collection -> {
                Toast.makeText(this, "Collections", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_cart -> {
                Toast.makeText(this, "Cart", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_profile -> {
                Toast.makeText(this, "Public Profile", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_account -> {
                Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        mainDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        if(MusicPlayerRemote.isServiceConnected()) {
            MusicPlayerRemote.quit()
        }

        logoutAccount(object: RequestDataReceiver {
            override fun onDataReceived(jsonString : String) {
                val intent = Intent(this@MainActivity, SplashActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent, options.toBundle())
                finish()
            }
            override fun onDataReceivedStream(reader : Reader) {

            }
            override fun onFailed(t: Throwable?) {
                showSnakbar("Logout failed.")
            }
        })

    }

    fun setDrawerEnabled(enabled: Boolean) {
//        val lockMode =
//            if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
//        mainDrawerLayout.setDrawerLockMode(lockMode)
        mDrawerToggle.isDrawerIndicatorEnabled = enabled
    }


    fun showToast(text : String) {
        runOnUiThread {
            val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    override fun showSnakbar(text : String) {
        runOnUiThread {
            Snackbar.make(findViewById(R.id.contentCoordinatorLayout), text, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
        }
    }

    override fun showCastMiniController() {
        mMiniControlFrameLayout.visibility = View.VISIBLE
        panelLayout.showPanel()
        PreferenceUtil.getInstance().showMiniController = true
    }

    override fun hideCastMiniController() {
        mMiniControlFrameLayout.visibility = View.GONE
        panelLayout.hidePanel()
        PreferenceUtil.getInstance().playingState = "idle"
        PreferenceUtil.getInstance().showMiniController = false
        PreferenceUtil.getInstance().playingSpeedIndex = 2
    }

    private fun setPanelSlideListeners(panelLayout: SlidingUpPanelLayout) {
        panelLayout.setPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(
                panel: View?,
                slideOffset: Float
            ) {
                val nowPlayingCard: View? = QuickControlFragment.topContainer
                nowPlayingCard?.visibility = View.VISIBLE
                nowPlayingCard?.alpha = 1 - slideOffset
            }

            override fun onPanelCollapsed(panel: View?) {
                val nowPlayingCard: View? = QuickControlFragment.topContainer
                nowPlayingCard?.alpha = 1f
                nowPlayingCard?.visibility = View.VISIBLE
            }

            override fun onPanelExpanded(panel: View?) {
                val nowPlayingCard: View? = QuickControlFragment.topContainer
                nowPlayingCard?.alpha = 0f
                nowPlayingCard?.visibility = View.GONE
            }

            override fun onPanelAnchored(panel: View?) {}
            override fun onPanelHidden(panel: View?) {}
        })
    }

    fun setupToolbar(homeIcon : Int?, title : String) {
        currentFragmentName = title
        when (currentFragmentName) {
            getString(R.string.search_podcast_overview) -> mToolbar.toolbarTitle.text = getString(R.string.podcast_overview)
            getString(R.string.search_episode_overview) -> mToolbar.toolbarTitle.text = getString(R.string.episode_overview)
            else -> {
                mToolbar.toolbarTitle.text = title
            }
        }
        if (homeIcon != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mToolbar.navigationIcon = getDrawable(homeIcon)
            } else {
                mToolbar.navigationIcon = resources.getDrawable(homeIcon)
            }
        }
    }

    fun navigateSearchPodcast(podcast : TopPodcast) {
        if (searchItem.isActionViewExpanded) {
            searchItem.collapseActionView()
        }
        mSearchSuggestLayout.visibility = View.GONE
        val action : NavDirections? = when (currentFragmentName) {
            getString(R.string.new_release) -> {
                NewReleaseFragmentDirections.actionNewReleaseToPodcastOverview(podcast.podcastId)
            }
            getString(R.string.in_progress) -> {
                InProgressFragmentDirections.actionInProgressToPodcastOverview(podcast.podcastId)
            }
            getString(R.string.subscriptions) -> {
                SubscriptionFragmentDirections.actionSubscriptionToPodcastOverview(podcast.podcastId)
            }
            getString(R.string.listen_later) -> {
                ListenLaterFragmentDirections.actionListenLaterToPodcastOverview(podcast.podcastId)
            }
            getString(R.string.bookmark) -> {
                BookmarkFragmentDirections.actionBookmarkToPodcastOverview(podcast.podcastId)
            }
            getString(R.string.podcast_overview) -> {
                PodcastOverviewFragmentDirections.actionPodcastToPodcastOverview(podcast.podcastId)
            }
            getString(R.string.episode_overview) -> {
                EpisodeOverviewFragmentDirections.actionEpisodeToPodcastOverview(podcast.podcastId)
            }
            else -> {
                null
            }
        }
        if (action != null) {
            currentNavController?.value?.navigate(action)
        }
    }

    private fun gotoSearchFragment(q: String?) {
        if (searchItem.isActionViewExpanded) {
            searchItem.collapseActionView()
        }
        mSearchSuggestLayout.visibility = View.GONE
        val query = q ?: ""
        val action : NavDirections? = when (currentFragmentName) {
            getString(R.string.new_release) -> {
                NewReleaseFragmentDirections.actionNewReleaseToSearchFragment(query)
            }
            getString(R.string.in_progress) -> {
                InProgressFragmentDirections.actionInProgressToSearchFragment(query)
            }
            getString(R.string.subscriptions) -> {
                SubscriptionFragmentDirections.actionSubscriptionToSearchFragment(query)
            }
            getString(R.string.listen_later) -> {
                ListenLaterFragmentDirections.actionListenLaterToSearchFragment(query)
            }
            getString(R.string.bookmark) -> {
                BookmarkFragmentDirections.actionBookmarkToSearchFragment(query)
            }
            getString(R.string.podcast_overview) -> {
                PodcastOverviewFragmentDirections.actionPodcastToSearchFragment(query)
            }
            getString(R.string.episode_overview) -> {
                EpisodeOverviewFragmentDirections.actionEpisodeToSearchFragment(query)
            }
            else -> {
                null
            }
        }
        if (action != null) {
            currentNavController?.value?.navigate(action)
        }
    }


    companion object {
    }
}
