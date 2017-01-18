package eventmanager.microservice.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import java.util.Map;

/**
 * Created by Flo on 07/04/2016.
 */
public class AppConfiguration extends Configuration {

    private Map<String,String> dbConfiguration;

    @JsonProperty
    public Map<String, String> getDbConfiguration() {
        return dbConfiguration;
    }

    @JsonProperty
    public void setDbConfiguration(Map<String, String> dbConfiguration) {
        this.dbConfiguration = dbConfiguration;
    }

}
