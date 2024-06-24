package com.dffe.crudapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ProductosFragment extends Fragment {

    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/poo";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root1234";

    private EditText etProductName, etProductStock;
    private Button btnAddProduct, btnUpdateProduct, btnDeleteProduct;
    private ListView listViewProducts;
    private ArrayAdapter<Producto> adapter;
    private ArrayList<Producto> productosList = new ArrayList<>();
    //private MainActivity mainActivity = new MainActivity();

    public ProductosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.crud_productos, container, false);
        setupViews(view);
        new LoadProductsTask().execute();
        return view;
    }

    private void setupViews(View view) {
        etProductName = view.findViewById(R.id.etProductName);
        etProductStock = view.findViewById(R.id.etProductStock);
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnUpdateProduct = view.findViewById(R.id.btnUpdateProduct);
        btnDeleteProduct = view.findViewById(R.id.btnDeleteProduct);
        listViewProducts = view.findViewById(R.id.listViewProducts);

        //adapter = new ProductoAdapter(mainActivity, productosList);
        //ListViewProducts.setAdapter(adapter);
        new LoadProductsTask().execute();


        btnAddProduct.setOnClickListener(v -> {
            String productName = etProductName.getText().toString();
            int productStock;
            try {
                productStock = Integer.parseInt(etProductStock.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Stock must be a number", Toast.LENGTH_SHORT).show();
                return;
            }
            new InsertProductTask().execute(productName, productStock);
        });

        btnUpdateProduct.setOnClickListener(v -> {
            String productName = etProductName.getText().toString();
            int productStock;
            try {
                productStock = Integer.parseInt(etProductStock.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Stock must be a number", Toast.LENGTH_SHORT).show();
                return;
            }
            new UpdateProductTask().execute(productName, productStock);
        });

        btnDeleteProduct.setOnClickListener(v -> {
            String productName = etProductName.getText().toString();

            new DeleteProductTask().execute(productName);
        });
    }

    private class LoadProductsTask extends AsyncTask<Void, Void, ArrayList<Producto>> {

        @Override
        protected ArrayList<Producto> doInBackground(Void... voids) {
            ArrayList<Producto> productos = new ArrayList<>();
            Connection conn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCTOS");
                while (rs.next()) {
                    int id = rs.getInt("PROCODIGO");
                    String nombre = rs.getString("PRONOMBRE");
                    int stock = rs.getInt("PROSTOCK");
                    productos.add(new Producto(id, nombre, stock));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return productos;
        }

        @Override
        protected void onPostExecute(ArrayList<Producto> productos) {
            if (productos != null) {
                adapter = new ProductoAdapter(getActivity(), productos);
                listViewProducts.setAdapter(adapter);
            } else {
                Toast.makeText(getActivity(),"No se pudieron cargar los productos", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public class ProductoAdapter extends ArrayAdapter<Producto> {
        public ProductoAdapter(Context context, ArrayList<Producto> productos) {
            super(context, 0, productos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Producto producto = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_producto, parent, false);
            }
            TextView tvNombre = convertView.findViewById(R.id.tvNombre);
            TextView tvStock = convertView.findViewById(R.id.tvStock);
            tvNombre.setText(producto.getNombre());
            tvStock.setText(String.valueOf(producto.getStock()));
            return convertView;
        }
    }

    private class InsertProductTask extends AsyncTask<Object, Void, Boolean> {
        private String errorMessage = null;

        @Override
        protected Boolean doInBackground(Object... params) {
            String nombre = (String) params[0];
            int stock = (int) params[1];
            Connection conn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                insertProducto(conn, nombre, stock);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getActivity(), "Producto añadido correctamente", Toast.LENGTH_SHORT).show();
                new LoadProductsTask().execute();
                etProductName.setText("");
                etProductStock.setText("");
            } else {
                Toast.makeText(getActivity(), "Error al agregar producto: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateProductTask extends AsyncTask<Object, Void, Boolean> {
        private String errorMessage = null;

        @Override
        protected Boolean doInBackground(Object... params) {
            //int id = (int) params[0];
            String nombre = (String) params[0];
            int stock = (int) params[1];
            Connection conn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                updateProducto(conn, nombre, stock);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getActivity(), "Producto actualizado correctamente", Toast.LENGTH_SHORT).show();
                new LoadProductsTask().execute();
                etProductName.setText("");
                etProductStock.setText("");
            } else {
                Toast.makeText(getActivity(), "Error al actualizar producto: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteProductTask extends AsyncTask<Object, Void, Boolean> {
        private String errorMessage = null;

        @Override
        protected Boolean doInBackground(Object... params) {
            String id =(String) params[0];
            Connection conn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                deleteProducto(conn, id);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getActivity(), "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                new LoadProductsTask().execute();
                etProductName.setText("");
                etProductStock.setText("");
            } else {
                Toast.makeText(getActivity(), "Error al eliminar producto: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void insertProducto(Connection conn, String nombre, int stock) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "INSERT INTO PRODUCTOS (PRONOMBRE, PROSTOCK) VALUES ('" + nombre + "', " + stock + ")";
        stmt.executeUpdate(query);
    }

    private void updateProducto(Connection conn, String nombre, int stock) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "UPDATE PRODUCTOS SET PROSTOCK = " + stock + " WHERE PRONOMBRE = '" + nombre+"'";
        stmt.executeUpdate(query);
    }

    private void deleteProducto(Connection conn, String id) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "DELETE FROM PRODUCTOS WHERE PRONOMBRE = '" + id+"'";
        stmt.executeUpdate(query);
    }

    private ArrayList<Producto> getAllProductos(Connection conn) throws SQLException {
        ArrayList<Producto> productos = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCTOS");
        while (rs.next()) {
            int id = rs.getInt("PROCODIGO");
            String nombre = rs.getString("PRONOMBRE");
            int stock = rs.getInt("PROSTOCK");
            productos.add(new Producto(id, nombre, stock));
        }
        return productos;
    }
}
