/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times;

import java.util.Random;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.widget.ImageView;

/**
 * Flicker animation for 1 candle.
 * 
 * @author Moshe W
 */
public class CandleAnimation implements Runnable {

	private static final int LEVELS = 6;
	private static final int PERIOD = (int) (DateUtils.SECOND_IN_MILLIS >> 1);

	private final Handler mHandler;
	private final Drawable mCandle;
	private int mSpriteIndex;
	/** Randomizer. */
	private final Random mRandom;

	/**
	 * Create a new animation.
	 * 
	 * @param handler
	 *            the timer.
	 * @param view
	 *            the image view.
	 */
	public CandleAnimation(Handler handler, ImageView view) {
		this(handler, view, null);
	}

	/**
	 * Create a new animation.
	 * 
	 * @param handler
	 *            the timer.
	 * @param view
	 *            the image view.
	 * @param random
	 *            the delay randomizer.
	 */
	public CandleAnimation(Handler handler, ImageView view, Random random) {
		mHandler = handler;
		if (view == null)
			throw new IllegalArgumentException("view required");
		mRandom = random;
		mCandle = view.getDrawable();
	}

	@Override
	public void run() {
		int index = mSpriteIndex;
		mCandle.setLevel(index);
		index++;
		if (index >= LEVELS)
			index = 0;
		mSpriteIndex = index;
		if (mRandom == null)
			mHandler.postDelayed(this, PERIOD);
		else
			mHandler.postDelayed(this, mRandom.nextInt(PERIOD));
	}

}
