package br.com.alocarioca.eAssinatura;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.view.View;
import android.widget.EditText;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoginActivity extends AppCompatActivity {

    EditText editLogin;
    EditText editSenha;
    Button btnLogin;
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    DBController controller = new DBController(this);
    DataUser dataUser = new DataUser();

    HashMap<String, String> queryValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editLogin = (EditText)findViewById(R.id.editUsuario);
        editSenha = (EditText)findViewById(R.id.editSenha);

        populaLogin();

    }

    public void checkLogin(View arg0) {

        // Get text from email and passord field
        final String usuario = editLogin.getText().toString();
        final String senha = editSenha.getText().toString();

        // Initialize  AsyncLogin() class with email and password
        new AsyncLogin().execute(usuario,senha);

    }

    private class AsyncLogin extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(LoginActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tConectando...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }
        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://201.38.172.137/api/eAssinatura/login.inc.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", params[0])
                        .appendQueryParameter("password", params[1]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if (result.equalsIgnoreCase("false")){

                // If username and password does not match display a error message
                Toast.makeText(LoginActivity.this, "Usuário e/ou senha inválidos", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(LoginActivity.this, "OOPs! Algo deu errado. Problemas na conexão.", Toast.LENGTH_LONG).show();

            } else {
                try {

                    JSONArray jArray = new JSONArray(result);

                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        dataUser.idUsuario = json_data.getString("id_usuario");
                        dataUser.nomUsuario = json_data.getString("nom_usuario");
                        dataUser.desLogin = json_data.getString("des_login");
                        dataUser.desSenha = json_data.getString("des_senha");
                        dataUser.codAgente = json_data.getString("cod_agente");
                        dataUser.domAtivo = json_data.getInt("dom_ativo");
                    }
                    if (dataUser.domAtivo != 1) {
                        Toast.makeText(LoginActivity.this, "Usuário Inativo", Toast.LENGTH_LONG).show();
                    } else {

                        gravaUsuario(dataUser);

                        Intent intent = new Intent (LoginActivity.this, MenuActivity.class);
                        startActivity(intent);
                        LoginActivity.this.finish();
                    }
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(LoginActivity.this, result.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void mensagem(String titulo, String texto) {
        final AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle(titulo);
        alertDialog.setMessage(texto);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void gravaUsuario(DataUser dataUser) {

        ArrayList<HashMap<String, String>> usersynclist;
        usersynclist = new ArrayList<HashMap<String, String>>();

        Gson gson = new GsonBuilder().create();

        try {

            queryValues = new HashMap<>();
            queryValues.put("id_usuario", dataUser.idUsuario);
            queryValues.put("nom_usuario", dataUser.nomUsuario);
            queryValues.put("des_login", dataUser.desLogin);
            queryValues.put("des_senha", dataUser.desSenha);
            queryValues.put("cod_agente", dataUser.codAgente);
            controller.insertUser(queryValues);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void populaLogin() {

        dataUser = controller.usuario();

        if (!dataUser.nomUsuario.isEmpty()) {
            editLogin.setText(dataUser.desLogin);
            editSenha.setText(dataUser.desSenha);
            // Get text from email and passord field
            final String usuario = editLogin.getText().toString();
            final String senha = editSenha.getText().toString();

            // Initialize  AsyncLogin() class with email and password
            new AsyncLogin().execute(usuario,senha);
        }

    }

    public void closeApp(View arg0) {
        finishAffinity();
    }

}


