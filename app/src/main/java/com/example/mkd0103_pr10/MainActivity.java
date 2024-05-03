package com.example.mkd0103_pr10;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    // Список для хранения объектов валют
    public List<CurrencyV> currencyList = new ArrayList<>();
    // Выбранная валюта
    public CurrencyV selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Метод вызывается при создании активности
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Запуск асинхронной задачи для получения данных о валютах
        new FetchDataTask().execute();
    }

    public void AlertDialog(String title, String message){
        // Создает и отображает диалоговое окно с заданным заголовком и сообщением.
        // Диалоговое окно не может быть отменено и содержит кнопку “OK”, которая закрывает диалог.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("OK", (dialog, which) -> dialog.cancel());

        AlertDialog alter = builder.create();
        alter.show();
    }

    public void Consider(View view) {
        // Метод срабатывает при нажатии кнопки, он считывает значения из полей
        // и затем выполняет расчет в зависимости от состояния переключателя ToDollar.
        EditText amountField = findViewById(R.id.Amount);
        Switch toDollarSwitch = findViewById(R.id.ToDollar);
        TextView resultField = findViewById(R.id.Result);

        if (amountField.getText().length() <= 0) {
            AlertDialog("Уведомление", "Заполните все поля.");
            return;
        }

        float amount = Float.parseFloat(amountField.getText().toString());
        float rate = (float) selected.Value;
        float result;

        if (toDollarSwitch.isChecked()) {
            result = amount / rate;
            resultField.setText(result + selected.CharCode + ".");
        } else {
            result = amount * rate;
            resultField.setText(result + "р.");
        }
    }

    public void URL(View view) {
        // Метод открывает веб-страницу
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sberbank.ru/ru/quotes/currencies"));
        startActivity(intent);
    }

    private class FetchDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            // Метод выполняет запрос к API в фоновом потоке и возвращает результат в виде строки
            String result = "";
            try {
                URL url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result += line;
                }
                rd.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // Метод обрабатывает полученные данные и обновляет пользовательский интерфейс
            try {
                JSONObject json = new JSONObject(result);
                JSONObject valute = json.getJSONObject("Valute");

                Iterator<String> keys = valute.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject currency = valute.getJSONObject(key);
                    String name = currency.getString("Name");
                    String charCode = currency.getString("CharCode");
                    int nominal = currency.getInt("Nominal");
                    double value = currency.getDouble("Value");

                    if(nominal > 1)
                        value /= (double) nominal;

                    CurrencyV currencyV = new CurrencyV(name, charCode, value);
                    if(selected == null)
                        selected = currencyV;
                    currencyList.add(currencyV);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Spinner spinner = findViewById(R.id.CurrencySelection);
            List<String> names = new ArrayList<>();
            for (CurrencyV currency : currencyList) {
                names.add(currency.Name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Метод вызывается при выборе элемента в выпадающем списке
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    Optional<CurrencyV> c = currencyList.stream().filter(item -> item.Name.equals(selectedItem)).findFirst();
                    selected = c.get();
                    TextView t = findViewById(R.id.TextTranslate);
                    t.setText("Перевести в " + selected.CharCode);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Метод вызывается, когда ничего не выбрано в выпадающем списке
                }
            });
        }
    }

    public class CurrencyV{
        // Класс для хранения информации о валюте
        public String Name;
        public String CharCode;
        public double Value;
        CurrencyV(String Name, String CharCode, double Value)
        {
            this.Name = Name;
            this.CharCode = CharCode;
            this.Value = Value;
        }
    }
}
