import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Cliente — versão RMI.
 *
 * O que o cliente faz:
 *  1. Localiza o RMI Registry (host + porta).
 *  2. Faz "lookup" do objeto remoto pelo NOME e recebe uma referência que
 *     obedece à interface Calculadora.
 *  3. Chama os métodos remotos como se o objeto fosse LOCAL:  calc.soma(3, 5).
 *
 * A mágica do RMI: essa referência (o "stub") empacota os parâmetros, manda
 * pela rede, espera o retorno e o desempacota — tudo automático. Nós não
 * escrevemos nada de protocolo, parsing ou serialização como no socket.
 *
 * Mantemos a MESMA interface de comandos do cliente de socket (você digita
 * "SOMA 3 5") só para a comparação ficar justa — mas repare que, por baixo,
 * cada comando vira uma simples chamada de método.
 */
public class ClienteRMI {

    private static final String HOST_PADRAO = "localhost";
    private static final int PORTA_PADRAO = 1099;
    private static final String NOME = "CalculadoraRMI";

    public static void main(String[] args) {
        // Permite rodar assim:  java ClienteRMI 192.168.0.10 1099
        String host = (args.length >= 1) ? args[0] : HOST_PADRAO;
        int porta = (args.length >= 2) ? Integer.parseInt(args[1]) : PORTA_PADRAO;

        try {
            // 1. e 2. Encontra o Registry e busca o objeto remoto pelo nome.
            Registry registry = LocateRegistry.getRegistry(host, porta);
            Calculadora calc = (Calculadora) registry.lookup(NOME);

            System.out.println("[Cliente] Conectado ao objeto remoto '" + NOME + "'.");
            System.out.println("[Cliente] Digite operacoes (ex: SOMA 3 5). Digite SAIR para encerrar.");

            Scanner teclado = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String comando = teclado.nextLine().trim();

                if (comando.isEmpty()) {
                    continue;
                }
                if (comando.equalsIgnoreCase("SAIR")) {
                    System.out.println("CONEXÃO FINALIZADA");
                    break;
                }

                System.out.println(processar(calc, comando));
            }
        } catch (Exception e) {
            System.err.println("[Cliente] Erro: " + e.getMessage()
                    + " (o servidor RMI esta rodando?)");
        }

        System.out.println("[Cliente] Encerrado.");
    }

    /**
     * Interpreta "OPERACAO A B" e chama o método remoto correspondente.
     * Mantém o mesmo formato de saída do cliente de socket para comparar.
     */
    private static String processar(Calculadora calc, String comando) {
        String[] p = comando.split("\\s+");

        if (p.length != 3) {
            return "ERRO Formato invalido. Use: OPERACAO A B (ex: SOMA 3 5)";
        }

        double a, b;
        try {
            a = Double.parseDouble(p[1]);
            b = Double.parseDouble(p[2]);
        } catch (NumberFormatException e) {
            return "ERRO Os operandos precisam ser numeros.";
        }

        try {
            switch (p[0].toUpperCase()) {
                case "SOMA": return "RESULTADO " + calc.soma(a, b);
                case "SUB":  return "RESULTADO " + calc.sub(a, b);
                case "MUL":  return "RESULTADO " + calc.mul(a, b);
                case "DIV":  return "RESULTADO " + calc.div(a, b);
                default:     return "ERRO Operacao desconhecida: " + p[0];
            }
        } catch (Exception e) {
            // Exceções lançadas no servidor (ex.: divisão por zero) chegam
            // aqui automaticamente, via RMI.
            return "ERRO " + e.getMessage();
        }
    }
}
