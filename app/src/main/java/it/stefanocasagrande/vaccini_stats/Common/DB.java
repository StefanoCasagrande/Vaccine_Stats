package it.stefanocasagrande.vaccini_stats.Common;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import it.stefanocasagrande.vaccini_stats.json_classes.anagrafica_regioni_eta;
import it.stefanocasagrande.vaccini_stats.json_classes.anagrafica_vaccini_summary.anagrafica_vaccini_summary_data;
import it.stefanocasagrande.vaccini_stats.json_classes.consegne_vaccini.consegne_vaccini_data;
import it.stefanocasagrande.vaccini_stats.json_classes.somministrazioni_data;
import it.stefanocasagrande.vaccini_stats.json_classes.vaccini_summary.vaccini_summary_data;

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

        sql_query="CREATE TABLE DELIVERIES (id INTEGER PRIMARY KEY, area nvarchar(150), fornitore nvarchar(150), numero_dosi INTEGER, data_consegna nvarchar(24), codice_NUTS1 nvarchar(150), codice_NUTS2 nvarchar(150), codice_regione_ISTAT INTEGER, nome_area nvarchar(150))";
        sqLiteDatabase.execSQL(sql_query);

        sql_query="CREATE TABLE SUMMARY_BY_AGE (id INTEGER PRIMARY KEY, fascia_anagrafica nvarchar(150), ultimo_aggiornamento nvarchar(150), totale INTEGER, sesso_maschile INTEGER, sesso_femminile INTEGER, prima_dose INTEGER, seconda_dose INTEGER)";
        sqLiteDatabase.execSQL(sql_query);

        sql_query="CREATE TABLE SUMMARY_BY_LOCATION (id INTEGER PRIMARY KEY, area nvarchar(150), dosi_somministrate INTEGER, dosi_consegnate INTEGER, ultimo_aggiornamento NVARCHAR(50), nome_area nvarchar(50))";
        sqLiteDatabase.execSQL(sql_query);

        sql_query="CREATE TABLE SOMMINISTRAZIONI (id INTEGER PRIMARY KEY, data_somministrazione integer,area nvarchar(150),totale INTEGER, nome_area nvarchar(150), prima_dose INTEGER, seconda_dose INTEGER)";
        sqLiteDatabase.execSQL(sql_query);

        sql_query="CREATE TABLE POPOLAZIONE (id INTEGER PRIMARY KEY, fascia_anagrafica nvarchar(150), territorio nvarchar(150), totale INTEGER)";
        sqLiteDatabase.execSQL(sql_query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void Check_Table()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql_query;

        sql_query="CREATE TABLE if not exists  SOMMINISTRAZIONI (id INTEGER PRIMARY KEY, data_somministrazione integer,area nvarchar(150),totale INTEGER, nome_area nvarchar(150))";
        db.execSQL(sql_query);

        sql_query="CREATE TABLE if not exists POPOLAZIONE (id INTEGER PRIMARY KEY, fascia_anagrafica nvarchar(150), territorio nvarchar(150), totale INTEGER)";
        db.execSQL(sql_query);

        sql_query="CREATE TABLE if not exists SUMMARY_BY_LOCATION_AGE (id INTEGER PRIMARY KEY, area nvarchar(150), fascia_anagrafica nvarchar(150), prima_dose INTEGER, seconda_dose INTEGER)";
        db.execSQL(sql_query);

        if (!doColumnExists("SOMMINISTRAZIONI", "prima_dose",db))
        {
            sql_query="ALTER TABLE SOMMINISTRAZIONI ADD COLUMN prima_dose INTEGER";
            db.execSQL(sql_query);
        }

        if (!doColumnExists("SOMMINISTRAZIONI", "seconda_dose",db))
        {
            sql_query="ALTER TABLE SOMMINISTRAZIONI ADD COLUMN seconda_dose INTEGER";
            db.execSQL(sql_query);
        }

        if (!doColumnExists("SUMMARY_BY_AGE", "dose_addizionale_booster",db))
        {
            sql_query="ALTER TABLE SUMMARY_BY_AGE ADD COLUMN dose_addizionale_booster INTEGER";
            db.execSQL(sql_query);
        }

        if (!doColumnExists("SOMMINISTRAZIONI", "dose_addizionale_booster",db))
        {
            sql_query="ALTER TABLE SOMMINISTRAZIONI ADD COLUMN dose_addizionale_booster INTEGER";
            db.execSQL(sql_query);
        }
    }

    //region Utility

    public static boolean doColumnExists (String table, String column, SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("PRAGMA table_info("+ table +")", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                if (column.equalsIgnoreCase(name)) {
                    cursor.close();
                    return true;
                }
            }
        }

        if (cursor!=null)
            cursor.close();
        return false;
    }

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

    //region Deliveries

    public List<consegne_vaccini_data> Get_Deliveries(String area_name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        List<consegne_vaccini_data> lista = new ArrayList<>();

        String sql_query = "select area, fornitore, numero_dosi, data_consegna, codice_NUTS1, codice_NUTS2, codice_regione_ISTAT, nome_area from DELIVERIES ";

        if (!area_name.equals(""))
            sql_query+=String.format(" where nome_area=%s ", Validate_String(area_name));

        sql_query+=" order by data_consegna desc";

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
    public List<consegne_vaccini_data> Get_Deliveries_GroupBy_Area()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        List<consegne_vaccini_data> lista = new ArrayList<>();

        String sql_query = "select dosi_consegnate, a.nome_area, ultima_consegna, dosi_somministrate, prima_dose from ( select Sum(numero_dosi) as dosi_consegnate, DELIVERIES.nome_area, max(data_consegna) as ultima_consegna from DELIVERIES group by DELIVERIES.nome_area ) a inner join ( select sum(totale) as dosi_somministrate, nome_area, sum(prima_dose) as prima_dose from SOMMINISTRAZIONI group by nome_area ) b on b.nome_area = a.nome_area ";

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()){
            do {
                consegne_vaccini_data var = new consegne_vaccini_data();

                var.numero_dosi = c.getInt(0);
                var.nome_area = c.getString(1);
                var.data_consegna = c.getString(2);
                var.dosi_somministrate = c.getInt(3);
                var.prima_dose = c.getInt(4);
                lista.add(var);

            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return lista;
    }

    public boolean Insert_Deliveries(List<consegne_vaccini_data> lista) {
        if (lista.size() == 0)
            return false;

        if (Delete("DELIVERIES", "")) {

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

            Insert_Multi("INSERT INTO DELIVERIES ( area, fornitore, numero_dosi, data_consegna, codice_NUTS1, codice_NUTS2, codice_regione_ISTAT, nome_area ) VALUES ", sql_insert_values);
        }

        return true;
    }

    //endregion

    //region Summary_By_Age

    public boolean Insert_anagrafica_vaccini_summary(List<anagrafica_vaccini_summary_data> lista)
    {
        if (lista.size() == 0)
            return false;

        if (Delete("SUMMARY_BY_AGE", "")) {

            List<String> sql_insert_values = new ArrayList<>();

            for (anagrafica_vaccini_summary_data var : lista)
                sql_insert_values.add(String.format("(%s, %s, %s, %s, %s, %s, %s, %s)",
                        Validate_String(var.fascia_anagrafica),
                        var.totale,
                        var.sesso_maschile,
                        var.sesso_femminile,
                        var.prima_dose,
                        var.seconda_dose,
                        var.dose_addizionale_booster,
                        Validate_String(var.ultimo_aggiornamento)
                ));

            Insert_Multi("INSERT INTO SUMMARY_BY_AGE ( fascia_anagrafica, totale, sesso_maschile, sesso_femminile, prima_dose, seconda_dose, dose_addizionale_booster, ultimo_aggiornamento ) VALUES ", sql_insert_values);
        }

        return true;
    }

    public int Get_Totale_Vaccini_Somministrazioni()
    {
        String sql_query = "SELECT Sum(totale) from SUMMARY_BY_AGE ";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(sql_query, null);

        c.moveToFirst();
        int valore =  c.getInt(0);

        c.close();
        return valore;
    }

    public List<anagrafica_vaccini_summary_data> Get_anagrafica_vaccini_summary()
    {
        String sql_query = "SELECT fascia_anagrafica, totale, sesso_maschile, sesso_femminile, prima_dose, seconda_dose, dose_addizionale_booster, ultimo_aggiornamento from SUMMARY_BY_AGE ";

        SQLiteDatabase db = this.getWritableDatabase();
        List<anagrafica_vaccini_summary_data> lista = new ArrayList<>();

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()){
            do {
                anagrafica_vaccini_summary_data var = new anagrafica_vaccini_summary_data();

                var.fascia_anagrafica = c.getString(0);
                var.totale = c.getInt(1);
                var.sesso_maschile = c.getInt(2);
                var.sesso_femminile = c.getInt(3);
                var.prima_dose = c.getInt(4);
                var.seconda_dose = c.getInt(5);
                var.dose_addizionale_booster = c.getInt(6);
                var.ultimo_aggiornamento = c.getString(7);
                lista.add(var);

            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return lista;
    }

    //endregion

    //region Anagrafica Regione

    private List<String> get_Area_List()
    {
        String sql_query="select area from SUMMARY_BY_LOCATION group by area";

        SQLiteDatabase db = this.getWritableDatabase();
        List<String> lista = new ArrayList<>();

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()) {
            do {
                lista.add(c.getString(0));
            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return lista;
    }

    public List<String> get_Eta_List()
    {
        String sql_query="select fascia_anagrafica from SUMMARY_BY_AGE group by fascia_anagrafica";

        SQLiteDatabase db = this.getWritableDatabase();
        List<String> lista = new ArrayList<>();

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()) {
            do {
                lista.add(c.getString(0));
            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return lista;
    }

    public anagrafica_regioni_eta Get_Anagrafica_Regione(String area, String fascia_eta)
    {
        String sql_query=String.format("select fascia_anagrafica, prima_dose, seconda_dose from SUMMARY_BY_LOCATION_AGE inner join ( select area, nome_area from SUMMARY_BY_LOCATION group by area, nome_area ) desc_zone on desc_zone.area = SUMMARY_BY_LOCATION_AGE.area where nome_area=%s and fascia_anagrafica=%s", Validate_String(area), Validate_String(fascia_eta));

        SQLiteDatabase db = this.getWritableDatabase();
        anagrafica_regioni_eta oggetto = new anagrafica_regioni_eta();

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()) {
            do {
                oggetto.fascia_eta = c.getString(0);
                oggetto.area = area;
                oggetto.prima_dose = c.getInt(1);
                oggetto.seconda_dose = c.getInt(2);
            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return oggetto;
    }

    public boolean Insert_Anagrafica_Regione(JsonObject lista_anagrafica_regione)
    {
        if (lista_anagrafica_regione==null)
            return false;

        List<String> sql_insert_values = new ArrayList<>();

        if (Delete("SUMMARY_BY_LOCATION_AGE", "")) {

            List<String> lista_area = get_Area_List();
            List<String> lista_eta = get_Eta_List();

            for(String area : lista_area)
            {
                for(String eta : lista_eta)
                {
                    if (lista_anagrafica_regione.getAsJsonObject("dataset")==null || lista_anagrafica_regione.getAsJsonObject("dataset").getAsJsonObject(area)==null || lista_anagrafica_regione.getAsJsonObject("dataset").getAsJsonObject(area).getAsJsonObject(eta)==null)
                        break;

                    int prima_dose = lista_anagrafica_regione.getAsJsonObject("dataset").getAsJsonObject(area).getAsJsonObject(eta).get("prima_dose").getAsInt();
                    int seconda_dose = lista_anagrafica_regione.getAsJsonObject("dataset").getAsJsonObject(area).getAsJsonObject(eta).get("seconda_dose").getAsInt();

                    sql_insert_values.add(String.format("(%s, %s, %s, %s)",
                            Validate_String(eta),
                            Validate_String(area),
                            prima_dose,
                            seconda_dose
                    ));

                }
            }

            Insert_Multi("INSERT INTO SUMMARY_BY_LOCATION_AGE ( fascia_anagrafica, area, prima_dose, seconda_dose ) VALUES ", sql_insert_values);
        }

        return true;
    }

    //endregion

    //region Summary_By_Location

    public boolean Insert_vaccini_summary(List<vaccini_summary_data> lista)
    {
        if (lista.size() == 0)
            return false;

        if (Delete("SUMMARY_BY_LOCATION", "")) {

            List<String> sql_insert_values = new ArrayList<>();

            for (vaccini_summary_data var : lista)
                sql_insert_values.add(String.format("(%s, %s, %s, %s, %s)",
                        Validate_String(var.area),
                        var.dosi_somministrate,
                        var.dosi_consegnate,
                        Validate_String(var.ultimo_aggiornamento),
                        Validate_String(var.nome_area)
                ));

            Insert_Multi("INSERT INTO SUMMARY_BY_LOCATION ( area, dosi_somministrate, dosi_consegnate, ultimo_aggiornamento, nome_area ) VALUES ", sql_insert_values);
        }

        return true;
    }

    public List<vaccini_summary_data> Get_vaccini_summary(String area_name)
    {
        String sql_query = "SELECT area, dosi_somministrate, dosi_consegnate, ultimo_aggiornamento, SUMMARY_BY_LOCATION.nome_area, prima_dose from SUMMARY_BY_LOCATION inner join ( select nome_area, sum(prima_dose) as prima_dose from SOMMINISTRAZIONI group by nome_area ) b on b.nome_area = SUMMARY_BY_LOCATION.nome_area ";

        if (!area_name.equals(""))
            sql_query+=" where SUMMARY_BY_LOCATION.nome_area=" + Validate_String(area_name);

        SQLiteDatabase db = this.getWritableDatabase();
        List<vaccini_summary_data> lista = new ArrayList<>();

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()){
            do {
                vaccini_summary_data var = new vaccini_summary_data();

                var.area = c.getString(0);
                var.dosi_somministrate = c.getInt(1);
                var.dosi_consegnate = c.getInt(2);
                var.ultimo_aggiornamento = c.getString(3);
                var.nome_area = c.getString(4);
                var.prima_dose = c.getInt(5);
                lista.add(var);

            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return lista;
    }

    //endregion

    //region SOMMINISTRAZIONI

    public boolean Insert_Somministrazioni(List<List<String>> lista) {

        if (lista.size() <2)
            return false;

        if (Delete("SOMMINISTRAZIONI", "")) {

            List<String> sql_insert_values = new ArrayList<>();

            int i =0;
            int posizione_data_somministrazione=0;
            int posizione_area=0;
            int posizione_totale=0;
            int posizione_sesso_maschile=0;
            int posizione_sesso_femminile=0;
            int posizione_prima_dose=0;
            int posizione_seconda_dose=0;
            int posizione_codice_NUTS1=0;
            int posizione_codice_NUTS2=0;
            int posizione_codice_regione_ISTAT=0;
            int posizione_nome_area=0;
            int posizione_dose_addizionale_booster=0;

            for (List<String> var : lista)
            {
                if (i==0)
                {
                    i=i+1;
                    /*
                    ,,,,,,,,,,,,,,
                     */


                    for (int posizione=0;posizione<var.size();posizione++)
                    {
                        switch (var.get(posizione).toLowerCase())
                        {
                            case "data_somministrazione":
                                posizione_data_somministrazione=posizione;
                                break;
                            case "area":
                                posizione_area=posizione;
                                break;
                            case "totale":
                                posizione_totale=posizione;
                                break;
                            case "sesso_maschile":
                                posizione_sesso_maschile=posizione;
                                break;
                            case "sesso_femminile":
                                posizione_sesso_femminile=posizione;
                                break;
                            case "prima_dose":
                                posizione_prima_dose=posizione;
                                break;
                            case "seconda_dose":
                                posizione_seconda_dose=posizione;
                                break;
                            case "codice_NUTS1":
                                posizione_codice_NUTS1=posizione;
                                break;
                            case "codice_NUTS2":
                                posizione_codice_NUTS2=posizione;
                                break;
                            case "codice_regione_ISTAT":
                                posizione_codice_regione_ISTAT=posizione;
                                break;
                            case "nome_area":
                                posizione_nome_area=posizione;
                                break;
                            case "dose_addizionale_booster":
                                posizione_dose_addizionale_booster=posizione;
                                break;
                        }
                    }
                }
                else {

                    sql_insert_values.add(String.format("(%s, %s, %s, %s, %s, %s, %s)",
                            Common.get_int_from_Date(var.get(posizione_data_somministrazione)),
                            Validate_String(var.get(posizione_area)),
                            var.get(posizione_totale),
                            Validate_String(var.get(posizione_nome_area)),
                            var.get(posizione_prima_dose),        // prima dose
                            var.get(posizione_seconda_dose),      // seconda dose
                            var.get(posizione_dose_addizionale_booster)));      // seconda dose
                }
            }

            Insert_Multi("INSERT INTO SOMMINISTRAZIONI ( data_somministrazione,area,totale, nome_area, prima_dose, seconda_dose, dose_addizionale_booster ) VALUES ", sql_insert_values);
        }

        return true;
    }

    public int get_Prime_Dosi_Null()
    {
        String sql_query = "SELECT sum(prima_dose) from SOMMINISTRAZIONI ";
        SQLiteDatabase db = this.getWritableDatabase();
        int dosi=0;

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()) {
            do {
                dosi = c.getInt(0);
            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return dosi;
    }

    public List<somministrazioni_data> get_Somministrazioni(int start_date, int end_date, String area_name)
    {
        String sql_query = "SELECT data_somministrazione,sum(totale), sum(prima_dose), sum(seconda_dose), sum(dose_addizionale_booster) from SOMMINISTRAZIONI  ";

        sql_query +=String.format(" where data_somministrazione between %s and %s ", start_date, end_date);

        if (!TextUtils.isEmpty(area_name))
            sql_query+= String.format(" and nome_area=%s ", Validate_String(area_name));

        sql_query+=" group by data_somministrazione order by data_somministrazione";

        SQLiteDatabase db = this.getWritableDatabase();
        List<somministrazioni_data> lista = new ArrayList<>();

        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()){
            do {
                somministrazioni_data var = new somministrazioni_data();

                var.data_somministrazione = c.getInt(0);
                var.totale = c.getInt(1);
                var.prima_dose = c.getInt(2);
                var.seconda_dose = c.getInt(3);
                var.terza_dose = c.getInt(4);
                lista.add(var);

            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return lista;
    }

    //endregion

    //region Popolazione

    public boolean Insert_Popolazione(List<List<String>> lista) {

        if (Delete("POPOLAZIONE", "")) {

            List<String> sql_insert_values = new ArrayList<>();

            int posizione_age=0;
            int posizione_location=0;
            int posizione_total=0;

            int i=0;

            for (List<String> var : lista)
            {
                if (i==0)
                {
                    i+=1;

                    for(int posizione=0;posizione<var.size();posizione++)
                    {
                        switch (var.get(posizione).toLowerCase())
                        {
                            case "age":
                                posizione_age=posizione;
                                break;
                            case "location":
                                posizione_location=posizione;
                                break;
                            case "total":
                                posizione_total=posizione;
                                break;
                        }
                    }
                }
                else
                {
                    sql_insert_values.add(String.format("(%s, %s, %s)",
                            Validate_String(var.get(posizione_age)),
                            Validate_String(var.get(posizione_location)),
                            var.get(posizione_total)));
                }
            }


            Insert_Multi("INSERT INTO POPOLAZIONE ( fascia_anagrafica, territorio, totale ) VALUES ", sql_insert_values);
        }

        return true;
    }

    public int Get_Popolazione(String age, String location)
    {
        String sql_query="SELECT SUM(TOTALE) from POPOLAZIONE WHERE 1=1 ";
        int value=0;

        if (!TextUtils.isEmpty(age))
            sql_query+=" AND FASCIA_ANAGRAFICA=" + Validate_String(age);

        if (!TextUtils.isEmpty(location))
            sql_query+=" AND territorio=" + Validate_String(location);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(sql_query, null);
        if (c.moveToFirst()){
            do {
                value = c.getInt(0);

            } while(c.moveToNext());
        }
        c.close();
        db.close();

        return value;
    }

    //endregion
}
