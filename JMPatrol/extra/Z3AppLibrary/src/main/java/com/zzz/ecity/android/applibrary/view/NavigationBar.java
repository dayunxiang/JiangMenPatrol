package com.zzz.ecity.android.applibrary.view;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.zzz.ecity.android.applibrary.R;
import com.zzz.ecity.android.applibrary.dialog.ButtonCompat;
import com.zzz.ecity.android.applibrary.drawable.PressedEffectStateListDrawable;
import com.zzz.ecity.android.applibrary.utils.ColorUtil;

public class NavigationBar extends RelativeLayout {
	
	private static final int DEFAULT_BUTTON_TEXT_COLOR = 0xFF007AFF;
	private static final int MAX_BUTTON_NUM_LEFT = 4;
	private static final int MAX_BUTTON_NUM_RIGHT = 4;
	private static final int BUTTON_TEXT_SIZE = 14;
	
	private int mMinHeight;
	private int mHorizontalPadding;
	
	private int mNavigationBarStyle = Style.NORMAL;
	
	private LinearLayout mLeftContainer;
	private LinearLayout mRightContainer;
	private LinearLayout mCenterContainer;
	private TitleView mTitleView;
	private TabView mTabView;
	private View mHorizontalDivider;
	
	private int mMaxButtonNumLeft = MAX_BUTTON_NUM_LEFT;
	private int mMaxButtonNumRight = MAX_BUTTON_NUM_RIGHT;
	
	private boolean mIsShown = true;
	
	public static class Style {
		/**
		 * A title (and a subtitle) with this style will be displayed.
		 */
		public static final int NORMAL = 0;
		/**
		 * A TabView contains several TabItems with this style will be displayed.
		 */
		public static final int TAB = 1;
	}
	
	public NavigationBar(Context context) {
		this(context, null);
	}
	
