package br.com.alocarioca.eAssinatura;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class ImportActivity extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    public String agente = "";

    private AlertDialog alerta;

    URL url = null;
    List<String> totais;
    ArrayAdapter<String> adaptador;
    ListView listaTotais;


    DBController controller = new DBController(this);
    DataListagem dataListagem = new DataListagem();
    DataMovimento dataMovimento = new DataMovimento();
    DataTotal dataTotal = new DataTotal();
    DataUser dataUser = new DataUser();

    HashMap<String, String> queryValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        codigoAgente();
    }

    public void initImport(View arg0) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atenção!");
        builder.setMessage("Confirma a importação dos dados?");
        builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new ImportActivity.AsyncLista().execute(agente);
                //new ImportActivity.AsyncMovimento().execute(agente);
               //listaTotal();
            }
        });
        builder.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ImportActivity.this, "Importação cancelada.", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();


    }


    private class AsyncLista extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(ImportActivity.this);
        HttpURLConnection conn;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tImportando Listagem. Aguarde...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }
        @Override
        protected String doInBackground(String... params) {

            //listagem de assinantes

            try {

                // Enter URL address where your php file resides
                url = new URL("http://www.rjsmart.com.br/eAssinatura/listagem.inc.php");

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
                        .appendQueryParameter("agente", params[0]);
                String query = builder.build().getQuery();


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

            String dtStart;
            String novaData;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat newformat = new SimpleDateFormat("dd/MM/yyyy");

            //this method will be running on UI thread

            if (result.equalsIgnoreCase("false")){

                // If username and password does not match display a error message
                pdLoading.dismiss();
                Toast.makeText(ImportActivity.this, "Nenhuma listagem disponível", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                pdLoading.dismiss();
                Toast.makeText(ImportActivity.this, "OOPs! Algo deu errado. Problemas na conexão.", Toast.LENGTH_LONG).show();

            } else {
                try {

                    controller.limpaTabela("listagem_jornal");

                    JSONArray jArray = new JSONArray(result);

                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        dtStart = json_data.getString("dat_edicao").substring(0,10);
                        Date data = format.parse(dtStart);
                        novaData =  newformat.format(data);
                        dataListagem.idRegistro = json_data.getString("id_registro");
                        dataListagem.codAgente = json_data.getString("cod_agente");
                        dataListagem.datEdicao = novaData;
                        dataListagem.codAssinatura = json_data.getString("cod_assinatura");
                        dataListagem.nomAssinante = json_data.getString("nom_assinante").trim();
                        dataListagem.desEndereco = json_data.getString("des_endereco").trim();
                        dataListagem.numEndereco = json_data.getString("num_endereco").trim();
                        dataListagem.desComplemento = json_data.getString("des_complemento").trim();
                        dataListagem.desBairro = json_data.getString("des_bairro").trim();
                        dataListagem.numCep = json_data.getString("num_cep");
                        dataListagem.desReferencia = json_data.getString("des_referencia").trim();
                        dataListagem.desProduto = json_data.getString("des_produto");
                        dataListagem.codModalidade = json_data.getString("cod_modalidade");
                        dataListagem.desModalidade = json_data.getString("des_modalidade");
                        dataListagem.qtdExemplares = json_data.getString("qtd_exemplares");
                        gravaListagem(dataListagem);
                    }
                    pdLoading.dismiss();
                    new ImportActivity.AsyncMovimento().execute(agente);
                } catch (JSONException e) {
                    Toast.makeText(ImportActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(ImportActivity.this, result.toString(), Toast.LENGTH_LONG).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void codigoAgente() {

        dataUser = controller.usuario();

        if (!dataUser.nomUsuario.isEmpty()) {
            agente = dataUser.codAgente;
        }

    }

    public void gravaListagem(DataListagem dataListagem) {

        try {

            queryValues = new HashMap<>();

            queryValues.put("des_status", "");
            queryValues.put("dat_edicao", dataListagem.datEdicao);
            queryValues.put("des_assinante", dataListagem.codAssinatura + "-" +
                                             dataListagem.nomAssinante);
            queryValues.put("des_endereco", dataListagem.desEndereco + ", " +
                                            dataListagem.numEndereco + " " +
                                            dataListagem.desComplemento + " " +
                                            dataListagem.desBairro);
            queryValues.put("des_referencia", dataListagem.desReferencia);
            queryValues.put("num_cep", dataListagem.numCep);
            queryValues.put("des_produto", dataListagem.desProduto);
            queryValues.put("des_modalidade", dataListagem.desModalidade);
            queryValues.put("id_registro", dataListagem.idRegistro);
            queryValues.put("cod_ordem", "1");
            queryValues.put("qtd_exemplares", dataListagem.qtdExemplares);
            queryValues.put("cod_agente", dataListagem.codAgente);
            queryValues.put("dom_lido", "");

            controller.insertListagem(queryValues);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class AsyncMovimento extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(ImportActivity.this);
        HttpURLConnection conn;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            pdLoading.setMessage("\tImportando Movimentações. Aguarde...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }
        @Override
        protected String doInBackground(String... params) {

            //listagem de movimentações

            try {

                // Enter URL address where your php file resides
                url = new URL("http://www.rjsmart.com.br/eAssinatura/movimento.inc.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                pdLoading.dismiss();
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
                        .appendQueryParameter("agente", params[0]);
                String query = builder.build().getQuery();

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

            String dtStart;
            String novaData;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat newformat = new SimpleDateFormat("dd/MM/yyyy");

            //this method will be running on UI thread

            if (result.equalsIgnoreCase("false")){

                // If username and password does not match display a error message
                pdLoading.dismiss();
                Toast.makeText(ImportActivity.this, "Nenhuma movimentação disponível", Toast.LENGTH_LONG).show();
                listaTotal();
            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                pdLoading.dismiss();
                Toast.makeText(ImportActivity.this, "OOPs! Algo deu errado. Problemas na conexão.", Toast.LENGTH_LONG).show();

            } else {
                try {

                    controller.limpaTabela("listagem_movimentacoes");

                    JSONArray jArray = new JSONArray(result);

                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        dtStart = json_data.getString("dat_edicao").substring(0,10);
                        Date data = format.parse(dtStart);
                        novaData =  newformat.format(data);
                        dataMovimento.idRegistro = json_data.getString("id_registro");
                        dataMovimento.codAgente = json_data.getString("cod_agente");
                        dataMovimento.datEdicao = novaData;
                        dataMovimento.desStatus = json_data.getString("des_status");
                        dataMovimento.desEndereco = json_data.getString("des_endereco");
                        dataMovimento.desComplemento = json_data.getString("des_complemento");
                        dataMovimento.desBairro = json_data.getString("des_bairro");
                        dataMovimento.codAssinante = json_data.getString("cod_assinante");
                        dataMovimento.nomAssinante = json_data.getString("nom_assinante");
                        dataMovimento.desProduto = json_data.getString("des_produto");
                        dataMovimento.codModalidade = json_data.getString("cod_modalidade");
                        gravaMovimento(dataMovimento);
                    }
                    pdLoading.dismiss();
                    listaTotal();
                } catch (JSONException e) {
                    pdLoading.dismiss();
                    Toast.makeText(ImportActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(ImportActivity.this, result.toString(), Toast.LENGTH_LONG).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void gravaMovimento(DataMovimento dataMovimento) {

        try {

            queryValues = new HashMap<>();

            queryValues.put("id_movimentacao", dataMovimento.idRegistro);
            queryValues.put("des_status", dataMovimento.desStatus.trim());
            queryValues.put("des_endereco_completo", dataMovimento.desEndereco.trim() + " " +
                                                     dataMovimento.desComplemento.trim() + " " +
                                                     dataMovimento.desBairro.trim());
            queryValues.put("cod_assinante", dataMovimento.codAssinante);
            queryValues.put("nom_assinante", dataMovimento.nomAssinante.trim());
            queryValues.put("des_produto", dataMovimento.desProduto);
            queryValues.put("des_modalidade", dataMovimento.codModalidade);
            queryValues.put("dat_edicao", dataMovimento.datEdicao);
            queryValues.put("cod_agente", dataMovimento.codAgente);

            controller.insertMovimento(queryValues);


            queryValues = new HashMap<>();


            queryValues.put("des_status", dataMovimento.desStatus.trim());
            queryValues.put("dat_edicao", dataListagem.datEdicao);
            queryValues.put("des_assinante", dataMovimento.codAgente.trim() + "-" +
                                             dataMovimento.nomAssinante.trim());
            queryValues.put("des_endereco", dataMovimento.desEndereco.trim() + ", " +
                                            dataMovimento.desComplemento.trim() + " " +
                                            dataMovimento.desBairro.trim());
            queryValues.put("des_referencia", "");
            queryValues.put("num_cep", "00000000");
            queryValues.put("des_produto", dataMovimento.desProduto);
            queryValues.put("des_modalidade", "");
            queryValues.put("id_registro", dataMovimento.idRegistro);
            queryValues.put("cod_ordem", "0");
            queryValues.put("qtd_exemplares", "");
            queryValues.put("cod_agente", dataMovimento.codAgente);
            queryValues.put("dom_lido", "");


            controller.insertListagem(queryValues);


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void listaTotal() {

    ArrayList<HashMap<String, String>> listatotal = controller.totalizacao();

    if (listatotal.size() != 0) {
        ListAdapter adapter = new SimpleAdapter(ImportActivity.this, listatotal, R.layout.view_total, new String[] {
                "dat_edicao", "des_produto","total" }, new int[] { R.id.datEdicaoLista, R.id.desProdutoLista, R.id.qtdExexmplaresLista });
        ListView myList = (ListView) findViewById(R.id.listatotal);
        myList.setAdapter(adapter);
    }

}

}
