# Checkpoint 5 — Bug Hunt PetFiap

## Identificação

**Grupo:** Fiapets

| Integrante | RM | Turma |
|---|---|---|
| Guilherme Vasques Tamai | RM563276 | 2CCPG |
| Mirella Mascarenhas | RM562092 | 2CCPG |
| Caio Castelão Carminato | RM563630 | 2CCPG |
| Vitor Komura de Freitas | RM563694 | 2CCPG |
| André Ayello de Nóbrega | RM561754 | 2CCPG |

| Campo | |
|---|---|
| **Total de bugs corrigidos** | 13 / 12 (12 do contrato + 1 extra encontrado na leitura do código) |
| **Total de ajustes de Clean Code** | 6 / 6 |
| **Total de testes novos escritos** | 6 / 6 |
| **Suíte final (Run As → JUnit Test)** | 28 testes, 0 falhas |

---

## Parte 1 — Bugs encontrados

| # | Sintoma observado (o que fiz/vi) | Causa raiz (arquivo e linha aproximada) | Correção aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | `GeradorProtocoloTest` vermelho: `getInstancia()` devolvia objetos diferentes e a numeração voltava a 1 a cada chamada | `GeradorProtocolo.getInstancia()`: criava `new GeradorProtocolo()` a cada chamada e nunca guardava no atributo estático `instancia` | Passou a guardar a instância criada em `instancia` e a retornar sempre a mesma (criada só se for `null`) | Padrão Singleton (Aula 14), atributo `static` |
| bug02 | Builder montava atendimento sem nome do pet (campo ficava `null`) e `AtendimentoBuilderTest` falhava | `AtendimentoBuilder`: variável sombreada (parâmetro com o mesmo nome do atributo, sem `this.`) e `construir()` sem validação dos obrigatórios | Uso de `this.` nos setters do builder e validação em `construir()` lançando `IllegalArgumentException` para nome/porte ausentes | Padrão Builder (Aula 14), `this`/escopo de variáveis, fail-fast |
| bug03 | Duração da Tosa vinha como 30 min (a do pai) em vez de 60; o código compilava sem erro | `Tosa.getDuracaoMinutos`: a assinatura (parâmetros) era diferente da de `Atendimento.getDuracaoMinutos()`, então criava uma sobrecarga em vez de sobrescrever | Assinatura corrigida para a mesma do método da superclasse, passando a sobrescrever de fato | Sobrescrita vs sobrecarga, polimorfismo (Aula 7) |
| bug04 | `expected: <Mimi> but was: <null>` no teste `devePreencherOsDadosDoPetNaConsulta` | Construtor de `ConsultaVeterinaria` não repassava os atributos para o construtor da superclasse | Passou a chamar `super(protocolo, petNome, petPorte, tutorNome, dataHora)` | Herança e construtores, `super` |
| bug05 | Agendamento duplicado (mesmo pet, mesmo horário) passava pela verificação de conflito; `deveRecusarAgendamentoComHorarioJaOcupado` falhava | `AgendaService.agendar`: comparação de `String` e `LocalDateTime` com `==`, que compara referência e não valor | Troca de `==` por `.equals()` nas comparações de nome do pet, data/hora e status | `==` vs `.equals()` (Aula 7) |
| bug06 | Buscar id inexistente devolvia `null` em vez de falhar; `deveLancarExcecaoQuandoAtendimentoNaoExiste` vermelho | `AgendaService.buscarPorId`: um `try/catch` engolia a `AtendimentoNaoEncontradoException` e retornava `null` | Removido o `try/catch`; a busca usa `findById(id).orElseThrow(...)` e a exceção chega ao chamador | Exceções customizadas unchecked (Aula 11), não silenciar exceções |
| bug07 | Era possível cancelar um atendimento já CONCLUÍDO; `deveRecusarCancelamentoDeAtendimentoJaConcluido` falhava | `Atendimento.cancelar()`: mudava o status sem verificar o status atual | Adicionada a validação: só cancela se estiver AGENDADO, senão lança `StatusInvalidoException` | Regras de negócio no model, exceções (Aula 11) |
| bug08 | Agendar `tipo=TOSA` criava um Banho (preço e duração errados); `deveCriarTosaQuandoTipoForTosa` falhava | `AtendimentoFactory.criar`: o `case "TOSA"` instanciava `new Banho(...)` | O `case "TOSA"` passou a instanciar `new Tosa(...)` | Padrão Factory (Aula 14), polimorfismo |
| bug09 | Agendamento com data/hora no passado era aceito e salvo | Não existia nenhuma validação de data passada no fluxo de agendamento | Adicionada a validação de data passada lançando `IllegalArgumentException` (primeira versão, no controller) | Validação de entrada, exceções |
| bug10 | Banho PEQUENO custava R$ 100 e GRANDE R$ 60 (invertidos em relação ao contrato). O teste `deveCustar60ReaisParaPorteGrande` (nosso) confirmava o valor errado, então o bug passava despercebido | `Banho.calcularPreco()`: valores de PEQUENO e do porte padrão (GRANDE) trocados | Corrigido para PEQUENO 60, MÉDIO 80, GRANDE 100 e corrigido o teste para esperar 100 | Polimorfismo e regras de preço no model; testes devem seguir o contrato |
| bug11 | O `AgendaService.agendar` aceitava data passada, consultava o banco e salvava; só o controller protegia | A regra de data passada estava no `AtendimentoController`, e o contrato diz que quem recusa é o service (sem consultar o banco) | Validação movida para o início de `AgendaService.agendar`, antes do `findByPetNome`, e removida do controller | Separação de responsabilidades (camada de serviço) |
| bug12 | O Singleton dizia "thread-safe" no comentário, mas duas requisições simultâneas podiam criar duas instâncias ou receber o mesmo protocolo | `GeradorProtocolo.getInstancia()` e `proximo()` sem sincronização | Adicionado `synchronized` nos dois métodos | Singleton e concorrência (Aula 14) |
| bug13 | Ao salvar um atendimento, o JPA exigia que o id fosse atribuído manualmente (não havia geração automática) | `Atendimento`: o campo `@Id private Long id` estava sem `@GeneratedValue` | Adicionado `@GeneratedValue(strategy = GenerationType.IDENTITY)` | JPA/Hibernate, mapeamento de entidade (Aula 13) |

