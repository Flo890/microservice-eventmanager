package eventmanager.microservice.exception;

/**
 * Created by Flo on 21/04/2016.
 * if properties are not or wrong set
 */
public class ServiceConfigurationException extends RuntimeException {

    public ServiceConfigurationException(String message){
        super(message);
    }

}
