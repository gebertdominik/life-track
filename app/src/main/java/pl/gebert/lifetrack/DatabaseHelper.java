package pl.gebert.lifetrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import pl.gebert.lifetrack.config.UserParam;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance = null;
    // Logcat tag
    private static final String LOG = DatabaseHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ltdb";

    // Table Names
    private static final String TABLE_USER_PARAMS = "user_parameters";

    // TABLE_USER_PARAMS Table - column namaes
    private static final String USER_PARAMETER_ID = "user_parameter_id";
    private static final String CODE = "code";
    private static final String VALUE = "value";

    private static final String WEIGHT_USER_PARAM_CODE = "WEIGHT";
    private static final String WEIGHT_USER_PARAM_DEFAULT_VALUE = "80";

    //Clinics table create statement
    private static final String CREATE_TABLE_USER_PARAMS = "CREATE TABLE " + TABLE_USER_PARAMS + "(" +
            USER_PARAMETER_ID + " INTEGER PRIMARY KEY," +
            CODE + " TEXT," +
            VALUE + " TEXT" +")";

    private static final String INSERT_WEIGHT_PARAMETER = "INSERT INTO " +TABLE_USER_PARAMS +
            " VALUES ( 1, \""+ WEIGHT_USER_PARAM_CODE + "\",\"" + WEIGHT_USER_PARAM_DEFAULT_VALUE +"\")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER_PARAMS);
        db.execSQL(INSERT_WEIGHT_PARAMETER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PARAMS);
        onCreate(db);
    }

    public long createUserParameter(UserParam userParam){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CODE, userParam.getCode());
        values.put(VALUE, userParam.getValue());

        long userParameterId = db.insert(TABLE_USER_PARAMS, null, values);

        return userParameterId;

    }

    public int updateUserParameter(UserParam userParam) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CODE, userParam.getCode());
        values.put(VALUE, userParam.getValue());

        return db.update(TABLE_USER_PARAMS, values, USER_PARAMETER_ID + " = ?",
                new String[] {String.valueOf(userParam.getUserParamId())});
    }

    public UserParam findUserParamByCode(String code){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_USER_PARAMS + " WHERE "
                + CODE + " = " + code;

        Log.e(LOG, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);

        if(c != null)
            c.moveToFirst();

        UserParam userParam = new UserParam();
        setUserParamValues(c, userParam);
        c.close();
        return userParam;
    }
    public void clearData(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PARAMS);
        onCreate(db);
    }

    public static DatabaseHelper getInstance(Context context){
        if(instance == null)
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private void setUserParamValues(Cursor c, UserParam userParam){
        userParam.setUserParamId(c.getInt(c.getColumnIndex(USER_PARAMETER_ID)));
        userParam.setCode(c.getString(c.getColumnIndex(CODE)));
        userParam.setValue(c.getString(c.getColumnIndex(VALUE)));

    }
}