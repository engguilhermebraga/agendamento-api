package com.guilhermebraga.agendamento_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guilhermebraga.agendamento_api.dto.request.AgendamentoRequest;
import com.guilhermebraga.agendamento_api.dto.response.AgendamentoResponse;
import com.guilhermebraga.agendamento_api.entity.StatusAgendamento;
import com.guilhermebraga.agendamento_api.exception.BusinessException;
import com.guilhermebraga.agendamento_api.exception.ResourceNotFoundException;
import com.guilhermebraga.agendamento_api.service.AgendamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("AgendamentoController — testes de integração com MockMvc")
class AgendamentoControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AgendamentoService agendamentoService;

    private AgendamentoRequest request;
    private AgendamentoResponse response;
    private LocalDateTime dataHoraFutura;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        dataHoraFutura = LocalDateTime.now()
            .plusDays(1)
            .withHour(10).withMinute(0).withSecond(0).withNano(0);

        request = AgendamentoRequest.builder()
            .clienteId(1L)
            .profissionalId(1L)
            .servicoId(1L)
            .dataHora(dataHoraFutura)
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

    // ================================================================
    // GET
    // ================================================================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/agendamentos — USER deve listar todos")
    void deveListarTodos() throws Exception {
        when(agendamentoService.listarTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/agendamentos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].clienteNome").value("Maria Silva"))
            .andExpect(jsonPath("$[0].status").value("AGENDADO"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/agendamentos/{id} — USER deve buscar por ID")
    void deveBuscarPorId() throws Exception {
        when(agendamentoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/agendamentos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.profissionalEspecialidade").value("Fisioterapeuta"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/agendamentos/{id} — deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoIdInexistente() throws Exception {
        when(agendamentoService.buscarPorId(99L))
            .thenThrow(new ResourceNotFoundException("Agendamento", 99L));

        mockMvc.perform(get("/api/v1/agendamentos/99"))
            .andExpect(status().isNotFound());
    }

    // ================================================================
    // POST
    // ================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/agendamentos — ADMIN deve criar agendamento")
    void deveCriarComoAdmin() throws Exception {
        when(agendamentoService.criar(any(AgendamentoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/agendamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("AGENDADO"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/agendamentos — USER recebe 403 Forbidden")
    void naoDevePermitirUserCriar() throws Exception {
        mockMvc.perform(post("/api/v1/agendamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/agendamentos — deve retornar 400 com IDs nulos e data passada")
    void deveRetornar400ComDadosInvalidos() throws Exception {
        AgendamentoRequest invalido = AgendamentoRequest.builder()
            .clienteId(null)
            .profissionalId(null)
            .servicoId(null)
            .dataHora(LocalDateTime.now().minusDays(1))
            .build();

        mockMvc.perform(post("/api/v1/agendamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/agendamentos — deve retornar 409 em conflito de horário")
    void deveRetornar409ComConflitoDeHorario() throws Exception {
        when(agendamentoService.criar(any(AgendamentoRequest.class)))
            .thenThrow(new BusinessException("O profissional já possui um agendamento neste horário."));

        mockMvc.perform(post("/api/v1/agendamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    // ================================================================
    // PUT
    // ================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/agendamentos/{id} — ADMIN deve atualizar")
    void deveAtualizarComoAdmin() throws Exception {
        when(agendamentoService.atualizar(eq(1L), any(AgendamentoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/agendamentos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/agendamentos/{id} — deve retornar 404 quando não encontrado")
    void deveRetornar404AoAtualizarInexistente() throws Exception {
        when(agendamentoService.atualizar(eq(99L), any(AgendamentoRequest.class)))
            .thenThrow(new ResourceNotFoundException("Agendamento", 99L));

        mockMvc.perform(put("/api/v1/agendamentos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    // ================================================================
    // PATCH STATUS
    // ================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/agendamentos/{id}/status — deve atualizar status para CONFIRMADO")
    void deveAtualizarStatusParaConfirmado() throws Exception {
        AgendamentoResponse confirmado = AgendamentoResponse.builder()
            .id(1L)
            .status(StatusAgendamento.CONFIRMADO)
            .clienteNome("Maria Silva")
            .build();
        when(agendamentoService.atualizarStatus(1L, StatusAgendamento.CONFIRMADO))
            .thenReturn(confirmado);

        mockMvc.perform(patch("/api/v1/agendamentos/1/status")
                .param("status", "CONFIRMADO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/agendamentos/{id}/status — deve retornar 409 em transição inválida")
    void deveRetornar409EmTransicaoInvalida() throws Exception {
        when(agendamentoService.atualizarStatus(1L, StatusAgendamento.AGENDADO))
            .thenThrow(new BusinessException("Transição de status inválida."));

        mockMvc.perform(patch("/api/v1/agendamentos/1/status")
                .param("status", "AGENDADO"))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/agendamentos/{id}/status — deve retornar 404 quando não encontrado")
    void deveRetornar404AoAtualizarStatusInexistente() throws Exception {
        when(agendamentoService.atualizarStatus(99L, StatusAgendamento.CONFIRMADO))
            .thenThrow(new ResourceNotFoundException("Agendamento", 99L));

        mockMvc.perform(patch("/api/v1/agendamentos/99/status")
                .param("status", "CONFIRMADO"))
            .andExpect(status().isNotFound());
    }

    // ================================================================
    // DELETE
    // ================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/agendamentos/{id} — ADMIN deve cancelar")
    void deveCancelarComoAdmin() throws Exception {
        doNothing().when(agendamentoService).cancelar(1L);

        mockMvc.perform(delete("/api/v1/agendamentos/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/agendamentos/{id} — USER recebe 403 Forbidden")
    void naoDevePermitirUserCancelar() throws Exception {
        mockMvc.perform(delete("/api/v1/agendamentos/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/agendamentos/{id} — deve retornar 409 quando agendamento já CONCLUIDO")
    void deveRetornar409AoCancelarAgendamentoConcluido() throws Exception {
        doThrow(new BusinessException("Agendamentos concluídos não podem ser cancelados."))
            .when(agendamentoService).cancelar(1L);

        mockMvc.perform(delete("/api/v1/agendamentos/1"))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/agendamentos/{id} — deve retornar 404 quando não encontrado")
    void deveRetornar404AoCancelarInexistente() throws Exception {
        doThrow(new ResourceNotFoundException("Agendamento", 99L))
            .when(agendamentoService).cancelar(99L);

        mockMvc.perform(delete("/api/v1/agendamentos/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/agendamentos — sem autenticação retorna 401")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos"))
            .andExpect(status().isUnauthorized());
    }
}
