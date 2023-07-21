package com.example.qr;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Info extends AppCompatActivity {
   // определение компонентов интерфейса
    String id; TextView Name;  Button change; EditText editDiametr;
    EditText editGost;   EditText editWeight;   EditText editLength;
    EditText editMaterial; EditText editProvider;  EditText editOrder;
    EditText editCoverage; EditText editLengthCoverage;  EditText editHardness;
    TextView ID;
    // переменный базы данных
    private DataBaseHelper mDBHelper;  private SQLiteDatabase mDb;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Initiating(); // инициализация компонентов интерфейса

        Bundle arguments = getIntent().getExtras();
        if(arguments!=null){
            id = arguments.get("id").toString();} // получение id по qr коду

       mDBHelper = new DataBaseHelper(this); // доступ к БД
       try {
            mDb = mDBHelper.getReadableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }


        String mQuery = "SELECT * FROM Изделие Where id = '"+id.trim()+"'"; // текст запроса
        Cursor mCur = mDb.rawQuery(mQuery, null); // выполнение запроса
        if (mCur != null) { // проверка существования записи
            if (mCur.moveToFirst()) { // помещение курсора на строчку
                // добавление(установка) данных в элементы
                Name.setText(mCur.getString(mCur.getColumnIndex("Наименование")));
                ID.setText(mCur.getString(mCur.getColumnIndex("id")));
                editGost.setText(mCur.getString(mCur.getColumnIndex("ГОСТ")));
                editWeight.setText(mCur.getString(mCur.getColumnIndex("Вес")));
                editLength.setText(mCur.getString(mCur.getColumnIndex("Длина")));
                editDiametr.setText(mCur.getString(mCur.getColumnIndex("Диаметр")));
                String id_Material = mCur.getString(mCur.getColumnIndex("id_Материал"));
                String id_Provider = mCur.getString(mCur.getColumnIndex("id_Поставщик"));
                editMaterial.setText(GetMaterial(mDb, id_Material));
                editProvider.setText(GetProvider(mDb, id_Provider));
                editOrder.setText(mCur.getString(mCur.getColumnIndex("id_Заказ")));
                editCoverage.setText(mCur.getString(mCur.getColumnIndex("МатериалПокрытия")));
                editLengthCoverage.setText(mCur.getString(mCur.getColumnIndex("ТолщинаПокрытия")));
                editHardness.setText(mCur.getString(mCur.getColumnIndex("Твердость")));
            }
            mCur.close(); // закрытие
        }

        change.setOnClickListener(new View.OnClickListener() { // событие нажатии кнопки
            @Override
            public void onClick(View view) {
                try {
                    mDBHelper.updateDataBase();
                } catch (IOException mIOException) {
                    throw new Error("UnableToUpdateDatabase");
                }
                try {
                    mDb = mDBHelper.getReadableDatabase();
                } catch (SQLException mSQLException) {
                    throw mSQLException;
                }

                String mQuery2 = "SELECT * FROM Заказ Where id_Заказ = '" + editOrder.getText().toString() +"'"; // запрос
                Cursor mCur2 = mDb.rawQuery(mQuery2, null); // выполнение запроса

                if (mCur2.getCount() <= 0) { // проверка наличия в бд
                   Toast.makeText(Info.this, "Данного заказа не существует", Toast.LENGTH_LONG).show();

                }
                else {
                    try {
                        mDb = mDBHelper.getWritableDatabase();
                    } catch (SQLException mSQLException) {
                        throw mSQLException;
                    }
                    // запись в бд
                        String qr = "INSERT INTO Изменения VALUES ('" + id + "'," + "'" + Name.getText().toString() + "',"
                                + Double.parseDouble(editWeight.getText().toString()) + "," +
                                Double.parseDouble(editLength.getText().toString()) + ",'" + editOrder.getText().toString()
                                + "', '"+ GetData() +"')";// запрос на вставку
                        mDb.execSQL(qr);// выполнение запроса

                    String query = "UPDATE Изделие SET Вес ="+ Double.parseDouble(editWeight.getText().toString()) + ", Длина ="
                            + Double.parseDouble(editLength.getText().toString()) +", id_Заказ = '"+editOrder.getText().toString()+
                            "' WHERE id = '"+ id+"'"; // запрос на обновление
                    mDb.execSQL(query); // выполнение запроса

                    Toast.makeText(Info.this, "Данные успешно изменены", Toast.LENGTH_LONG).show();
                    mCur2.close(); // закрытие
                }


            }
        });
    }

    public String GetProvider(SQLiteDatabase mDb, String id_Provider) {
        // получение наименования поставщика
        String mQuery2 = "SELECT * FROM Поставщик Where id_Поставщик = " + id_Provider; // запрос
        Cursor mCur2 = mDb.rawQuery(mQuery2, null); // выполнение запроса
        if (mCur2 != null) { // проверка наличия в бд
            if (mCur2.moveToFirst()) { // помещение курсора
               return mCur2.getString(mCur2.getColumnIndex("Наименование")); // установка значения
            }
       }
        return "";
    }
    public String GetMaterial(SQLiteDatabase mDb, String id_Material) {
        // получение наименования материала
        String Query = "SELECT * FROM Материал Where id_Материала = " + id_Material; // запрос
        Cursor cur = mDb.rawQuery(Query, null); // выполнение запроса
        if (cur != null) { // проверка наличия в бд
            if (cur.moveToFirst()) { // помещение курсора
              return  cur.getString(cur.getColumnIndex("Наименование")); // установка значения
            }
        }
        return "";
    }

      public String  GetData(){
          GregorianCalendar curent = new GregorianCalendar();
          Locale lokal = new Locale("ru", "RU");
          DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, lokal);
          return df.format(curent.getTime());
        }

    @Override
    public void onBackPressed() { // открытие камеры для распознования
        Intent intent = new Intent(Info.this, Menu.class);
        startActivity(intent);
    }

    private void Initiating() { // метод инициализации
        Name =  findViewById(R.id.Name);
        editGost = findViewById(R.id.editGost);
        editWeight = findViewById(R.id.editWeight);
        editLength = findViewById(R.id.editLength);
        editDiametr = findViewById(R.id.editDiametr);
        editMaterial = findViewById(R.id.editMaterial);
        editProvider = findViewById(R.id.editProvider);
        editOrder = findViewById(R.id.editOrder);
        editCoverage = findViewById(R.id.editCoverage);
        editLengthCoverage = findViewById(R.id.editLengthCoverage);
        editHardness = findViewById(R.id.editHardness);
        ID = findViewById(R.id.IDtext);
        change = findViewById(R.id.change); // кнопка изменить
    }
}