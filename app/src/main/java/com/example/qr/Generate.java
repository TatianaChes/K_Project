package com.example.qr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.WriterException;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Generate extends AppCompatActivity {
  // символы применяемые в генерации индентификатора
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    static String generateId = ""; // индентификатор

    // компоненты интерфейса
    Bitmap bitmap;  QRGEncoder qrgEncoder; Context context;
    ImageView qrCodeIV;  EditText dataEdt;  Button generateQrBtn;  Button saveimage;
    EditText editGost;  EditText editWeight;  EditText editLength;
    Spinner editMaterial;  Spinner editProvider;  Spinner editOrder;
    EditText editCoverage;  EditText editLengthCoverage;   EditText editHardness;
    Button btnOrder, btnProvider; EditText editDiametr;

    private DataBaseHelper mDBHelper;
    private SQLiteDatabase mDb;

    int countP; // переменная для получения количества записей в бд

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        Initiating(); // инициализация переменных
        mDBHelper = new DataBaseHelper(this);
        try {
            mDb = mDBHelper.getReadableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
        SpinnerChoise("Материал",editMaterial, mDb);
        SpinnerChoise("Заказ",editOrder, mDb); // заполнение спиннеров при открытии
        SpinnerChoise("Поставщик",editProvider, mDb);


        // разрешение на чтение и запись бд
        ActivityCompat.requestPermissions(Generate.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        ActivityCompat.requestPermissions(Generate.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        btnProvider.setOnClickListener(new View.OnClickListener() { // добавление поставщика

            @Override
            public void onClick(View view) {

                String selectQuery = "SELECT * FROM Поставщик"; // запрос
                Cursor cursor = mDb.rawQuery(selectQuery, null); // выполнение запроса
                countP =  cursor.getCount(); // получение количества
                cursor.close(); // закрытие

                // строительство диалогового окна в этой активности
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(Generate.this);

                //  получение подготовленного окна из файла ресурсов
                View mView = getLayoutInflater().inflate(R.layout.provider, null);

                // получение элементов
                EditText prName = (EditText) mView.findViewById(R.id.inputName);
                Button mTimerOk = (Button) mView.findViewById(R.id.btnTimerOk);
                Button mTimerCancel = (Button) mView.findViewById(R.id.btnTimerCancel);

                // создание
                mBuilder.setView(mView);
                final AlertDialog timerDialog = mBuilder.create();

                //  установка прослушивателей на кнопки
                mTimerOk.setOnClickListener(new View.OnClickListener() { // кнопка сохранить
                    @Override
                    public void onClick (View view) {

                        if (!prName.getText().toString().isEmpty()) { // проверка на пустоту
                            try {
                                mDb = mDBHelper.getWritableDatabase();
                            } catch (SQLException mSQLException) {
                                throw mSQLException;
                            }
                            // запрос
                            String query = "INSERT INTO Поставщик VALUES (" +(countP+1)+",'"+prName.getText().toString()+"')";
                            mDb.execSQL(query); // выполнение запроса
                            timerDialog.dismiss(); // закрытие окна
                            Toast.makeText(Generate.this, "Поставщик успешно добавлен", Toast.LENGTH_LONG).show();
                            SpinnerChoise("Поставщик",editProvider, mDb); // вставка полученных значений в спинер, для последующего выбора

                        } else { // если поле пустое -> вывод сообщения
                            Toast.makeText(Generate.this, "Поле не заполнено!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                //  нажатие кнопки отмена
                mTimerCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View view) {  timerDialog.dismiss(); } // закрытие окна
                }); //  Finally, показ Dialog
                timerDialog.show();
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() { // добавление заказа
            @Override
            public void onClick(View view) {

                // строительство диалогового окна в этой активности
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(Generate.this);

                //  получение подготовленного окна из файла ресурсов
                View mView = getLayoutInflater().inflate(R.layout.orger, null);

                // получение элементов
                EditText ord =  mView.findViewById(R.id.inputId);
                EditText ordName =  mView.findViewById(R.id.inputName);
                Button mTimerOk =  mView.findViewById(R.id.btnTimerOk);
                Button mTimerCancel =  mView.findViewById(R.id.btnTimerCancel);

                // создание
                mBuilder.setView(mView);
                final AlertDialog timerDialog = mBuilder.create();

                //  добавление простушивателей
                mTimerOk.setOnClickListener(new View.OnClickListener() { // нажатие сохранить
                    @Override
                    public void onClick (View view) {
                        // проверка на пустоту
                        if (!ord.getText().toString().isEmpty() && !ordName.getText().toString().isEmpty()) {
                            // запрос
                            try {
                                mDb = mDBHelper.getWritableDatabase();
                            } catch (SQLException mSQLException) {
                                throw mSQLException;
                            }
                            String query = "INSERT INTO Заказ VALUES ('"+ord.getText().toString()+"',"+ "'"+ordName.getText().toString()+"')";
                            mDb.execSQL(query); // выполнение запроса
                            Toast.makeText(Generate.this, "Заказ успешно добавлен", Toast.LENGTH_LONG).show();
                            timerDialog.dismiss(); // закрытие окна
                            SpinnerChoise("Заказ",editOrder, mDb); // вставка полученных значений в спиннер

                        } else {
                            // сообщение
                            Toast.makeText(Generate.this, "Не все поля заполнены!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                //  нажатие отмена
                mTimerCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View view) { timerDialog.dismiss(); } // закрытие окна
                });  //  Finally показ Dialog
                timerDialog.show();
            }
        });

        saveimage.setOnClickListener(new View.OnClickListener() { // кнопка сохранить
            @Override
            public void onClick(View view) {

                if(dataEdt.length() == 0 || editWeight.length() == 0 || editLength.length() == 0 || editDiametr.length() == 0
                || qrCodeIV.getDrawable() == null){
                    Toast.makeText(Generate.this, "Не все обязательные поля заполнены!", Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        mDb = mDBHelper.getReadableDatabase();
                    } catch (SQLException mSQLException) {
                        throw mSQLException;
                    }



                    // получение данных с формы
                    String Name = dataEdt.getText().toString();
                    String gost = editGost.getText().toString();
                    double weight = Double.parseDouble(editWeight.getText().toString());
                    double length = Double.parseDouble(editLength.getText().toString());
                    double diametr = Double.parseDouble(editDiametr.getText().toString());
                    String material = editMaterial.getSelectedItem().toString();
                    String order = editOrder.getSelectedItem().toString();
                    String provider = editProvider.getSelectedItem().toString();
                    String coverage = editCoverage.getText().toString();
                    String hardness = editHardness.getText().toString();
                    String LengthCoverage = editLengthCoverage.getText().toString();


                   provider = GetID("id_Поставщик","Поставщик",provider, mDb);
                   order = GetID("id_Заказ","Заказ",order, mDb);
                   material =  GetID("id_Материала","Материал",material, mDb);
                    // получение id поставщика по наименованию


                    // запись в бд
                    String query = "INSERT INTO Изделие VALUES ('" + generateId + "'," + "'" + Name + "',"
                            + "'" + gost + "'," + weight + "," + length + "," + diametr + "," + Integer.parseInt(material) + ","
                            + Integer.parseInt(provider) + "," + "'" + order + "'," + "'" + hardness + "',"
                            + "'" + coverage + "'," + "'" + LengthCoverage + "')";
                    mDb.execSQL(query);

                    // запись в бд
                    String query2 = "INSERT INTO Первично VALUES ('" + generateId + "'," + "'" + Name + "',"
                            + weight + "," + length + ",'" + order + "')";
                    mDb.execSQL(query2);
                    Toast.makeText(Generate.this, "Данные успешно сохранены", Toast.LENGTH_LONG).show();
                    saveToGallery(); // сохранение qr кода в виде картинки
                    Reset();
                }
            }
        });

        // кнопка сгенерировать
        generateQrBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    mDb = mDBHelper.getReadableDatabase();
                } catch (SQLException mSQLException) {
                    throw mSQLException;
                }
                generateId = getRandomString(4); // создание id
              // проверка уникальности id
                String mQuery = "SELECT id FROM Изделие Where id = '"+generateId+"'";
                Cursor mCur = mDb.rawQuery(mQuery, null);
                if (mCur.getCount() <= 0) {
                    }
                else{
                    generateId = getRandomString(4); // создание id
                }
                    mCur.close();
                    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                // инициализация переменной для отображения по умолчанию.
                    Display display = manager.getDefaultDisplay();
                // создание переменной для точки, которая
                // должна отображаться в QR-коде
                    Point point = new Point();
                    display.getSize(point);
                // получение ширины и  высоты точки
                    int width = point.x;
                    int height = point.y;
                // создание размера на основе ширины и высоты.
                    int dimen = width < height ? width : height;
                    dimen = dimen * 3 / 4;
                // установка этих размеров внутри нашего qr-кода
                // кодировщик для генерации нашего qr-кода.
                    qrgEncoder = new QRGEncoder(generateId, null, QRGContents.Type.TEXT, dimen);
                    try {
                        // получаем наш qr-код в виде растрового изображения.
                        bitmap = qrgEncoder.encodeAsBitmap();
                        // растровое изображение задано внутри нашего изображения
                        // просмотр с использованием метода .setimagebitmap.
                        qrCodeIV.setImageBitmap(bitmap);
                    } catch (WriterException e) {

                        Log.e("Tag", e.toString());
                    }
                }

        });
    }

    private void Reset() {  // очистка полей после добавления данных
        dataEdt.setText("");
        editGost.setText("");
        editWeight.setText("");
        editLength.setText("");
        editCoverage.setText("");
        editLengthCoverage.setText("");
        editHardness.setText("");
        editDiametr.setText("");
        qrCodeIV.setImageBitmap(null);
        editOrder.setSelection(editOrder.getCount() - 1);
        editProvider.setSelection(editProvider.getCount() - 1);
    }

    private void Initiating() { // инициализация

        qrCodeIV = findViewById(R.id.idIVQrcode);
        dataEdt = findViewById(R.id.idEdt);
        generateQrBtn = findViewById(R.id.idBtnGenerateQR);
        saveimage = findViewById(R.id.savegallery);
        context = getApplicationContext();
        editGost = findViewById(R.id.editGost);
        editWeight = findViewById(R.id.editWeight);
        editLength = findViewById(R.id.editLength);
        editMaterial = findViewById(R.id.editMaterial);
        editProvider = findViewById(R.id.editProvider);
        editOrder = findViewById(R.id.editOrder);
        editCoverage = findViewById(R.id.editCoverage);
        editLengthCoverage = findViewById(R.id.editLengthCoverage);
        editHardness = findViewById(R.id.editHardness);
        editDiametr = findViewById(R.id.editDiametr);
        btnProvider = findViewById(R.id.btnProvider);
        btnOrder = findViewById(R.id.btnOrder);

    }

    @SuppressLint("Range")
    private void SpinnerChoise(String NameTable, Spinner spinner,SQLiteDatabase mDb) { // метод заполнения спиннеров

        String query = "SELECT * FROM " + NameTable;// запрос
        List<String> list = new ArrayList<String>() ; // динамический массив
        Cursor cur = mDb.rawQuery(query, null);
        Integer j=0;
        list.add("");
        // перебираем все строки и добавляем в список
        if (cur.moveToFirst()) {
            do {
                // вставка по индексу
                list.add(j,cur.getString(cur.getColumnIndex("Наименование")));
                j+=1; // увеличение индекса
            } while (cur.moveToNext());
        }

        // установка данных
        ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(adapt);
        spinner.setSelection(spinner.getCount() - 1);
    }

    private static String getRandomString(final int sizeOfRandomString) { // метод генерации id
        final Random random = new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
    public String GetID(String id, String NameTable, String name, SQLiteDatabase mDb){
        String mQuery = "SELECT " + id + " FROM " + NameTable + " Where Наименование = '" + name.trim() + "'";
        Cursor mCur = mDb.rawQuery(mQuery, null);
        if (mCur != null) {
            if (mCur.moveToFirst()) {
                return mCur.getString(mCur.getColumnIndex(id));
            }
        }
        return "";
    }

    private void saveToGallery() { // сохранение qr кода
        // Получение пути
        Uri path = context.getContentResolver ().insert (MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        try
        {
            OutputStream stream = context.getContentResolver ().openOutputStream (path);
            bitmap.compress (Bitmap.CompressFormat.PNG, 90, stream); // получение итогового изображения
        }
        catch (FileNotFoundException e)
        {
           // Log.e (TAG, "E: " + e.getMessage ());
        }
   }
}