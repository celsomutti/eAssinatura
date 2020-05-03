package br.com.alocarioca.eAssinatura;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MovimentoActivity extends AppCompatActivity {

    DBController controller = new DBController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimento);
        Spinner spinAgente = (Spinner) findViewById(R.id.spinAgenteMov);
        Spinner spinEdicao = (Spinner) findViewById(R.id.datEdicaoListaMov);

        List<String> listaAgentes = controller.ListaAgentesMov();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listaAgentes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAgente.setAdapter(dataAdapter);
        if (listaAgentes.size() != 0) {
            spinAgente.setSelection(0);
        }

        List<String> listaData =  controller.ListaDatasMov();
        ArrayAdapter<String> dataAdapterData = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listaData);
        dataAdapterData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinEdicao.setAdapter(dataAdapterData);
        if (listaData.size() != 0) {
            spinEdicao.setSelection(0);
        }

    }

    public void ListagemMovimentos(View arg0) {

        Spinner spinAgente = (Spinner) findViewById(R.id.spinAgenteMov);
        Spinner spinEdicao = (Spinner) findViewById(R.id.datEdicaoListaMov);

        String dataParam = spinEdicao.getSelectedItem().toString();
        String agenteParam = spinAgente.getSelectedItem().toString();

        ArrayList<HashMap<String, String>> lista = controller.ListaMovimento(dataParam, agenteParam);

        if (lista.size() != 0) {
            ListAdapter adapter = new SimpleAdapter(MovimentoActivity.this, lista, R.layout.lista_movimentacoes, new String[] {
                    "id_movimentacao", "des_status","cod_assinante", "nom_assinante","des_endereco_completo", "des_produto"},
                    new int[] { R.id.textId, R.id.textStatus, R.id.textcodAss, R.id.textnomAss, R.id.textEndereco,  R.id.textproduto});
            ListView myList = (ListView) findViewById(R.id.listMovimentacoes);
            myList.setAdapter(adapter);

        } else {
            Toast.makeText(MovimentoActivity.this, "Movimentações não encontradas!", Toast.LENGTH_LONG).show();
        }


    }
}
