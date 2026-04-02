package com.guilhermebraga.agendamento_api.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários da camada de serviço de Agendamento.
 * <p>
 * Cobre os casos mais críticos de regra de negócio:
 * - Verificação de existência das entidades relacionadas
 * - Detecção de conflito de horário entre agendamentos
 * - Máquina de estados (transições de status permitidas e proibidas)
 * - Imutabilidade de agendamentos concluídos e cancelados
 *
 * @author Guilherme Braga
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgendamentoService — testes unitários")
class AgendamentoServiceTest {

    @Mock private AgendamentoRepository agendamentoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private ServicoRepository servicoRepository;
    @Mock private AgendamentoMapper agendamentoMapper;

    @InjectMocks
    private AgendamentoService agendamentoService;

    // ----------------------------------------------------------------
    // Entidades de apoio reutilizadas nos testes
    // ----------------------------------------------------------------
    private Cliente cliente;
    private Profissional profissional;
    private Servico servico;
    private AgendamentoRequest request;
    private Agendamento agendamento;
    private AgendamentoResponse response;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria Silva");
        cliente.setEmail("maria@email.com");

        profissional = new Profissional();
        profissional.setId(1L);
        profissional.setNome("Ana Souza");
        profissional.setEspecialidade("Fisioterapeuta");

        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Massagem Relaxante");
        servico.setDuracaoMinutos(60);
        servico.setPreco(new BigDecimal("120.00"));

        LocalDateTime dataHoraFutura = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

        request = AgendamentoRequest.builder()
                .clienteId(1L)
                .profissionalId(1L)
                .servicoId(1L)
                .dataHora(dataHoraFutura)
                .build();

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServico(servico);
        agendamento.setDataHora(dataHoraFutura);
        agendamento.setDataHoraFim(dataHoraFutura.plusMinutes(60));
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setCriadoEm(LocalDateTime.now());
        agendamento.setAtualizadoEm(LocalDateTime.now());