	public NavigationBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public NavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init();
	}

	private void init() {
		if (null == getBackground()) {
			setBackgroundColor(Color.WHITE);
		}
		mMinHeight = getContext().getResources().getDimensionPixelSize(R.dimen.min_navigation_bar_height);
		mHorizontalPadding = getContext().getResources().getDimensionPixelSize(R.dimen.navigation_bar_horizontal_padding);
		setMinimumHeight(mMinHeight);
		initCenterContainer();
		initTitleView();
		initLeftContainer();
		initRightContainer();
		initDivider();
	}
	
	private void initCenterContainer() {
		mCenterContainer = new LinearLayout(getContext());
		mCenterContainer.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(mCenterContainer, params);
	}
	
	private void initLeftContainer() {
		mLeftContainer = new LinearLayout(getContext());
		mLeftContainer.setGravity(Gravity.CENTER_VERTICAL);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.MATCH_PARENT);
		params.leftMargin = getContext().getResources().getDimensionPixelSize(R.dimen.navigation_bar_margin);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		addView(mLeftContainer, params);
	}
	
	private void initRightContainer() {
		mRightContainer = new LinearLayout(getContext());
		mRightContainer.setGravity(Gravity.CENTER_VERTICAL);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.MATCH_PARENT);
		params.rightMargin = getContext().getResources().getDimensionPixelSize(R.dimen.navigation_bar_margin);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		addView(mRightContainer, params);
	}
	
	private void initDivider() {
		mHorizontalDivider = new View(getContext());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				1);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		addView(mHorizontalDivider, params);
	}
	
	/**
	 * Set the NavigationBar style.
	 * 
	 * @param style See {@link NavigationBar.Style}
	 */
	public void setNavigationBarStyle(int style) {
		if (mNavigationBarStyle != style) {
			mNavigationBarStyle = style;
			switch (style) {
			case Style.NORMAL:
				initTitleView();
				break;

			case Style.TAB:
				initTabView();
				break;
			}
		}
	}
	
	public int getNavigationBarStyle() {
		return mNavigationBarStyle;
	}
	
	/**
	 * Add a button with text and drawable on the left.
	 * 
	 * @param text
	 * @param drawable
	 * @param l
	 * @return
	 */
	public Button addLeftButton(CharSequence text, Drawable drawable, View.OnClickListener l) {
		return addLeftButton(text, drawable, DEFAULT_BUTTON_TEXT_COLOR, l);
	}
	
	/**
	 * Add a button with text and drawable on the right.
	 * 
	 * @param text
	 * @param drawable
	 * @param l
	 * @return
	 */
	public Button addRightButton(CharSequence text, Drawable drawable, View.OnClickListener l) {
		return addRightButton(text, drawable, DEFAULT_BUTTON_TEXT_COLOR, l);
	}
	
	/**
	 * Set the max number of left buttons. Default is 3.
	 * 
	 * @param num
	 */
	public void setMaxButtonNumLeft(int num) {
		mMaxButtonNumLeft = num;
	}
	
	/**
	 * Set the max number of right buttons. Default is 3.
	 * 
	 * @param num
	 */
	public void setMaxButtonNumRight(int num) {
		mMaxButtonNumRight = num;
	}
	
	public Button addLeftButton(CharSequence text, Drawable drawable, int buttonTextColor, View.OnClickListener l) {
		int num = mLeftContainer.getChildCount();
		if (num >= mMaxButtonNumLeft) {
			throw new RuntimeException("The number of left navigation buttons can not be more than " + mMaxButtonNumLeft);
		}
		
		Button leftButton = createNavigationButton(text, drawable, buttonTextColor, l);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(mHorizontalPadding, 0, mHorizontalPadding, 0);
		mLeftContainer.addView(leftButton, params);
		return leftButton;
	}
	
	public Button addRightButton(CharSequence text, Drawable drawable, int buttonTextColor, View.OnClickListener l) {
		int num = mRightContainer.getChildCount();
		if (num >= mMaxButtonNumRight) {
			throw new RuntimeException("The number of right navigation buttons can not be more than " + mMaxButtonNumRight);
		}
		
		Button rightButton = createNavigationButton(text, drawable, buttonTextColor, l);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(mHorizontalPadding, 0, mHorizontalPadding, 0);
		mRightContainer.addView(rightButton, 0, params);
		return rightButton;
	}
	
	public void removeLeftButtons() {
		if (mLeftContainer != null) {
			mLeftContainer.removeAllViews();
		}
	}
	
	public void removeRightButtons() {
		if (mRightContainer != null) {
			mRightContainer.removeAllViews();
		}
	}
	
	public void removeAllButtons() {
		removeLeftButtons();
		removeRightButtons();
	}
	
	private Button createNavigationButton(CharSequence text, Drawable drawable, int buttonTextColor, View.OnClickListener l) {
		int buttonTextColorPressed = buttonTextColor - 0x88000000;
		ColorStateList colorStateList = ColorUtil.createColorStateList(buttonTextColor, buttonTextColorPressed);
		
		Button button = new Button(getContext(), null, 0);
		button.setBackgroundColor(Color.TRANSPARENT);
		button.setPadding(0, 0, 0, 0);
		button.setText(text);
		button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);
		button.setTextColor(colorStateList);
		button.setGravity(Gravity.CENTER);
		ButtonCompat.setAllCaps(button, false);
		
		if (drawable != null) {
			Drawable stateListDrawable = new PressedEffectStateListDrawable(drawable, buttonTextColor, buttonTextColorPressed);
			stateListDrawable.setBounds(0, 0, mMinHeight / 2, mMinHeight / 2);
			button.setCompoundDrawables(stateListDrawable, null, null, null);
		}
		button.setOnClickListener(l);
		return button;
	}
	
	private void initTitleView() {
		mTitleView = new TitleView(getContext());
		mCenterContainer.removeAllViews();
		mCenterContainer.addView(mTitleView);
		setTitleTextColor(DEFAULT_BUTTON_TEXT_COLOR);
	}
	
	public void setTitle(CharSequence text) {
		if (mTitleView != null) {
			mTitleView.setTitleText(text.toString());
		}
	}
	
	public void setSubTitle(CharSequence text) {
		if (mTitleView != null) {
			//mTitleView.setSubTitle(text);
		}
	}
	
	public void setTitleTextColor(int color) {
		if (mTitleView != null) {
			//mTitleView.setTextColor(color);
		}
	}
	
	private void initTabView() {
		mTabView = new TabView(getContext());
		mCenterContainer.removeAllViews();
		mCenterContainer.addView(mTabView);
	}
	
	public void setTabs(String[] titles, TabView.OnTabCheckedListener l,int defualtSelected) {
		setTabs(titles, Color.WHITE, DEFAULT_BUTTON_TEXT_COLOR, l,defualtSelected);
	}
	
	/**
	 * Set tabs after {@link setNavigationBarStyle(NavigationBar.Style.TAB)} has been called.
	 * @param titles
	 * @param uncheckedColor
	 * @param checkedColor
	 * @param l
	 * @param selectedIndex
	 */
	public void setTabs(String[] titles, int uncheckedColor, int checkedColor, TabView.OnTabCheckedListener l,int selectedIndex) {
		mTabView.setTabs(titles, uncheckedColor, checkedColor, l,selectedIndex);
	}
	
	public void setTabSizeFixed(boolean fixed) {
		mTabView.setFixedSize(fixed);
	}
	/**
	 * Set custom view to replace the title or tab.
	 * 
	 * @param v
	 * 		Custom view
	 */
	public void setCustomView(View v) {
		mCenterContainer.removeAllViews();
		mCenterContainer.addView(v);
	}
	
	public void hide() {
		ValueAnimator dismissAnimator = ValueAnimator.ofInt(mMinHeight, 0);
		dismissAnimator.setDuration(300);
		dismissAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				ViewGroup.LayoutParams params = getLayoutParams();
				params.height = (Integer) animator.getAnimatedValue();
				setLayoutParams(params);
			}
		});
		dismissAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {
				mIsShown = false;
			}

			@Override
			public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
				mIsShown = false;
			}
			
		});
		dismissAnimator.start();
	}
	
	public void show() {
		ValueAnimator appearAnimator = ValueAnimator.ofInt(0, mMinHeight);
		appearAnimator.setDuration(200);
		appearAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				ViewGroup.LayoutParams params = getLayoutParams();
				params.height = (Integer) animator.getAnimatedValue();
				setLayoutParams(params);
			}
		});
		appearAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationCancel(com.nineoldandroids.animation.Animator animation) {
				mIsShown = true;
			}

			@Override
			public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
				mIsShown = true;
			}
			
		});
		appearAnimator.start();
	}
	
	public boolean isShown() {
		return mIsShown;
	}
	
	public void setDividerColor(int color) {
		mHorizontalDivider.setBackgroundColor(color);
	}
	
}
