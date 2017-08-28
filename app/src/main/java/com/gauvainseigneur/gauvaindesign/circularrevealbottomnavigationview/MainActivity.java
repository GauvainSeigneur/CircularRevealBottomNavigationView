package com.gauvainseigneur.gauvaindesign.circularrevealbottomnavigationview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private BottomNavigationViewHelper mBottomNavigationViewHelper;
    private boolean mShiftingMode=true;
    private BottomNavigationView mBottomNavigationView;
    private View constantBackground;
    private View revealBackground;
    private View revealFront;
    private boolean iscolorRevealBackground;
    private static final long ACTIVE_ANIMATION_DURATION_MS = 115L;
    private int[] colorNumberarray;
    //for reveal color with shift mode
    private int currentItemSelected;
    private int previousItemSelected;
    private int revealItemPosition;
    private int revealFinalPosition;
    private TextView mTextMessage;
    //choose type of colorRevealMode
    private int ONE_COLOR_REVEAL_MODE=0;
    private int MULTIPLE_COLOR_REVEAL_MODE=1;
    public boolean isMultipleColorRevealMode;
    private int constantColor = R.color.colorPrimary;
    //Just for the demo
    Switch oneRevealActivator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        initViews();
        initBottomNavigationview(false,true,MULTIPLE_COLOR_REVEAL_MODE,constantColor);
        //
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
       //Just for the demo -- to be deleted
        oneRevealActivator= (Switch) findViewById(R.id.oneRevealActivation);
        oneRevealActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    initBottomNavigationview(false,true,ONE_COLOR_REVEAL_MODE,constantColor);
                    Log.d("You are :", "Checked");
                }
                else {
                    initBottomNavigationview(false,true,MULTIPLE_COLOR_REVEAL_MODE,constantColor);
                    Log.d("You are :", " Not Checked");
                }
            }
        });

    }

    /**********************************
     * Initialize Views
     *********************************/
    public void initViews() {
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        constantBackground = findViewById(R.id.navigation_constant_background);
        revealBackground =findViewById(R.id.navigation_reveal_background);
        revealFront =findViewById(R.id.navigation_reveal_front);
    }

    /**********************************
     * Initialize BottomNav Behavior
     *********************************/
    public void initBottomNavigationview(boolean disableShiftMode,
                                         boolean makeRevealBackgroundAnimation,
                                         int revealMode,
                                         int constantBackgroundColor) {
        iscolorRevealBackground = makeRevealBackgroundAnimation;
        if (makeRevealBackgroundAnimation==true) {
            revealFront.setVisibility(View.VISIBLE);
            revealFront.setBackgroundColor(ContextCompat.getColor(this, constantBackgroundColor));
            revealBackground.setVisibility(View.VISIBLE);
            revealBackground.setBackgroundColor(ContextCompat.getColor(this,
                    constantBackgroundColor));
            mBottomNavigationView.setBackgroundColor(ContextCompat.getColor(this,
                    android.R.color.transparent));
            if (revealMode==ONE_COLOR_REVEAL_MODE){
                isMultipleColorRevealMode=false;
                constantBackground.setVisibility(View.VISIBLE);
                constantBackground.setBackgroundColor(ContextCompat.getColor(this,
                        constantBackgroundColor));
            } else if (revealMode==MULTIPLE_COLOR_REVEAL_MODE){
                isMultipleColorRevealMode=true;
            }
        }else {
            constantBackground.setVisibility(View.GONE);
            revealFront.setVisibility(View.GONE);
            revealBackground.setVisibility(View.GONE);
            mBottomNavigationView.setBackgroundColor(ContextCompat.getColor(this,
                    constantBackgroundColor));
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
    //Find the position of each item of the BottomNavigationView
    //Even if the shiftMode is activated
    public void findRevealPosition() {
        final Resources res = getResources();
        int mInactiveItemMaxWidth = res.getDimensionPixelSize(
                android.support.design.R.dimen.design_bottom_navigation_item_max_width);
        int mInactiveItemMinWidth = res.getDimensionPixelSize(
                android.support.design.R.dimen.design_bottom_navigation_item_min_width);
        int mActiveItemMaxWidth = res.getDimensionPixelSize(
                android.support.design.R.dimen.design_bottom_navigation_active_item_max_width);

        int totalNavItems =  mBottomNavigationView.getMenu().size();
        int inactiveCount = totalNavItems - 1;
        int width=mBottomNavigationView.getResources().getDisplayMetrics().widthPixels;
        int activeMaxAvailable = width - inactiveCount * mInactiveItemMinWidth;
        final int activeWidth = Math.min(activeMaxAvailable, mActiveItemMaxWidth);
        int inactiveMaxAvailable = (width - activeWidth) / inactiveCount;
        final int inactiveWidth = Math.min(inactiveMaxAvailable, mInactiveItemMaxWidth);

        //find position of selected item when shiftingmode is activated
        if (mShiftingMode==true && totalNavItems>3) {
            if (currentItemSelected == previousItemSelected) {
                revealItemPosition = activeWidth / 2;
                revealFinalPosition = revealItemPosition + (inactiveWidth * currentItemSelected);
            } else {
                revealItemPosition = inactiveWidth / 2;
                if (currentItemSelected < previousItemSelected) {
                    revealFinalPosition = revealItemPosition + (inactiveWidth
                            * currentItemSelected);
                } else if (currentItemSelected > previousItemSelected) {
                    revealFinalPosition = revealItemPosition + activeWidth + (inactiveWidth *
                            (currentItemSelected - 1));
                }

            }

        } else {
            //if shiftmode is disabled or number of item is under four,
            //the item are equally distribued inside menu
            //we use these method to find the center of each item
            int bottomNavWidth = (getResources().getDisplayMetrics().widthPixels);
            int bottomNavItemSize = ((bottomNavWidth/totalNavItems)); //find width of items
            double itemTargetCenter = (((bottomNavItemSize)*(currentItemSelected+1))
                    -(bottomNavItemSize/2)); //find center of selected item
            revealFinalPosition = (int) itemTargetCenter;
        }
    }

    //handle reveal color background on item click
    //Handle of if you use makeMultipleRevealMode or makeOneRevealBackground
    public void setRevealColorBackground(@Nullable int revalColorArray, final int pos){
        findRevealPosition();
        float HeightrevealPosition = Utils.convertDpToPixel(56/2,this);
        colorNumberarray = context.getResources().getIntArray(revalColorArray);
        Animator animatorFront =
                ViewAnimationUtils.createCircularReveal(
                revealFront,
                revealFinalPosition,
                (int) HeightrevealPosition,
                0,
                mBottomNavigationView.getWidth());
        Animator animatorBackground =
                ViewAnimationUtils.createCircularReveal(
                revealBackground,
                revealFinalPosition,
                (int) HeightrevealPosition,
                0,
                mBottomNavigationView.getWidth());;

        if (iscolorRevealBackground && currentItemSelected != previousItemSelected) {
            mBottomNavigationView.setBackgroundColor(ContextCompat.getColor(this,
                    android.R.color.transparent));
            if (isMultipleColorRevealMode) {
                makeMultipleRevealMode(animatorFront, pos);
            } else {
                makeOneRevealBackground(animatorFront,animatorBackground);
            }
        }

    }

    //Used to make different color for each item thanks to a array of color hexa
    private void makeMultipleRevealMode(Animator animator,final int pos){
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                revealFront.setBackgroundColor(colorNumberarray[pos]);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                revealBackground.setBackgroundColor(colorNumberarray[pos]);
            }
        });

        animator.setStartDelay(0);
        animator.setDuration(ACTIVE_ANIMATION_DURATION_MS * 2);
        animator.start();

    }

    //Used to make just one color for reveal
    public void makeOneRevealBackground(Animator animatorRevealFront,
                                        Animator animatorRevealbackground){
        animatorRevealFront.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                revealFront.setBackgroundColor(ContextCompat.getColor(MainActivity.this,
                        constantColor));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });

        animatorRevealFront.setStartDelay(0);
        animatorRevealFront.setDuration(ACTIVE_ANIMATION_DURATION_MS * 3);
        animatorRevealFront.start();


        animatorRevealbackground.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                revealBackground.setBackgroundColor(ContextCompat.getColor(MainActivity.this,
                        R.color.colorWhite));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });

        animatorRevealbackground.setStartDelay(0);
        animatorRevealbackground.setDuration(ACTIVE_ANIMATION_DURATION_MS * 2);
        animatorRevealbackground.start();
    }

    //retrieve the menu state
    //used to define the previous selected item and the current
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
            setRevealColorBackground(R.array.reveal_bottom_nav_bg, currentItemSelected);
            if (currentItemSelected!=previousItemSelected) {
                //do something if you want like focus on top
            }
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
                case R.id.navigation_news:
                    return true;
                case R.id.navigation_account:
                    return true;
            }

            return false;
        }

    };


}
