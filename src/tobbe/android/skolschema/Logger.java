package tobbe.android.skolschema;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Logger {
	
	/* Class from my game: Current for Android */
	
	// Application context
	private static Context context;
	
	public static void initialize(Context context) {
		Logger.context = context;
	}

	public static void log(String msg) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(new File("/sdcard/Skolschema/logg.txt"), true));
			
			// Write the date
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd H:m");
		    StringBuilder dateStringBuilder = new StringBuilder(dateFormat.format(new Date()));
		    
		    out.append(dateStringBuilder.toString());
			
		    // Write the message
			out.append(" : " + msg + '\n');
			
			out.close();
		} catch(IOException e) {
			warningMessage("Kunde ej skriva till loggfilen.");
		}
	}
	
	public static void debug(String msg) {
		Log.d("Skolschema", msg);
	}
	
	public static void warningMessage(String msg) {
		Toast.makeText(context, "VARNING: " + msg, Toast.LENGTH_LONG).show();
	}
	

	public static void errorMessage(String msg) {
		Toast.makeText(context, "FEL: " + msg, Toast.LENGTH_LONG).show();
	}
}
