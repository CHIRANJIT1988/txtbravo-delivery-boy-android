package educing.tech.salesperson.alert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import educing.tech.salesperson.helper.OnTaskCompleted;


public class CustomAlertDialog 
{

	private Context context;
	private OnTaskCompleted listener;
	
	
	public CustomAlertDialog(Context _context, OnTaskCompleted listener)
	{
		this.listener = listener;
		this.context = _context;
	}


	// Alert dialog for save button
	public void showOKDialog(String title, String message, final String action)
	{

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialogBuilder.setTitle(title);

		alertDialogBuilder.setMessage(message).setCancelable(false) // set dialog message

				// Yes button click action
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//listener.onTaskCompleted(true, action);
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create(); // create alert dialog

		alertDialog.show(); // show it
	}


	public void showMessageDialog(String title, String message)
	{

		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(message);

		// Setting Cancelable
		alertDialog.setCancelable(true);

		// Showing Alert Message
		alertDialog.show();
	}


	// Alert dialog for save button
	public void showViewDialog(String title, String message, final String action)
	{

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialogBuilder.setTitle(title);

		// Setting Dialog Message
		alertDialogBuilder.setMessage(message);


		alertDialogBuilder.setMessage(message).setCancelable(true) // set dialog message

				// Yes button click action
				.setPositiveButton("View", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						listener.onTaskCompleted(true, 200, action);
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create(); // create alert dialog

		alertDialog.show(); // show it
	}
}