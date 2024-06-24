package com.dffe.crudapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ClientesFragment extends Fragment {

    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/poo";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root1234";

    private EditText etClientId, etClientName, etClientLastName, etClientCity, etClientGender;
    private Button btnAddClient, btnUpdateClient, btnDeleteClient;
    private ListView listViewClients;

    public ClientesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.crud_clientes, container, false);
        setupViews(view);
        return view;
    }

    private void setupViews(View view) {
        etClientId = view.findViewById(R.id.etClientId);
        etClientName = view.findViewById(R.id.etClientName);
        etClientLastName = view.findViewById(R.id.etClientLastName);
        etClientCity = view.findViewById(R.id.etClientCity);
        etClientGender = view.findViewById(R.id.etClientGender);
        btnAddClient = view.findViewById(R.id.btnAddClient);
        btnUpdateClient = view.findViewById(R.id.btnUpdateClient);
        btnDeleteClient = view.findViewById(R.id.btnDeleteClient);
        listViewClients = view.findViewById(R.id.listViewClients);

        new LoadClientsTask().execute();

        btnAddClient.setOnClickListener(v -> {
            String id = etClientId.getText().toString();
            String nombre = etClientName.getText().toString();
            String apellido = etClientLastName.getText().toString();
            String ciudad = etClientCity.getText().toString();
            String sexo = etClientGender.getText().toString();
            new InsertClientTask().execute(id, nombre, apellido, ciudad, sexo);
        });

        btnUpdateClient.setOnClickListener(v -> {
            String id = etClientId.getText().toString();
            String nombre = etClientName.getText().toString();
            String apellido = etClientLastName.getText().toString();
            String ciudad = etClientCity.getText().toString();
            String sexo = etClientGender.getText().toString();
            new UpdateClientTask().execute(id, nombre, apellido, ciudad, sexo);
        });

        btnDeleteClient.setOnClickListener(v -> {
            String id = etClientId.getText().toString();
            new DeleteClientTask().execute(id);
        });

        new LoadClientsTask().execute();
    }

    private class InsertClientTask extends AsyncTask<Object, Void, Boolean> {
        private String errorMessage = null;

        @Override
        protected Boolean doInBackground(Object... params) {
            String id = (String) params[0];
            String nombre = (String) params[1];
            String apellido = (String) params[2];
            String ciudad = (String) params[3];
            String sexo = (String) params[4];
            try (Connection conn = DatabaseUtils.getConnection()) {
                String query = "INSERT INTO CLIENTES (CLIID, CLINOMBRE, CLIAPELLIDO, CLICIUDAD, CLISEXO) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, id);
                    stmt.setString(2, nombre);
                    stmt.setString(3, apellido);
                    stmt.setString(4, ciudad);
                    stmt.setString(5, sexo);
                    stmt.executeUpdate();
                }
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
                Toast.makeText(getActivity(), "Cliente añadido correctamente", Toast.LENGTH_SHORT).show();
                new LoadClientsTask().execute();
            } else {
                Toast.makeText(getActivity(), "Error al agregar cliente: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadClientsTask extends AsyncTask<Void, Void, ArrayList<Cliente>> {
        @Override
        protected ArrayList<Cliente> doInBackground(Void... voids) {
            try (Connection conn = DatabaseUtils.getConnection()) {
                return DatabaseUtils.getAllClientes(conn);
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Cliente> clientes) {
            ClienteAdapter adapter = new ClienteAdapter(getActivity(), clientes);
            listViewClients.setAdapter(adapter);
        }
    }

    private class UpdateClientTask extends AsyncTask<Object, Void, Boolean> {
        private String errorMessage = null;

        @Override
        protected Boolean doInBackground(Object... params) {
            String id = (String) params[0];
            String nombre = (String) params[1];
            String apellido = (String) params[2];
            String ciudad = (String) params[3];
            String sexo = (String) params[4];
            try (Connection conn = DatabaseUtils.getConnection()) {
                String query = "UPDATE CLIENTES SET CLINOMBRE = ?, CLIAPELLIDO = ?, CLICIUDAD = ?, CLISEXO = ? WHERE CLIID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, nombre);
                    stmt.setString(2, apellido);
                    stmt.setString(3, ciudad);
                    stmt.setString(4, sexo);
                    stmt.setString(5, id);
                    stmt.executeUpdate();
                }
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
                Toast.makeText(getActivity(), "Cliente actualizado correctamente", Toast.LENGTH_SHORT).show();
                new LoadClientsTask().execute();
            } else {
                Toast.makeText(getActivity(), "Error al actualizar cliente: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteClientTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = null;

        @Override
        protected Boolean doInBackground(String... params) {
            String id = params[0];
            try (Connection conn = DatabaseUtils.getConnection()) {
                String query = "DELETE FROM CLIENTES WHERE CLIID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, id);
                    stmt.executeUpdate();
                }
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
                Toast.makeText(getActivity(), "Cliente eliminado correctamente", Toast.LENGTH_SHORT).show();
                new LoadClientsTask().execute();
            } else {
                Toast.makeText(getActivity(), "Error al eliminar cliente: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
    public class ClienteAdapter extends ArrayAdapter<Cliente> {
        public ClienteAdapter(Context context, ArrayList<Cliente> clientes) {
            super(context, 0, clientes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Cliente cliente = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_cliente, parent, false);
            }
            TextView tvId = convertView.findViewById(R.id.tvId);
            TextView tvNombre = convertView.findViewById(R.id.tvNombre);
            TextView tvApellido = convertView.findViewById(R.id.tvApellido);
            TextView tvCiudad = convertView.findViewById(R.id.tvCiudad);
            TextView tvSexo = convertView.findViewById(R.id.tvSexo);

            tvId.setText(cliente.getId());
            tvNombre.setText(cliente.getNombre());
            tvApellido.setText(cliente.getApellido());
            tvCiudad.setText(cliente.getCiudad());
            tvSexo.setText(cliente.getSexo());

            return convertView;
        }
    }

    private static class DatabaseUtils {
        static Connection getConnection() throws SQLException, ClassNotFoundException {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }

        static void insertCliente(Connection conn, String id, String nombre, String apellido, String ciudad, String sexo) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                String query = "INSERT INTO CLIENTES (CLIID, CLINOMBRE, CLIAPELLIDO, CLICIUDAD, CLISEXO) VALUES ('" + id + "', '" + nombre + "', '" + apellido + "', '" + ciudad + "', '" + sexo + "')";
                stmt.executeUpdate(query);
            }
        }

        static ArrayList<Cliente> getAllClientes(Connection conn) throws SQLException {
            ArrayList<Cliente> clientes = new ArrayList<>();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM CLIENTES")) {
                while (rs.next()) {
                    String id = rs.getString("CLIID");
                    String nombre = rs.getString("CLINOMBRE");
                    String apellido = rs.getString("CLIAPELLIDO");
                    String ciudad = rs.getString("CLICIUDAD");
                    String sexo = rs.getString("CLISEXO");
                    clientes.add(new Cliente(id, nombre, apellido, ciudad, sexo));
                }
            }
            return clientes;
        }

        static void updateCliente(Connection conn, String id, String nombre, String apellido, String ciudad, String sexo) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                String query = "UPDATE CLIENTES SET CLINOMBRE = '" + nombre + "', CLIAPELLIDO = '" + apellido + "', CLICIUDAD = '" + ciudad + "', CLISEXO = '" + sexo + "' WHERE CLIID = '" + id + "'";
                stmt.executeUpdate(query);
            }
        }

        static void deleteCliente(Connection conn, String id) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                String query = "DELETE FROM CLIENTES WHERE CLIID = '" + id + "'";
                stmt.executeUpdate(query);
            }
        }
    }

}
