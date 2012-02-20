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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public abstract class HeightDialog {
	private Activity mActivity;

	public HeightDialog(Activity activity) {
		mActivity = activity;
	}

	public Dialog createDialog(int id) {
		if (id == 1) {
			return createHeightUnitDialog();
		}
		if (id == 2) {
			return createHeightDialog();
		}
		if (id == 3) {
			return createInvalidHeightDialog();
		}
		return null;
	}

	protected abstract void done();

	private Dialog createHeightUnitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.height_unit);
		builder.setSingleChoiceItems(R.array.height_unit_labels, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						SharedPreferences.Editor editor = PreferenceManager
								.getDefaultSharedPreferences(mActivity).edit();
						editor.putString(
								"height_unit",
								mActivity.getResources().getStringArray(
										R.array.height_unit_values)[item]);
						editor.commit();
						mActivity.showDialog(2);
					}
				});
		builder.setNeutralButton(R.string.skip,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						done();
					}
				});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				done();
			}
		});
		return builder.create();
	}

	private Dialog createHeightDialog() {
		final View view = mActivity.getLayoutInflater().inflate(
				R.layout.height, null);
		final Spinner unitSpinner = (Spinner) view
				.findViewById(R.id.height_unit);
		final View cmInput = view.findViewById(R.id.cm_input);
		final View ftInput = view.findViewById(R.id.ft_input);
		final EditText cmEditText = (EditText) view.findViewById(R.id.cm);
		final EditText ftEditText = (EditText) view.findViewById(R.id.ft);
		final EditText inEditText = (EditText) view.findViewById(R.id.in);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(mActivity,
				R.array.height_unit_labels,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		unitSpinner.setAdapter(adapter);
		unitSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView parent, View view,
							int position, long id) {
						cmInput.setVisibility(position > 0 ? View.GONE
								: View.VISIBLE);
						ftInput.setVisibility(position > 0 ? View.VISIBLE
								: View.GONE);
					}

					public void onNothingSelected(AdapterView parent) {
					}
				});

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(mActivity);
		int height = preferences.getInt("height", 0);

		if ("ft".equals(preferences.getString("height_unit", null))) {
			unitSpinner.setSelection(1);
			cmInput.setVisibility(View.GONE);

			if (height > 0) {
				ftEditText.setText("" + height / 12);
				inEditText.setText("" + height % 12);
			}
		} else {
			ftInput.setVisibility(View.GONE);

			if (height > 0) {
				cmEditText.setText("" + height);
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.height_unit);
		builder.setView(view);
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						int unit = unitSpinner.getSelectedItemPosition();
						int height;

						try {
							height = unit > 0 ? 12
									* Integer.parseInt(ftEditText.getText()
											.toString())
									+ Integer.parseInt(inEditText.getText()
											.toString()) : Integer
									.parseInt(cmEditText.getText().toString());
						} catch (NumberFormatException e) {
							mActivity.showDialog(3);
							return;
						}

						if (height < 1) {
							mActivity.showDialog(3);
							return;
						}

						SharedPreferences.Editor editor = PreferenceManager
								.getDefaultSharedPreferences(mActivity).edit();
						editor.putString(
								"height_unit",
								mActivity.getResources().getStringArray(
										R.array.height_unit_values)[unit]);
						editor.putInt("height", height);
						editor.commit();
						done();
					}
				});

		if (height > 0) {
			builder.setNegativeButton(R.string.delete,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							SharedPreferences.Editor editor = PreferenceManager
									.getDefaultSharedPreferences(mActivity)
									.edit();
							editor.remove("height");
							editor.remove("height_unit");
							editor.commit();
							done();
						}
					});
		}

		builder.setNeutralButton(height > 0 ? R.string.cancel : R.string.skip,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						done();
					}
				});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				done();
			}
		});
		return builder.create();
	}

	private Dialog createInvalidHeightDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(R.string.invalid_height);
		builder.setNeutralButton(R.string.close,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						mActivity.showDialog(2);
					}
				});
		return builder.create();
	}
}
