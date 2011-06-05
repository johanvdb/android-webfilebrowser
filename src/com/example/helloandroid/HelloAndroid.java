package com.example.helloandroid;

import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HelloAndroid extends ListActivity {

	private String path = "http://floppy.vdb.local/browser/index.php/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new DownloadTask().execute(path);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 2, 0, "Quit");
		return true;
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == 2)
			this.finish();
		return true;
	}

	public void showList(ArrayList<String> list) {
		Toast.makeText(getApplicationContext(), "Got good response",
				Toast.LENGTH_LONG).show();
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list));
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				new DownloadTask().execute(path + "/"
						+ ((TextView) view).getText());
			}
		});
	}

	public void showError(String error) {
		AlertDialog.Builder errorBox = new AlertDialog.Builder(this);
		errorBox.setMessage("An error has occurred: " + error);
		errorBox.setNeutralButton("Ok", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
		errorBox.show();
	}

	private class DownloadTask extends
			AsyncTask<String, Integer, ArrayList<String>> {

		private Exception error = null;

		@Override
		protected ArrayList<String> doInBackground(String... urls) {
			final ArrayList<String> items = new ArrayList<String>();

			for (String url : urls) {
				try {
					HttpGet getRequest = new HttpGet(url);
					HttpClient client = new DefaultHttpClient();
					HttpResponse response = client.execute(getRequest);

					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser parser = factory.newSAXParser();
					parser.parse(response.getEntity().getContent(),
							new DefaultHandler() {
								@Override
								public void startElement(String uri,
										String localName, String qName,
										Attributes attributes)
										throws SAXException {
									if (localName.equals("file")) {
										items.add(attributes.getValue("name"));
									}
								}
							});
				} catch (Exception e) {
					Log.e(this.toString(), e.toString(), e);
					error = e;
					return null;
				}
			}
			return items;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			if (error != null) {
				showError(error.getMessage());
			} else {
				showList(result);
			}
		}

	}
}