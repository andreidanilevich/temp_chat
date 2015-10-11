package by.andreidanilevich.temp_chat;

import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {

	// ИМЯ СЕРВЕРА (url зарегистрированного нами сайта)
	// например http://l29340eb.bget.ru
	String server_name = "http://l29340eb.bget.ru";

	Spinner spinner_author, spinner_client;
	String author, client;
	Button open_chat_btn, open_chat_reverce_btn, delete_server_chat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.i("chat", "+ MainActivity - запуск приложения");

		open_chat_btn = (Button) findViewById(R.id.open_chat_btn);
		open_chat_reverce_btn = (Button) findViewById(R.id.open_chat_reverce_btn);
		delete_server_chat = (Button) findViewById(R.id.delete_server_chat);

		// запустим FoneService
		this.startService(new Intent(this, FoneService.class));

		// заполним 2 выпадающих меню для выбора автора и получателя сообщения
		// 5 мужских и 5 женских имен
		// установим слушателей
		spinner_author = (Spinner) findViewById(R.id.spinner_author);
		spinner_client = (Spinner) findViewById(R.id.spinner_client);

		spinner_author.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, new String[] { "Петя",
						"Вася", "Коля", "Андрей", "Сергей", "Оля", "Лена",
						"Света", "Марина", "Наташа" }));
		spinner_client.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, new String[] { "Петя",
						"Вася", "Коля", "Андрей", "Сергей", "Оля", "Лена",
						"Света", "Марина", "Наташа" }));
		spinner_client.setSelection(5);

		open_chat_btn.setText("Открыть чат: "
				+ spinner_author.getSelectedItem().toString() + " > "
				+ spinner_client.getSelectedItem().toString());
		open_chat_reverce_btn.setText("Открыть чат: "
				+ spinner_client.getSelectedItem().toString() + " > "
				+ spinner_author.getSelectedItem().toString());

		spinner_author
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View itemSelected, int selectedItemPosition,
							long selectedId) {

						author = spinner_author.getSelectedItem().toString();

						open_chat_btn.setText("Открыть чат: "
								+ spinner_author.getSelectedItem().toString()
								+ " > "
								+ spinner_client.getSelectedItem().toString());
						open_chat_reverce_btn.setText("Открыть чат: "
								+ spinner_client.getSelectedItem().toString()
								+ " > "
								+ spinner_author.getSelectedItem().toString());
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

		spinner_client
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View itemSelected, int selectedItemPosition,
							long selectedId) {

						client = spinner_client.getSelectedItem().toString();

						open_chat_btn.setText("Открыть чат: "
								+ spinner_author.getSelectedItem().toString()
								+ " > "
								+ spinner_client.getSelectedItem().toString());
						open_chat_reverce_btn.setText("Открыть чат: "
								+ spinner_client.getSelectedItem().toString()
								+ " > "
								+ spinner_author.getSelectedItem().toString());
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
	}

	// откроем чат с выбранным автором и получателем
	public void open_chat(View v) {
		// быстрая проверка
		if (author.equals(client)) {
			// если автор и получатель одинаковы
			// чат не открываем
			Toast.makeText(this, "author = client !", Toast.LENGTH_SHORT)
					.show();
		} else {
			// откроем нужный чат author > client
			Intent intent = new Intent(MainActivity.this, ChatActivity.class);
			intent.putExtra("author", author);
			intent.putExtra("client", client);
			startActivity(intent);
		}
	}

	// откроем чат с выбранным автором и получателем, только наоборот
	public void open_chat_reverce(View v) {
		// быстрая проверка
		if (author.equals(client)) {
			// если автор и получатель одинаковы
			// чат не открываем
			Toast.makeText(this, "author = client !", Toast.LENGTH_SHORT)
					.show();
		} else {
			// откроем нужный чат client > author
			Intent intent = new Intent(MainActivity.this, ChatActivity.class);
			intent.putExtra("author", client);
			intent.putExtra("client", author);
			startActivity(intent);
		}
	}

	// отправим запрос на сервер о удалении таблицы с чатами
	public void delete_server_chats(View v) {

		Log.i("chat", "+ MainActivity - запрос на удаление чата с сервера");

		delete_server_chat.setEnabled(false);
		delete_server_chat.setText("Запрос отправлен. Ожидайте...");

		DELETEfromChat delete_from_chat = new DELETEfromChat();
		delete_from_chat.execute();
	}

	// удалим локальную таблицу чатов
	// и создадим такуюже новую
	public void delete_local_chats(View v) {

		Log.i("chat", "+ MainActivity - удаление чата с этого устройства");

		SQLiteDatabase chatDBlocal;
		chatDBlocal = openOrCreateDatabase("chatDBlocal.db",
				Context.MODE_PRIVATE, null);
		chatDBlocal.execSQL("drop table chat");
		chatDBlocal
				.execSQL("CREATE TABLE IF NOT EXISTS chat (_id integer primary key autoincrement, author, client, data, text)");

		Toast.makeText(getApplicationContext(),
				"Чат на этом устройстве удален!", Toast.LENGTH_SHORT).show();
	}

	// отправим запрос на сервер о удалении таблицы с чатами
	// если он пройдет - таблица будет удалена
	// если не пройдет (например нет интернета или сервер недоступен)
	// - покажет сообщение
	private class DELETEfromChat extends AsyncTask<Void, Void, Integer> {

		Integer res;
		HttpURLConnection conn;

		protected Integer doInBackground(Void... params) {

			try {
				URL url = new URL(server_name + "/chat.php?action=delete");
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(10000); // ждем 10сек
				conn.setRequestMethod("POST");
				conn.setRequestProperty("User-Agent", "Mozilla/5.0");
				conn.connect();
				res = conn.getResponseCode();
				Log.i("chat", "+ MainActivity - ответ сервера (200 = ОК): "
						+ res.toString());

			} catch (Exception e) {
				Log.i("chat",
						"+ MainActivity - ответ сервера ОШИБКА: "
								+ e.getMessage());
			} finally {
				conn.disconnect();
			}

			return res;
		}

		protected void onPostExecute(Integer result) {

			try {
				if (result == 200) {
					Toast.makeText(getApplicationContext(),
							"Чат на сервере удален!", Toast.LENGTH_SHORT)
							.show();
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						"Ошибка выполнения запроса.", Toast.LENGTH_SHORT)
						.show();
			} finally {
				// сделаем кнопку активной
				delete_server_chat.setEnabled(true);
				delete_server_chat.setText("Удалить все чаты на сервере!");
			}
		}
	}

	public void onBackPressed() {
		Log.i("chat", "+ MainActivity - выход из приложения");
		finish();
	}
}
