/*
 * Copyright (C) 2011 Senseless Solutions 
 *
 * Licensed under the GNU General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * Weight Chart is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Modified Source code: http://code.google.com/p/weight-chart/
 * Original Source code: http://fredrik.jemla.eu/weightchart
 */

package net.casainho.smartweightchart;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

public class LegendView extends View {
	private final Paint mAveragePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mBmiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mDescriptionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPlotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public LegendView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAveragePaint.setColor(0xffcc6600);
		mAveragePaint.setStrokeWidth(2);
		mAveragePaint.setStyle(Paint.Style.STROKE);
		mBmiPaint.setColor(0xff446688);
		mBmiPaint.setTextAlign(Paint.Align.CENTER);
		mDescriptionPaint.setTextSize(1.75f * mDescriptionPaint.getTextSize());
		mGradientPaint.setColor(0x2666cc00);
		mGridPaint.setColor(0xffaaaaaa);
		mGridPaint.setStrokeWidth(.5f);
		mGridPaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setColor(0xff66cc00);
		mLinePaint.setStrokeWidth(2);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mPlotPaint.setColor(0xff66cc00);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Paint descriptionPaint = mDescriptionPaint;
		float lesserTextSize = mTextPaint.getTextSize();
		float textSize = descriptionPaint.getTextSize();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getContext());

		// Weight

		String weightUnit = preferences.getString("weight_unit", null);
		canvas.drawRect(textSize / 2, 1.5f * textSize + lesserTextSize / 2,
				2.5f * textSize, 2.5f * textSize, mGradientPaint);
		canvas.drawLine(textSize / 2, 1.5f * textSize + lesserTextSize / 2,
				2.5f * textSize, 1.5f * textSize + lesserTextSize / 2,
				mLinePaint);
		RectF oval = new RectF(1.5f * textSize - 3, 1.5f * textSize
				+ lesserTextSize / 2 - 3, 1.5f * textSize + 3, 1.5f * textSize
				+ lesserTextSize / 2 + 3);
		canvas.drawOval(oval, mPlotPaint);
		canvas.drawText("12.3", 1.5f * textSize, 1.5f * textSize, mTextPaint);
		canvas.drawText(
				getContext()
						.getString(
								"lb".equals(weightUnit) ? R.string.weight_lb_legend
										: "st".equals(weightUnit) ? R.string.weight_st_legend
												: R.string.weight_kg_legend),
				3 * textSize, 1.8f * textSize, descriptionPaint);

		// BMI

		int height = preferences.getInt("height", 0);

		if (height > 0) {
			canvas.save();
			canvas.rotate(-90);
			canvas.drawText("12.34", -4 * textSize, 1.5f * textSize
					+ lesserTextSize / 2, mBmiPaint);
			canvas.restore();
			canvas.drawText(
					String.format(
							getContext().getString(R.string.bmi_with_height),
							"ft".equals(preferences.getString("height_unit",
									null)) ? String.format(getContext()
									.getString(R.string.height_ft),
									height / 12, height % 12) : String.format(
									getContext().getString(R.string.height_cm),
									height)), 3 * textSize, 4.3f * textSize,
					descriptionPaint);
			canvas.translate(0, 2.5f * textSize);
		}

		// Average

		canvas.drawLine(textSize / 2, 4f * textSize, 2.5f * textSize,
				4f * textSize, mAveragePaint);
		canvas.drawText(getContext().getString(R.string.average), 3 * textSize,
				4.3f * textSize, descriptionPaint);

		// Date

		canvas.drawText(
				getContext().getResources().getStringArray(R.array.weekdays)[0],
				1.5f * textSize, 6.5f * textSize - lesserTextSize / 5,
				mTextPaint);
		canvas.drawText("31", 1.5f * textSize, 6.5f * textSize + .9f
				* lesserTextSize, mTextPaint);
		canvas.drawText(getContext().getString(R.string.date_legend),
				3 * textSize, 6.8f * textSize, descriptionPaint);

		// Scroll

		canvas.drawText(getContext().getString(R.string.drag_chart_to_scroll),
				3 * textSize, 9.4f * textSize, descriptionPaint);
	}
}
