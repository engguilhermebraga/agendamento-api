package com.guilhermebraga.agendamento_api;

import com.guilhermebraga.agendamento_api.entity.Agendamento;
import com.guilhermebraga.agendamento_api.entity.Cliente;
import com.guilhermebraga.agendamento_api.entity.Profissional;
import com.guilhermebraga.agendamento_api.entity.Servico;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import com.guilhermebraga.agendamento_api.repository.AgendamentoRepository;
import com.guilhermebraga.agendamento_api.repository.ClienteRepository;
import com.guilhermebraga.agendamento_api.repository.ProfissionalRepository;
import com.guilhermebraga.agendamento_api.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes da camada de Repository do Agendamento.
 *
 * Foco: validar a query {@code findConflitosHorario} contra um banco H2
 * real (não mock), exercitando todos os cenários de sobreposição de
 * horários relevantes para a regra de negócio.
 *
 * Estratégia: {@code @SpringBootTest + @Transactional} — cada teste roda
 * em sua própria transação que é revertida ao final, garantindo isolamento
 * sem necessidade de limpeza manual.
 *
 * @author Guilherme Braga
 */
@SpringBootTest
@Transactional
@DisplayName("AgendamentoRepository — testes da camada de persistência")
class AgendamentoRepositoryTest {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    private Cliente cliente;
    private Profissional profissional;
    private Servico servico;
    private LocalDateTime amanhaAs10h;
    private LocalDateTime amanhaAs11h;

    private static final List<StatusAgendamento> STATUS_ATIVOS =
            List.of(StatusAgendamento.AGENDADO, StatusAgendamento.CONFIRMADO);

    @BeforeEach
    void setUp() {
        cliente = clienteRepository.save(Cliente.builder()
                .nome("Cliente Teste")
                .email("cliente@teste.com")
                .telefone("98988887777")
                .cpf("12345678900")
                .build());

        profissional = profissionalRepository.save(Profissional.builder()
                .nome("Profissional Teste")
                .especialidade("Fisioterapia")
                .email("profi@teste.com")
                .telefone("98999876543")
                .build());

        servico = servicoRepository.save(Servico.builder()
                .nome("Massagem 60min")
                .descricao("Sessão de 60 minutos")
                .duracaoMinutos(60)
                .preco(new BigDecimal("120.00"))
                .build());

        amanhaAs10h = LocalDateTime.now()
                .plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
        amanhaAs11h = amanhaAs10h.plusHours(1);
    }

    // ================================================================
    // findConflitosHorario — 5 cenários de sobreposição de horários
    // ================================================================

    @Nested
    @DisplayName("findConflitosHorario() — cenários de overlap de horários")
    class FindConflitosHorarioTests {

        @Test
        @DisplayName("Cenário 1 — mesmo horário exato deve detectar conflito")
        void cenario1_mesmoHorarioExatoDeveDetectarConflito() {
            // Existente: 10:00 - 11:00
            persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);

            // Novo: 10:00 - 11:00 (mesmos horários)
            List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                    profissional.getId(), amanhaAs10h, amanhaAs11h, STATUS_ATIVOS);

