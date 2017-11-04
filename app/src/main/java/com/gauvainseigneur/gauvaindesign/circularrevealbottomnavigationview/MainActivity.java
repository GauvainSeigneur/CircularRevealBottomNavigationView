package com.gauvainseigneur.gauvaindesign.circularrevealbottomnavigationview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private Resources res;
    private BottomNavigationViewHelper mBottomNavigationViewHelper;
    private boolean mShiftingMode=true;
    private RelativeLayout bottomNavParentlayout;
    private BottomNavigationView mBottomNavigationView;
    private FrameLayout bottomNavBackgroundContainer;
    private View revealBackground;
    private View revealFront;
    private boolean iscolorRevealBackground;
    private static final long ACTIVE_ANIMATION_DURATION_MS = 115L;
    private static final long REVEAL_ANIMATION_DURATION_MS = 445L;
    private static final long TRANSLATE_ANIMATION_DURATION_MS = 227L;
    private int totalNavItems;
    private int activeWidth;
    private int inactiveWidth;
    private int targetWidth;
    private int[] colorNumberarray;
    //for reveal color with shift mode
    private int currentItemSelected;
    private int previousItemSelected;
    private int revealItemPosition;
    private int revealFinalPosition;
    //choose type of colorRevealMode
    private int MULTIPLE_COLOR_REVEAL_MODE=1;
    //Others Views
    private CoordinatorLayout parentLayout;
    //todo :Just for the demo -- to be deleted
    Switch oneRevealActivator;
    Switch keepActiveView;

    View selectorRound;

    int selectedBottomNavItemWidth;
    int unselectedBottomNavItemWidth;
    boolean fromRightToLeft;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        res = getResources();
        initViews();
        initBottomNavigationview(false,false,R.color.colorPrimary);
        //redefinne height of bottom nav to set it under navigation bar
        //only if a soft navigation bar is available
        setBottomNavUnderNavigationbar();
        //set up click listner on bottom navigation menu
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //todo :Just for the demo -- to be deleted
        oneRevealActivator= (Switch) findViewById(R.id.oneRevealActivation);


    }

    /**********************************
     * Initialize Views
     *********************************/
    public void initViews() {
        parentLayout =(CoordinatorLayout) findViewById(R.id.container);
        bottomNavParentlayout = (RelativeLayout) findViewById(R.id.bottom_nav_parent_layout);
        bottomNavBackgroundContainer = (FrameLayout) findViewById(R.id.bottom_nav_background_container);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        revealBackground =findViewById(R.id.navigation_reveal_background);
        revealFront =findViewById(R.id.navigation_reveal_front);
        selectorRound =findViewById(R.id.selector_round);
    }

    /**********************************
     * Initialize BottomNav Behavior
     *********************************/
    //just for the demo
    public void initBottomNavigationview(boolean disableShiftMode,
                                         boolean makeRevealBackgroundAnimation,
                                         int constantBackgroundColor) {
        iscolorRevealBackground = makeRevealBackgroundAnimation;
        mBottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        if (makeRevealBackgroundAnimation) {
            selectorRound.setVisibility(View.GONE);
            revealFront.setVisibility(View.VISIBLE);
            revealFront.setBackgroundColor(ContextCompat.getColor(this, constantBackgroundColor));
            revealBackground.setVisibility(View.VISIBLE);
            revealBackground.setBackgroundColor(ContextCompat.getColor(this, constantBackgroundColor));
        }else {
            selectorRound.setVisibility(View.VISIBLE);
            revealFront.setBackgroundColor(ContextCompat.getColor(this, constantBackgroundColor));
            revealBackground.setVisibility(View.GONE);
        }

        if (disableShiftMode==true) {
            mBottomNavigationViewHelper.disableShiftMode(mBottomNavigationView);
            mShiftingMode=false;
        } else {
            mShiftingMode=true;
        }

    }

    /*************************************
     * Handle revealColor and Menu state
     ************************************/
    /**
     *
     */
    public void retrieveMenuItemDimension() {
        //Retrieve activeWidth && inactivewidth when shiftmode is activated or not
        final int mInactiveItemMaxWidth = res.getDimensionPixelSize(android.support.design.R.dimen.design_bottom_navigation_item_max_width);
        final int mInactiveItemMinWidth = res.getDimensionPixelSize(android.support.design.R.dimen.design_bottom_navigation_item_min_width);
        final int mActiveItemMaxWidth = res.getDimensionPixelSize(android.support.design.R.dimen.design_bottom_navigation_active_item_max_width);
        final int inactiveCount = totalNavItems - 1;
        final int bottomNavWidth = mBottomNavigationView.getResources().getDisplayMetrics().widthPixels;
        totalNavItems = mBottomNavigationView.getMenu().size();
        final int activeMaxAvailable = bottomNavWidth - inactiveCount * mInactiveItemMinWidth;
        activeWidth = Math.min(activeMaxAvailable, mActiveItemMaxWidth);
        final int inactiveMaxAvailable = (bottomNavWidth - activeWidth) / inactiveCount;
        inactiveWidth = Math.min(inactiveMaxAvailable, mInactiveItemMaxWidth);
        targetWidth=inactiveWidth/2;
    }


    /**
     *
     */
    public void findRevealPosition() {
        retrieveMenuItemDimension();
        //find position of selected item when shiftingmode is activated
        if (mShiftingMode==true && totalNavItems>3) {
            if (currentItemSelected == previousItemSelected) {
                revealItemPosition = activeWidth / 2;
                revealFinalPosition = revealItemPosition + (inactiveWidth * currentItemSelected);
            } else {
                revealItemPosition = inactiveWidth / 2;
                if (currentItemSelected < previousItemSelected) {
                    revealFinalPosition = revealItemPosition + (inactiveWidth * currentItemSelected);
                    //item will move from left to right
                    fromRightToLeft=false;
                } else if (currentItemSelected > previousItemSelected) {
                    revealFinalPosition = revealItemPosition + activeWidth + (inactiveWidth * (currentItemSelected - 1));
                    //item will move from left to right
                    fromRightToLeft=true;
                }
            }
            selectedBottomNavItemWidth = activeWidth;
            unselectedBottomNavItemWidth = inactiveWidth;

        } else {
            //if shiftmode is disabled or number of item is under four,
            //the item are equally distribued inside menu
            //we use these method to find the center of each item
            int bottomNavWidth = (getResources().getDisplayMetrics().widthPixels);
            selectedBottomNavItemWidth = ((bottomNavWidth/totalNavItems)); //find width of items
            unselectedBottomNavItemWidth = selectedBottomNavItemWidth;
            double itemTargetCenter = (((selectedBottomNavItemWidth)*(currentItemSelected+1)) -(selectedBottomNavItemWidth/2)); //find center of selected item
            revealFinalPosition = (int) itemTargetCenter;
        }
    }

    /**
     *
     * @param revalColorArray
     * @param pos
     */
    public void setRevealColorAnimationBackground(@Nullable int revalColorArray, final int pos){
        colorNumberarray = context.getResources().getIntArray(revalColorArray);

        if (currentItemSelected != previousItemSelected) {
            if (iscolorRevealBackground) {
                //animation
                colorCircularRevealAnimator(revealFront, targetWidth, pos);
            } else {
                //second animation
                roundedSelectorRevealAnimation(
                        selectorRound,
                        targetWidth,
                        pos, //position for circular reval start
                        selectedBottomNavItemWidth+20,
                        mBottomNavigationView.getHeight()*3, //height
                        (unselectedBottomNavItemWidth*pos)-10, //margin left
                        0 //margin right
                );
            }
        }

    }

    /**
     *
     * @param target
     * @param fromRightToLeft
     */
    public void translateX(View target, Boolean fromRightToLeft) {
        final Animation animation;
        if (fromRightToLeft){
            animation = new TranslateAnimation((unselectedBottomNavItemWidth-30),0,0,0);
        } else {
            animation = new TranslateAnimation(-(unselectedBottomNavItemWidth-30),0,0,0);
        }

        animation.setDuration(TRANSLATE_ANIMATION_DURATION_MS);
        target.setAnimation(animation);

    }


    /**
     *
     * @param viewTarget
     * @param startRevealRadius
     * @return
     */
    private Animator roundedSelectorRevealAnimation (View viewTarget, int startRevealRadius,
                                                     final int pos,
                                                     final int width, final int height,
                                                     final int left, final int right){
        findRevealPosition();
        Animator animator =
                ViewAnimationUtils.createCircularReveal(
                        viewTarget,
                        viewTarget.getWidth()/2,
                        viewTarget.getHeight()/2,
                        startRevealRadius,
                        viewTarget.getWidth()
                );
        //duration : triple of icon animation duration
        animator.setDuration(REVEAL_ANIMATION_DURATION_MS);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                //selectorRound.setBackgroundColor(colorNumberarray[pos]);
                final FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(width, height);
                layoutParams.setMargins(left, 0, right, 0);
                layoutParams.gravity= Gravity.CENTER_VERTICAL;
                selectorRound.setLayoutParams(layoutParams);
                selectorRound.setVisibility(View.VISIBLE);
                if (mShiftingMode==true)
                    translateX(selectorRound,fromRightToLeft);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });


        animator.start();
        return animator;
    }



    /**
     *
     * @param viewTarget
     * @param startRevealRadius
     * @return
     */
    private Animator colorCircularRevealAnimator (View viewTarget, int startRevealRadius, final int pos){
        findRevealPosition();
        float HeightrevealPosition = Utils.convertDpToPixel(56/2,this);
        Animator animator =
                ViewAnimationUtils.createCircularReveal(

                        viewTarget,
                        revealFinalPosition,
                        (int) HeightrevealPosition,
                        startRevealRadius,//0,
                        mBottomNavigationView.getWidth()
                );
        //duration : triple of icon animation duration
        animator.setDuration(REVEAL_ANIMATION_DURATION_MS);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                revealFront.setBackgroundColor(colorNumberarray[pos]);
                if (mShiftingMode==true)
                    translateX(revealFront,fromRightToLeft);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                revealBackground.setBackgroundColor(colorNumberarray[pos]);
            }
        });


        animator.start();
        return animator;
    }

    /**
     *
     * @param item
     */
    private void updateBottomNavMenuState(@NonNull MenuItem item) {
        for (int i=0;i<mBottomNavigationView.getMenu().size();i++){
            if(item==mBottomNavigationView.getMenu().getItem(i)){
                previousItemSelected = currentItemSelected;
                currentItemSelected = i;
                break;
            }
        }
    }

    /***************************************************
     * OnClick Listener for BottomNavigation Menu Item
     ***************************************************/
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            updateBottomNavMenuState(item);
            setRevealColorAnimationBackground(R.array.reveal_bottom_nav_bg, currentItemSelected);
            if (currentItemSelected!=previousItemSelected) {
                //do something if you want like focus on top
            }
            switch (item.getItemId()) {
                case R.id.navigation_movies_tv:
                    return true;
                case R.id.navigation_music:
                    return true;
                case R.id.navigation_book:
                    return true;
                case R.id.navigation_newsstand:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
            }

            return false;
        }

    };

    /**********************************
     * Window Support
     *********************************/
    //this methd is used in addition to the style "AppTheme.BottomNavigationActivit"
    // aplied on this activity in the manifest...
    public void setBottomNavUnderNavigationbar() {
        windowNoLimit();
        if (hasSoftNavBar(this)) {
            // 99% sure there's a navigation bar... does not work every time...
            // so we redefine the height of the bottom navigation View
            float newBottomNavHeight = Utils.convertDpToPixel(48+56,this);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                    bottomNavBackgroundContainer.getLayoutParams();
            params.height = (int) newBottomNavHeight;
            bottomNavBackgroundContainer.setLayoutParams(params);

        } else {
            //do nothing
        }
    }

    //check if the device has a soft navigationbar
    //to draw behinf if it has it
    boolean hasSoftNavBar(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // navigation bar was introduced in Android 4.0 (API level 14)
            Resources resources = context.getResources();
            int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
            if (id > 0) {
                return resources.getBoolean(id);
            } else {
                // Check for keys
                boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
                boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
                return !hasMenuKey && !hasBackKey;
            }
        } else {
            return false;
        }
    }
    public void windowNoLimit () {
        //this value needs to be combined with style to works fine...
        parentLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

    }


}