        response = AgendamentoResponse.builder()
                .id(1L)
                .clienteId(1L)
                .clienteNome("Maria Silva")
                .profissionalId(1L)
                .profissionalNome("Ana Souza")
                .servicoId(1L)
                .servicoNome("Massagem Relaxante")
                .dataHora(dataHoraFutura)
                .status(StatusAgendamento.AGENDADO)
                .build();
    }

    // ================================================================
    // CRIAR AGENDAMENTO
    // ================================================================

    @Nested
    @DisplayName("criar()")
    class CriarAgendamento {

        @Test
        @DisplayName("deve criar agendamento sem conflito de horário")
        void deveCriarAgendamentoComSucesso() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            // Sem conflitos — lista vazia
            when(agendamentoRepository.findConflitosHorario(anyLong(), any(), any(), anyList()))
                    .thenReturn(List.of());
            when(agendamentoMapper.toEntity(eq(request), eq(cliente), eq(profissional), eq(servico)))
                    .thenReturn(agendamento);
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            AgendamentoResponse resultado = agendamentoService.criar(request);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
            verify(agendamentoRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("deve lançar BusinessException quando houver conflito de horário")
        void deveLancarExcecaoQuandoHouverConflitoDeHorario() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            // Simula conflito — retorna um agendamento existente no mesmo horário
            when(agendamentoRepository.findConflitosHorario(anyLong(), any(), any(), anyList()))
                    .thenReturn(List.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.criar(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("horário");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando cliente não existir")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.criar(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando profissional não existir")
        void deveLancarExcecaoQuandoProfissionalNaoEncontrado() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.criar(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Profissional");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando serviço não existir")
        void deveLancarExcecaoQuandoServicoNaoEncontrado() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.criar(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Serviço");
        }
    }

    // ================================================================
    // ATUALIZAR STATUS — MÁQUINA DE ESTADOS
    // ================================================================

    @Nested
    @DisplayName("atualizarStatus() — transições permitidas")
    class AtualizarStatusPermitido {

        @Test
        @DisplayName("deve transitar de AGENDADO para CONFIRMADO")
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
        @DisplayName("deve transitar de AGENDADO para CANCELADO")
        void deveTransitarDeAgendadoParaCancelado() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            agendamentoService.atualizarStatus(1L, StatusAgendamento.CANCELADO);

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        }

        @Test
        @DisplayName("deve transitar de CONFIRMADO para CONCLUIDO")
        void deveTransitarDeConfirmadoParaConcluido() {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            agendamentoService.atualizarStatus(1L, StatusAgendamento.CONCLUIDO);

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CONCLUIDO);
        }

        @Test
        @DisplayName("deve transitar de CONFIRMADO para CANCELADO")
        void deveTransitarDeConfirmadoParaCancelado() {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            agendamentoService.atualizarStatus(1L, StatusAgendamento.CANCELADO);

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        }
    }

    @Nested
    @DisplayName("atualizarStatus() — transições proibidas")
    class AtualizarStatusProibido {

        @Test
        @DisplayName("não deve permitir transição de CONCLUIDO para qualquer status")
        void naoDevePermitirTransicaoDeConcluidoParaQualquerStatus() {
            agendamento.setStatus(StatusAgendamento.CONCLUIDO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            // Todas as transições a partir de CONCLUIDO devem lançar exceção
            for (StatusAgendamento novoStatus : StatusAgendamento.values()) {
                assertThatThrownBy(() -> agendamentoService.atualizarStatus(1L, novoStatus))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining("inválida");
            }

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("não deve permitir transição de CANCELADO para qualquer status")
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
        @DisplayName("não deve permitir transição de AGENDADO diretamente para CONCLUIDO")
        void naoDevePermitirTransicaoDeAgendadoParaConcluido() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.atualizarStatus(1L, StatusAgendamento.CONCLUIDO))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ================================================================
    // CANCELAR
    // ================================================================

    @Nested
    @DisplayName("cancelar()")
    class CancelarAgendamento {

        @Test
        @DisplayName("deve cancelar agendamento com status AGENDADO")
        void deveCancelarAgendamentoComSucesso() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

            assertThatCode(() -> agendamentoService.cancelar(1L))
                    .doesNotThrowAnyException();

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
            verify(agendamentoRepository, times(1)).save(agendamento);
        }

        @Test
        @DisplayName("deve cancelar agendamento com status CONFIRMADO")
        void deveCancelarAgendamentoConfirmado() {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

            agendamentoService.cancelar(1L);

            assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        }

        @Test
        @DisplayName("não deve cancelar agendamento já CONCLUIDO")
        void naoDeveCancelarAgendamentoConcluido() {
            agendamento.setStatus(StatusAgendamento.CONCLUIDO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.cancelar(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("concluídos");

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
                    .hasMessageContaining("99");
        }
    }

    // ================================================================
    // ATUALIZAR (PUT)
    // ================================================================

    @Nested
    @DisplayName("atualizar()")
    class AtualizarAgendamento {

        @Test
        @DisplayName("não deve permitir atualizar agendamento CONCLUIDO")
        void naoDeveAtualizarAgendamentoConcluido() {
            agendamento.setStatus(StatusAgendamento.CONCLUIDO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.atualizar(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("concluídos");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("não deve permitir atualizar agendamento CANCELADO")
        void naoDeveAtualizarAgendamentoCancelado() {
            agendamento.setStatus(StatusAgendamento.CANCELADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.atualizar(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cancelados");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve atualizar agendamento AGENDADO sem conflito de horário")
        void deveAtualizarAgendamentoSemConflito() {
            agendamento.setStatus(StatusAgendamento.AGENDADO);
            when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
            when(agendamentoRepository.findConflitosHorarioExcluindoId(
                    anyLong(), any(), any(), anyList(), anyLong()))
                    .thenReturn(List.of());
            when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);
            when(agendamentoMapper.toResponse(agendamento)).thenReturn(response);

            AgendamentoResponse resultado = agendamentoService.atualizar(1L, request);

            assertThat(resultado).isNotNull();
            verify(agendamentoMapper).updateEntityFromRequest(
                    eq(request), eq(agendamento), eq(cliente), eq(profissional), eq(servico));
        }
    }
}
