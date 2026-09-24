# Calculadora Distribuída — Versão 1: Sockets (TCP)

Primeira das três versões da atividade (Sockets, RPC/gRPC e RMI). Esta versão
implementa uma calculadora cliente-servidor usando **sockets TCP** puros em Java.

## Arquitetura

```
   Cliente                                  Servidor
 ┌──────────┐   "SOMA 3 5"   (TCP)   ┌──────────────────┐
 │ teclado  │ ─────────────────────► │  ServerSocket     │
 │ Scanner  │                        │  accept()         │
 │  Socket  │ ◄───────────────────── │  1 thread/cliente │
 └──────────┘   "RESULTADO 8.0"      └──────────────────┘
```

- **Transporte:** TCP (classe `Socket` / `ServerSocket`), que garante entrega
  ordenada e confiável — atende ao requisito de confiabilidade do enunciado.
- **Concorrência:** o servidor abre **uma thread por cliente**, então vários
  clientes podem operar ao mesmo tempo.
- **Protocolo:** definido **na mão**, em texto, uma linha por mensagem.

### Protocolo da aplicação

| Cliente envia      | Servidor responde                     |
|--------------------|---------------------------------------|
| `SOMA 3 5`         | `RESULTADO 8.0`                       |
| `SUB 10 4`         | `RESULTADO 6.0`                       |
| `MUL 2 6`          | `RESULTADO 12.0`                      |
| `DIV 8 2`          | `RESULTADO 4.0`                       |
| `DIV 5 0`          | `ERRO Divisao por zero.`              |
| (formato errado)   | `ERRO Formato invalido. ...`          |
| `SAIR`             | `TCHAU` (e encerra a conexão)         |

## Como compilar

Requisito: **JDK 17+** instalado (`javac -version`).

```bash
cd src
javac ServidorCalculadora.java ClienteCalculadora.java
```

## Como executar

Abra **dois terminais** (o servidor precisa estar de pé antes do cliente).

**Terminal 1 — servidor:**
```bash
cd src
java ServidorCalculadora
```

**Terminal 2 — cliente:**
```bash
cd src
java ClienteCalculadora
```

Depois é só digitar operações no cliente:
```
> SOMA 3 5
RESULTADO 8.0
> DIV 8 2
RESULTADO 4.0
> SAIR
TCHAU
```

## Como testar em portas/máquinas diferentes

- **Portas distintas na mesma máquina:** já é o padrão — servidor e cliente
  rodam em processos separados falando pela porta `5000` no `localhost`.
- **Máquinas diferentes:** rode o servidor numa máquina, descubra o IP dela
  (`ipconfig` no Windows, `ip addr` no Linux) e no cliente passe host e porta
  por argumento:
  ```bash
  java ClienteCalculadora 192.168.0.10 5000
  ```
  (Pode ser preciso liberar a porta 5000 no firewall da máquina servidora.)

Para trocar a porta do servidor, altere a constante `PORTA` em
`ServidorCalculadora.java`.

## Pontos para o relatório (comparação)

- **Nível de abstração: o mais baixo dos três.** Você é responsável por abrir a
  conexão, definir o formato das mensagens, serializar/desserializar os dados
  (aqui, `split` de string e `Double.parseDouble`) e tratar erros de rede.
- **Vantagem:** controle total e zero dependências externas — só a biblioteca
  padrão do Java. É o mais leve e o mais portável entre linguagens.
- **Desvantagem:** todo o "encanamento" é manual. Adicionar uma operação nova
  significa mexer no protocolo de texto nas duas pontas. Não há verificação de
  tipos entre cliente e servidor — se o formato divergir, quebra em tempo de
  execução.
- **Caso de uso prático:** protocolos de baixo nível, alta performance ou quando
  você precisa falar com sistemas heterogêneos e quer controle fino sobre os
  bytes que trafegam.

## Arquivos

- `src/ServidorCalculadora.java` — servidor TCP multithread.
- `src/ClienteCalculadora.java` — cliente interativo de linha de comando.
