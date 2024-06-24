package com.dffe.crudapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;

class Producto {
    private int id;
    private String nombre;
    private int stock;

    public Producto(int id, String nombre, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
class Cliente {
    private String id;
    private String nombre;
    private String apellido;
    private String ciudad;
    private String sexo;

    public Cliente(String id, String nombre, String apellido, String ciudad, String sexo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.ciudad = ciudad;
        this.sexo = sexo;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getSexo() {
        return sexo;
    }
}


public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/poo";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root1234";

    private EditText etUsername, etPassword, etProductName, etProductStock;
    private Button btnLogin, btnAddProduct, btnDeleteProduct;
    private FrameLayout frameLayout;
    private View crudProductosView;
    private ListView listViewProducts;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupLoginViews();

    }
    private void setupLoginViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            new LoginTask().execute(username, password);
        });
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            try (Connection conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM LOGIN WHERE USUSUARIO = '" + username + "' AND USPASSWORD = '" + password + "'")) {

                return rs.next();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                loadMainContent();
                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadMainContent() {
        setContentView(R.layout.tab_main);

        viewPager = findViewById(R.id.viewpager1);
        tabLayout = findViewById(R.id.tablayout);

        tabLayout.setupWithViewPager(viewPager);

        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        vpAdapter.addFragment(new ProductosFragment(),"Productos");
        vpAdapter.addFragment(new ClientesFragment(),"Clientes");
        vpAdapter.addFragment(new VenderFragment(),"Vender");

        viewPager.setAdapter(vpAdapter);
    }
}