## Parte 2 — Ajustes de Clean Code

| # | Onde estava | Qual princípio/boas práticas era violado | O que eu mudei |
|---|---|---|---|
| clean01 | `Atendimento`: método morto `calcularDescontoFidelidade` | Código morto / KISS: método que ninguém chama e que polui o model | Removido o método da classe `Atendimento` |
| clean02 | `AtendimentoController`: o mesmo método morto `calcularDescontoFidelidade` | Código morto: regra de negócio fora do model e sem uso | Removido o método do controller |
| clean03 | `AtendimentoFactory.criar(int p, String t, String n, String po, String tu, LocalDateTime d)` | Nomes significativos: parâmetros de uma letra não revelam a intenção | Renomeados para `protocolo`, `tipo`, `petNome`, `petPorte`, `tutorNome`, `dataHora` |
| clean04 | `System.out.println` em `AgendaService.agendar` e no construtor de `GeradorProtocolo` | Log de produção com `System.out` não tem nível, formato nem controle | No service passou a usar `Logger` do SLF4J (`log.info`) e o print do construtor do singleton foi removido |
| clean05 | String literal `"AGENDADO"` repetida em `Atendimento` e `AgendaService` | Número/string mágica: duplicação e risco de erro de digitação | Criada a constante `Atendimento.STATUS_AGENDADO`, usada no model e no service |
| clean06 | `Tosa.getDuracaoMinutos()` sem `@Override` e comentários obsoletos (`CLEAN CODE 01/02` e "validação fica por conta do controller") | Falta de `@Override` (foi a causa do bug03) e comentários que mentem sobre o código | Adicionado `@Override` na Tosa e removidos os comentários desatualizados |

## Parte 3 — Testes novos (regras que estavam sem cobertura)

| # | Teste escrito (classe.método) | Regra coberta | Resultado ao escrever (vermelho/verde) |
|---|---|---|---|
| teste01 | `AgendaServiceTest.deveCancelarAtendimentoAgendado` | `cancelar()` em AGENDADO vira CANCELADO e salva | Verde de cara (regra já estava correta) |
| teste02 | `AgendaServiceTest.deveRecusarCancelamentoDeAtendimentoJaConcluido` | `cancelar()` em CONCLUIDO recusa com `StatusInvalidoException` e nada é salvo | Verde (protegia a correção do bug07) |
| teste03 | `AgendaServiceTest.deveBuscarAtendimentosPorNomeDoPet` | Busca de atendimentos por nome do pet usa o repository | Verde de cara |
| teste04 | `BanhoTest.deveCustar80ReaisParaPorteMedio` e `deveCustar100ReaisParaPorteGrande` | Preço do Banho por porte (MÉDIO 80, GRANDE 100) | Vermelho depois de corrigido o valor esperado para 100: revelou o **bug10** |
| teste05 | `TosaTest.deveCustar90ReaisParaPorteMedio` e `deveCustar120ReaisParaPorteGrande` | Preço da Tosa por porte (MÉDIO 90, GRANDE 120) | Verde de cara |
| teste06 | `AtendimentoBuilderTest.deveGarantirQueBuilderConstroiComSucessoDadosValidos` | Builder cria o atendimento quando os dados são válidos | Verde de cara |

---

## Parte 4 — Perguntas de reflexão

### 1. A suíte como contrato (Aula 15)
Rodamos a suíte e, em vez de sair lendo o código inteiro, partimos de cada mensagem de falha. O melhor exemplo foi `expected: <Mimi> but was: <null>` em `devePreencherOsDadosDoPetNaConsulta`: o `null` no nome do pet apontava direto para a criação do objeto, e não para a regra de preço, e achamos que o construtor da `ConsultaVeterinaria` não repassava os atributos para o `super` (bug04). O mesmo vale para o `assertSame` do `GeradorProtocoloTest`, que levou ao `getInstancia()` sem guardar a instância (bug01). Com `curl` teríamos de subir a API, ter o Oracle no ar e conferir o resultado manualmente. A suíte roda em segundos, sem banco, e repete a mesma verificação toda vez. Ela também avisa quando uma correção quebra outra coisa.

