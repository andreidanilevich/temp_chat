package by.andreidanilevich.temp_chat;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ChatActivity extends Activity {

	// ИМЯ СЕРВЕРА (url зарегистрированного нами сайта)
	// например http://l29340eb.bget.ru
	String server_name = "http://l29340eb.bget.ru";

	ListView lv; // полоса сообщений
	EditText et;
	Button bt;
	SQLiteDatabase chatDBlocal;
	String author, client;
	INSERTtoChat insert_to_chat; // класс отправляет новое сообщение на сервер
	UpdateReceiver upd_res; // класс ждет сообщение от сервиса и получив его -
							// обновляет ListView

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);

		// получим 2 переменные по которым будем отбирать инфу из БД:
		// author - от чьего имени идет чат
		// client - с кем чатимся
		Intent intent = getIntent();
		author = intent.getStringExtra("author");
		client = intent.getStringExtra("client");

		Log.i("chat", "+ ChatActivity - открыт author = " + author
				+ " | client = " + client);

		lv = (ListView) findViewById(R.id.lv);
		et = (EditText) findViewById(R.id.et);
		bt = (Button) findViewById(R.id.bt);

		chatDBlocal = openOrCreateDatabase("chatDBlocal.db",
				Context.MODE_PRIVATE, null);
		chatDBlocal
				.execSQL("CREATE TABLE IF NOT EXISTS chat (_id integer primary key autoincrement, author, client, data, text)");

		// Создаём и регистрируем широковещательный приёмник

		upd_res = new UpdateReceiver();
		registerReceiver(upd_res, new IntentFilter(
				"by.andreidanilevich.action.UPDATE_ListView"));

		create_lv();
	}

	// обновим lv = заполним нужные позиции в lv информацией из БД
	@SuppressLint("SimpleDateFormat")
	public void create_lv() {

		Cursor cursor = chatDBlocal.rawQuery(
				"SELECT * FROM chat WHERE author = '" + author
						+ "' OR author = '" + client + "' ORDER BY data", null);
		if (cursor.moveToFirst()) {
			// если в базе есть элементы соответствующие
			// нашим критериям отбора

			// создадим массив, создадим hashmap и заполним его результатом
			// cursor
			ArrayList<HashMap<String, Object>> mList = new ArrayList<HashMap<String, Object>>();
			HashMap<String, Object> hm;

			do {
				// мое сообщение !!!
				// если автор сообщения = автор
				// и получатель сообщения = клиент
				if (cursor.getString(cursor.getColumnIndex("author")).equals(
						author)
						&& cursor.getString(cursor.getColumnIndex("client"))
								.equals(client)) {

					hm = new HashMap<>();
					hm.put("author", author);
					hm.put("client", "");
					hm.put("list_client", "");
					hm.put("list_client_time", "");
					hm.put("list_author",
							cursor.getString(cursor.getColumnIndex("text")));
					hm.put("list_author_time", new SimpleDateFormat(
							"HH:mm - dd.MM.yyyy").format(new Date(cursor
							.getLong(cursor.getColumnIndex("data")))));
					mList.add(hm);

				}

				// сообщение мне !!!!!!!
				// если автор сообщения = клиент
				// и если получатель сообщения = автор
				if (cursor.getString(cursor.getColumnIndex("author")).equals(
						client)
						&& cursor.getString(cursor.getColumnIndex("client"))
								.equals(author)) {

					hm = new HashMap<>();
					hm.put("author", "");
					hm.put("client", client);
					hm.put("list_author", "");
					hm.put("list_author_time", "");
					hm.put("list_client",
							cursor.getString(cursor.getColumnIndex("text")));
					hm.put("list_client_time", new SimpleDateFormat(
							"HH:mm - dd.MM.yyyy").format(new Date(cursor
							.getLong(cursor.getColumnIndex("data")))));
					mList.add(hm);

				}

			} while (cursor.moveToNext());

			// покажем lv
			SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),
					mList, R.layout.list, new String[] { "list_author",
							"list_author_time", "list_client",
							"list_client_time", "author", "client" },
					new int[] { R.id.list_author, R.id.list_author_time,
							R.id.list_client, R.id.list_client_time,
							R.id.author, R.id.client });

			lv.setAdapter(adapter);
			cursor.close();

		}

		Log.i("chat",
				"+ ChatActivity ======================== обновили поле чата");

	}

	public void send(View v) {
		// запишем наше новое сообщение
		// вначале проверим на пустоту

		if (!et.getText().toString().trim().equals("")) {

			// кнопку сделаем неактивной
			bt.setEnabled(false);

			// если чтото есть - действуем!
			insert_to_chat = new INSERTtoChat();
			insert_to_chat.execute();

		} else {
			// если ничего нет - нечего и писать
			et.setText("");
		}
	}

	// отправим сообщение на сервер

	private class INSERTtoChat extends AsyncTask<Void, Void, Integer> {

		HttpURLConnection conn;
		Integer res;

		protected Integer doInBackground(Void... params) {

			try {

				// соберем линк для передачи новой строки
				String post_url = server_name
						+ "/chat.php?action=insert&author="
						+ URLEncoder.encode(author, "UTF-8")
						+ "&client="
						+ URLEncoder.encode(client, "UTF-8")
						+ "&text="
						+ URLEncoder.encode(et.getText().toString().trim(),
								"UTF-8");

				Log.i("chat",
						"+ ChatActivity - отправляем на сервер новое сообщение: "
								+ et.getText().toString().trim());

				URL url = new URL(post_url);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(10000); // ждем 10сек
				conn.setRequestMethod("POST");
				conn.setRequestProperty("User-Agent", "Mozilla/5.0");
				conn.connect();

				res = conn.getResponseCode();
				Log.i("chat", "+ ChatActivity - ответ сервера (200 - все ОК): "
						+ res.toString());

			} catch (Exception e) {
				Log.i("chat",
						"+ ChatActivity - ошибка соединения: " + e.getMessage());

			} finally {
				// закроем соединение
				conn.disconnect();
			}
			return res;
		}

		protected void onPostExecute(Integer result) {

			try {
				if (result == 200) {
					Log.i("chat", "+ ChatActivity - сообщение успешно ушло.");
					// сбросим набранный текст
					et.setText("");
				}
			} catch (Exception e) {
				Log.i("chat", "+ ChatActivity - ошибка передачи сообщения:\n"
						+ e.getMessage());
				Toast.makeText(getApplicationContext(),
						"ошибка передачи сообщения", Toast.LENGTH_SHORT).show();
			} finally {
				// активируем кнопку
				bt.setEnabled(true);
			}
		}
	}

	// ресивер приёмник ждет сообщения от FoneService
	// если сообщение пришло, значит есть новая запись в БД - обновим ListView
	public class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("chat",
					"+ ChatActivity - ресивер получил сообщение - обновим ListView");
			create_lv();
		}
	}

	// выходим из чата
	public void onBackPressed() {
		Log.i("chat", "+ ChatActivity - закрыт");
		unregisterReceiver(upd_res);
		finish();
	}
}
