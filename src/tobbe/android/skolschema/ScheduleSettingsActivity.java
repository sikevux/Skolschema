package tobbe.android.skolschema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import android.app.Activity;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleSettingsActivity extends Activity implements OnCheckedChangeListener, OnClickListener {

	private Spinner school;
	private EditText password;
	private EditText studentID;
	private RadioGroup scheduleType;
	private TextView specificWeekLabel;
	private EditText specificWeekNumber;
	private CheckBox mainSchedule;
	private Button saveSettings;
	
	private File scheduleDir;
	private File baseDir;
	
	private String schoolID = null;
	
	
	/**
	 * This code block is also found in AddScheduleActivity.java.
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_settings);

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
		saveSettings = (Button) findViewById(R.id.saveSettings);
		saveSettings.setOnClickListener(this);
		
		baseDir = new File(Environment.getExternalStorageDirectory(), ScheduleActivity.BASE_DIR);
		
		String schedulePath = getIntent().getExtras().getString("schedule");
		scheduleDir = new File(schedulePath);
		
		try {	
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(scheduleDir, ScheduleActivity.INFO_FILE))));
		
			String schoolID = in.readLine();
			this.schoolID = schoolID;
			
			if(schoolID.equals("19400")) {
				school.setSelection(0);
			} else if(schoolID.equals("18600")) {
				school.setSelection(1);
			} else if(schoolID.equals("26900")) {
				school.setSelection(2);
			} else if(schoolID.equals("87850")) {
				school.setSelection(3);
			} else if(schoolID.equals("27750")) {
				school.setSelection(4);
			}
			
			password.setText(in.readLine());
			studentID.setText(in.readLine());
			
			String type = in.readLine();
			
			if(type.equals("weekly")) {
				((RadioButton) findViewById(R.id.weeklyRadio)).setChecked(true);	
			} else if(type.equals("daily")) {
				((RadioButton) findViewById(R.id.dailyRadio)).setChecked(true);	
			} else {
				String specificWeek = in.readLine();
				specificWeekNumber.setText(specificWeek);
				((RadioButton) findViewById(R.id.specificWeekRadio)).setChecked(true);	
			}
			
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			finish();
		}
		
		try {
			BufferedReader msIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(baseDir, ScheduleActivity.MAIN_SCHEDULE_FILE))));
			
			if(msIn.readLine().equals(scheduleDir.getAbsolutePath())) {
				mainSchedule.setChecked(true);
			}
			
			msIn.close();
		} catch (IOException e) {
			// No main schedule has been set
		}
        
	}
	
	@Override
	public void onClick(View v) {
		if(v == saveSettings) {
			// Check the student ID
			String sStudentID = studentID.getText().toString();
			sStudentID.trim();
			
			if(sStudentID == "") {
				Toast.makeText(this, getResources().getString(R.string.tooShortID), Toast.LENGTH_LONG).show();
				scheduleDir.delete();
				return;
			}
			
			/*
			 * Let's make sure that the students don't need to be able to read instructions
			 * to use the application.
			 */
			sStudentID = sStudentID.replaceAll("\\s+", "");

			if( Pattern.compile("^(\\d{10}|\\d{13}|\\d{12})$").matcher(sStudentID).matches() ) {
				if( Pattern.compile("^(\\d{12}|\\d{13})$").matcher(sStudentID).matches() ) {
					sStudentID = sStudentID.substring(2);
				}
				sStudentID = new StringBuffer(sStudentID).insert(6, "-").toString();
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
					
					if(sSpecificWeekNumber == "") {
						Toast.makeText(this, getResources().getString(R.string.tooShortWeek), Toast.LENGTH_LONG).show();
						out.close();
						return;
					}
					
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
				Toast.makeText(this, getResources().getString(R.string.couldNotWriteInfo), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
			finish();
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
}