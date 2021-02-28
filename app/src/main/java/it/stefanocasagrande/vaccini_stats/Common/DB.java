package it.stefanocasagrande.vaccini_stats.Common;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_data;

public class DB extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    public DB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql_query="CREATE TABLE CONFIGURATION (id INTEGER PRIMARY KEY, NAME nvarchar(150), VALUE nvarchar(150))";
        sqLiteDatabase.execSQL(sql_query);

        sql_query="CREATE TABLE CONSEGNE_VACCINI (id INTEGER PRIMARY KEY, area nvarchar(150), fornitore nvarchar(150), numero_dosi INTEGER, data_consegna nvarchar(24), codice_NUTS1 nvarchar(150), codice_NUTS2 nvarchar(150), codice_regione_ISTAT INTEGER, nome_area nvarchar(150))";
        sqLiteDatabase.execSQL(sql_query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //region Utility

    public boolean Insert_Multi(String header, List<String> list_insert)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        StringBuilder blocco = new StringBuilder();
        int i = 0;

        if (list_insert.size()==0)
            return true;

        for (String s: list_insert) {

            if (!s.equals(" ")) {

                if (i==0)
                {
                    blocco.append(header);
                }

                i = i + 1;
                blocco.append(String.format("%s, ", s));

                if (i == 500) {
                    i = 0;
                    db.execSQL(blocco.substring(0, blocco.length() - 2));
                    blocco.setLength(0);
                }
            }
        }

        if (blocco.length()!=0) {
            db.execSQL(blocco.substring(0, blocco.length() - 2));
            blocco.setLength(0);
        }

        return true;
    }

    public boolean Delete(String tableName, String filter)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(String.format("DELETE FROM %s %s", tableName, filter));

        return true;
    }

    public String Validate_String(String valore)
    {
        if (TextUtils.isEmpty(valore))
            return "NULL";
        else
            return DatabaseUtils.sqlEscapeString(valore);
    }

    //endregion

    //region Configuration

    public boolean Set_Configurazione(String name, String value)
    {
        if (Delete("CONFIGURATION", "where NAME=" + Validate_String(name))) {

            String sql = String.format("INSERT INTO CONFIGURATION ( NAME, VALUE ) VALUES (%s, %s)  ", Validate_String(name), Validate_String(value));

            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(sql);

            return true;
        }
        else
            return false;
    }

    public String Get_Configurazione(String name)
    {
        String value="";

        String sql_query="SELECT VALUE from CONFIGURATION where NAME=" + Validate_String(name);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()){
            do {
                value = c.getString(0);

            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return value;
    }

    //endregion

    //region Consegne

    public List<consegne_vaccini_data> Get_Consegne(String area_name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        List<consegne_vaccini_data> lista = new ArrayList<>();

        String sql_query = "select area, fornitore, numero_dosi, data_consegna, codice_NUTS1, codice_NUTS2, codice_regione_ISTAT, nome_area from CONSEGNE_VACCINI where nome_area=" + Validate_String(area_name) + " order by data_consegna desc";

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()){
            do {
                consegne_vaccini_data var = new consegne_vaccini_data();

                var.nome_area = c.getString(0);
                var.fornitore = c.getString(1);
                var.numero_dosi = c.getInt(2);
                var.data_consegna = c.getString(3);
                var.codice_NUTS1 = c.getString(4);
                var.codice_NUTS2 = c.getString(5);
                var.codice_regione_ISTAT = c.getInt(6);
                var.nome_area = c.getString(7);

                lista.add(var);

            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return lista;
    }
    public List<consegne_vaccini_data> Get_Consegne_GroupBy_Area()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        List<consegne_vaccini_data> lista = new ArrayList<>();

        String sql_query = "select Sum(numero_dosi), nome_area, max(data_consegna) from CONSEGNE_VACCINI group by nome_area";

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()){
            do {
                consegne_vaccini_data var = new consegne_vaccini_data();

                var.numero_dosi = c.getInt(0);
                var.nome_area = c.getString(1);
                var.data_consegna = c.getString(2);
                lista.add(var);

            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return lista;
    }

    public void Insert_Consegne(List<consegne_vaccini_data> lista) {
        if (lista.size() == 0)
            return;

        if (Delete("CONSEGNE_VACCINI", "")) {

            List<String> sql_insert_values = new ArrayList<>();

            for (consegne_vaccini_data var : lista)
                sql_insert_values.add(String.format("(%s, %s, %s, %s, %s, %s, %s, %s)",
                        Validate_String(var.area),
                        Validate_String(var.fornitore),
                        var.numero_dosi,
                        Validate_String(var.data_consegna),
                        Validate_String(var.codice_NUTS1),
                        Validate_String(var.codice_NUTS2),
                        var.codice_regione_ISTAT,
                        Validate_String(var.nome_area)
                        ));

            Insert_Multi("INSERT INTO CONSEGNE_VACCINI ( area, fornitore, numero_dosi, data_consegna, codice_NUTS1, codice_NUTS2, codice_regione_ISTAT, nome_area ) VALUES ", sql_insert_values);
        }
    }

    //endregion
}
