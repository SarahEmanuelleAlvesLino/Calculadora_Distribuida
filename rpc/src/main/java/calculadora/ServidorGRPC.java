package calculadora;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * Servidor — versão gRPC (RPC).
 *
 * O servidor sobe na porta 50051 e registra a implementação do serviço
 * Calculadora. Toda a parte de rede (HTTP/2, serialização binária das
 * mensagens, threads) é cuidada pelo gRPC — nós só implementamos a lógica.
 *
 * As classes CalculadoraGrpc, OperacaoRequest e OperacaoResponse NÃO estão
 * neste projeto escritas à mão: elas são GERADAS a partir de
 * src/main/proto/calculadora.proto quando você roda "mvn compile".
 */
public class ServidorGRPC {

    private static final int PORTA = 50051;

    public static void main(String[] args) throws Exception {
        Server servidor = ServerBuilder.forPort(PORTA)
                .addService(new CalculadoraServiceImpl())
                .build()
                .start();

        System.out.println("[Servidor] gRPC no ar na porta " + PORTA + ". Aguardando chamadas...");

        // Mantém a JVM viva enquanto o servidor estiver rodando (Ctrl+C encerra).
        servidor.awaitTermination();
    }

    /**
     * Implementação do serviço. Estende a classe-base GERADA a partir do .proto
     * (CalculadoraGrpc.CalculadoraImplBase). Cada método remoto recebe a
     * requisição e usa um StreamObserver para devolver a resposta.
     */
    static class CalculadoraServiceImpl extends CalculadoraGrpc.CalculadoraImplBase {

        @Override
        public void soma(OperacaoRequest req, StreamObserver<OperacaoResponse> resp) {
            System.out.println("[Servidor] soma(" + req.getA() + ", " + req.getB() + ")");
            responder(resp, req.getA() + req.getB());
        }

        @Override
        public void sub(OperacaoRequest req, StreamObserver<OperacaoResponse> resp) {
            System.out.println("[Servidor] sub(" + req.getA() + ", " + req.getB() + ")");
            responder(resp, req.getA() - req.getB());
        }

        @Override
        public void mul(OperacaoRequest req, StreamObserver<OperacaoResponse> resp) {
            System.out.println("[Servidor] mul(" + req.getA() + ", " + req.getB() + ")");
            responder(resp, req.getA() * req.getB());
        }

        @Override
        public void div(OperacaoRequest req, StreamObserver<OperacaoResponse> resp) {
            System.out.println("[Servidor] div(" + req.getA() + ", " + req.getB() + ")");
            if (req.getB() == 0) {
                // O gRPC tem um canal de ERRO estruturado (Status). Não precisamos
                // inventar um código de erro em texto como no socket.
                resp.onError(Status.INVALID_ARGUMENT
                        .withDescription("Divisao por zero.")
                        .asRuntimeException());
                return;
            }
            responder(resp, req.getA() / req.getB());
        }

        // Empacota o resultado numa OperacaoResponse e o envia ao cliente.
        private void responder(StreamObserver<OperacaoResponse> resp, double resultado) {
            OperacaoResponse resposta = OperacaoResponse.newBuilder()
                    .setResultado(resultado)
                    .build();
            resp.onNext(resposta);   // envia a resposta
            resp.onCompleted();      // encerra a chamada
        }
    }
}
