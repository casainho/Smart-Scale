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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class PreferencesActivity extends PreferenceActivity {
	private HeightDialog mHeightDialog;

	private final static int DIALOG_GOAL = 10;
	private final static int DIALOG_MAX = 11;
	private final static int DIALOG_ABOUT = 12;
	private final static int DIALOG_VIEW_DAYS = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		updateWeightUnit(preferences.getString("weight_unit", null));
		findPreference("weight_goal").setSummary(
				preferences.getString("weight_goal", "") + " "
						+ findPreference("weight_unit").getSummary());
		findPreference("weight_max").setSummary(
				preferences.getString("weight_max", "") + " "
						+ findPreference("weight_unit").getSummary());
		updateHeight();
		
		// Added by casainho@gmail.com
		updateViewDays();
		
		mHeightDialog = new HeightDialog(this) {
			protected void done() {
				removeDialog(2);
				updateHeight();
			}
		};
		findPreference("weight_unit").setOnPreferenceChangeListener(
				new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						updateWeightUnit(newValue);
						return true;
					}
				});
		findPreference("height").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						showDialog(PreferenceManager
								.getDefaultSharedPreferences(
										PreferencesActivity.this).getInt(
										"height", 0) < 1 ? 1 : 2);
						return true;
					}
				});
		// Added by casainho@gmail.com
		findPreference("view_days").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						showDialog(DIALOG_VIEW_DAYS);
						return true;
					}
				});
		
		findPreference("weight_goal").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						showDialog(DIALOG_GOAL);
						return true;
					}
				});
		findPreference("weight_max").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						showDialog(DIALOG_MAX);
						return true;
					}
				});
		findPreference("website").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri
								.parse(getString(R.string.website_url))));
						return true;
					}
				});
		findPreference("license").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						showDialog(0);
						return true;
					}
				});

		/* added preferences aitorthered@senselesssolutions */
		findPreference("market").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri
								.parse(getString(R.string.market_url))));
						return true;
					}
				});

		findPreference("about").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						showDialog(DIALOG_ABOUT);
						return true;
					}
				});

		findPreference("market_author").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri
								.parse(getString(R.string.market_url_author))));
						return true;
					}
				});

		findPreference("translate").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						/* Create the Intent */
						final Intent emailIntent = new Intent(
								android.content.Intent.ACTION_SEND);

						/* Fill it with Data */
						emailIntent.setType("plain/text");
						emailIntent
								.putExtra(
										android.content.Intent.EXTRA_EMAIL,
										new String[] { getString(R.string.translate_email) });
						emailIntent.putExtra(
								android.content.Intent.EXTRA_SUBJECT,
								getString(R.string.translate_email_subject));
						emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
								getString(R.string.translate_email_text));

						/* Send it off to the Activity-Chooser */
						startActivity(Intent.createChooser(emailIntent,
								"Send mail..."));
						return true;
					}
				});

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == 0) {
			return createLicenseDialog();
		} else if (id == DIALOG_GOAL) {
			return createGoalWeightDialog();
		} else if (id == DIALOG_MAX) {
			return createMaxWeightDialog();
		} else if (id == DIALOG_ABOUT) {
			return createAboutDialog();

			// Added by casainho@gmail.com	
		} else if (id == DIALOG_VIEW_DAYS) {
			return createViewDaysDialog();
		}	
		return mHeightDialog.createDialog(id);
	}

	private Dialog createLicenseDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.license_description);
		builder.setNeutralButton(R.string.close, null);
		return builder.create();
	}

	private Dialog createGoalWeightDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.ad_goal_weight,
				null);
		((EditText) textEntryView.findViewById(R.id.ET_weight))
				.setText(PreferenceManager.getDefaultSharedPreferences(
						PreferencesActivity.this).getString("weight_goal", ""));
		return new AlertDialog.Builder(PreferencesActivity.this)
				// .setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.ad_goal_weight_title)
				.setView(textEntryView)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String summary = ((EditText) textEntryView
										.findViewById(R.id.ET_weight))
										.getText().toString();
								findPreference("weight_goal").setSummary(
										summary
												+ " "
												+ findPreference("weight_unit")
														.getSummary());
								SharedPreferences.Editor edit = PreferenceManager
										.getDefaultSharedPreferences(
												PreferencesActivity.this)
										.edit();
								edit.putString("weight_goal", summary);
								edit.commit();
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked cancel so do some stuff */
							}
						}).create();
	}

	private Dialog createMaxWeightDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory
				.inflate(R.layout.ad_max_weight, null);
		((EditText) textEntryView.findViewById(R.id.ET_max_weight))
				.setText(PreferenceManager.getDefaultSharedPreferences(
						PreferencesActivity.this).getString("weight_max", ""));
		return new AlertDialog.Builder(PreferencesActivity.this)
				// .setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.ad_max_weight_title)
				.setView(textEntryView)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String summary = ((EditText) textEntryView
										.findViewById(R.id.ET_max_weight))
										.getText().toString();
								findPreference("weight_max").setSummary(
										summary
												+ " "
												+ findPreference("weight_unit")
														.getSummary());
								SharedPreferences.Editor edit = PreferenceManager
										.getDefaultSharedPreferences(
												PreferencesActivity.this)
										.edit();
								edit.putString("weight_max", summary);
								edit.commit();
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked cancel so do some stuff */
							}
						}).create();
	}

	private Dialog createAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.about_description);
		builder.setNeutralButton(R.string.close, null);
		return builder.create();
	}

	private void updateWeightUnit(Object unit) {
		findPreference("weight_unit").setSummary(
				getResources().getStringArray(R.array.weight_unit_labels)["lb"
						.equals(unit) ? 1 : "st".equals(unit) ? 2 : 0]);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		findPreference("weight_goal").setSummary(
				preferences.getString("weight_goal", null) + " "
						+ findPreference("weight_unit").getSummary());
		findPreference("weight_max").setSummary(
				preferences.getString("weight_max", null) + " "
						+ findPreference("weight_unit").getSummary());
	}

	private void updateHeight() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(PreferencesActivity.this);
		int height = preferences.getInt("height", 0);
		String summary = height < 1 ? null : "ft".equals(preferences.getString(
				"height_unit", null)) ? String.format(
				getString(R.string.height_ft), height / 12, height % 12)
				: String.format(getString(R.string.height_cm), height);
		findPreference("height").setSummary(summary);
	}
	
	// Added by casainho@gmail.com
	private void updateViewDays() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(PreferencesActivity.this);
		String summary = preferences.getString("view_days", "");
		findPreference("view_days").setSummary(summary);
	}
	
	// Added by casainho@gmail.com
	private Dialog createViewDaysDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory
				.inflate(R.layout.add_view_days, null);
		((EditText) textEntryView.findViewById(R.id.ET_view_days))
				.setText(PreferenceManager.getDefaultSharedPreferences(
						PreferencesActivity.this).getString("view_days", ""));
		return new AlertDialog.Builder(PreferencesActivity.this)
				// .setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.add_view_days_title)
				.setView(textEntryView)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String summary = ((EditText) textEntryView
										.findViewById(R.id.ET_view_days))
										.getText().toString();
								findPreference("view_days").setSummary(
										summary + " " + getString(R.string.days));
								SharedPreferences.Editor edit = PreferenceManager
										.getDefaultSharedPreferences(
												PreferencesActivity.this)
										.edit();
								edit.putString("view_days", summary);
								edit.commit();
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked cancel so do some stuff */
							}
						}).create();
	}
}
