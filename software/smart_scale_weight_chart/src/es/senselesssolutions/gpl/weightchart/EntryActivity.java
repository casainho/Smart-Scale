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

import android.app.Activity;
//import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;
import java.util.GregorianCalendar;

public class EntryActivity extends Activity {
	private boolean mStone;
	private long mId;
	private GregorianCalendar mDateTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entry);
		String weightUnit = PreferenceManager.getDefaultSharedPreferences(this)
				.getString("weight_unit", null);
		mStone = "st".equals(weightUnit);
		((TextView) findViewById(R.id.weight_unit_major))
				.setText(mStone ? R.string.st
						: "lb".equals(weightUnit) ? R.string.lb : R.string.kg);

		if (!mStone) {
			findViewById(R.id.weight_minor).setVisibility(View.GONE);
			findViewById(R.id.weight_unit_minor).setVisibility(View.GONE);
		}

		mDateTime = new GregorianCalendar();
		mDateTime.set(GregorianCalendar.SECOND, 0);
		// findViewById(R.id.date_time).setVisibility(View.VISIBLE);
		initDateTime();

		((Button) findViewById(R.id.date))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						showDialog(0);
					}
				});
		((Button) findViewById(R.id.time))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						showDialog(1);
					}
				});
		((Button) findViewById(R.id.ok))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						saveAndFinish();
					}
				});

		if (savedInstanceState != null) {
			mId = savedInstanceState.getLong("id");
			mDateTime = (GregorianCalendar) savedInstanceState
					.getSerializable("datetime");

			if (mDateTime != null) {
				// findViewById(R.id.set_manually).setVisibility(View.GONE);
				initDateTime();
				return;
			}
		} else {
			mId = getIntent().getLongExtra(
					"es.senselesssolutions.gpl.weightchart.Id", 0);

			if (mId != 0) {
				Database database = new Database(EntryActivity.this);
				Cursor cursor = database
						.query("SELECT weight, created_at FROM weight WHERE _id = "
								+ mId);

				try {
					if (cursor.moveToNext()) {
						int weight = cursor.getInt(0);

						if (mStone) {
							((EditText) findViewById(R.id.weight_major))
									.setText("" + weight / 140);
							((EditText) findViewById(R.id.weight_minor))
									.setText("" + weight % 140 / 10.);
						} else {
							((EditText) findViewById(R.id.weight_major))
									.setText("" + weight / 10.);
						}

						mDateTime = new GregorianCalendar();
						mDateTime.setTimeInMillis(1000L * cursor.getInt(1));
						// findViewById(R.id.set_manually).setVisibility(View.GONE);
						initDateTime();
						return;
					}
				} finally {
					cursor.close();
					database.close();
				}

				mId = 0;
			}
		}

		/*
		 * ((Button)findViewById(R.id.set_manually)).setOnClickListener(new
		 * View.OnClickListener() { public void onClick(View view) { } });
		 */
		// findViewById(R.id.date_time).setVisibility(View.GONE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("id", mId);
		outState.putSerializable("datetime", mDateTime);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == 0) {
			return new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							mDateTime.set(GregorianCalendar.YEAR, year);
							mDateTime.set(GregorianCalendar.MONTH, monthOfYear);
							mDateTime.set(GregorianCalendar.DATE, dayOfMonth);
							((Button) findViewById(R.id.date))
									.setText(DateFormat.getDateFormat(
											EntryActivity.this).format(
											mDateTime.getTime()));
						}
					}, mDateTime.get(GregorianCalendar.YEAR), mDateTime
							.get(GregorianCalendar.MONTH), mDateTime
							.get(GregorianCalendar.DATE));
		}
		if (id == 1) {
			return new TimePickerDialog(this,
					new TimePickerDialog.OnTimeSetListener() {
						public void onTimeSet(TimePicker view, int hourOfDay,
								int minute) {

							mDateTime.set(GregorianCalendar.HOUR_OF_DAY,
									hourOfDay);
							mDateTime.set(GregorianCalendar.MINUTE, minute);
							((Button) findViewById(R.id.time))
									.setText(DateFormat.getTimeFormat(
											EntryActivity.this).format(
											mDateTime.getTime()));
						}
					}, mDateTime.get(GregorianCalendar.HOUR_OF_DAY),
					mDateTime.get(GregorianCalendar.MINUTE),
					DateFormat.is24HourFormat(this));
		}
		return null;
	}

	private void initDateTime() {
		Date date = mDateTime.getTime();
		((Button) findViewById(R.id.date)).setText(DateFormat.getDateFormat(
				this).format(date));
		((Button) findViewById(R.id.time)).setText(DateFormat.getTimeFormat(
				this).format(date));
	}

	private void saveAndFinish() {
		int weight;

		try {
			weight = (int) (10 * Float
					.parseFloat(((EditText) findViewById(R.id.weight_major))
							.getText().toString()) + .5);

			if (mStone) {
				weight = 14
						* weight
						+ (int) (10 * Float
								.parseFloat(((EditText) findViewById(R.id.weight_minor))
										.getText().toString()) + .5);
			}
		} catch (NumberFormatException e) {
			Toast.makeText(EntryActivity.this, R.string.invalid_weight,
					Toast.LENGTH_SHORT).show();
			return;
		}

		long createdAt = (mDateTime == null ? System.currentTimeMillis()
				: mDateTime.getTime().getTime()) / 1000;
		Database database = new Database(EntryActivity.this);

		if (mId == 0) {
			database.exec(
					"INSERT INTO weight (weight, created_at) VALUES (?, ?)",
					new Object[] { weight, createdAt });
		} else {
			database.exec(
					"UPDATE weight SET weight = ?, created_at = ? WHERE _id = ?",
					new Object[] { weight, createdAt, mId });
		}

		database.close();
		Toast.makeText(EntryActivity.this,
				mId == 0 ? R.string.weight_added : R.string.entry_edited,
				Toast.LENGTH_SHORT).show();
		finish();
	}
}
