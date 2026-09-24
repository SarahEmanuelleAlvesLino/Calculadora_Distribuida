import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementação do objeto remoto — versão RMI.
 *
 * É aqui que a lógica da calculadora realmente vive. Este objeto será
 * "exportado" (publicado na rede) e registrado no RMI Registry pelo servidor.
 *
 * Estender UnicastRemoteObject faz o trabalho pesado de exportar o objeto:
 * abre uma porta, cria o "esqueleto" que recebe as chamadas da rede e converte
 * cada chamada remota numa chamada de método normal aqui dentro.
 */
public class CalculadoraImpl extends UnicastRemoteObject implements Calculadora {

    // O construtor precisa declarar RemoteException por causa da exportação.
    public CalculadoraImpl() throws RemoteException {
        super();
    }

    @Override
    public double soma(double a, double b) throws RemoteException {
        System.out.println("[Servidor] soma(" + a + ", " + b + ")");
        return a + b;
    }

    @Override
    public double sub(double a, double b) throws RemoteException {
        System.out.println("[Servidor] sub(" + a + ", " + b + ")");
        return a - b;
    }

    @Override
    public double mul(double a, double b) throws RemoteException {
        System.out.println("[Servidor] mul(" + a + ", " + b + ")");
        return a * b;
    }

    @Override
    public double div(double a, double b) throws RemoteException {
        System.out.println("[Servidor] div(" + a + ", " + b + ")");
        if (b == 0) {
            // Esta exceção é lançada NO SERVIDOR, mas o RMI a serializa e
            // relança automaticamente NO CLIENTE. O cliente só precisa
            // dar um catch — sem nenhum protocolo de erro combinado na mão.
            throw new ArithmeticException("Divisao por zero.");
        }
        return a / b;
    }
}
