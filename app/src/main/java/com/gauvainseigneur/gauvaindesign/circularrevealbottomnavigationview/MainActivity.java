package com.gauvainseigneur.gauvaindesign.circularrevealbottomnavigationview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationMenu;
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
import android.widget.RadioGroup;
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
    //view used for reveal animation
    private View revealBackground;
    private View revealFront;
    private View selectorRound;
    private boolean iscolorRevealBackground;
    private static final long ACTIVE_ANIMATION_DURATION_MS = 115L;
    private static final long REVEAL_ANIMATION_DURATION_MS = 445L;
    private static final long TRANSLATE_ANIMATION_DURATION_MS = 300L;
    //int for menu item dimension
    private int[] mTempChildWidths;
    private int mSelectedItemPosition = 0;
    int childWidth;
    int extra;
    private int totalNavItems;
    private int activeWidth;
    private int inactiveWidth;
    //private int targetWidth;
    private int[] colorNumberarray;
    //for reveal color with shift mode
    private int currentItemSelected;
    private int previousItemSelected;
    private int revealItemPosition;
    private int revealFinalPosition;
    boolean fromRightToLeft;
    //Others Views
    private CoordinatorLayout parentLayout;

    int REVEAL_COLOR_MODE = 0;
    int REVEAL_SELECTOR_MODE = 1;

    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        res = getResources();
        initViews();
        //redefinne height of bottom nav to set it under navigation bar
        //only if a soft navigation bar is available
        initBottomNavigationview(REVEAL_COLOR_MODE,R.color.bottomNavColor_1);
        setBottomNavUnderNavigationbar();
        //set up click listner on bottom navigation menu
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //todo :Just for the demo -- to be deleted
        radioGroup = (RadioGroup) findViewById(R.id.radio_group_choice);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.colorRevealRadio) {
                    initBottomNavigationview(REVEAL_COLOR_MODE,R.color.bottomNavColor_1);
                } else {
                    initBottomNavigationview(REVEAL_SELECTOR_MODE,R.color.colorPrimary);
                }
            }

        });

        Switch switchShiftMode = (Switch) findViewById(R.id.shiftMode_activator);
        switchShiftMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    disableShiftMode(false);
                    //reinitializie bottom nav after disable or not shiftmode
                    radioGroup.check(radioGroup.getCheckedRadioButtonId());
                } else {
                    disableShiftMode(true);
                    radioGroup.check(radioGroup.getCheckedRadioButtonId());
                }
            }
        });

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
    public void initBottomNavigationview(int makeRevealBackgroundAnimation,
                                         int constantBackgroundColor) {
        mBottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        if (makeRevealBackgroundAnimation == REVEAL_COLOR_MODE) {
            iscolorRevealBackground = true;
            selectorRound.setVisibility(View.GONE);
            revealFront.setVisibility(View.VISIBLE);
            revealFront.setBackgroundColor(ContextCompat.getColor(this, constantBackgroundColor));
            revealBackground.setVisibility(View.VISIBLE);
            revealBackground.setBackgroundColor(ContextCompat.getColor(this, constantBackgroundColor));
        }else {
            iscolorRevealBackground = false;
            selectorRound.setVisibility(View.VISIBLE);
            revealFront.setBackgroundColor(ContextCompat.getColor(this, constantBackgroundColor));
            revealBackground.setVisibility(View.GONE);
        }

        mTempChildWidths = new int[BottomNavigationMenu.MAX_ITEM_COUNT];

    }
    public void disableShiftMode(boolean disableShiftMode){
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
     *Retrieve menu item dimension in their different states
     *Active & Inactive
     *
     * After that find Reveal position named revealFinalPosition
     */
    public void findRevealPosition() {
        //number of item of the menu
        totalNavItems = mBottomNavigationView.getMenu().size();
        //Retrieve activeWidth && inactivewidth when shiftmode is activated or not
        final int mInactiveItemMaxWidth = res.getDimensionPixelSize(android.support.design.R.dimen.design_bottom_navigation_item_max_width);
        final int mInactiveItemMinWidth = res.getDimensionPixelSize(android.support.design.R.dimen.design_bottom_navigation_item_min_width);
        final int mActiveItemMaxWidth = res.getDimensionPixelSize(android.support.design.R.dimen.design_bottom_navigation_active_item_max_width);
        //width of BottomNavigation
        final int width = mBottomNavigationView.getResources().getDisplayMetrics().widthPixels;
        final int count = mBottomNavigationView.getMenu().size();

        if (mShiftingMode) {
            //STEP 1: retrieve menu item dimension in all state if shiftmode is active
            final int inactiveCount = totalNavItems - 1;
            final int activeMaxAvailable = width - inactiveCount * mInactiveItemMinWidth;
            activeWidth = Math.min(activeMaxAvailable, mActiveItemMaxWidth);
            final int inactiveMaxAvailable = (width - activeWidth) / inactiveCount;
            inactiveWidth = Math.min(inactiveMaxAvailable, mInactiveItemMaxWidth);
            extra = width - activeWidth - inactiveWidth * inactiveCount;
            for (int i = 0; i < count; i++) {
                mTempChildWidths[i] = (i == mSelectedItemPosition) ? activeWidth : inactiveWidth;
                if (extra > 0) {
                    mTempChildWidths[i]++;
                    extra--;
                }
            }
            //STEP 2: define reveal position according to menu item dimensions
            if (totalNavItems>3) {
                if (currentItemSelected == previousItemSelected) {
                    revealItemPosition = activeWidth / 2;
                    revealFinalPosition = revealItemPosition + (inactiveWidth * currentItemSelected);
                } else {
                    revealItemPosition = inactiveWidth / 2;
                    if (currentItemSelected < previousItemSelected) {
                        revealFinalPosition = revealItemPosition + (inactiveWidth * currentItemSelected);
                        //item will move from left to right
                        fromRightToLeft = false;
                    } else if (currentItemSelected > previousItemSelected) {
                        revealFinalPosition = revealItemPosition + activeWidth + (inactiveWidth * (currentItemSelected - 1));
                        //item will move from left to right
                        fromRightToLeft = true;
                    }
                }

            }

        }

        else {
            //STEP 1: Retrieve menu item dimension in all state if shiftmode is unactive
            final int maxAvailable = width / (count == 0 ? 1 : count);
            childWidth = Math.min(maxAvailable, mActiveItemMaxWidth);
            inactiveWidth = childWidth;
            activeWidth = childWidth;
            extra = width - childWidth * count;
            for (int i = 0; i < count; i++) {
                mTempChildWidths[i] = childWidth;
                if (extra > 0) {
                    mTempChildWidths[i]++;
                    extra--;
                }
            }

            //STEP 2: define reveal position according to menu item dimensions
            double itemTargetCenter = (((childWidth)*(currentItemSelected+1)) -(childWidth/2)); //find center of selected item
            revealFinalPosition = (int) itemTargetCenter;

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
            animation = new TranslateAnimation((inactiveWidth-20),0,0,0);
        } else {
            animation = new TranslateAnimation(-(inactiveWidth-20),0,0,0);
        }

        animation.setDuration(TRANSLATE_ANIMATION_DURATION_MS);
        //animation.setInterpolator(new LinearOutSlowInInterpolator());
        target.setAnimation(animation);

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
                colorCircularRevealAnimator(revealFront, inactiveWidth-20, pos); //targetWidth
            } else {
                //second animation
                roundedSelectorRevealAnimation(
                        selectorRound,
                        inactiveWidth-20, //targetWidth
                        pos, //position for circular reval start
                        activeWidth+20,
                        mBottomNavigationView.getHeight()*3, //height
                        (inactiveWidth*pos)-10, //margin left
                        0 //margin right
                );
            }
        }

    }

    /**
     *
     * @param viewTarget
     * @param startRevealRadius
     * @return
     */
    private Animator roundedSelectorRevealAnimation (View viewTarget,
                                                     int startRevealRadius,
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
