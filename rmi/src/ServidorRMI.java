import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Servidor — versão RMI.
 *
 * O que o servidor faz:
 *  1. Cria o objeto remoto (CalculadoraImpl), que já se exporta na construção.
 *  2. Sobe um RMI Registry (uma espécie de "lista telefônica" de objetos
 *     remotos) na porta 1099.
 *  3. Registra o objeto nesse Registry sob um NOME. É por esse nome que o
 *     cliente vai encontrá-lo.
 *
 * Observação: diferente do servidor de socket, aqui NÃO existe um laço
 * accept()/readLine(). O próprio RMI cria as threads e recebe as chamadas por
 * baixo dos panos. Nós só publicamos o objeto e deixamos a JVM rodando.
 */
public class ServidorRMI {

    private static final int PORTA = 1099;              // porta padrão do RMI Registry
    private static final String NOME = "CalculadoraRMI"; // nome de registro do objeto

    public static void main(String[] args) {
        try {
            // 1. Cria o objeto remoto.
            CalculadoraImpl calc = new CalculadoraImpl();

            // 2. Cria (sobe) o RMI Registry embutido nesta própria JVM.
            //    Assim você NÃO precisa rodar o comando 'rmiregistry' à parte.
            Registry registry = LocateRegistry.createRegistry(PORTA);

            // 3. Publica o objeto no Registry sob o nome combinado.
            registry.rebind(NOME, calc);

            System.out.println("[Servidor] Objeto remoto '" + NOME
                    + "' registrado na porta " + PORTA + ".");
            System.out.println("[Servidor] Servidor RMI no ar. Aguardando chamadas...");
            // O main termina aqui, mas a JVM continua viva: o objeto exportado
            // mantém threads do RMI ativas esperando chamadas. Para parar,
            // use Ctrl+C.
        } catch (Exception e) {
            System.err.println("[Servidor] Erro ao iniciar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