### 2. Mock e injeção de dependência (Aulas 13 a 15)
No `AgendaService`, o campo `repository` está marcado com `@Autowired`. Em produção, o container do Spring cria um `AtendimentoRepository` (a implementação do Spring Data JPA ligada ao Oracle) e injeta no service. No `AgendaServiceTest`, o `@Mock` cria um repository falso e o `@InjectMocks`, junto com o `MockitoExtension`, injeta esse falso no campo do service. Ou seja, em produção quem injeta é o Spring e no teste é o Mockito. O service só conhece o tipo da interface `AtendimentoRepository`, não sabe qual implementação recebeu. Por isso o teste roda sem banco e sem subir o Spring: o `when(repository.findByPetNome("Rex")).thenReturn(...)` define o que o falso devolve, e o `verify(repository, never()).save(any())` confere se o service evitou salvar quando recusou o agendamento.

### 3. `==` vs `.equals()` (Aula 7)
No `AgendaService.agendar`, a verificação de conflito comparava `LocalDateTime` e `String` com `==`, que compara a referência na memória e não o conteúdo. No teste `deveRecusarAgendamentoComHorarioJaOcupado`, a segunda data é criada com `LocalDateTime.parse(...)`, ou seja, é outro objeto com o mesmo valor, e por isso o `==` dava `false` e o agendamento duplicado passava. Com `String` o `==` "funciona por sorte" em literais como `"Rex"`: o Java guarda literais iguais no mesmo lugar (string pool), então as duas referências coincidem. Se o nome viesse de um `@RequestParam` (criado em tempo de execução), o `==` falharia. Trocamos tudo por `.equals()`, que compara o valor. Também mantivemos o padrão `"AGENDADO".equals(status)`, que evita `NullPointerException`.

### 4. Sobrescrita vs sobrecarga (Aula 7)
Na `Tosa`, o método de duração tinha parâmetros diferentes de `getDuracaoMinutos()` da classe `Atendimento`. Em Java, isso não é sobrescrita (override), é sobrecarga (overload): um método novo, com o mesmo nome e outra assinatura, que convive com o original. Compilava sem erro e o `Atendimento.getDuracaoMinutos()` continuava valendo, então a Tosa devolvia 30 minutos em vez de 60, e o polimorfismo não acontecia. A anotação `@Override` obriga o compilador a conferir que existe um método igual na superclasse; se a assinatura estiver diferente, dá erro de compilação na hora. No `Banho` a anotação estava presente, na `Tosa` não. Por isso adicionamos `@Override` na Tosa (clean06).

### 5. Singleton manual vs bean do Spring (Aula 14)
O `GeradorProtocolo` garante, com construtor `private` e o atributo estático `instancia`, que exista um único objeto e, portanto, uma única numeração global. O bug01 era que o `getInstancia()` fazia `new GeradorProtocolo()` sem guardar em `instancia`: cada chamada criava um gerador novo e o protocolo voltava sempre a 1. Depois ainda corrigimos o bug12, porque o `getInstancia()` e o `proximo()` não eram `synchronized`, e duas requisições simultâneas poderiam repetir um número. O `AgendaService`, anotado com `@Service`, não corre esse risco de criação: o container do Spring cria um único bean por padrão e injeta sempre a mesma instância com `@Autowired`, sem `static` nem `getInstancia()` escrito à mão. O cuidado que continua valendo é manter o service sem estado mutável, já que a instância é compartilhada entre requisições.

### 6. Cobertura de testes: onde parar? (Aula 15)
Vale manter os testes que já nasceram verdes: eles protegem contra regressão, e o `TosaTest` com os preços de MÉDIO e GRANDE é um exemplo, pois se alguém trocar os valores do `calcularPreco()` ele vermelha na hora. Aprendemos também que verde não significa correto: o `BanhoTest.deveCustar60ReaisParaPorteGrande` passava, mas confirmava o valor errado do contrato (GRANDE custa R$ 100) e escondeu o bug10 até relermos a tabela. Em um projeto real com prazo, priorizaríamos o caminho feliz de cada regra e os caminhos de erro de maior risco, como recusar o agendamento no mesmo horário e recusar concluir ou cancelar fora do status AGENDADO, porque são os casos que geram prejuízo. Perseguir 100% de cobertura tem retorno baixo: getters e setters simples quase não protegem nada.

---

## Parte 5 — Espaço livre (opcional)

```
Um dos testes que escrevemos ficou verde confirmando um valor errado (Banho GRANDE = 60);
só percebemos ao comparar com a tabela de preços do enunciado. A lição foi que o teste
precisa nascer do contrato, e não do que o código atual devolve.
```
