package tobbe.android.skolschema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddScheduleActivity extends Activity implements OnClickListener, OnCheckedChangeListener {

	private File baseDir;
	
	private EditText scheduleName;
	private Spinner school;
	private EditText password;
	private EditText studentID;
	private RadioGroup scheduleType;
	private TextView specificWeekLabel;
	private EditText specificWeekNumber;
	private CheckBox mainSchedule;
	private Button addSchedule;
	
	private String schoolID = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_schedule);
		
		baseDir = new File(Environment.getExternalStorageDirectory(), ScheduleActivity.BASE_DIR);
		
		scheduleName = (EditText) findViewById(R.id.scheduleName);
		school = (Spinner) findViewById(R.id.school);
		
		final String[] schoolSpinnerItems = getResources().getStringArray(R.array.schoolList);
		ArrayAdapter<String> schoolSpinnerAdapter = new ArrayAdapter<String>(this,
		        R.layout.school_spinner, schoolSpinnerItems);
		schoolSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		school.setAdapter(schoolSpinnerAdapter);
		school.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				switch(position) {
				case 0:
					schoolID = "19400";
					break;
				case 1:
					schoolID = "18600";
					break;
				case 2:
					schoolID = "26900";
					break;
				case 3:
					schoolID = "87850";
					break;
				case 4:
					schoolID = "27750";
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
		
		password = (EditText) findViewById(R.id.password);
		studentID = (EditText) findViewById(R.id.studentID);
		scheduleType = (RadioGroup) findViewById(R.id.scheduleType);
		scheduleType.setOnCheckedChangeListener(this);
		specificWeekLabel = (TextView) findViewById(R.id.specificWeekLabel);
		specificWeekNumber = (EditText) findViewById(R.id.specificWeekNumber);
		mainSchedule = (CheckBox) findViewById(R.id.mainSchedule);
		addSchedule = (Button) findViewById(R.id.addSchedule);
		addSchedule.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == addSchedule) {
			// Check the schedule name
			String sScheduleName = scheduleName.getText().toString();
			sScheduleName.trim();
			
			File scheduleDir = null;
			if(sScheduleName != "") {
				// Check so that a schedule with that name doesn't already exist
				scheduleDir = new File(baseDir, sScheduleName);
				
				if(scheduleDir.exists()) {
					Toast.makeText(this, "Upptaget schemanamn.", Toast.LENGTH_LONG).show();
					return;
				}
				
				scheduleDir.mkdir(); 
			} else {
				Toast.makeText(this, getResources().getString(R.string.tooShortSchedule), Toast.LENGTH_LONG).show();
				return;
			}
			
			// Check the student ID
			String sStudentID = studentID.getText().toString();
			sStudentID.trim();
			
			if(sStudentID == "") {
				Toast.makeText(this, getResources().getString(R.string.tooShortID), Toast.LENGTH_LONG).show();
				scheduleDir.delete();
				return;
			}
			
			// Open the info file
			try {
				PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(scheduleDir, ScheduleActivity.INFO_FILE))));
	
				out.println(schoolID);
				out.println(password.getText().toString());
				out.println(sStudentID);
				
				
				if(scheduleType.getCheckedRadioButtonId() == R.id.weeklyRadio) {
					out.println("weekly");
				} else if(scheduleType.getCheckedRadioButtonId() == R.id.dailyRadio) {
					out.println("daily");
				} else if(scheduleType.getCheckedRadioButtonId() == R.id.specificWeekRadio) {
					String sSpecificWeekNumber = specificWeekNumber.getText().toString();
					sSpecificWeekNumber.trim();
					
					/*
					 This is commented because of Fäladsgården.
			
					if(sSpecificWeekNumber == "") {
						Toast.makeText(this, "För kort specifik vecka.", Toast.LENGTH_LONG).show();
						out.close();
						scheduleDir.delete();
						return;
					}
					*/
					
					out.println("sweek");
					out.println(sSpecificWeekNumber);
				}
				
				out.close();
				
				if(mainSchedule.isChecked()) {
					PrintWriter msOut = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(baseDir, ScheduleActivity.MAIN_SCHEDULE_FILE))));
					msOut.println(scheduleDir.getAbsolutePath());
					msOut.close();
				}
				
			} catch(IOException e) {
				Toast.makeText(this, getResources().getString(R.string.couldNotWriteInfo) , Toast.LENGTH_LONG).show();
				scheduleDir.delete();
				e.printStackTrace();
			}

			returnSchedule(scheduleDir.getAbsolutePath());
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if(group == scheduleType) {
			if(checkedId == R.id.specificWeekRadio) {
				specificWeekLabel.setVisibility(View.VISIBLE);
				specificWeekNumber.setVisibility(View.VISIBLE);
			} else {
				specificWeekLabel.setVisibility(View.GONE);
				specificWeekNumber.setVisibility(View.GONE);
			}
		}
	}
	
	/* Leaves the activity and passes the selected schedule to the parent activity. */
	public void returnSchedule(String scheduleFolder) {
		// An intent to pass the return value
		Intent ret = new Intent();
		ret.putExtra("schedule", scheduleFolder);
		
		// Set the intent as the result
		setResult(Activity.RESULT_OK, ret);
		
		// Return from the activity
		finish();
	}
}