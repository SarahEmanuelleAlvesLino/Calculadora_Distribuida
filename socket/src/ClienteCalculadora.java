import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Cliente da calculadora distribuída — versão SOCKET (TCP).
 *
 * O cliente:
 *  - Conecta no servidor (host + porta).
 *  - Le comandos do teclado, envia pro servidor e mostra a resposta.
 *
 * Comandos aceitos (os mesmos do protocolo do servidor):
 *   SOMA 3 5 | SUB 10 4 | MUL 2 6 | DIV 8 2 | SAIR
 */
public class ClienteCalculadora {

    // Para testar na MESMA maquina, use "localhost".
    // Para testar em maquinas DIFERENTES, coloque aqui o IP do servidor.
    // Dica: da pra passar host e porta por argumento (veja o main).
    private static final String HOST_PADRAO = "localhost";
    private static final int PORTA_PADRAO = 5000;

    public static void main(String[] args) {
        // Permite rodar assim:  java ClienteCalculadora 192.168.0.10 5000
        String host = (args.length >= 1) ? args[0] : HOST_PADRAO;
        int porta = (args.length >= 2) ? Integer.parseInt(args[1]) : PORTA_PADRAO;

        System.out.println("[Cliente] Conectando em " + host + ":" + porta + "...");

        try (
            Socket socket = new Socket(host, porta);
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            Scanner teclado = new Scanner(System.in)
        ) {
            System.out.println("[Cliente] Conectado! Digite operacoes (ex: SOMA 3 5).");
            System.out.println("[Cliente] Digite SAIR para encerrar.");

            while (true) {
                System.out.print("> ");
                String comando = teclado.nextLine().trim();

                if (comando.isEmpty()) {
                    continue;
                }

                // Envia o comando pro servidor.
                saida.println(comando);

                // Le a resposta do servidor.
                String resposta = entrada.readLine();
                if (resposta == null) {
                    System.out.println("[Cliente] Servidor encerrou a conexao.");
                    break;
                }
                System.out.println(resposta);

                if (comando.equalsIgnoreCase("SAIR")) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("[Cliente] Host desconhecido: " + host);
        } catch (IOException e) {
            System.err.println("[Cliente] Erro de conexao: " + e.getMessage()
                    + " (o servidor esta rodando?)");
        }

        System.out.println("[Cliente] Encerrado.");
    }
}
