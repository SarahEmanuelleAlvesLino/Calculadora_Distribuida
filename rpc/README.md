# Calculadora Distribuída — Versão 2: RPC com gRPC

Segunda abordagem da atividade (RPC). A mesma calculadora, agora com **gRPC**:
o contrato dos serviços é escrito em um arquivo `.proto` e o build **gera
automaticamente** o código de comunicação (mensagens + stubs). O cliente chama
um "procedimento remoto" (`stub.soma(req)`) e o gRPC cuida do transporte.

> Observação de transparência: diferente das versões socket e RMI, esta não pôde
> ser compilada/testada no ambiente onde foi gerada (sem acesso ao repositório
> Maven). A sintaxe do `.proto` foi validada com o `protoc` real, e os nomes dos
> métodos gerados conferidos. O primeiro `mvn compile` na sua máquina baixa as
> dependências e gera o restante.

## Arquitetura

```
   Cliente                                             Servidor
 ┌────────────┐   stub.soma(OperacaoRequest{a,b})   ┌──────────────────────┐
 │ ClienteGRPC│ ──────── HTTP/2 + Protobuf ───────► │ CalculadoraServiceImpl│
 │  (stub      │                                    │  (estende a base      │
 │  gerado)    │ ◄──── OperacaoResponse{resultado} ─│   gerada do .proto)   │
 └────────────┘                                     └──────────────────────┘
        ▲                                                     ▲
        └──── ambos usam o código GERADO a partir de ────────┘
                     src/main/proto/calculadora.proto
```

- **Contrato:** `calculadora.proto` define o serviço `Calculadora` e as
  mensagens `OperacaoRequest` / `OperacaoResponse`.
- **Transporte:** HTTP/2, com as mensagens serializadas em **Protobuf** (binário
  compacto) — tudo automático.
- **Código gerado:** `CalculadoraGrpc`, `OperacaoRequest`, `OperacaoResponse`
  nascem do `.proto` em `mvn compile` (ficam em `target/generated-sources/`).

## Arquivos deste módulo

- `src/main/proto/calculadora.proto` — o **contrato** (o que você escreve à mão).
- `src/main/java/calculadora/ServidorGRPC.java` — sobe o servidor e implementa a lógica.
- `src/main/java/calculadora/ClienteGRPC.java` — abre o canal e chama os métodos remotos.
- `pom.xml` — dependências do gRPC e o plugin que gera o código do `.proto`.

## Pré-requisitos

- **JDK 17+** (`java -version`).
- **Maven** (`mvn -version`). Se não tiver, no Ubuntu/WSL:
  `sudo apt update && sudo apt install maven`
- **Internet no primeiro build** (o Maven baixa o gRPC, o Protobuf e o `protoc`).

## Como compilar

Na pasta `rpc/`:

```bash
mvn compile
```

Isso baixa as dependências, gera o código a partir do `.proto` e compila tudo.
(É normal o primeiro build demorar um pouco.)

## Como executar

Dois terminais, na pasta `rpc/` (servidor primeiro):

**Terminal 1 — servidor:**
```bash
mvn exec:java -Dexec.mainClass=calculadora.ServidorGRPC
```
Ele imprime "gRPC no ar na porta 50051. Aguardando chamadas..." e fica de pé
(pare com `Ctrl+C`).

**Terminal 2 — cliente:**
```bash
mvn exec:java -Dexec.mainClass=calculadora.ClienteGRPC
```
```
> SOMA 3 5
RESULTADO 8.0
> DIV 5 0
ERRO Divisao por zero.
> SAIR
Conexao finalizada.
```

## Como testar em máquinas diferentes

O cliente aceita host e porta como argumentos:
```bash
mvn exec:java -Dexec.mainClass=calculadora.ClienteGRPC -Dexec.args="192.168.0.10 50051"
```
(Libere a porta 50051 no firewall da máquina servidora.)

## Problemas comuns

- **`mvn: command not found`** → instale o Maven (ver Pré-requisitos).
- **Primeiro build falha por rede** → precisa de internet para baixar as libs e
  o `protoc`; refaça `mvn compile` com conexão.
- **`cannot find symbol: class CalculadoraGrpc`** no editor → rode `mvn compile`
  uma vez; o código gerado aparece em `target/generated-sources/` e o VS Code
  passa a reconhecê-lo.

## Pontos para o relatório (comparação)

- **Nível de abstração: médio-alto.** Mais alto que o socket (você não mexe em
  bytes nem em protocolo de texto), comparável ao RMI em conforto de uso, mas
  com uma diferença-chave: o contrato é **neutro de linguagem**.
- **Contrato explícito e versionável.** O `.proto` é a fonte da verdade; o mesmo
  arquivo geraria cliente/servidor em Python, Go, C++, etc. → **interoperável
  entre linguagens** (algo que o RMI não oferece).
- **Serialização eficiente.** Protobuf é binário e compacto, sobre HTTP/2.
- **Erros estruturados.** A divisão por zero vira um `Status.INVALID_ARGUMENT`,
  um canal de erro padronizado — não um código de erro inventado em texto.
- **Custo:** exige toolchain (Maven/Gradle + `protoc` + plugins) e uma etapa de
  geração de código. É o setup mais pesado dos três.
- **Caso de uso prático:** microsserviços e APIs de alta performance entre
  serviços escritos em linguagens diferentes — o cenário típico de uso do gRPC
  na indústria.

## Comparação final das três versões

| Aspecto             | Socket                    | gRPC (RPC)                       | RMI                              |
|---------------------|---------------------------|----------------------------------|----------------------------------|
| Unidade de troca    | texto `"SOMA 3 5"`        | chamada `stub.soma(req)`         | método `calc.soma(3,5)`          |
| Contrato            | informal (combinado)      | arquivo `.proto` (gerado)        | interface Java                   |
| Quem serializa      | você (na mão)             | gRPC (Protobuf, binário)         | RMI (serialização Java)          |
| Transporte          | TCP puro                  | HTTP/2                           | protocolo próprio do RMI (JRMP)  |
| Erros               | strings inventadas        | `Status` padronizado             | exceções Java                    |
| Interoperabilidade  | qualquer linguagem        | várias linguagens (via `.proto`) | só Java                          |
| Setup               | nenhum (só o JDK)         | pesado (Maven + protoc)          | leve (só o JDK)                  |
| Abstração           | baixa                     | média-alta                       | alta                             |

