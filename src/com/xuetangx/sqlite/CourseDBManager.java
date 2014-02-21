package com.xuetangx.sqlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.xuetangx.util.ConstantUtils;

public class CourseDBManager extends DBManager {
	private CourseDBHelper helper;
	public CourseDBManager(Context c) {
		helper = new CourseDBHelper(c);
		db = helper.getWritableDatabase();
		// TODO Auto-generated constructor stub
	}
	public void getDatabase() {
		if(!db.isOpen()) {
			db = helper.getWritableDatabase();
		}
	}
	public boolean refreshData(String json, String courseID) {
		getDatabase();
		ContentValues value = new ContentValues();
		value.put("course_data", json);
		value.put("course_id", courseID);
		int num  = db.update(ConstantUtils.T_COURSE_DATA, value, "course_id = ?", new String[]{courseID});
		if(num == 0) {
			long num2 = db.insert(ConstantUtils.T_COURSE_DATA, null, value);
			if (num2 == -1) {
				return false;
			}else {
				return true;
			}
		}
		return true;
	}
	public boolean refreshCourseList(List<HashMap<String, Object>> data) {
		return refresh(data, ConstantUtils.T_COURSE);
	}
	private boolean refresh(List<HashMap<String, Object>> data, String table) {
		getDatabase();
		db.beginTransaction();
		try{
			db.execSQL("delete from " + table);
			for(HashMap<String, Object> item : data) {
				ContentValues values = new ContentValues();
				Iterator iter = item.entrySet().iterator(); 
				while (iter.hasNext()) { 
					Map.Entry entry = (Map.Entry) iter.next(); 
					values.put(entry.getKey().toString(), entry.getValue().toString());
				} 
				db.insert(table, null, values);
			}
			db.setTransactionSuccessful();
			return true;
		}catch(Exception e){
			return false;
		}
		finally{
			db.endTransaction();
		}	
	}
	public boolean refreshEnrollment(List<HashMap<String, Object>> data, String username) {
		for(HashMap<String, Object> item: data) {
			item.put("username", username);
		}
		return refresh(data, ConstantUtils.T_ENROLLMENT);
		
	}
	public List<HashMap<String, String>> queryCourseList() {
		List courseList = new ArrayList<HashMap<String, String>>();
		return courseList;
	}
	public String queryOneCourseData(String courseID) {
		Cursor c = null;
		try{
			c = db.query(ConstantUtils.T_COURSE_DATA, new String[]{"course_data"}, "course_id = ?", new String[]{courseID}, null, null, null, "1");
			if( c.getColumnCount() == 0) {
				return null;
			}else {
				return c.getString(c.getColumnIndex("course_data"));
			}
		}
		finally{
			if(c != null) {
				c.close();
			}
		}
	}
	public HashMap<String, String> getEnrollmentItem(Cursor c) {
		HashMap<String, String> map = new HashMap<String, String>();
		try{
			map.put("course_id", c.getString(c.getColumnIndex("course_id")));
			map.put("display_name", c.getString(c.getColumnIndex("display_name")));
			map.put("display_org", c.getString(c.getColumnIndex("display_org")));
			map.put("start", c.getString(c.getColumnIndex("start")));
			map.put("course_image_url", c.getString(c.getColumnIndex("course_image_url")));
		}catch(Exception e) {
			
		}
		return map;
	}
	public List<HashMap<String, String>> queryEnrollment(String username) {
		List enrollment = new ArrayList<HashMap<String, String>>();
		Cursor c = null;
		try{
			c = db.query(ConstantUtils.T_ENROLLMENT, null, "username = ?", new String[]{username}, null, null, null);
			c.moveToFirst();
			while (!c.isAfterLast()) {
				enrollment.add(getEnrollmentItem(c));
				c.moveToNext();
			}
		}finally{
			if (c != null) {
				c.close();
			}
		}
		return enrollment;
	}
	/**
	 * this must close 
	 */
	public void closeDB() {
		db.close();
	}

}
