package pl.gebert.lifetrack.config;

/**
 * Created by Dominik Gebert on 2016-01-06.
 */
public class UserParam {

        private int userParamId;
        private String code;
        private String value;


    public UserParam(){
        super();
    }

    public UserParam(String code, String value){
        this.code = code;
        this.value = value;
    }

    public int getUserParamId() {
        return userParamId;
    }

    public void setUserParamId(int userParamId) {
        this.userParamId = userParamId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
