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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public abstract class FileCommands {
	private Activity mActivity;
	private Database mDatabase;

	public FileCommands(Activity activity, Database database) {
		mActivity = activity;
		mDatabase = database;
	}

	public boolean onOptionsItemSelected(int id) {
		if (id == R.id.add) {
			mActivity.startActivity(new Intent(mActivity, EntryActivity.class));
		} else if (id == R.id.import_csv) {
			showImportCsvDialog();
		} else if (id == R.id.export_csv) {
			showExportCsvDialog();
		} else if (id == R.id.export_ods) {
			showExportOdsDialog();
		} else if (id == R.id.export_png) {
			showExportPngDialog();
		} else if (id == R.id.count_entries) {
			showEntryCountDialog();
		} else if (id == R.id.delete_all_entries) {
			showDeleteAllEntriesDialog();
		} else if (id == R.id.preferences) {
			mActivity.startActivity(new Intent(mActivity,
					PreferencesActivity.class));
		} else {
			return false;
		}
		return true;
	}

	protected abstract void fillData();

	private void showDialog(AlertDialog.Builder builder) {
		AlertDialog dialog = builder.create();
		dialog.setOwnerActivity(mActivity);
		dialog.show();
	}

	private void showMessageDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(message);
		builder.setNeutralButton(R.string.close, null);
		showDialog(builder);
	}

	private void showMessageDialog(int resId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(resId);
		builder.setNeutralButton(R.string.close, null);
		showDialog(builder);
	}

	private void showMessageDialog(int resId,
			DialogInterface.OnClickListener callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(resId);
		builder.setNeutralButton(R.string.close, callback);
		showDialog(builder);
	}

	private void showMessageDialog(int resId, File file) {
		showMessageDialog(String.format(mActivity.getString(resId),
				file.toString()));
	}

	private void showPromptDialog(int title, int positiveButton,
			CharSequence text, final PromptCallback positiveCallback,
			DialogInterface.OnClickListener neutralCallback) {
		View view = mActivity.getLayoutInflater()
				.inflate(R.layout.prompt, null);
		((TextView) view.findViewById(R.id.label)).setText(mActivity
				.getString(R.string.file_input));
		final EditText input = (EditText) view.findViewById(R.id.input);
		input.setText(text);
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(title);
		builder.setView(view);
		builder.setPositiveButton(positiveButton,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						positiveCallback.run(input.getText().toString());
					}
				});
		builder.setNeutralButton(R.string.cancel, neutralCallback);
		showDialog(builder);
	}

	private void toast(int resId) {
		Toast.makeText(mActivity, resId, Toast.LENGTH_SHORT).show();
	}

	private void toast(int resId, File file) {
		Toast.makeText(mActivity,
				String.format(mActivity.getString(resId), file.toString()),
				Toast.LENGTH_SHORT).show();
	}

	private void sendFile(File file, String type) {
		toast(R.string.exported_to, file);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType(type);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		mActivity.startActivity(Intent.createChooser(intent,
				mActivity.getString(R.string.share_file)));
	}

	private void showImportCsvDialog() {
		String state = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(state)
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			showMessageDialog(R.string.media_not_available);
			return;
		}

		showPromptDialog(R.string.import_csv, R.string.import_,
				new File(Environment.getExternalStorageDirectory(),
						"weight.csv").toString(), new PromptCallback() {
					public void run(String text) {
						importCsv(new File(text));
					}
				}, null);
	}

	private void importCsv(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			SimpleDateFormat format1 = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
			Pattern pattern = Pattern
					.compile("^\\s*([^,;]+)\\s*[,;]\\s*(\\d+\\.?\\d*)\\s*([,;].*|)");
			ArrayList<EntryData> list = new ArrayList<EntryData>();

			for (;;) {
				String line = reader.readLine();

				if (line == null) {
					break;
				}

				Matcher matcher = pattern.matcher(line);

				if (!matcher.matches()) {
					reader.close();
					showMessageDialog(R.string.invalid_file_format);
					return;
				}

				Date date;
				String dateString = matcher.group(1);

				try {
					date = format1.parse(dateString);
				} catch (ParseException e1) {
					try {
						date = format2.parse(dateString);
					} catch (ParseException e2) {
						showMessageDialog(R.string.invalid_file_format);
						return;
					}
				}

				EntryData entry = new EntryData();
				entry.weight = (int) (10 * Float.parseFloat(matcher.group(2)));
				entry.created_at = (int) (date.getTime() / 1000);
				list.add(entry);
			}

			reader.close();
			mDatabase.beginTransaction();

			try {
				for (EntryData entry : list) {
					mDatabase
							.exec("INSERT INTO weight (weight, created_at) VALUES (?, ?)",
									new Object[] { entry.weight,
											entry.created_at });
				}

				mDatabase.setTransactionSuccessful();
			} finally {
				mDatabase.endTransaction();
			}

			fillData();
			toast(R.string.imported_from, file);
		} catch (FileNotFoundException e) {
			showMessageDialog(R.string.file_not_found, file);
		} catch (IOException e) {
			showMessageDialog(R.string.failed_open_file, file);
		}
	}

	private void showExportCsvDialog() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			showMessageDialog(R.string.media_not_available);
			return;
		}

		final Cursor cursor = mDatabase
				.query("SELECT weight, created_at FROM weight ORDER BY created_at");

		if (cursor.getCount() < 1) {
			cursor.close();
			showMessageDialog(R.string.no_entries);
			return;
		}

		showPromptDialog(R.string.export_csv, R.string.export,
				new File(Environment.getExternalStorageDirectory(),
						"weight.csv").toString(), new PromptCallback() {
					public void run(String text) {
						try {
							exportCsv(new File(text), cursor);
						} finally {
							cursor.close();
						}
					}
				}, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						cursor.close();
					}
				});
	}

	private void exportCsv(File file, Cursor cursor) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			while (cursor.moveToNext()) {
				writer.write(format.format(new Date(1000L * cursor.getInt(1)))
						+ "," + cursor.getInt(0) / 10.);
				writer.newLine();
			}

			writer.close();
		} catch (IOException e) {
			showMessageDialog(R.string.failed_save_file, file);
			return;
		}

		toast(R.string.exported_to, file);

		/* aitorthered@senselesssolutions Add Dialog to ask if send by email */
		sendFile(file, "text/csv");
	}

	private void showExportOdsDialog() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			showMessageDialog(R.string.media_not_available);
			return;
		}

		final Cursor cursor = mDatabase
				.query("SELECT weight, created_at FROM weight ORDER BY created_at");

		if (cursor.getCount() < 1) {
			showMessageDialog(R.string.no_entries);
			return;
		}

		showPromptDialog(R.string.export_ods, R.string.export,
				new File(Environment.getExternalStorageDirectory(),
						"weight.ods").toString(), new PromptCallback() {
					public void run(String text) {
						try {
							exportOds(new File(text), cursor);
						} finally {
							cursor.close();
						}
					}
				}, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						cursor.close();
					}
				});
	}

	private void exportOds(File file, Cursor cursor) {
		try {
			ZipOutputStream output = new ZipOutputStream(
					new BufferedOutputStream(new FileOutputStream(file)));
			OutputStreamWriter writer = new OutputStreamWriter(output);
			output.putNextEntry(new ZipEntry("META-INF/manifest.xml"));
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" "
					+ "standalone=\"yes\"?><manifest:manifest xmlns:manifest="
					+ "\"urn:oasis:names:tc:opendocument:xmlns:manifest:1.0\">"
					+ "<manifest:file-entry manifest:media-type=\""
					+ "application/vnd.oasis.opendocument.spreadsheet\" "
					+ "manifest:full-path=\"/\"></manifest:file-entry>"
					+ "</manifest:manifest>");
			writer.flush();
			output.putNextEntry(new ZipEntry("content.xml"));
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter
					.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
							+ "<office:document-content "
							+ "xmlns:number=\"urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0\" "
							+ "xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" "
							+ "xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\" "
							+ "xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\" "
							+ "xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\">"
							+ "<office:automatic-styles>"
							+ "<style:style style:family=\"table-column\" style:name=\"co1\">"
							+ "<style:table-column-properties style:column-width=\"3.5cm\"/>"
							+ "</style:style>"
							+ "<number:date-style number:automatic-order=\"true\" style:name=\"N1\">"
							+ "<number:year number:style=\"long\"/><number:text>-</number:text>"
							+ "<number:month number:style=\"long\"/><number:text>-</number:text>"
							+ "<number:day number:style=\"long\"/><number:text> </number:text>"
							+ "<number:hours number:style=\"long\"/><number:text>:</number:text>"
							+ "<number:minutes number:style=\"long\"/><number:text>:</number:text>"
							+ "<number:seconds number:style=\"long\"/></number:date-style>"
							+ "<number:number-style style:name=\"N2\"><number:number "
							+ "number:decimal-places=\"1\" number:min-integer-digits=\"1\"/>"
							+ "</number:number-style>"
							+ "<style:style style:name=\"ce1\" style:data-style-name=\"N1\" "
							+ "style:family=\"table-cell\"/>"
							+ "<style:style style:name=\"ce2\" style:data-style-name=\"N2\" "
							+ "style:family=\"table-cell\"/>"
							+ "</office:automatic-styles>"
							+ "<office:body><office:spreadsheet><table:table table:name=\"weight\">"
							+ "<table:table-column table:default-cell-style-name=\"ce1\" "
							+ "table:style-name=\"co1\"/>"
							+ "<table:table-column table:default-cell-style-name=\"ce2\"/>");
			SimpleDateFormat format1 = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss");
			SimpleDateFormat format2 = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			while (cursor.moveToNext()) {
				bufferedWriter.write("<table:table-row><table:table-cell "
						+ "office:value-type=\"date\" office:date-value=\"");
				Date date = new Date(1000L * cursor.getInt(1));
				bufferedWriter.write(format1.format(date));
				bufferedWriter.write("\"><text:p>");
				bufferedWriter.write(format2.format(date));
				bufferedWriter
						.write("</text:p></table:table-cell>"
								+ "<table:table-cell office:value-type=\"float\" office:value=\"");
				String weight = "" + cursor.getInt(0) / 10.;
				bufferedWriter.write(weight);
				bufferedWriter.write("\"><text:p>");
				bufferedWriter.write(weight);
				bufferedWriter
						.write("</text:p></table:table-cell></table:table-row>");
			}

			bufferedWriter
					.write("</table:table></office:spreadsheet></office:body>"
							+ "</office:document-content>");
			bufferedWriter.close();
		} catch (IOException e) {
			showMessageDialog(R.string.failed_save_file, file);
			return;
		}

		sendFile(file, "application/vnd.oasis.opendocument.spreadsheet");
	}

	private void showExportPngDialog() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			showMessageDialog(R.string.media_not_available);
			return;
		}

		final View view = mActivity.getLayoutInflater().inflate(
				R.layout.export_png, null);
		final EditText fileInput = (EditText) view.findViewById(R.id.file);
		final EditText sizeXInput = (EditText) view.findViewById(R.id.size_x);
		final EditText sizeYInput = (EditText) view.findViewById(R.id.size_y);
		final EditText daysInput = (EditText) view.findViewById(R.id.days);
		fileInput.setText(new File(Environment.getExternalStorageDirectory(),
				"weight.png").toString());
		sizeXInput.setText("800");
		sizeYInput.setText("480");
		daysInput.setText("30");

		final GregorianCalendar date = new GregorianCalendar();
		final Button endDateButton = (Button) view.findViewById(R.id.end_date);
		endDateButton.setText(DateFormat.getDateFormat(mActivity).format(
				date.getTime()));
		endDateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new DatePickerDialog(mActivity,
						new DatePickerDialog.OnDateSetListener() {
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
								date.set(GregorianCalendar.YEAR, year);
								date.set(GregorianCalendar.MONTH, monthOfYear);
								date.set(GregorianCalendar.DATE, dayOfMonth);
								endDateButton.setText(DateFormat.getDateFormat(
										mActivity).format(date.getTime()));
							}
						}, date.get(GregorianCalendar.YEAR), date
								.get(GregorianCalendar.MONTH), date
								.get(GregorianCalendar.DATE)).show();
			}
		});

		new Runnable() {
			public void run() {
				final DialogInterface.OnClickListener callback = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((ViewManager) view.getParent()).removeView(view);
						run();
					}
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle(R.string.export_png);
				builder.setView(view);
				builder.setPositiveButton(R.string.export,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								int days;
								int sizeX;
								int sizeY;
								try {
									sizeX = Integer.parseInt(sizeXInput
											.getText().toString());
									sizeY = Integer.parseInt(sizeYInput
											.getText().toString());
								} catch (NumberFormatException e) {
									showMessageDialog(R.string.invalid_size,
											callback);
									return;
								}
								if (sizeX < 1 || sizeY < 1) {
									showMessageDialog(R.string.invalid_size,
											callback);
									return;
								}
								try {
									days = Integer.parseInt(daysInput.getText()
											.toString());
								} catch (NumberFormatException e) {
									showMessageDialog(R.string.invalid_days,
											callback);
									return;
								}
								if (days < 1) {
									showMessageDialog(R.string.invalid_days,
											callback);
									return;
								}
								exportPng(
										new File(fileInput.getText().toString()),
										sizeX,
										sizeY,
										date,
										days,
										((CheckBox) view
												.findViewById(R.id.transparency))
												.isChecked());
							}
						});
				builder.setNeutralButton(R.string.cancel, null);
				showDialog(builder);
			}
		}.run();
	}

	private void exportPng(File file, int sizeX, int sizeY,
			GregorianCalendar date, int days, boolean transparency) {
		Bitmap bitmap = Bitmap.createBitmap(sizeX, sizeY,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		if (!transparency) {
			canvas.drawColor(0xffffffff);
		}

		ChartDraw draw = new ChartDraw(mActivity, mDatabase, date);
		draw.mDays = days;
		draw.setSize(sizeX, sizeY);
		draw.draw(canvas);

		try {
			FileOutputStream output = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, output);
			output.close();
		} catch (IOException e) {
			showMessageDialog(R.string.failed_save_file, file);
			return;
		}

		sendFile(file, "image/png");
	}

	private void showEntryCountDialog() {
		Cursor cursor = mDatabase.query("SELECT COUNT(*) FROM weight");
		cursor.moveToNext();
		int count = cursor.getInt(0);
		cursor.close();
		showMessageDialog(String.format(mActivity.getResources()
				.getQuantityString(R.plurals.entry_count, count), count));
	}

	private void showDeleteAllEntriesDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(R.string.question_delete_all);
		builder.setPositiveButton(R.string.delete,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteAllEntries();
					}
				});
		builder.setNeutralButton(R.string.cancel, null);
		showDialog(builder);
	}

	private void deleteAllEntries() {
		mDatabase.exec("DELETE FROM weight");
		fillData();
		toast(R.string.all_entries_deleted);
	}
}

class EntryData {
	public int weight;
	public int created_at;
}

interface PromptCallback {
	public void run(String text);
}