            assertThat(conflitos)
                    .as("agendamento no mesmo horário exato deve conflitar")
                    .hasSize(1);
        }

        @Test
        @DisplayName("Cenário 2 — sobreposição parcial no INÍCIO do novo deve conflitar")
        void cenario2_sobreposicaoNoInicioDoNovoDeveConflitar() {
            // Existente: 10:00 - 11:00
            persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);

            // Novo: 10:30 - 11:30 → começa durante o existente
            List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                    profissional.getId(),
                    amanhaAs10h.plusMinutes(30),
                    amanhaAs11h.plusMinutes(30),
                    STATUS_ATIVOS);

            assertThat(conflitos)
                    .as("novo começando durante existente deve conflitar")
                    .hasSize(1);
        }

        @Test
        @DisplayName("Cenário 3 — sobreposição parcial no FIM do novo deve conflitar")
        void cenario3_sobreposicaoNoFimDoNovoDeveConflitar() {
            // Existente: 10:00 - 11:00
            persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);

            // Novo: 09:30 - 10:30 → termina durante o existente
            List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                    profissional.getId(),
                    amanhaAs10h.minusMinutes(30),
                    amanhaAs10h.plusMinutes(30),
                    STATUS_ATIVOS);

            assertThat(conflitos)
                    .as("novo terminando durante existente deve conflitar")
                    .hasSize(1);
        }

        @Test
        @DisplayName("Cenário 4 — novo agendamento que ENGLOBA o existente deve conflitar")
        void cenario4_novoEnglobandoExistenteDeveConflitar() {
            // Existente: 10:00 - 11:00
            persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);

            // Novo: 09:00 - 12:00 → engloba completamente o existente
            List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                    profissional.getId(),
                    amanhaAs10h.minusHours(1),
                    amanhaAs11h.plusHours(1),
                    STATUS_ATIVOS);

            assertThat(conflitos)
                    .as("novo englobando existente deve conflitar")
                    .hasSize(1);
        }

        @Test
        @DisplayName("Cenário 5 — horário ADJACENTE (encosta no fim) NÃO deve conflitar")
        void cenario5_horarioAdjacenteNaoDeveConflitar() {
            // Existente: 10:00 - 11:00
            persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);

            // Novo: 11:00 - 12:00 → começa exatamente quando existente termina
            // Como query usa `dataHora < dataHoraFim` e `dataHoraFim > dataHoraInicio`,
            // intervalos adjacentes NÃO se sobrepõem.
            List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                    profissional.getId(),
                    amanhaAs11h,
                    amanhaAs11h.plusHours(1),
                    STATUS_ATIVOS);

            assertThat(conflitos)
                    .as("agendamento adjacente (sem overlap) não deve conflitar")
                    .isEmpty();
        }
    }

    // ================================================================
    // findConflitosHorario — filtros adicionais (status, profissional)
    // ================================================================

    @Test
    @DisplayName("findConflitosHorario — agendamento CANCELADO é ignorado mesmo com overlap")
    void deveIgnorarAgendamentoCanceladoMesmoComOverlap() {
        persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.CANCELADO);

        List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                profissional.getId(), amanhaAs10h, amanhaAs11h, STATUS_ATIVOS);

        assertThat(conflitos).isEmpty();
    }

    @Test
    @DisplayName("findConflitosHorario — agendamento CONCLUIDO é ignorado mesmo com overlap")
    void deveIgnorarAgendamentoConcluidoMesmoComOverlap() {
        persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.CONCLUIDO);

        List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                profissional.getId(), amanhaAs10h, amanhaAs11h, STATUS_ATIVOS);

        assertThat(conflitos).isEmpty();
    }

    @Test
    @DisplayName("findConflitosHorario — agendamento de OUTRO profissional não conflita")
    void deveIgnorarOverlapDeOutroProfissional() {
        persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);

        // Cria um segundo profissional e busca conflito por ele
        Profissional outroProfi = profissionalRepository.save(Profissional.builder()
                .nome("Outro Profissional")
                .especialidade("Massoterapia")
                .email("outro@teste.com")
                .telefone("98911112222")
                .build());

        List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                outroProfi.getId(), amanhaAs10h, amanhaAs11h, STATUS_ATIVOS);

        assertThat(conflitos).isEmpty();
    }

    @Test
    @DisplayName("findConflitosHorario — status CONFIRMADO também é considerado conflito")
    void deveDetectarConflitoComStatusConfirmado() {
        persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.CONFIRMADO);

        List<Agendamento> conflitos = agendamentoRepository.findConflitosHorario(
                profissional.getId(), amanhaAs10h, amanhaAs11h, STATUS_ATIVOS);

        assertThat(conflitos).hasSize(1);
        assertThat(conflitos.get(0).getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    // ================================================================
    // findConflitosHorarioExcluindoId — atualização de agendamento
    // ================================================================

    @Test
    @DisplayName("findConflitosHorarioExcluindoId — não retorna o próprio agendamento atualizado")
    void deveExcluirOProprioAgendamentoNaAtualizacao() {
        Agendamento existente = persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);

        List<Agendamento> conflitos = agendamentoRepository.findConflitosHorarioExcluindoId(
                profissional.getId(), amanhaAs10h, amanhaAs11h, STATUS_ATIVOS, existente.getId());

        assertThat(conflitos)
                .as("o próprio agendamento deve ser excluído da busca")
                .isEmpty();
    }

    // ================================================================
    // Métodos derivados — findByClienteId, findByStatus, etc.
    // ================================================================

    @Test
    @DisplayName("findByClienteId — retorna apenas agendamentos do cliente informado")
    void findByClienteIdDeveRetornarApenasDoClienteInformado() {
        persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);

        List<Agendamento> resultado = agendamentoRepository.findByClienteId(cliente.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCliente().getId()).isEqualTo(cliente.getId());
    }

    @Test
    @DisplayName("findByStatus — retorna apenas agendamentos no status pesquisado")
    void findByStatusDeveFiltrarPeloStatus() {
        persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);
        persistirAgendamento(amanhaAs11h, amanhaAs11h.plusHours(1), StatusAgendamento.CONFIRMADO);

        List<Agendamento> agendados = agendamentoRepository.findByStatus(StatusAgendamento.AGENDADO);
        List<Agendamento> confirmados = agendamentoRepository.findByStatus(StatusAgendamento.CONFIRMADO);

        assertThat(agendados).hasSize(1);
        assertThat(confirmados).hasSize(1);
        assertThat(agendados.get(0).getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(confirmados.get(0).getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    @DisplayName("findByDataInterval — retorna agendamentos dentro do intervalo, ordenados ASC")
    void findByDataIntervalDeveRetornarOrdenadosCrescente() {
        persistirAgendamento(amanhaAs11h, amanhaAs11h.plusHours(1), StatusAgendamento.AGENDADO);
        persistirAgendamento(amanhaAs10h, amanhaAs11h, StatusAgendamento.AGENDADO);

        List<Agendamento> resultado = agendamentoRepository.findByDataInterval(
                amanhaAs10h.minusHours(1),
                amanhaAs11h.plusHours(2));

        assertThat(resultado).hasSize(2);
        // Deve vir ordenado crescente por dataHora
        assertThat(resultado.get(0).getDataHora()).isBefore(resultado.get(1).getDataHora());
    }

    // ================================================================
    // Helper
    // ================================================================

    private Agendamento persistirAgendamento(LocalDateTime inicio, LocalDateTime fim, StatusAgendamento status) {
        Agendamento ag = Agendamento.builder()
                .cliente(cliente)
                .profissional(profissional)
                .servico(servico)
                .dataHora(inicio)
                .dataHoraFim(fim)
                .status(status)
                .build();
        return agendamentoRepository.save(ag);
    }
}
