package com.guilhermebraga.agendamento_api;

import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.response.AgendamentoResponse;
import com.guilhermebraga.agendamento_api.entity.*;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.mapper.AgendamentoMapper;
import com.guilhermebraga.agendamento_api.repository.AgendamentoRepository;
import com.guilhermebraga.agendamento_api.repository.ClienteRepository;
import com.guilhermebraga.agendamento_api.repository.ProfissionalRepository;
import com.guilhermebraga.agendamento_api.repository.ServicoRepository;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários completos da camada de serviço de Agendamento.
 * <p>
 * Cobertura:
 * - criar(): criação com sucesso, conflito de horário, entidades inexistentes
 * - buscarPorId(): retorno com sucesso e ResourceNotFoundException
 * - listarTodos(): lista com resultados e lista vazia
 * - atualizar(): atualização com sucesso, imutabilidade de CONCLUIDO/CANCELADO,
 *   conflito de horário na atualização, entidades inexistentes
 * - atualizarStatus(): todas as transições válidas e inválidas da máquina de estados
 * - cancelar(): cancelamento com sucesso, bloqueio para CONCLUIDO/CANCELADO, ID inexistente
 *
 * @author Guilherme Braga
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgendamentoService — testes unitários")
class AgendamentoServiceTest {

    // ----------------------------------------------------------------
    // Mocks dos repositórios e do mapper
    // ----------------------------------------------------------------

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private AgendamentoMapper agendamentoMapper;

    @InjectMocks
    private AgendamentoService agendamentoService;

    // ----------------------------------------------------------------
    // Entidades de apoio reutilizadas nos testes
    // ----------------------------------------------------------------

    private Cliente cliente;
    private Profissional profissional;
    private Servico servico;
    private AgendamentoRequest requestValido;
    private Agendamento agendamento;
    private AgendamentoResponse response;
    private LocalDateTime dataHoraFutura;

    // ----------------------------------------------------------------
    // Setup — inicializa fixtures antes de cada teste
    // ----------------------------------------------------------------

    @BeforeEach
    void setUp() {
        criarMocksPadrao();
    }

    // ================================================================
    // HELPER METHODS
    // ================================================================

