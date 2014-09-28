import br.com.criativasoft.opendevice.wsrest.WSServerConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;

/**
 * @autor Ricardo JL Rufino
 * @date 05/07/14.
 */
public class TestServer {

    public static void main(String[] args) throws ConnectionException {
        new WSServerConnection(8181).connect();
    }
}
