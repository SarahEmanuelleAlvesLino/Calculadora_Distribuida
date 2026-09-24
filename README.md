# Calculadora Distribuída — Sockets, RPC e RMI

Atividade de Sistemas Distribuídos: uma mesma aplicação (calculadora
cliente-servidor) implementada em **três versões**, para comparar as abordagens.

| Versão   | Tecnologia            | Status        |
|----------|-----------------------|---------------|
| 1        | Sockets (TCP)         | ✅ pronta     |
| 2        | RPC (gRPC em Java)    | ⬜ a fazer    |
| 3        | RMI (Java RMI)        | ⬜ a fazer    |

A aplicação é a mesma nas três: o cliente pede uma operação (`soma`, `sub`,
`mul`, `div`) com dois operandos e o servidor devolve o resultado. O que muda é
**como** cliente e servidor conversam — e é isso que o relatório compara.

## Estrutura

```
calculadora-distribuida/
├── socket/          # Versão 1 — sockets TCP puros
│   ├── src/
│   │   ├── ServidorCalculadora.java
│   │   └── ClienteCalculadora.java
│   └── README.md    # como compilar, rodar e testar
├── rpc/             # (em breve)
├── rmi/             # (em breve)
└── relatorio/       # relatório comparativo final
```

Cada pasta tem seu próprio README com instruções. Comece pela
[versão Socket](socket/README.md).

## Eixo da comparação (para o relatório)

As três versões formam uma escada de abstração crescente:

1. **Socket** — nível mais baixo: você define o protocolo e serializa tudo na mão.
2. **RPC (gRPC)** — você define um *contrato* (`.proto`) e a ferramenta gera o
   código de comunicação; você chama funções remotas.
3. **RMI** — nível mais alto: você invoca **métodos de objetos remotos** quase
   como se fossem locais.
