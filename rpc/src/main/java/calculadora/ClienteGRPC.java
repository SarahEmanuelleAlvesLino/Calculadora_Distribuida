package calculadora;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.Scanner;

/**
 * Cliente — versão gRPC (RPC).
 *
 * O cliente abre um "canal" com o servidor e usa um "stub" (objeto gerado) para
 * chamar os métodos remotos. Diferente do socket, não montamos mensagens de
 * texto na mão; diferente do RMI, o contrato é neutro de linguagem (.proto).
 *
 * Mantemos a MESMA UX das outras versões (você digita "SOMA 3 5") só para a
 * comparação ficar justa — mas por baixo, cada comando vira uma chamada de
 * procedimento remoto: stub.soma(req).
 */
public class ClienteGRPC {

    public static void main(String[] args) {
        // Permite rodar apontando para outra máquina/porta:
        //   mvn exec:java -Dexec.mainClass=calculadora.ClienteGRPC -Dexec.args="192.168.0.10 50051"
        String host = (args.length >= 1) ? args[0] : "localhost";
        int porta = (args.length >= 2) ? Integer.parseInt(args[1]) : 50051;

        // O "canal" é a conexão com o servidor. usePlaintext() = sem TLS
        // (ok para teste local; em produção usaria criptografia).
        ManagedChannel canal = ManagedChannelBuilder.forAddress(host, porta)
                .usePlaintext()
                .build();

        // O "stub" é o objeto GERADO pelo qual chamamos os métodos remotos.
        // BlockingStub = a chamada espera a resposta (síncrona), como socket/RMI.
        CalculadoraGrpc.CalculadoraBlockingStub stub = CalculadoraGrpc.newBlockingStub(canal);

        System.out.println("[Cliente] Conectado ao servidor gRPC em " + host + ":" + porta + ".");
        System.out.println("[Cliente] Digite operacoes (ex: SOMA 3 5). Digite SAIR para encerrar.");

        try (Scanner teclado = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String comando = teclado.nextLine().trim();

                if (comando.isEmpty()) {
                    continue;
                }
                if (comando.equalsIgnoreCase("SAIR")) {
                    System.out.println("Conexao finalizada.");
                    break;
                }

                System.out.println(processar(stub, comando));
            }
        } finally {
            // Fecha o canal de forma ordenada.
            canal.shutdown();
        }

        System.out.println("[Cliente] Encerrado.");
    }

    /**
     * Interpreta "OPERACAO A B", monta a requisição e chama o método remoto.
     * Mantém o mesmo formato de saída das versões socket e RMI.
     */
    private static String processar(CalculadoraGrpc.CalculadoraBlockingStub stub, String comando) {
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

        // Monta a mensagem de requisição (classe gerada a partir do .proto).
        OperacaoRequest req = OperacaoRequest.newBuilder().setA(a).setB(b).build();

        try {
            OperacaoResponse resp;
            switch (p[0].toUpperCase()) {
                case "SOMA": resp = stub.soma(req); break;
                case "SUB":  resp = stub.sub(req); break;
                case "MUL":  resp = stub.mul(req); break;
                case "DIV":  resp = stub.div(req); break;
                default:     return "ERRO Operacao desconhecida: " + p[0];
            }
            return "RESULTADO " + resp.getResultado();
        } catch (StatusRuntimeException e) {
            // Erros vindos do servidor (ex.: divisão por zero) chegam como Status.
            return "ERRO " + e.getStatus().getDescription();
        }
    }
}
