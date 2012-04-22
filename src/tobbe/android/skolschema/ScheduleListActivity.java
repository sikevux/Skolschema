package tobbe.android.skolschema;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class ScheduleListActivity extends ListActivity implements OnClickListener, OnItemClickListener, OnItemLongClickListener {
	
	private Button newSchedule;
	
	private File baseDir;
	
	private String[] scheduleNames;
	private String[] schedulePaths;
	
	private ArrayAdapter<String> adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_list);
		
		newSchedule = (Button) findViewById(R.id.newSchedule);
		newSchedule.setOnClickListener(this);
		
		// Create a File object representing the base dir of the program on the external storage
		baseDir = new File(Environment.getExternalStorageDirectory(), ScheduleActivity.BASE_DIR);

		// Fill the list
		refreshList();
		
		// Set OnClick-listeners
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);
	}

	public void refreshList() {
		// Create two temporary lists to store the names and paths in
		List<String> tmpScheduleNames = new LinkedList<String>();
		List<String> tmpSchedulePaths = new LinkedList<String>();
		
		// Fill the temporary lists
		for(File child : baseDir.listFiles()) {
			if(child.isDirectory()) {
				tmpScheduleNames.add(child.getName());
				tmpSchedulePaths.add(child.getAbsolutePath());
			}
		}
		
		// Convert the lists to arrays
		scheduleNames =  Arrays.asList(tmpScheduleNames.toArray()).toArray(new String[tmpScheduleNames.toArray().length]);
		schedulePaths = Arrays.asList(tmpSchedulePaths.toArray()).toArray(new String[tmpSchedulePaths.toArray().length]);
		
		// Recreate the list adapter
		adapter = new ArrayAdapter<String>(this, R.layout.schedule_list_item, R.id.scheduleNameField, scheduleNames);
		setListAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		returnSchedule(schedulePaths[position]);
	}	
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {	
		final int fPosition = position;
		
		// Ask the user if the schedule shall be deleted
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Ta bort schema");
		adb.setMessage("Vill du verkligen ta bort schemat: " + scheduleNames[position] + '?');
		
		adb.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteFolder(new File(schedulePaths[fPosition]));
				refreshList();
			}
		});
		adb.setNegativeButton("Nej", null);
		
		// Show the dialog
		adb.create().show();
		
		return false;
	}
	
	@Override
	public void onClick(View v) {
		if(v == newSchedule) {
			// Let the user add a schedule
			Intent i = new Intent(this, AddScheduleActivity.class);
			startActivityForResult(i, 0);
		}
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(data != null) {
	    	String scheduleFolder = data.getExtras().getString("schedule");
	    	returnSchedule(scheduleFolder);
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
	
	/* Deletes a folder. */
	public void deleteFolder(File folder) {
		for(File child : folder.listFiles()) {
			if(child.isDirectory()) {
				deleteFolder(child);
			} else {
				child.delete();
			}
		}
		
		folder.delete();
	}

}
