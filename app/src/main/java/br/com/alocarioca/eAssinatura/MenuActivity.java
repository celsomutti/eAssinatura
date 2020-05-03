package br.com.alocarioca.eAssinatura;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    DBController controller = new DBController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void importar(View arg0) {
        Intent intent = new Intent (MenuActivity.this, ImportActivity.class);
        startActivity(intent);
    }

    public void logOff(View arg0) {

        controller.limpaTabela("login_usuarios");
        controller.limpaTabela("listagem_movimentacoes");
        controller.limpaTabela("listagem_jornal");

        Intent intent = new Intent (MenuActivity.this, LoginActivity.class);
        startActivity(intent);
        MenuActivity.this.finish();
    }

    public void listagem(View arg0) {
        Intent intent = new Intent (MenuActivity.this, ListagemActivity.class);
        startActivity(intent);
    }

    public void movimento(View arg0) {
        Intent intent = new Intent (MenuActivity.this, MovimentoActivity.class);
        startActivity(intent);
    }

}
