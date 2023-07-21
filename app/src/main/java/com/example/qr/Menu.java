package com.example.qr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Menu extends AppCompatActivity {
   // объявление элементов интерфейса
    Button create; Button open;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        // инициализация кнопок
        create = findViewById(R.id.generate);
        open = findViewById(R.id.open);
        create.setOnClickListener(new View.OnClickListener() { // обработка нажатия на кнопку
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, Generate.class); // открытие генератора qr кодов
                startActivity(intent);
            }
        });
        open.setOnClickListener(new View.OnClickListener() { // обработка нажатия на кнопку
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, MainActivity.class); // открытие камеры для распознования
                startActivity(intent);
            }
        });
    }
}