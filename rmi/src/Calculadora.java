import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface REMOTA da calculadora — versão RMI.
 *
 * Esta é a peça central do RMI: o "contrato" que diz quais métodos podem ser
 * chamados remotamente. Tanto o servidor (que implementa) quanto o cliente
 * (que chama) precisam conhecer esta interface.
 *
 * Regras que o RMI exige de uma interface remota:
 *  - estender java.rmi.Remote (marca a interface como "invocável remotamente");
 *  - todo método remoto deve declarar "throws RemoteException" (porque uma
 *    chamada pela rede pode falhar por motivos que uma chamada local nunca teria).
 *
 * Compare com o socket: lá não havia interface nenhuma. O "contrato" era um
 * texto combinado ("SOMA 3 5") que nada garantia em tempo de compilação. Aqui,
 * o contrato é verificado pelo compilador Java.
 */
public interface Calculadora extends Remote {

    double soma(double a, double b) throws RemoteException;

    double sub(double a, double b) throws RemoteException;

    double mul(double a, double b) throws RemoteException;

    /** Pode lançar ArithmeticException (divisão por zero) — ela viaja de volta
     *  ao cliente pela própria infraestrutura do RMI. */
    double div(double a, double b) throws RemoteException;
}
