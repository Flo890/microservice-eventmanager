package eventmanager.microservice.exception;

/**
 * Created by Flo on 24/04/2016.
 * represents problems that usually cannot occur, but have to be handled. e.g. missing driver classes in classpath
 */
public class ServiceStateException extends RuntimeException {

    public ServiceStateException(String message, Exception e){
        super(message,e);
    }

    public ServiceStateException(String message){
        super(message);
    }

}
