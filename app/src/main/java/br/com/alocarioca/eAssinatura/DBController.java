package br.com.alocarioca.eAssinatura;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBController  extends SQLiteOpenHelper {

	DataUser dataUser = new DataUser();
	DataTotal dataTotal = new DataTotal();

	public DBController(Context applicationcontext) {
        super(applicationcontext, "ejornal.db", null, 1);
    }
	//Creates Table
	@Override
	public void onCreate(SQLiteDatabase database) {

		String query;

		//query = "CREATE TABLE IF NOT EXISTS sqlite_sequence(name,seq)";
		//database.execSQL(query);
		query = "CREATE TABLE IF NOT EXISTS login_usuarios(id_login Text PRIMARY KEY NOT NULL, nom_usuario Text, des_login Text NOT NULL, des_senha Text, cod_agente Text)";
		database.execSQL(query);
		query = "CREATE TABLE IF NOT EXISTS listagem_movimentacoes(cod_agente Terxt, id_movimentacao Text PRIMARY KEY NOT NULL, des_status Text NOT NULL, des_endereco_completo Text NOT NULL, cod_assinante Text NOT NULL, nom_assinante Text  NOT NULL, des_produto Text  NOT NULL, des_modalidade Text  NOT NULL, dat_edicao Text  NOT NULL  )";
		database.execSQL(query);
		query = "CREATE TABLE IF NOT EXISTS listagem_jornal(cod_agente Text, des_status Text, dat_edicao Text NOT NULL, des_assinante Text NOT NULL, des_endereco Text NOT NULL, des_referencia Text, num_cep Text, des_produto Text NOT NULL, des_modalidade Text, id_registro Text PRIMARY KEY  NOT NULL, cod_ordem Text, qtd_exemplares Text NOT NULL, dom_lido Text)";
		database.execSQL(query);
	}
	@Override
	public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
		/*String query;
		query = "DROP TABLE IF EXISTS sqlite_sequence";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS login_usuarios";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS listagem_movimentacoes";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS listagem_jornal";
		database.execSQL(query);
        onCreate(database);*/
	}
	
	
	/**
	 * Inserts User into SQLite DB
	 * @param queryValues
	 */
	public void insertUser(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id_login", queryValues.get("id_usuario"));
		values.put("nom_usuario", queryValues.get("nom_usuario"));
		values.put("des_login", queryValues.get("des_login"));
		values.put("des_senha", queryValues.get("des_senha"));
		values.put("cod_agente", queryValues.get("cod_agente"));
		limpaTabela("login_usuarios");
		database.insert("login_usuarios", null, values);
		database.close();
	}
	

	public ArrayList<HashMap<String, String>> totalizacao() {
		ArrayList<HashMap<String, String>> totalLista;
		totalLista = new ArrayList<HashMap<String, String>>();
		String selectQuery = "select dat_edicao, des_produto, sum(cast(qtd_exemplares as SIGNED)) as total from listagem_jornal where des_status = '' group by dat_edicao, des_produto";
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    if (cursor.moveToFirst()) {
	        do {

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("dat_edicao", cursor.getString(0).substring(0,10));
				map.put("des_produto", cursor.getString(1));
				map.put("total", cursor.getString(2));
     			totalLista.add(map);
	        } while (cursor.moveToNext());
	    }
	    database.close();
	    return totalLista;
	}

	public void limpaTabela (String tabela) {
		SQLiteDatabase database = this.getWritableDatabase();
		database.delete(tabela,null,null);
		//database.close();
	}

	public DataUser usuario() {

		String selectQuery = "SELECT * FROM login_usuarios";

		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
        dataUser.idUsuario =  "";
        dataUser.nomUsuario = "";
        dataUser.desLogin = "";
        dataUser.desSenha = "";
        dataUser.codAgente = "";
		if (cursor.moveToFirst()) {
			do {
				dataUser.idUsuario =  cursor.getString(0);
				dataUser.nomUsuario = cursor.getString(1);
				dataUser.desLogin = cursor.getString(2);
				dataUser.desSenha = cursor.getString(3);
				dataUser.codAgente = cursor.getString(4);
			} while (cursor.moveToNext());
		}
		database.close();

		return dataUser;

	}

	public void insertListagem(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id_registro",queryValues.get("id_registro"));
		values.put("des_status",queryValues.get("des_status"));
		values.put("dat_edicao", queryValues.get("dat_edicao"));
		values.put("des_assinante", queryValues.get("des_assinante"));
		values.put("des_endereco", queryValues.get("des_endereco"));
		values.put("des_referencia", queryValues.get("des_referencia"));
		values.put("num_cep", queryValues.get("num_cep"));
		values.put("des_produto", queryValues.get("des_produto"));
		values.put("des_modalidade", queryValues.get("des_modalidade"));
		values.put("cod_ordem", "1");
		values.put("qtd_exemplares", queryValues.get("qtd_exemplares"));
		values.put("cod_agente",queryValues.get("cod_agente"));
		values.put("dom_lido",queryValues.get("dom_lido"));
		database.insert("listagem_jornal", null, values);
		database.close();
	}

	public void insertMovimento(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id_movimentacao", queryValues.get("id_movimentacao"));
		values.put("des_status", queryValues.get("des_status"));
		values.put("des_endereco_completo", queryValues.get("des_endereco_completo"));
		values.put("dat_edicao", queryValues.get("dat_edicao"));
		values.put("cod_assinante", queryValues.get("cod_assinante"));
		values.put("nom_assinante", queryValues.get("nom_assinante"));
		values.put("des_produto", queryValues.get("des_produto"));
		values.put("des_modalidade", queryValues.get("des_modalidade"));
		values.put("cod_agente",queryValues.get("cod_agente"));
		database.insert("listagem_movimentacoes", null, values);

		database.close();
	}


    public ArrayList<HashMap<String, String>> Listagem(String data, String agente) {
        ArrayList<HashMap<String, String>> listagem;
        listagem = new ArrayList<HashMap<String, String>>();
        String selectQuery = "select id_registro, des_status, dat_edicao, des_assinante, " +
				             "des_endereco, des_referencia, num_cep, des_produto, des_modalidade," +
				             " cod_ordem, qtd_exemplares, dom_lido from listagem_jornal " +
				             "where dat_edicao =  '" +  data + "' and cod_agente = '" + agente +
				             "'order by des_status desc, cod_ordem, des_endereco";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id_registro", cursor.getString(0));
                map.put("des_status", cursor.getString(1));
                map.put("dat_edicao", cursor.getString(2).substring(0,10));
                map.put("des_assinante", cursor.getString(3));
                map.put("des_endereco", cursor.getString(4));
                map.put("des_referencia", cursor.getString(5));
                map.put("num_cep", cursor.getString(6));
                map.put("des_produto", cursor.getString(7));
                map.put("des_modalidade", cursor.getString(8));
                map.put("cod_ordem", cursor.getString(9));
                map.put("qtd_exemplares", cursor.getString(10));
				map.put("dom_lido", cursor.getString(11));

                listagem.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return listagem;
    }

	public ArrayList<HashMap<String, String>> ListaMovimento(String data, String agente) {
		ArrayList<HashMap<String, String>> listagem;
		listagem = new ArrayList<HashMap<String, String>>();
		String selectQuery = "select id_movimentacao, des_status, dat_edicao, cod_assinante, " +
				" nom_assinante, des_endereco_completo, des_produto, des_modalidade " +
				" from listagem_movimentacoes " +
				"where dat_edicao = '" +  data + "' and cod_agente = '" + agente +
				"' order by des_endereco_completo";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("id_movimentacao", cursor.getString(0).trim());
				map.put("des_status", cursor.getString(1).trim());
				map.put("dat_edicao", cursor.getString(2));
				map.put("cod_assinante", cursor.getString(3).trim());
				map.put("nom_assinante", cursor.getString(4).trim());
				map.put("des_endereco_completo", cursor.getString(5));
				map.put("des_produto", cursor.getString(6));
				map.put("des_modalidade", cursor.getString(7));

				listagem.add(map);
			} while (cursor.moveToNext());
		}
		database.close();
		return listagem;
	}


	public ArrayList<String> ListaAgentes() {
		ArrayList<String> listagem;
		listagem = new ArrayList<String>();
		String selectQuery = "select distinct cod_agente from listagem_jornal";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {

				listagem.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		database.close();
		return listagem;
	}

	public ArrayList<String> ListaDatas() {
		ArrayList<String> listagem;
		listagem = new ArrayList<String>();
		String selectQuery = "select distinct dat_edicao from listagem_jornal where des_status = '' order by dat_edicao";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {

				listagem.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		database.close();
		return listagem;
	}

	public void MarcaEntregue(String id, String lido) {
		String updateMarca = "update listagem_jornal set dom_lido = '" + lido + "' where id_registro = '" +
				             id + "'";
		SQLiteDatabase database = this.getWritableDatabase();
		database.execSQL(updateMarca);
	}


    public ArrayList<String> ListaDatasMov() {
        ArrayList<String> listagem;
        listagem = new ArrayList<String>();
        String selectQuery = "select distinct dat_edicao from listagem_movimentacoes order by dat_edicao";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                listagem.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        database.close();
        return listagem;
    }

    public ArrayList<String> ListaAgentesMov() {
        ArrayList<String> listagem;
        listagem = new ArrayList<String>();
        String selectQuery = "select distinct cod_agente from listagem_movimentacoes";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                listagem.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        database.close();
        return listagem;
    }
}

