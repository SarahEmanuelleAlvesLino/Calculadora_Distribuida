# Calculadora Distribuída — Sockets, RPC e RMI

Atividade de Sistemas Distribuídos: uma mesma aplicação (calculadora
cliente-servidor) implementada em **três versões**, para comparar as abordagens.

| Versão   | Tecnologia            | Porta   | Status        |
|----------|-----------------------|---------|---------------|
| 1        | Sockets (TCP)         | 5000    | ✅ pronta     |
| 2        | RPC (gRPC em Java)    | 50051   | ✅ pronta     |
| 3        | RMI (Java RMI)        | 1099    | ✅ pronta     |

A aplicação é a mesma nas três: o cliente pede uma operação (`SOMA`, `SUB`,
`MUL`, `DIV`) com dois operandos e o servidor devolve o resultado. O que muda é
**como** cliente e servidor conversam — e é isso que o relatório compara.

## Estrutura

```
calculadora-distribuida/
├── socket/          # Versão 1 — sockets TCP puros
│   ├── src/
│   │   ├── ServidorCalculadora.java
│   │   └── ClienteCalculadora.java
│   └── README.md
├── rpc/             # Versão 2 — gRPC (Maven)
│   ├── pom.xml
│   ├── src/main/proto/calculadora.proto      # o contrato
│   ├── src/main/java/calculadora/ServidorGRPC.java
│   ├── src/main/java/calculadora/ClienteGRPC.java
│   └── README.md
├── rmi/             # Versão 3 — Java RMI
│   ├── src/
│   │   ├── Calculadora.java       # interface remota
│   │   ├── CalculadoraImpl.java   # objeto remoto
│   │   ├── ServidorRMI.java       # registra o objeto no Registry
│   │   └── ClienteRMI.java        # faz lookup e invoca métodos
│   └── README.md
└── relatorio/       # relatório comparativo final
    └── RELATORIO.md
```

Cada pasta tem seu próprio README com detalhes. O
[relatório comparativo](relatorio/RELATORIO.md) reúne a análise das três versões.

## Pré-requisitos

- **JDK 17+** (`java -version` e `javac -version`) — para as três versões.
- **Maven** (`mvn -version`) — apenas para a versão gRPC. No Ubuntu/WSL:
  `sudo apt update && sudo apt install maven`
- Conexão com a internet no **primeiro** build da gRPC (baixa gRPC, Protobuf e o `protoc`).

## Como executar

Em todas as versões o modelo é o mesmo: o **servidor** fica rodando em um
terminal e o **cliente** roda em **outro terminal**. Suba sempre o servidor
primeiro. No cliente, digite operações no formato `OPERACAO A B` (ex.: `SOMA 3 5`)
e `SAIR` para encerrar.

### Versão 1 — Socket

```bash
cd socket/src
javac ServidorCalculadora.java ClienteCalculadora.java   # compilar (uma vez)
java ServidorCalculadora                                 # terminal 1 (servidor)
java ClienteCalculadora                                  # terminal 2 (cliente)
```

### Versão 2 — gRPC (Maven)

```bash
cd rpc
mvn compile                                              # compila e gera código do .proto (uma vez)
mvn exec:java -Dexec.mainClass=calculadora.ServidorGRPC  # terminal 1 (servidor)
mvn exec:java -Dexec.mainClass=calculadora.ClienteGRPC   # terminal 2 (cliente)
```

### Versão 3 — RMI

```bash
cd rmi/src
javac Calculadora.java CalculadoraImpl.java ServidorRMI.java ClienteRMI.java   # compilar (uma vez)
java ServidorRMI                                         # terminal 1 (servidor)
java ClienteRMI                                          # terminal 2 (cliente)
```

Para parar qualquer servidor, use `Ctrl+C` no terminal dele.

### Testar em máquinas diferentes

O cliente das três versões aceita **host** e **porta** por argumento, apontando
para o IP da máquina servidora. Exemplos:

```bash
java ClienteCalculadora 192.168.0.10 5000                                  # socket
java ClienteRMI 192.168.0.10 1099                                          # rmi
mvn exec:java -Dexec.mainClass=calculadora.ClienteGRPC -Dexec.args="192.168.0.10 50051"   # grpc
```

(Libere a porta correspondente no firewall da máquina servidora.)

## Eixo da comparação (para o relatório)

As três versões formam uma escada de abstração crescente:

1. **Socket** — nível mais baixo: você define o protocolo e serializa tudo na mão
   (`"SOMA 3 5"`).
2. **RPC (gRPC)** — você define um *contrato* (`.proto`) e a ferramenta gera o
   código de comunicação; você chama procedimentos remotos (`stub.soma(req)`).
3. **RMI** — nível mais alto: você invoca **métodos de objetos remotos** quase
   como se fossem locais (`calc.soma(3, 5)`).
