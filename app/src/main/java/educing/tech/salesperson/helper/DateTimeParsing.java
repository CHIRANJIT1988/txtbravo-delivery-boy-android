package educing.tech.salesperson.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateTimeParsing 
{

	public static long calculateHourDifference(String complain_date)
	{

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try
		{

			Date date = format.parse(complain_date);

			long diff = (System.currentTimeMillis() - date.getTime()) / (60 * 1000);

			return diff;
		}

		catch (ParseException e)
		{
			e.printStackTrace();
		}

		return 0;
	}


	public static String dateTimeFormat(String str)
	{

		String datetime = "";

		try
		{

			String[] parts = str.split(" ")[0].split("-");

			int year = Integer.parseInt(parts[0]);
			int month = Integer.parseInt(parts[1]);
			int date = Integer.parseInt(parts[2]);

			final Calendar c = Calendar.getInstance();


			if(c.get(Calendar.YEAR) == year)
			{

				if(c.get(Calendar.DATE) == date && (c.get(Calendar.MONTH) + 1) == month)
				{

					String[] time = str.split(" ")[1].split(":");
					datetime = setDateTime(Integer.parseInt(time[0]), Integer.parseInt(time[1]));
				}

				else
				{

					switch(month)
					{

						case 1:

							datetime = "Jan " + date;
							break;

						case 2:

							datetime = "Feb " + date;
							break;

						case 3:

							datetime = "Mar " + date;
							break;

						case 4:

							datetime = "Apr " + date;
							break;

						case 5:

							datetime = "May " + date;
							break;

						case 6:

							datetime = "Jun " + date;
							break;

						case 7:

							datetime = "Jul " + date;
							break;

						case 8:

							datetime = "Aug " + date;
							break;

						case 9:

							datetime = "Sep " + date;
							break;

						case 10:

							datetime = "Oct " + date;
							break;

						case 11:

							datetime = "Nov " + date;
							break;

						case 12:

							datetime = "Dec " + date;
							break;
					}
				}
			}

			else
			{
				datetime =  date + "/" + month + "/" + year;
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		return datetime;
	}


	public static String setDateTime(int hour, int minute)
	{

		String timeSet = "";

		if (hour > 12)
		{
			hour -= 12;
			timeSet = "PM";
		}

		else if (hour == 0)
		{
			hour += 12;
			timeSet = "AM";
		}

		else if (hour == 12)
		{
			timeSet = "PM";
		}

		else
		{
			timeSet = "AM";
		}


		String minutes;

		if (minute < 10)
		{
			minutes = "0" + minute;
		}

		else
		{
			minutes = String.valueOf(minute);
		}

		return String.valueOf(hour + ":" + minutes + " " + timeSet);
	}


	public static boolean escalateTime(String complaint_datetime)
	{

		SimpleDateFormat parser = new SimpleDateFormat("HH:mm");

		try
		{

			Date start_time = parser.parse("4:00");
			Date end_time = parser.parse("21:00");


			Date complaint_time = parser.parse(complaint_datetime.split(" ")[1]);


			if (complaint_time.after(start_time) && complaint_time.before(end_time))
			{
				return true;
			}

			return false;
		}

		catch (ParseException e)
		{
			// Invalid date was entered
		}

		return false;
	}
}