    /**
     * Inicializa todas as entidades de apoio com dados válidos e consistentes.
     * Chamado automaticamente pelo @BeforeEach antes de cada teste.
     */
    private void criarMocksPadrao() {
        dataHoraFutura = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        cliente = Cliente.builder()
                .id(1L)
                .nome("Maria Silva")
                .email("maria@email.com")
                .telefone("98991234567")
                .cpf("12345678901")
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();

        profissional = Profissional.builder()
                .id(1L)
                .nome("Ana Souza")
                .especialidade("Fisioterapeuta")
                .email("ana@email.com")
                .telefone("98999876543")
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();

        servico = Servico.builder()
                .id(1L)
                .nome("Massagem Relaxante")
                .descricao("Massagem corporal relaxante de 60 minutos")
                .duracaoMinutos(60)
                .preco(new BigDecimal("120.00"))
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();

        requestValido = criarAgendamentoValido();

        agendamento = Agendamento.builder()
                .id(1L)
                .cliente(cliente)
                .profissional(profissional)
                .servico(servico)
                .dataHora(dataHoraFutura)
                .dataHoraFim(dataHoraFutura.plusMinutes(60))
                .status(StatusAgendamento.AGENDADO)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();

        response = AgendamentoResponse.builder()
                .id(1L)
                .clienteId(1L)
                .clienteNome("Maria Silva")
                .profissionalId(1L)
                .profissionalNome("Ana Souza")
                .profissionalEspecialidade("Fisioterapeuta")
                .servicoId(1L)
                .servicoNome("Massagem Relaxante")
                .servicoDuracaoMinutos(60)
                .dataHora(dataHoraFutura)
                .status(StatusAgendamento.AGENDADO)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    /**
     * Cria um AgendamentoRequest com todos os campos válidos.
     * Usa data futura (amanhã às 10h) e IDs existentes.
     */
    private AgendamentoRequest criarAgendamentoValido() {
        return AgendamentoRequest.builder()
                .clienteId(1L)
                .profissionalId(1L)
                .servicoId(1L)
                .dataHora(dataHoraFutura)
                .build();
    }

    /**
     * Cria um AgendamentoRequest com campos inválidos para testar validações.
     * IDs nulos e data no passado.
     */
    private AgendamentoRequest criarAgendamentoInvalido() {
        return AgendamentoRequest.builder()
                .clienteId(null)
                .profissionalId(null)
                .servicoId(null)
                .dataHora(LocalDateTime.now().minusDays(1))
                .build();
    }

    /**
     * Cria um segundo agendamento para simular conflito de horário.
     * Mesmo profissional, horário sobreposto ao agendamento padrão.
     */
    private Agendamento criarAgendamentoConflitante() {
        return Agendamento.builder()
                .id(2L)
                .cliente(cliente)
                .profissional(profissional)
                .servico(servico)
                .dataHora(dataHoraFutura.plusMinutes(30))
                .dataHoraFim(dataHoraFutura.plusMinutes(90))
                .status(StatusAgendamento.AGENDADO)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    // ================================================================
    // CRIAR AGENDAMENTO
    // ================================================================

    @Nested
    @DisplayName("criar()")
    class CriarAgendamento {

        @Test
        @DisplayName("deve criar agendamento com sucesso quando não houver conflito")
        void deveCriarAgendamentoComSucesso() {
            // Arrange — configura todos os mocks para o fluxo feliz
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            when(agendamentoRepository.findConflitosHorario(anyLong(), any(), any(), anyList()))
                    .thenReturn(Collections.emptyList());
            when(agendamentoMapper.toEntity(eq(requestValido), eq(cliente), eq(profissional), eq(servico)))
                    .thenReturn(agendamento);
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            // Act
            AgendamentoResponse resultado = agendamentoService.criar(requestValido);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getClienteNome()).isEqualTo("Maria Silva");
            assertThat(resultado.getProfissionalNome()).isEqualTo("Ana Souza");
            assertThat(resultado.getServicoNome()).isEqualTo("Massagem Relaxante");
            assertThat(resultado.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
            assertThat(resultado.getDataHora()).isEqualTo(dataHoraFutura);

            // Verifica que o save foi chamado exatamente uma vez
            verify(agendamentoRepository, times(1)).save(any(Agendamento.class));
            verify(agendamentoMapper).toEntity(requestValido, cliente, profissional, servico);
            verify(agendamentoMapper).toResponse(agendamento);
        }

        @Test
        @DisplayName("deve lançar BusinessException quando houver conflito de horário do profissional")
        void deveLancarExcecaoQuandoHouverConflitoDeHorario() {
            // Arrange — simula conflito retornando um agendamento existente
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            when(agendamentoRepository.findConflitosHorario(anyLong(), any(), any(), anyList()))
                    .thenReturn(List.of(criarAgendamentoConflitante()));

            // Act & Assert
            assertThatThrownBy(() -> agendamentoService.criar(requestValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("horário");

            // Verifica que o save nunca foi chamado (agendamento não deve ser persistido)
            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve verificar overlap correto — profissional com dois agendamentos sobrepostos")
        void deveDetectarOverlapDeProfissional() {
            // Arrange — cria request no mesmo horário que um agendamento existente
            AgendamentoRequest requestSobreposto = AgendamentoRequest.builder()
                    .clienteId(1L)
                    .profissionalId(1L)
                    .servicoId(1L)
                    .dataHora(dataHoraFutura.plusMinutes(30)) // começa 30min depois do existente
                    .build();

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            // Simula que o repository encontrou conflito (overlap: 10:30 < 11:00 e 10:00 < 11:30)
            when(agendamentoRepository.findConflitosHorario(
                    eq(1L), eq(requestSobreposto.getDataHora()), any(), anyList()))
                    .thenReturn(List.of(agendamento));

            // Act & Assert
            assertThatThrownBy(() -> agendamentoService.criar(requestSobreposto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("profissional já possui um agendamento");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando cliente não existir")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.criar(requestValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");

            // Não deve buscar profissional ou serviço se o cliente não existe
            verify(profissionalRepository, never()).findById(anyLong());
            verify(servicoRepository, never()).findById(anyLong());
            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando profissional não existir")
        void deveLancarExcecaoQuandoProfissionalNaoEncontrado() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.criar(requestValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Profissional");

            verify(servicoRepository, never()).findById(anyLong());
            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando serviço não existir")
        void deveLancarExcecaoQuandoServicoNaoEncontrado() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.criar(requestValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Serviço");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve calcular dataHoraFim corretamente com base na duração do serviço")
        void deveCalcularDataHoraFimCorretamente() {
            // Arrange — serviço de 90 minutos
            Servico servico90min = Servico.builder()
                    .id(2L)
                    .nome("Sessão de Fisioterapia")
                    .duracaoMinutos(90)
                    .preco(new BigDecimal("180.00"))
                    .build();

            AgendamentoRequest req = AgendamentoRequest.builder()
                    .clienteId(1L)
                    .profissionalId(1L)
                    .servicoId(2L)
                    .dataHora(dataHoraFutura)
                    .build();

            LocalDateTime fimEsperado = dataHoraFutura.plusMinutes(90);

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(2L)).thenReturn(Optional.of(servico90min));
            // Verifica que o repository é chamado com a dataHoraFim calculada corretamente
            when(agendamentoRepository.findConflitosHorario(
                    eq(1L), eq(dataHoraFutura), eq(fimEsperado), anyList()))
                    .thenReturn(Collections.emptyList());
            when(agendamentoMapper.toEntity(any(), any(), any(), eq(servico90min)))
                    .thenReturn(agendamento);
            when(agendamentoRepository.save(any())).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(any())).thenReturn(response);

            // Act
            agendamentoService.criar(req);

            // Assert — confirma que findConflitosHorario recebeu a dataHoraFim correta
            verify(agendamentoRepository).findConflitosHorario(
                    eq(1L), eq(dataHoraFutura), eq(fimEsperado), anyList());
        }
    }

    // ================================================================
    // BUSCAR POR ID
    // ================================================================

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar AgendamentoResponse quando ID existir")
        void deveRetornarAgendamentoQuandoIdExistir() {
            // Arrange
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            // Act
            AgendamentoResponse resultado = agendamentoService.buscarPorId(1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getClienteNome()).isEqualTo("Maria Silva");
            assertThat(resultado.getProfissionalNome()).isEqualTo("Ana Souza");
            assertThat(resultado.getServicoNome()).isEqualTo("Massagem Relaxante");
            assertThat(resultado.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);

            verify(agendamentoRepository, times(1)).findById(1L);
            verify(agendamentoMapper, times(1)).toResponse(agendamento);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando ID não existir")
        void deveLancarExcecaoQuandoIdNaoExistir() {
            when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.buscarPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Agendamento")
                    .hasMessageContaining("99");

            verify(agendamentoMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException para ID nulo convertido")
        void deveLancarExcecaoParaIdInexistente() {
            when(agendamentoRepository.findById(0L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.buscarPorId(0L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ================================================================
    // LISTAR TODOS
    // ================================================================

    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("deve retornar lista de agendamentos quando existirem registros")
        void deveRetornarListaDeAgendamentos() {
            // Arrange — cria segundo agendamento para testar lista com 2 itens
            Agendamento agendamento2 = Agendamento.builder()
                    .id(2L)
                    .cliente(cliente)
                    .profissional(profissional)
                    .servico(servico)
                    .dataHora(dataHoraFutura.plusHours(3))
                    .dataHoraFim(dataHoraFutura.plusHours(4))
                    .status(StatusAgendamento.CONFIRMADO)
                    .criadoEm(LocalDateTime.now())
                    .atualizadoEm(LocalDateTime.now())
                    .build();

            AgendamentoResponse response2 = AgendamentoResponse.builder()
                    .id(2L)
                    .clienteId(1L)
                    .clienteNome("Maria Silva")
                    .profissionalId(1L)
                    .profissionalNome("Ana Souza")
                    .servicoId(1L)
                    .servicoNome("Massagem Relaxante")
                    .dataHora(dataHoraFutura.plusHours(3))
                    .status(StatusAgendamento.CONFIRMADO)
                    .build();

            when(agendamentoRepository.findAll()).thenReturn(List.of(agendamento, agendamento2));
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);
            when(agendamentoMapper.toResponse(agendamento2)).thenReturn(response2);

            // Act
            List<AgendamentoResponse> resultado = agendamentoService.listarTodos();

            // Assert
            assertThat(resultado)
                    .isNotNull()
                    .hasSize(2);
            assertThat(resultado.get(0).getId()).isEqualTo(1L);
            assertThat(resultado.get(0).getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
            assertThat(resultado.get(1).getId()).isEqualTo(2L);
            assertThat(resultado.get(1).getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);

            verify(agendamentoRepository, times(1)).findAll();
            verify(agendamentoMapper, times(2)).toResponse(any(Agendamento.class));
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não existirem agendamentos")
        void deveRetornarListaVaziaQuandoNaoExistiremAgendamentos() {
            when(agendamentoRepository.findAll()).thenReturn(Collections.emptyList());

            List<AgendamentoResponse> resultado = agendamentoService.listarTodos();

            assertThat(resultado)
                    .isNotNull()
                    .isEmpty();

            verify(agendamentoRepository, times(1)).findAll();
            verify(agendamentoMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("deve retornar lista com apenas um agendamento")
        void deveRetornarListaComUmAgendamento() {
            when(agendamentoRepository.findAll()).thenReturn(List.of(agendamento));
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            List<AgendamentoResponse> resultado = agendamentoService.listarTodos();

            assertThat(resultado)
                    .isNotNull()
                    .hasSize(1)
                    .first()
                    .satisfies(r -> {
                        assertThat(r.getId()).isEqualTo(1L);
                        assertThat(r.getClienteNome()).isEqualTo("Maria Silva");
                    });
        }
    }

    // ================================================================
    // ATUALIZAR (PUT)
    // ================================================================

    @Nested
    @DisplayName("atualizar()")
    class AtualizarAgendamento {

        @Test
        @DisplayName("deve atualizar agendamento AGENDADO com sucesso quando não houver conflito")
        void deveAtualizarAgendamentoSemConflito() {
            // Arrange
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            when(agendamentoRepository.findConflitosHorarioExcluindoId(
                    anyLong(), any(), any(), anyList(), anyLong()))
                    .thenReturn(Collections.emptyList());
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            // Act
            AgendamentoResponse resultado = agendamentoService.atualizar(1L, requestValido);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);

            // Verifica que o mapper de update foi chamado com os parâmetros corretos
            verify(agendamentoMapper).updateEntityFromRequest(
                    eq(requestValido), eq(agendamento), eq(cliente), eq(profissional), eq(servico));
            verify(agendamentoRepository, times(1)).save(agendamento);
        }

        @Test
        @DisplayName("deve atualizar agendamento CONFIRMADO com sucesso")
        void deveAtualizarAgendamentoConfirmado() {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            when(agendamentoRepository.findConflitosHorarioExcluindoId(
                    anyLong(), any(), any(), anyList(), anyLong()))
                    .thenReturn(Collections.emptyList());
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            AgendamentoResponse resultado = agendamentoService.atualizar(1L, requestValido);

            assertThat(resultado).isNotNull();
            verify(agendamentoRepository, times(1)).save(agendamento);
        }

        @Test
        @DisplayName("não deve permitir atualizar agendamento CONCLUIDO — imutável")
        void naoDeveAtualizarAgendamentoConcluido() {
            agendamento.setStatus(StatusAgendamento.CONCLUIDO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.atualizar(1L, requestValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("concluídos");

            verify(agendamentoRepository, never()).save(any());
            verify(agendamentoMapper, never()).updateEntityFromRequest(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("não deve permitir atualizar agendamento CANCELADO — imutável")
        void naoDeveAtualizarAgendamentoCancelado() {
            agendamento.setStatus(StatusAgendamento.CANCELADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.atualizar(1L, requestValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cancelados");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar BusinessException quando houver conflito de horário na atualização")
        void deveLancarExcecaoQuandoHouverConflitoNaAtualizacao() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            // Simula conflito com outro agendamento existente
            when(agendamentoRepository.findConflitosHorarioExcluindoId(
                    anyLong(), any(), any(), anyList(), anyLong()))
                    .thenReturn(List.of(criarAgendamentoConflitante()));

            assertThatThrownBy(() -> agendamentoService.atualizar(1L, requestValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("horário");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando agendamento não existir")
        void deveLancarExcecaoQuandoAgendamentoNaoExistir() {
            when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.atualizar(99L, requestValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Agendamento")
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando cliente não existir na atualização")
        void deveLancarExcecaoQuandoClienteNaoExistirNaAtualizacao() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.atualizar(1L, requestValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando profissional não existir na atualização")
        void deveLancarExcecaoQuandoProfissionalNaoExistirNaAtualizacao() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.atualizar(1L, requestValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Profissional");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando serviço não existir na atualização")
        void deveLancarExcecaoQuandoServicoNaoExistirNaAtualizacao() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.atualizar(1L, requestValido))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Serviço");
        }

        @Test
        @DisplayName("deve ignorar o próprio agendamento ao verificar conflitos na atualização")
        void deveIgnorarProprioAgendamentoNaVerificacaoDeConflito() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            when(agendamentoRepository.findConflitosHorarioExcluindoId(
                    anyLong(), any(), any(), anyList(), eq(1L)))
                    .thenReturn(Collections.emptyList());
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            agendamentoService.atualizar(1L, requestValido);

            // Verifica que usou findConflitosHorarioExcluindoId (e não findConflitosHorario)
            // passando o ID do agendamento sendo atualizado para excluí-lo da verificação
            verify(agendamentoRepository).findConflitosHorarioExcluindoId(
                    eq(1L), any(), any(), anyList(), eq(1L));
            verify(agendamentoRepository, never()).findConflitosHorario(
                    anyLong(), any(), any(), anyList());
        }
    }

    // ================================================================
    // ATUALIZAR STATUS — TRANSIÇÕES PERMITIDAS
    // ================================================================

    @Nested
    @DisplayName("atualizarStatus() — transições permitidas")
    class AtualizarStatusPermitido {

        @Test
        @DisplayName("AGENDADO → CONFIRMADO: deve transitar com sucesso")
        void deveTransitarDeAgendadoParaConfirmado() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            agendamentoService.atualizarStatus(1L, StatusAgendamento.CONFIRMADO);

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
            verify(agendamentoRepository, times(1)).save(agendamento);
        }

        @Test
        @DisplayName("AGENDADO → CANCELADO: deve transitar com sucesso")
        void deveTransitarDeAgendadoParaCancelado() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            agendamentoService.atualizarStatus(1L, StatusAgendamento.CANCELADO);

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
            verify(agendamentoRepository).save(agendamento);
        }

        @Test
        @DisplayName("CONFIRMADO → CONCLUIDO: deve transitar com sucesso")
        void deveTransitarDeConfirmadoParaConcluido() {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            agendamentoService.atualizarStatus(1L, StatusAgendamento.CONCLUIDO);

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CONCLUIDO);
            verify(agendamentoRepository).save(agendamento);
        }

        @Test
        @DisplayName("CONFIRMADO → CANCELADO: deve transitar com sucesso")
        void deveTransitarDeConfirmadoParaCancelado() {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            agendamentoService.atualizarStatus(1L, StatusAgendamento.CANCELADO);

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
            verify(agendamentoRepository).save(agendamento);
        }
    }

    // ================================================================
    // ATUALIZAR STATUS — TRANSIÇÕES PROIBIDAS
    // ================================================================

    @Nested
    @DisplayName("atualizarStatus() — transições proibidas")
    class AtualizarStatusProibido {

        @Test
        @DisplayName("CONCLUIDO → qualquer status: deve lançar BusinessException")
        void naoDevePermitirTransicaoDeConcluidoParaQualquerStatus() {
            agendamento.setStatus(StatusAgendamento.CONCLUIDO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            for (StatusAgendamento novoStatus : StatusAgendamento.values()) {
                assertThatThrownBy(() -> agendamentoService.atualizarStatus(1L, novoStatus))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining("inválida");
            }

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("CANCELADO → qualquer status: deve lançar BusinessException")
        void naoDevePermitirTransicaoDeCanceladoParaQualquerStatus() {
            agendamento.setStatus(StatusAgendamento.CANCELADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            for (StatusAgendamento novoStatus : StatusAgendamento.values()) {
                assertThatThrownBy(() -> agendamentoService.atualizarStatus(1L, novoStatus))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining("inválida");
            }

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("AGENDADO → CONCLUIDO: não deve permitir pular CONFIRMADO")
        void naoDevePermitirPularDeAgendadoParaConcluido() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.atualizarStatus(1L, StatusAgendamento.CONCLUIDO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inválida");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("AGENDADO → AGENDADO: transição para o mesmo status deve ser inválida")
        void naoDevePermitirTransicaoParaMesmoStatusAgendado() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.atualizarStatus(1L, StatusAgendamento.AGENDADO))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("CONFIRMADO → AGENDADO: não deve permitir retroceder status")
        void naoDevePermitirRetrocederDeConfirmadoParaAgendado() {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.atualizarStatus(1L, StatusAgendamento.AGENDADO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inválida");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException para ID inexistente")
        void deveLancarExcecaoParaIdInexistenteNaAtualizacaoDeStatus() {
            when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.atualizarStatus(99L, StatusAgendamento.CONFIRMADO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ================================================================
    // CANCELAR
    // ================================================================

    @Nested
    @DisplayName("cancelar()")
    class CancelarAgendamento {

        @Test
        @DisplayName("deve cancelar agendamento com status AGENDADO com sucesso")
        void deveCancelarAgendamentoAgendado() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

            assertThatCode(() -> agendamentoService.cancelar(1L))
                    .doesNotThrowAnyException();

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
            verify(agendamentoRepository, times(1)).save(agendamento);
        }

        @Test
        @DisplayName("deve cancelar agendamento com status CONFIRMADO com sucesso")
        void deveCancelarAgendamentoConfirmado() {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

            agendamentoService.cancelar(1L);

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
            verify(agendamentoRepository).save(agendamento);
        }

        @Test
        @DisplayName("não deve cancelar agendamento já CONCLUIDO")
        void naoDeveCancelarAgendamentoConcluido() {
            agendamento.setStatus(StatusAgendamento.CONCLUIDO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.cancelar(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("concluídos");

            // Status não deve ter mudado
            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CONCLUIDO);
            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("não deve cancelar agendamento já CANCELADO")
        void naoDeveCancelarAgendamentoJaCancelado() {
            agendamento.setStatus(StatusAgendamento.CANCELADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.cancelar(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cancelado");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException para ID inexistente")
        void deveLancarExcecaoParaIdInexistente() {
            when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.cancelar(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Agendamento")
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("deve persistir o status CANCELADO no repositório após cancelar")
        void devePersistirStatusCanceladoNoRepositorio() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

            agendamentoService.cancelar(1L);

            // Captura o argumento passado ao save para verificar o status
            verify(agendamentoRepository).save(argThat(a ->
                    a.getStatus() == StatusAgendamento.CANCELADO));
        }
    }
}
