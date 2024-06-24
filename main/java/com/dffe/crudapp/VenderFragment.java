package com.dffe.crudapp;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VenderFragment extends Fragment {

    private EditText etVentaProCodigo, etVentaCliId, etVentaCantidad;
    private TextView tvStockDisponible;
    private Button btnAddVenta;

    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/poo";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root1234";

    public VenderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vender, container, false);
        setupViews(view);
        return view;
    }

    private void setupViews(View view) {
        etVentaProCodigo = view.findViewById(R.id.etVentaProCodigo);
        etVentaCliId = view.findViewById(R.id.etVentaCliId);
        etVentaCantidad = view.findViewById(R.id.etVentaCantidad);
        tvStockDisponible = view.findViewById(R.id.tvStockDisponible);
        btnAddVenta = view.findViewById(R.id.btnAddVenta);

        etVentaProCodigo.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String proCodigo = etVentaProCodigo.getText().toString();
                if (!proCodigo.isEmpty()) {
                    new CheckStockTask().execute(proCodigo);
                }
            }
        });

        btnAddVenta.setOnClickListener(v -> {
            String proCodigo = etVentaProCodigo.getText().toString();
            String cliId = etVentaCliId.getText().toString();
            int cantidad;

            try {
                cantidad = Integer.parseInt(etVentaCantidad.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Cantidad debe ser un número", Toast.LENGTH_SHORT).show();
                return;
            }

            new InsertVentaTask().execute(proCodigo, cliId, cantidad);
        });
    }

    private class CheckStockTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String proCodigo = params[0];
            Connection conn = null;
            int stock = -1;

            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                stock = getStock(conn, proCodigo);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            return stock;
        }

        @Override
        protected void onPostExecute(Integer stock) {
            if (stock >= 0) {
                tvStockDisponible.setText("Stock disponible: " + stock);
            } else {
                tvStockDisponible.setText("Stock no disponible");
            }
        }

        private int getStock(Connection conn, String proCodigo) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                String query = "SELECT PROSTOCK FROM PRODUCTOS WHERE PROCODIGO = " + proCodigo;
                ResultSet rs = stmt.executeQuery(query);

                if (rs.next()) {
                    return rs.getInt("PROSTOCK");
                }
            }
            return -1;
        }
    }

    private class InsertVentaTask extends AsyncTask<Object, Void, Boolean> {

        private String errorMessage = null;

        @Override
        protected Boolean doInBackground(Object... params) {
            String proCodigo = (String) params[0];
            String cliId = (String) params[1];
            int cantidad = (int) params[2];
            Connection conn = null;

            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                if (checkAndReduceStock(conn, proCodigo, cantidad)) {
                    insertVenta(conn, proCodigo, cliId, cantidad);
                    return true;
                } else {
                    errorMessage = "Stock no disponible";
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return false;
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getActivity(), "Venta registrada correctamente", Toast.LENGTH_SHORT).show();
                etVentaProCodigo.setText("");
                etVentaCliId.setText("");
                etVentaCantidad.setText("");
                tvStockDisponible.setText("Stock disponible: ");
            } else {
                Toast.makeText(getActivity(), "Error al registrar venta: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }

        private boolean checkAndReduceStock(Connection conn, String proCodigo, int cantidad) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                String query = "SELECT PROSTOCK FROM PRODUCTOS WHERE PROCODIGO = " + proCodigo;
                ResultSet rs = stmt.executeQuery(query);

                if (rs.next()) {
                    int stock = rs.getInt("PROSTOCK");
                    if (stock >= cantidad) {
                        String updateQuery = "UPDATE PRODUCTOS SET PROSTOCK = PROSTOCK - " + cantidad + " WHERE PROCODIGO = " + proCodigo;
                        stmt.executeUpdate(updateQuery);
                        return true;
                    }
                }
                return false;
            }
        }

        private void insertVenta(Connection conn, String proCodigo, String cliId, int cantidad) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                String query = "INSERT INTO VENTAS (PROCODIGO, CLIID, VENCANTIDAD) VALUES ('" + proCodigo + "', '" + cliId + "', " + cantidad + ")";
                stmt.executeUpdate(query);
            }
        }
    }
}
