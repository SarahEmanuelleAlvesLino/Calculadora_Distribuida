import java.io.*;
import java.net.*;

/**
 * Servidor da calculadora distribuída — versão SOCKET (TCP).
 *
 * Responsabilidades:
 *  - Abrir um ServerSocket numa porta e ficar escutando conexões.
 *  - Para cada cliente que conecta, tratar a comunicação em uma thread
 *    separada (assim vários clientes podem usar o servidor ao mesmo tempo).
 *  - Interpretar mensagens de texto no formato "OPERACAO A B" e devolver
 *    o resultado.
 *
 * Protocolo (texto, uma linha por mensagem):
 *   Cliente  -> Servidor : SOMA 3 5 | SUB 10 4 | MUL 2 6 | DIV 8 2 | SAIR
 *   Servidor -> Cliente  : RESULTADO 8.0   ou   ERRO <mensagem>
 *
 * Ponto importante para o relatório: em socket NÓS definimos o protocolo na
 * mão e fazemos todo o parsing/serialização das mensagens. É esse trabalho
 * manual que o RPC e o RMI escondem de nós.
 */
public class ServidorCalculadora {

    // Porta onde o servidor vai escutar. Troque aqui se a porta estiver ocupada.
    private static final int PORTA = 5000;

    public static void main(String[] args) {
        System.out.println("[Servidor] Iniciando na porta " + PORTA + "...");

        // try-with-resources garante que o ServerSocket seja fechado no final.
        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            System.out.println("[Servidor] Aguardando conexoes...");

            // Laco infinito: o servidor fica de pe aceitando novos clientes.
            while (true) {
                // accept() BLOQUEIA a execucao ate um cliente conectar.
                Socket cliente = servidor.accept();
                String ip = cliente.getInetAddress().getHostAddress();
                System.out.println("[Servidor] Cliente conectado: " + ip);

                // Cada cliente e tratado em sua propria thread, permitindo
                // varios clientes simultaneos.
                new Thread(() -> tratarCliente(cliente)).start();
            }
        } catch (IOException e) {
            System.err.println("[Servidor] Erro ao iniciar: " + e.getMessage());
        }
    }

    /**
     * Trata toda a conversa com UM cliente ate ele mandar SAIR ou desconectar.
     */
    private static void tratarCliente(Socket cliente) {
        try (
            // Fluxo de ENTRADA: le o que o cliente enviou (linha a linha).
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(cliente.getInputStream()));
            // Fluxo de SAIDA: envia respostas de volta. O 'true' liga o autoFlush.
            PrintWriter saida = new PrintWriter(cliente.getOutputStream(), true)
        ) {
            String linha;
            // readLine() devolve null quando o cliente fecha a conexao.
            while ((linha = entrada.readLine()) != null) {
                linha = linha.trim();

                if (linha.equalsIgnoreCase("SAIR")) {
                    saida.println("CONEXÃO FINALIZADA");
                    break;
                }

                String resposta = processar(linha);
                saida.println(resposta);
                System.out.println("[Servidor] " + linha + "  ->  " + resposta);
            }
        } catch (IOException e) {
            System.err.println("[Servidor] Conexao perdida: " + e.getMessage());
        } finally {
            try {
                cliente.close();
            } catch (IOException ignore) {
                // Nada a fazer se ja estiver fechado.
            }
            System.out.println("[Servidor] Cliente desconectado.");
        }
    }

    /**
     * Recebe uma linha "OPERACAO A B", faz o calculo e devolve a resposta
     * ja no formato do protocolo ("RESULTADO x" ou "ERRO ...").
     */
    private static String processar(String linha) {
        String[] partes = linha.split("\\s+");

        if (partes.length != 3) {
            return "ERRO Formato invalido. Use: OPERACAO A B (ex: SOMA 3 5)";
        }

        String operacao = partes[0].toUpperCase();
        double a, b;

        // Tenta converter os dois operandos em numero.
        try {
            a = Double.parseDouble(partes[1]);
            b = Double.parseDouble(partes[2]);
        } catch (NumberFormatException e) {
            return "ERRO Os operandos precisam ser numeros.";
        }

        // Escolhe a operacao.
        switch (operacao) {
            case "SOMA":
                return "RESULTADO " + (a + b);
            case "SUB":
                return "RESULTADO " + (a - b);
            case "MUL":
                return "RESULTADO " + (a * b);
            case "DIV":
                if (b == 0) {
                    return "ERRO Divisao por zero.";
                }
                return "RESULTADO " + (a / b);
            default:
                return "ERRO Operacao desconhecida: " + operacao;
        }
    }
}
