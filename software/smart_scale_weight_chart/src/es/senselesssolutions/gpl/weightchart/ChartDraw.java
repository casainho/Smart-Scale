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

package es.senselesssolutions.gpl.weightchart;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.Log;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class ChartDraw {
	public int mDays;
	public float mScrollX = 0;
	private int mSizeX;
	private int mSizeY;
	private boolean mStone;
	private double mBmiFactor;
	private Database mDatabase;
	private GregorianCalendar mDate;
	private final Paint mAveragePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mBmiBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mBmiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mLeftTextBackgroundPaint = new Paint(
			Paint.ANTI_ALIAS_FLAG);
	private final Paint mLeftTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPlotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mTextBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private String[] mWeekdays;

	/* Added by aitorthered@senselesssolutions */
	private float mMaxWeight = 0;
	private float mGoalWeight = 0;
	private Context ctx = null;

	/* ^Added by aitorthered@senselesssolutions */

	public ChartDraw(Context context, Database database, GregorianCalendar date) {
		date.set(GregorianCalendar.HOUR_OF_DAY, 24);
		date.set(GregorianCalendar.MINUTE, 0);
		date.set(GregorianCalendar.SECOND, 0);
		mDate = date;
		mAveragePaint.setColor(0xffcc6600);
		mAveragePaint.setStrokeWidth(2);
		mAveragePaint.setStyle(Paint.Style.STROKE);
		mBmiBackgroundPaint.setColor(0xccffffff);
		mBmiBackgroundPaint.setStrokeWidth(2);
		mBmiBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mBmiPaint.setColor(0xff446688);
		mGridPaint.setColor(0xffaaaaaa);
		mGridPaint.setStrokeWidth(.5f);
		mGridPaint.setStyle(Paint.Style.STROKE);
		mLeftTextBackgroundPaint.setColor(0xccffffff);
		mLeftTextBackgroundPaint.setStrokeWidth(2);
		mLeftTextBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mLinePaint.setColor(0xff66cc00);
		mLinePaint.setStrokeWidth(2);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mPlotPaint.setColor(0xff66cc00);
		mTextBackgroundPaint.setColor(0xccffffff);
		mTextBackgroundPaint.setStrokeWidth(2);
		mTextBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mTextBackgroundPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mDatabase = database;
		mWeekdays = context.getResources().getStringArray(R.array.weekdays);
		loadPreferences(context);
	}

	public void loadPreferences(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String weightUnit = preferences.getString("weight_unit", null);
		try {
			mMaxWeight = Float.parseFloat(preferences.getString("weight_max",
					"0")) * 10;
			mGoalWeight = Float.parseFloat(preferences.getString("weight_goal",
					"0")) * 10;
		} catch (NumberFormatException nfe) {
			Log.e("WeightChart", "Error parsing Max Weight or Goal Weight");
		}
		mStone = "st".equals(weightUnit);
		int height = preferences.getInt("height", 0);
		mBmiFactor = height > 0 ? (mStone || "lb".equals(weightUnit) ? .045359237
				: .1)
				/ ("ft".equals(preferences.getString("height_unit", null)) ? 0.00064516
						: .0001) / height / height
				: 0;
		ctx = context;
	}

	public void setSize(int sizeX, int sizeY) {
		mSizeX = sizeX;
		mSizeY = sizeY;
		mGradientPaint.setShader(new LinearGradient(0, sizeY - 2.7f
				* mTextPaint.getTextSize(), 0, sizeY, 0x2666cc00, 0x66cc00,
				Shader.TileMode.CLAMP));

		if (mDays == 0) {
			mDays = sizeX / mTextPaint.getTextSize() / 21 < 2 ? 14 : 21;
		}
	}

	public void draw(Canvas canvas) {
		int days = mDays;
		int sizeX = mSizeX;
		int sizeY = mSizeY;
		int dateOffset = Math.round(days * mScrollX / sizeX) - 1;
		canvas.save();
		canvas.translate(mScrollX - sizeX * dateOffset / days, 0);
		GregorianCalendar endDate = (GregorianCalendar) mDate.clone();
		endDate.add(GregorianCalendar.DATE, -dateOffset);
		GregorianCalendar startDate = (GregorianCalendar) endDate.clone();
		startDate.add(GregorianCalendar.DATE, -9 - days);
		Paint gridPaint = mGridPaint;
		Paint leftTextBackgroundPaint = mLeftTextBackgroundPaint;
		Paint leftTextPaint = mLeftTextPaint;
		Paint textPaint = mTextPaint;
		float textSize = textPaint.getTextSize();
		float chartPosY = sizeY - 2.6f * textSize - 3;
		float chartSizeY = sizeY - 4.7f * textSize - 3;

		/* Added by aitorthered@senselesssolutions */
		Paint maxWeightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		maxWeightPaint.setColor(0xffff0000);
		maxWeightPaint.setStrokeWidth(1f);
		maxWeightPaint.setStyle(Paint.Style.STROKE);
		Paint goalWeightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		goalWeightPaint.setColor(0xff00ff00);
		goalWeightPaint.setStrokeWidth(1f);
		goalWeightPaint.setStyle(Paint.Style.STROKE);
		/* Added by aitorthered@senselesssolutions */

		// Draw vertical lines separating weeks

		for (int i = 5 - (startDate.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7; i < days; i += 7) {
			float x = sizeX * i / (float) days;
			canvas.drawLine(x, 0, x, sizeY, gridPaint);
		}

		// Fetch data

		Cursor cursor = mDatabase
				.query("SELECT weight, created_at FROM weight WHERE created_at >= "
						+ startDate.getTimeInMillis()
						/ 1000
						+ " AND created_at < "
						+ endDate.getTimeInMillis()
						/ 1000 + " ORDER BY created_at");
		int count = cursor.getCount();

		if (count > 0) {
			// Prepare data

			int ordinal = -9;
			int maxValue = 0;
			int minValue = Integer.MAX_VALUE;
			ArrayList<ChartEntryData> entries = new ArrayList<ChartEntryData>(
					Math.min(count, days));
			endDate = (GregorianCalendar) startDate.clone();

			while (cursor.moveToNext()) {
				long time = 1000L * cursor.getInt(1);
				if (endDate.getTimeInMillis() <= time) {
					endDate.add(GregorianCalendar.DATE, 1);

					while (endDate.getTimeInMillis() <= time) {
						ordinal++;
						endDate.add(GregorianCalendar.DATE, 1);
					}

					ChartEntryData entry = new ChartEntryData();
					entry.ordinal = ordinal;
					entry.weight = cursor.getInt(0);
					entries.add(entry);
					ordinal++;

					if (ordinal > -2) {
						maxValue = Math.max(maxValue, entry.weight);
						minValue = Math.min(minValue, entry.weight);
					}
				}
			}

			/* added by aitorthered@senselesssolutions */
			if (mGoalWeight >= minValue - 3 * 10 && mGoalWeight < minValue) {
				minValue = (int) mGoalWeight - 1 * 10;
			}
			if (mMaxWeight <= maxValue + 2 * 10 && mMaxWeight > maxValue) {
				maxValue = (int) mMaxWeight + (int) 0.2 * 10;
			}

			int stride = 1;
			int delta = maxValue - minValue;

			if (delta < 1) {
				delta = 2;
				minValue--;
			}

			// Draw horizontal lines

			while (delta >= 30 * stride) {
				stride *= 10;
			}
			if (delta >= 15 * stride) {
				stride *= 5;
			}
			if (delta >= 6 * stride) {
				stride *= 2;
			}
			for (int i = stride - minValue % stride; i <= delta; i += stride) {
				float y = chartPosY - chartSizeY * i / delta;
				canvas.drawLine(-sizeX, y, sizeX, y, gridPaint);
			}

			// Draw filled area

			int i = 0;
			int subsetCount = 0;
			int subsetIndex = 0;
			float subsetSum = 0;
			Path averagePath = new Path();
			Path path = new Path();

			int lastWeight = 0;
			for (ChartEntryData entry : entries) {
				ChartEntryData startEntry = entries.get(subsetIndex);
				if (startEntry.ordinal <= entry.ordinal - 7) {
					subsetCount--;
					subsetIndex++;
					subsetSum -= startEntry.y;
				}
				entry.x = sizeX * (entry.ordinal + .5f) / days;
				entry.y = chartPosY - chartSizeY * (entry.weight - minValue)
						/ delta;
				/* Added by aitorthered@senselesssolutions */
				lastWeight = entry.weight;
				/* ^Added by aitorthered@senselesssolutions */
				subsetCount++;
				subsetSum += entry.y;
				if (i > 0) {
					averagePath.lineTo(entry.x, subsetSum / subsetCount);
					path.lineTo(entry.x, entry.y);
				} else {
					averagePath.moveTo(entry.x, entry.y);
					path.moveTo(entry.x, entry.y);
				}
				i++;
			}

			/* Added by aitorthered@senselesssolutions for max and goal weight */
			if (mMaxWeight <= maxValue && mMaxWeight >= minValue) {
				float mMaxWeightPaint = chartPosY - chartSizeY
						* (mMaxWeight - minValue) / delta;
				canvas.drawLine(-sizeX, mMaxWeightPaint, sizeX,
						mMaxWeightPaint, maxWeightPaint);
				canvas.drawText(ctx.getString(R.string.max_weight_label),
						sizeX / 3, mMaxWeightPaint - 3, maxWeightPaint);
				if (lastWeight > mMaxWeight) { // We are over the alert!
					canvas.drawText(ctx.getString(R.string.max_weight_alert),
							sizeX / 3, sizeY / 2, maxWeightPaint);
				}
			}
			if (mGoalWeight <= maxValue && mGoalWeight >= minValue) {
				float mGoalWeightPaint = chartPosY - chartSizeY
						* (mGoalWeight - minValue) / delta;
				canvas.drawLine(-sizeX, mGoalWeightPaint, sizeX,
						mGoalWeightPaint, goalWeightPaint);
				canvas.drawText(ctx.getString(R.string.goal_weight_label),
						sizeX / 3, mGoalWeightPaint - 3, goalWeightPaint);
			}
			/* Added by aitorthered@senselesssolutions for max and goal weight */

			canvas.drawPath(path, mLinePaint);
			path.lineTo(entries.get(entries.size() - 1).x, sizeY);
			path.lineTo(entries.get(0).x, sizeY);
			canvas.drawPath(path, mGradientPaint);
			canvas.drawPath(averagePath, mAveragePaint);
			boolean stone = mStone;
			double bmiFactor = mBmiFactor;
			Paint bmiBackgroundPaint = mBmiBackgroundPaint;
			Paint bmiPaint = mBmiPaint;
			Paint plotPaint = mPlotPaint;
			Paint textBackgroundPaint = mTextBackgroundPaint;
			RectF oval = new RectF();
			NumberFormat numberFormat = NumberFormat.getInstance();
			numberFormat.setMaximumFractionDigits(2);
			numberFormat.setMinimumFractionDigits(2);
			int weight = -1;
			float halfAscent = textPaint.ascent() / 2;

			for (ChartEntryData entry : entries) {
				if (entry.ordinal < -2) {
					weight = entry.weight;
					continue;
				}

				oval.set(entry.x - 3, entry.y - 3, entry.x + 3, entry.y + 3);
				canvas.drawOval(oval, plotPaint);

				if (entry.weight != weight) {
					weight = entry.weight;

					if (bmiFactor > 0) {
						canvas.save();
						canvas.rotate(-90);
						String s = numberFormat.format(bmiFactor * weight);
						float x = (entry.y < 5 * textSize ? -3.2f : 1.8f)
								* textSize - entry.y;
						float y = entry.x - halfAscent;
						canvas.drawText(s, x, y, bmiBackgroundPaint);
						canvas.drawText(s, x, y, bmiPaint);
						canvas.restore();
					}

					String s = stone ? "" + weight / 140 + "'" + weight % 140
							/ 10. : "" + weight / 10.;
					float x = entry.x;
					float y = entry.y - textSize / 2;
					canvas.drawText(s, x, y, textBackgroundPaint);
					canvas.drawText(s, x, y, textPaint);
				}
			}
		}

		cursor.close();

		// Draw text for days at bottom

		float monthPosY = sizeY - 2.7f * textSize;
		float weekdayPosY = sizeY - 1.6f * textSize;
		float datePosY = sizeY - textSize / 2;
		String[] weekdays = mWeekdays;
		boolean compact = sizeX / textSize / days < 2;
		int i = -2;
		startDate.add(GregorianCalendar.DATE, 7);

		while (i < days) {
			float x = (2.f * sizeX * i + sizeX) / days / 2;
			int date = startDate.get(GregorianCalendar.DATE);
			int weekday = (startDate.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7;

			if (!compact || weekday == 0) {
				canvas.drawText(weekdays[weekday], x, weekdayPosY, textPaint);
				canvas.drawText("" + date, x, datePosY, textPaint);
			}

			if (date == 1) {
				x -= textSize / 2;
				String s = new SimpleDateFormat("yyyy-MM").format(startDate
						.getTime());
				canvas.drawText(s, x, monthPosY, leftTextBackgroundPaint);
				canvas.drawText(s, x, monthPosY, leftTextPaint);
			}

			startDate.add(GregorianCalendar.DATE, 1);
			i++;
		}
	} // END draw
}

class ChartEntryData {
	int ordinal;
	int weight;
	float x;
	float y;
}
