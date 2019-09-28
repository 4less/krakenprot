package sequence.utils;

/**
 * Created by joachim on 16.03.19.
 */
public class UnknownAAException extends RuntimeException {

    public UnknownAAException(){
        super();
    }

    public UnknownAAException(String message){
        super(message);
    }
}