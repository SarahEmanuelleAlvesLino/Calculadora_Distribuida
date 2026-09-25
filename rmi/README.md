# Calculadora Distribuída — Versão 3: RMI (Java Remote Method Invocation)

Terceira versão da atividade. A mesma calculadora cliente-servidor, agora com
**RMI**: o cliente invoca **métodos de um objeto remoto** quase como se ele
fosse local. É o maior nível de abstração das três versões.

## Arquitetura

```
   Cliente                         RMI Registry                  Servidor
 ┌──────────┐   1. lookup("CalculadoraRMI")   ┌───────────┐
 │ ClienteRMI│ ───────────────────────────────►│  registro │
 │           │ ◄─── referência (stub) ─────────│ porta 1099│
 │           │                                 └───────────┘
 │           │   2. calc.soma(3, 5)                          ┌──────────────┐
 │  (stub)   │ ────────────────────────────────────────────►│ CalculadoraImpl│
 │           │ ◄─── retorno 8.0 ────────────────────────────│  (objeto real) │
 └──────────┘                                                └──────────────┘
```

1. O servidor cria o objeto remoto e o **registra** por um nome no RMI Registry.
2. O cliente faz **lookup** desse nome e recebe uma referência (o *stub*).
3. Chamar `calc.soma(3, 5)` no stub dispara a chamada pela rede; o RMI empacota
   os parâmetros, executa o método no objeto real e devolve o retorno.

## Arquivos

- `src/Calculadora.java` — **interface remota** (o contrato). Estende `Remote`;
  todo método declara `throws RemoteException`.
- `src/CalculadoraImpl.java` — **implementação** do objeto remoto (a lógica).
  Estende `UnicastRemoteObject`, que exporta o objeto.
- `src/ServidorRMI.java` — sobe o Registry na porta 1099 e registra o objeto.
- `src/ClienteRMI.java` — faz lookup e invoca os métodos remotos.

## Como compilar

Requisito: **JDK 17+**. A partir do Java 9 **não é preciso gerar stubs** com
`rmic` — eles são criados dinamicamente.

```bash
cd src
javac Calculadora.java CalculadoraImpl.java ServidorRMI.java ClienteRMI.java
```

## Como executar

Dois terminais (servidor primeiro).

**Terminal 1 — servidor:**
```bash
cd src
java ServidorRMI
```
Ele imprime "Servidor RMI no ar. Aguardando chamadas..." e **continua rodando**
(a JVM fica viva por causa do objeto exportado). Pare com `Ctrl+C`.

**Terminal 2 — cliente:**
```bash
cd src
java ClienteRMI
```
```
> SOMA 3 5
RESULTADO 8.0
> DIV 5 0
ERRO Divisao por zero.
> SAIR
TCHAU
```

## Como testar em máquinas diferentes

1. O cliente precisa ter a interface `Calculadora.class` no seu classpath
   (copie-a junto), pois ele usa esse contrato para conversar com o objeto.
2. Rode o cliente apontando para o IP do servidor:
   ```bash
   java ClienteRMI 192.168.0.10 1099
   ```
3. Se o servidor "anunciar" a si mesmo com um endereço errado (erro comum de
   RMI em rede), suba-o fixando o IP dele:
   ```bash
   java -Djava.rmi.server.hostname=192.168.0.10 ServidorRMI
   ```
   (E libere a porta 1099 no firewall da máquina servidora.)

## Pontos para o relatório (comparação)

- **Nível de abstração: o mais alto dos três.** Você chama `calc.soma(a, b)` —
  uma chamada de método comum. Não há protocolo de texto, nem parsing, nem
  serialização manual: o RMI cuida de tudo.
- **Contrato verificado em compilação.** A interface `Calculadora` é conhecida
  pelas duas pontas; se você errar a assinatura de um método, o compilador
  acusa — ao contrário do socket, onde um `"SOMA"` digitado errado só quebraria
  em tempo de execução.
- **Exceções viajam de graça.** A divisão por zero é um `throw` no servidor que
  chega como exceção no cliente, sem nenhum código de erro combinado.
- **Vantagem:** produtividade e código limpo; ótimo quando cliente e servidor
  são ambos em Java.
- **Desvantagem:** é **amarrado ao Java** (as duas pontas precisam ser Java e
  compartilhar a interface). Em rede real, a configuração de host/porta/registry
  costuma dar mais dor de cabeça que um socket simples.
- **Caso de uso prático:** sistemas distribuídos 100% Java, como serviços
  internos corporativos onde a produtividade e a integração com o ecossistema
  Java compensam a falta de interoperabilidade entre linguagens.

## Comparando com o socket (resumo rápido)

| Aspecto              | Socket                          | RMI                                  |
|----------------------|---------------------------------|--------------------------------------|
| Unidade de troca     | mensagem de texto `"SOMA 3 5"`  | chamada de método `calc.soma(3,5)`   |
| Quem serializa       | você (na mão)                   | o RMI (automático)                   |
| Contrato             | combinado informalmente         | interface Java (checada no compilador)|
| Erros                | você inventa códigos/strings    | exceções Java propagadas             |
| Interoperabilidade   | qualquer linguagem              | só Java                              |
| Abstração            | baixa                           | alta                                 |
