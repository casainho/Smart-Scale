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

//import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
//import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//import android.util.Log;

public class Database {
	private SQLiteDatabase mDatabase;
	private SQLiteOpenHelper mHelper;

	public Database(Context context) {
		mHelper = new SQLiteOpenHelper(context, "data", null, 1) {
			@Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL("CREATE TABLE weight (_id INTEGER PRIMARY KEY AUTOINCREMENT, weight, created_at)");
				db.execSQL("CREATE INDEX weight_created_at ON weight (created_at)");
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
			}
		};
		mDatabase = mHelper.getWritableDatabase();
	}

	public void close() {
		mHelper.close();
	}

	public void beginTransaction() {
		mDatabase.beginTransaction();
	}

	public void endTransaction() {
		mDatabase.endTransaction();
	}

	public void setTransactionSuccessful() {
		mDatabase.setTransactionSuccessful();
	}

	public void exec(String query) {
		mDatabase.execSQL(query);
	}

	public void exec(String query, Object[] values) {
		mDatabase.execSQL(query, values);
	}

	public Cursor query(String query) {
		return mDatabase.rawQuery(query, null);
	}
}
