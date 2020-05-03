package br.com.alocarioca.eAssinatura;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.widget.ListView.*;

public class ListagemActivity extends AppCompatActivity {

    DBController controller = new DBController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listagem);

        Spinner spinAgente = (Spinner) findViewById(R.id.spinAgente);
        Spinner spinEdicao = (Spinner) findViewById(R.id.datEdicaoLista);

        List<String> listaAgentes = controller.ListaAgentes();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listaAgentes );
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAgente.setAdapter(dataAdapter);
        if (listaAgentes.size() !=0 ) {
            spinAgente.setSelection(0);
        }

        List<String> listaData = controller.ListaDatas();
        ArrayAdapter<String> dataAdapterData = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listaData );
        dataAdapterData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinEdicao.setAdapter(dataAdapterData);
        if (listaData.size() !=0 ) {
            spinEdicao.setSelection(0);
        }

    }

    public void listaEntregas (View arg0) {

        Spinner spinAgente = (Spinner) findViewById(R.id.spinAgente);
        Spinner spinEdicao = (Spinner) findViewById(R.id.datEdicaoLista);

        String dataParam = spinEdicao.getSelectedItem().toString();
        String codigoAgente = spinAgente.getSelectedItem().toString();


        ArrayList<HashMap<String, String>> lista = controller.Listagem(dataParam, codigoAgente);

        if (lista.size() != 0) {
            final ListAdapter adapter = new SimpleAdapter(ListagemActivity.this, lista, R.layout.lista_assinantes, new String[] {
                    "id_registro", "des_status","des_assinante","des_endereco", "des_referencia", "des_produto", "des_modalidade", "qtd_exemplares", "dom_lido"},
                    new int[] { R.id.textId, R.id.textStatus, R.id.textAssinante, R.id.textEndereco, R.id.textReferencia, R.id.textproduto, R.id.textModalidade, R.id.textQtde, R.id.textLido });
            final ListView myList = (ListView) findViewById(R.id.listAssinaturas);
            myList.setAdapter(adapter);

            myList.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView tv = (TextView) view;
                    TextView tv2 = view.findViewById(R.id.textId);
                    TextView tv3 = view.findViewById(R.id.textLido);
                    TextView tv4 = view.findViewById(R.id.textStatus);
                    if (tv4.getText().length() == 0) {
                        if (tv3.getText().length() == 0) {
                            tv3.setText("ENTREGUE");
                        } else {
                            tv3.setText("");
                        }

                        controller.MarcaEntregue(tv2.getText().toString(), tv3.getText().toString());
                    }

                    return false;
                }
            });

        } else {
            Toast.makeText(ListagemActivity.this, "Listagem não encontrada!", Toast.LENGTH_LONG).show();
        }

    }
}
